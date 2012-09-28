package memdb;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import memdb.autogen.Index;
import memdb.autogen.Predicate;
import memdb.autogen.PredicateComparator;
import memdb.autogen.QueryIndexResult;
import memdb.autogen.Record;
import memdb.autogen.ScanContinuation;
import memdb.autogen.SortOrder;
import memdb.autogen.TimestampedValue;

/** A concurrent test for the IndexedDatabase. Consists of multiple threads. If
 * i is the thread index, each thread tries to set all the column values to i
 * for all records with keys "key_n" where n is multiple of i.
 * 
 * Verifies if setRecord and getRecord are atomic.
 * Verifies if getQueryIndex returns result that are consistent with the set
 * records and are in the correct order. */

public class IndexedDatabaseConcurrentTest {

	/** Returns a list of predicates on the lower two bits of an integer stored in
	 * the byte buffer (bits 24 and 25) on each column.
	 * @param numColumn Number of predicates: one for each column.
	 * @param bit0 Predicate comparator for lower most bit.
	 * @param bit1 Predicate comparator for the lower most but one bit. */
	private ArrayList<Predicate> createPredicates(int numColumn,
			PredicateComparator bit0, PredicateComparator bit1) {
		ArrayList<Predicate> list = new ArrayList<Predicate>();
		for (short i = 0; i < numColumn; i++) {
			list.add(new Predicate(bit0, i, (short) 24));
			list.add(new Predicate(bit1, i, (short) 25));
		}
		return list;
	} 

	/** Creates and returns a list of four indexes that check the lower two bits
	 * of an integer stored in the byte buffer.
	 * @param numColumn: Number of columns in each row. */
	public ArrayList<Index> createIndexes(int numColumn) {
		SortOrder order = new SortOrder();
		order.setSortByColumnValue((short) 0);
		ArrayList<Index> indexes = new ArrayList<Index>();

		indexes.add(new Index((short) 0, "key_", createPredicates(numColumn,
				PredicateComparator.BIT_FALSE, PredicateComparator.BIT_FALSE), order));
		indexes.add(new Index((short) 1, "key_", createPredicates(numColumn,
				PredicateComparator.BIT_TRUE, PredicateComparator.BIT_FALSE), order));
		indexes.add(new Index((short) 2, "key_", createPredicates(numColumn,
				PredicateComparator.BIT_FALSE, PredicateComparator.BIT_TRUE), order));
		indexes.add(new Index((short) 3, "key_", createPredicates(numColumn,
				PredicateComparator.BIT_TRUE, PredicateComparator.BIT_TRUE), order));

		return indexes;
	}

	/** Client thread that performs various queries to the IndexedDatabse. */
	public class ClientThread implements Runnable {
		/** An instance of the IndexedDatabase client. */
		private IndexedDatabaseClient client;
		
		/** Number of columns in the database. */
		private int numColumns;
		
		/** Number of records in the database. */
		private int numRecords;
		
		/** Index of this thread. */
		private int threadIndex;

		/** Creates a new Client.
		 * @param server Server that this client connects to.
		 * @param port The port number of the server.
		 * @param index Index of this thread.
		 * @param numRecords Number of records in the database.
		 * @param numColumns Number of columns in the database.
		 */
		public ClientThread(String server, short port, int index, int numRecords,
				int numColumns) {
			client = new IndexedDatabaseClient(server, port);
			this.threadIndex = index;
			this.numRecords = numRecords;
			this.numColumns = numColumns;
		}

		/** Verifies if the given record has the same value for each column. 
		 * @throws Exception if an inconsistent record is found. */
		private boolean verifyRecord(Record record) throws Exception {
			int expected = -1;			
			for (TimestampedValue value : record.columns.values()) {
				int val = value.value.asIntBuffer().get(0);
				if (expected == -1) {
					expected = val;
				} else if (expected != val) {
					throw new Exception("Inconsistent value in record: " + record);						
				}
			}
			return true;
		}

		/** Returns the integer from a key of the form "key_<integer>".
		 * @param key The key string to be parsed. 
		 * @throws Exception if the key is malformed. */
		private int getIntegerFromKey(String key) throws Exception {
			String[] splits = key.split("_");
			if (splits.length == 2) {
				return Integer.parseInt(splits[1]);
			} else {
				throw new Exception("Malformed key: " + key);
			}
		}

		@Override
		public void run() {
			System.out.println("Running thread : " + threadIndex);
			Record rec = new Record();
			rec.columns = new HashMap<Short, TimestampedValue>();
			// Set each column value as the thread index.
			for (short columnIndex = 0; columnIndex < numColumns; columnIndex++) {
				TimestampedValue tValue = new TimestampedValue();
				tValue.value = ByteBuffer.allocate(4);
				tValue.value.asIntBuffer().put(0, threadIndex);
				rec.columns.put(columnIndex, tValue);
			}

			// Set records that are a multiple of the thread index.
			for (int i = threadIndex; i <= numRecords; i += threadIndex) {
				// Set the current timestamp for all columns.
				for (short columnIndex = 0; columnIndex < numColumns; columnIndex++) {
					rec.columns.get(columnIndex).timestamp = System.nanoTime();
				}
				try {
					// Set the record and verify it.
					client.setRecord("key_" + i, rec);
					if (verifyRecord(client.getRecord("key_" + i))) {
						System.out.println(
								"Verified record: key_" + i + " in thread " + threadIndex);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println("Done thread: " + threadIndex);
		}
		
		/** Verifies the four indexes. Throws an exception if the query index result
		 * is not sorted on the appropriate column or contains a record which it
		 * should not. */
		public boolean verifyQueryIndexes(int numRecords) throws Exception {
			boolean correct = true;
			for (short i = 0; i < 4; i++) {
				long queryTime = System.nanoTime();
				
				// Get the query index.
				QueryIndexResult result = client.queryIndex(
						i, numRecords, new ScanContinuation());
				System.out.println("Result for index " + i + " is " + result);
				Record prevRecord = null;
				for (String key : result.matchingKeys) {
					// Get the record for each matching key in the result.
					Record record = client.getRecord(key);
					ByteBuffer val = record.columns.get((short) 0).value;
					ByteBuffer prevVal = null;
					if (prevRecord != null) {
						prevVal = prevRecord.columns.get((short) 0).value;
					}
					long updateTime = record.columns.get((short) 0).timestamp;
					// Check if the order of the result values is ascending only if the 
					// update time is before the query time.
					if (prevVal != null && prevVal.compareTo(val) > 0 
							&& updateTime < queryTime) {
						throw new Exception("Inconsistent order in index: " + i + 
								" for Current record = " + record +
								"\n Previous record = " + prevRecord);
					}
					
					// Get the integer (thread index) from the key.
					int keyInt = getIntegerFromKey(key);
					int valInt = val.asIntBuffer().get(0);
					
					// If update time is before query time, check if the key of the record
					// is a multiple of the value inside the record. Also check if the
					// value is in the correct index.
					if (updateTime < queryTime && 
								(keyInt % valInt != 0 && valInt % 4 != i)) {
						throw new Exception("Inconsistent record in index: " + i	+ 
								" Record = " + record + " Index = " + result);
					}
					prevVal = val;
					prevRecord = record;
				}
			}
			return correct;
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.err.println("Usage: java IndexedDatabaseConcurrentTest <numThreads>"
					+ " <numColumns> <numRecords>.");
			return;
		}
		
		// Parse arguments.
		int numThreads = Integer.parseInt(args[0]);
		int numColumns = Integer.parseInt(args[1]);;
		int numRecords = Integer.parseInt(args[2]);;
		
		// Generate a random port.
		short port = (short) (2000 + Math.random() * 14000);
		IndexedDatabaseConcurrentTest test = new IndexedDatabaseConcurrentTest();
		
		// Create a server.
		IndexedDatabaseServer server = new IndexedDatabaseServer(
				port, test.createIndexes(numColumns));
		Thread serverThread = new Thread(server);
		serverThread.start();
		Thread.sleep(100);

		// Create and start numThreads threads.
		Thread[] threads = new Thread[numThreads];
		for (int i = 0; i < numThreads; i++) {
			threads[i] = new Thread(test.new ClientThread(
					"localhost", port, i+2, numRecords, numColumns));
			threads[i].start();
		}

		// Wait for all threads to join.
		for (int i = 0; i < numThreads; i++) {
			threads[i].join();
		}
		
		// Verify if the indices are correct after sleeping for some time.
		Thread.sleep(500);
		ClientThread client = test.new ClientThread(
				"localhost", port, 0, numRecords, numColumns);
		if (client.verifyQueryIndexes(numRecords)) {
			System.out.println("Query indexes verified successfully.");
		}

		// Stop the server.
		server.stop();
		serverThread.join(1);
		
		// Stopping a TThreadPoolServer takes 60 seconds. Exit instead.
		System.exit(0);
	}
}
