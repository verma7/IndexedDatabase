package memdb;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.Assert;
import memdb.autogen.Index;
import memdb.autogen.InvalidIndex;
import memdb.autogen.Predicate;
import memdb.autogen.PredicateComparator;
import memdb.autogen.QueryIndexResult;
import memdb.autogen.Record;
import memdb.autogen.RecordNotFound;
import memdb.autogen.ScanContinuation;
import memdb.autogen.ServiceException;
import memdb.autogen.SortOrder;
import memdb.autogen.TimestampedValue;

import org.junit.After;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Single threaded tests for Indexed database. */
@RunWith(JUnit4.class)
public class IndexedDatabaseSingleThreadedTest {

	/** Thread for running the server. */
	private Thread serverThread;
	
	/** Instance of the database server. */
	private IndexedDatabaseServer server;
	
	/** Instance of the database client. */
	private IndexedDatabaseClient client;	

	/** Port at which the database server is started, and the client connect to.*/
	private static short port = 10030;

	/** Helper method that returns a new TimestampedValue with the given timestamp
	 * with the value consisting of two bytes byte0 and byte1.*/
	private TimestampedValue newTimeStampedValue(int timestamp, int byte0,
			int byte1) {
		TimestampedValue value = new TimestampedValue();
		value.timestamp = timestamp;
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.put(0, (byte) byte0);
		buffer.put(1, (byte) byte1);
		value.setValue(buffer);
		return value;
	}

	/** Starts the thrift server in a separate thread and creates a client. */
	public void startServerAndClient(IndexedDatabaseServer server)
			throws Exception {
		this.server = server;
		this.serverThread = new Thread(server); 
		serverThread.start();
		try {
			// Wait for the server start up
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Create a new client.
		client = new IndexedDatabaseClient("localhost", port);
		
		// Increment the port number for each test (since gracefully shutting down
		// TThreadPoolServer takes 60 seconds.
		port++;
	}

	@After
	/** Closes the connection to the server. */
	public void tearDownAfterClass() throws Exception {
		server.stop();
		serverThread.join(1);
		client.closeConnection();
	}

	/**
	 * Tests a simple set() of a record and checks if we get() the same record.
	 * Also checks if set() when key or record is null or get() of non-existent
	 * keys throws the appropriate exceptions.
	 */
	@Test
	public void testSimpleSetAndGet() throws Exception {
		startServerAndClient(new IndexedDatabaseServer(port));
		Record rec = new Record();
		rec.columns = new HashMap<Short, TimestampedValue>();
		rec.columns.put((short) 7, newTimeStampedValue(100, 8, 9));

		// Set and get should return the same record.
		client.setRecord("key1", rec);
		Record gotRecord = client.getRecord("key1");
		Assert.assertEquals(rec, gotRecord);

		// Test if setRecord is idempotent.
		client.setRecord("key1", rec);
		gotRecord = client.getRecord("key1");
		Assert.assertEquals(rec, gotRecord);

		// Try to get a non-existent key.
		ExpectedException exception = ExpectedException.none();
		try {
			gotRecord = client.getRecord("non-existent-key");
		} catch (Exception e) {
			exception.expect(RecordNotFound.class);			
		}

		// Try to set a null key and non-null record.
		try {
			client.setRecord(null, rec);
		} catch (Exception e) {
			exception.expect(ServiceException.class);
		}

		// Try to set a non-null key and null record.
		try {
			client.setRecord("non-null key", null);
		} catch (Exception e) {
			exception.expect(ServiceException.class);
		}
	}

	/** Tests if merging of two records is correctly performed.  */
	@Test
	public void testMergeMultipleRecords() throws Exception {
		startServerAndClient(new IndexedDatabaseServer(port));
		Record rec1 = new Record();
		rec1.columns = new HashMap<Short, TimestampedValue>();
		rec1.columns.put((short) 0, newTimeStampedValue(40, 4, 4));
		rec1.columns.put((short) 1, newTimeStampedValue(50, 5, 5));
		rec1.columns.put((short) 2, newTimeStampedValue(60, 6, 6));
		rec1.columns.put((short) 3, newTimeStampedValue(70, 7, 7));

		Record rec2 = new Record();
		rec2.columns = new HashMap<Short, TimestampedValue>();
		rec2.columns.put((short) 0, newTimeStampedValue(40, 0, 0));
		rec2.columns.put((short) 1, newTimeStampedValue(150, 1, 1));
		rec2.columns.put((short) 2, newTimeStampedValue(30, 2, 2));
		rec2.columns.put((short) 5, newTimeStampedValue(30, 3, 3));

		Record expected = new Record();
		expected.columns = new HashMap<Short, TimestampedValue>();
		// Record with the larger value wins.
		expected.columns.put((short) 0, newTimeStampedValue(40, 4, 4));
		// Records with the larger timestamp wins.
		expected.columns.put((short) 1, newTimeStampedValue(150, 1, 1));
		expected.columns.put((short) 2, newTimeStampedValue(60, 6, 6));
		// Columns not present in other Records are merged.
		expected.columns.put((short) 3, newTimeStampedValue(70, 7, 7));
		expected.columns.put((short) 5, newTimeStampedValue(30, 3, 3));
		
		// Test if setRecord is idempotent.
		client.setRecord("key", rec1);
		client.setRecord("key", rec1);
		Assert.assertEquals(rec1, client.getRecord("key"));

		client.setRecord("key", rec2);
		// Test is records are merged correctly.
		Assert.assertTrue(rec1 != client.getRecord("key"));
		Assert.assertTrue(rec2 != client.getRecord("key"));
		Assert.assertEquals(expected, client.getRecord("key"));

		// Test if setRecord is commutative by setting rec2 before rec1.
		client.setRecord("key_opposite", rec2);
		client.setRecord("key_opposite", rec2);
		client.setRecord("key_opposite", rec1);
		client.setRecord("key_opposite", rec1);
		Assert.assertEquals(expected, client.getRecord("key_opposite"));		
	}

	/** Tests the getQueryIndex functionality when the database has no indexes. */
	@Test
	public void testZeroQueryIndex() throws Exception {
		ArrayList<Index> index = new ArrayList<Index>();
		startServerAndClient(new IndexedDatabaseServer(port, index));
		
		ExpectedException exception = ExpectedException.none();
		try {
			client.queryIndex((short) 0, 10, new ScanContinuation());
		} catch (InvalidIndex e) {
			exception.expect(InvalidIndex.class);
		}
	}

	/** Tests the getQueryIndex functionality. */
	@Test
	public void testQueryIndex() throws Exception {
		ArrayList<Index> index = new ArrayList<Index>();

		// First index has one predicate that checks if the 0th bit in column 0
		// value is FALSE.
		ArrayList<Predicate> ps1 = new ArrayList<Predicate>();		
		Predicate p1 = new Predicate(PredicateComparator.BIT_FALSE, (short) 0, (short) 0);
		SortOrder order1 = new SortOrder();
		order1.setSortByColumnValue((short) 0);
		ps1.add(p1);
		index.add(new Index((short) 0, "key_", ps1, order1));

		// Second index has one predicate that checks if the 0th bit in column 0
		// value is TRUE.
		ArrayList<Predicate> ps2 = new ArrayList<Predicate>();		
		Predicate p2 = new Predicate(PredicateComparator.BIT_TRUE, (short) 0, (short) 0);
		SortOrder order2 = new SortOrder();
		order2.setSortByColumnValue((short) 1);
		ps2.add(p2);
		index.add(new Index((short) 1, "key_", ps2, order2));	

		startServerAndClient(new IndexedDatabaseServer(port, index));
		Record rec1 = new Record();
		rec1.columns = new HashMap<Short, TimestampedValue>();
		rec1.columns.put((short) 0, newTimeStampedValue(40, 0, 4));
		rec1.columns.put((short) 1, newTimeStampedValue(50, 5, 5));
		client.setRecord("key_12", rec1);
		Assert.assertEquals(rec1, client.getRecord("key_12"));

		Record rec2 = new Record();
		rec2.columns = new HashMap<Short, TimestampedValue>();
		rec2.columns.put((short) 0, newTimeStampedValue(40, 1, 6));
		rec2.columns.put((short) 1, newTimeStampedValue(50, 7, 2));
		client.setRecord("key_13", rec2);
		Assert.assertEquals(rec2, client.getRecord("key_13"));

		QueryIndexResult result1 = client.queryIndex(
				(short) 0, 5, new ScanContinuation());
		Assert.assertEquals(1, result1.matchingKeys.size());
		Assert.assertEquals("key_12", result1.matchingKeys.get(0));

		QueryIndexResult result2 = client.queryIndex(
				(short) 1, 5, new ScanContinuation());
		Assert.assertEquals(1, result2.matchingKeys.size());
		Assert.assertEquals("key_13", result2.matchingKeys.get(0));		
	}

	/** Tests if the query indexes correctly check the conjunctions and bits. */
	@Test
	public void testConjunctionsAndBits() throws Exception {
		ArrayList<Index> index = new ArrayList<Index>();

		// First index has two predicates that check if the 0th bit in column 0
		// value is FALSE and 8th bit in column 1 is FALSE.
		ArrayList<Predicate> ps1 = new ArrayList<Predicate>();		
		ps1.add(new Predicate(PredicateComparator.BIT_FALSE, (short) 0, (short) 0));
		ps1.add(new Predicate(PredicateComparator.BIT_FALSE, (short) 1, (short) 8));
		SortOrder order1 = new SortOrder();
		order1.setSortByColumnValue((short) 0);
		index.add(new Index((short) 0, "key_", ps1, order1));

		// Second index has one predicate that checks if the 15th bit in column 1
		// value is TRUE and 0th bit in column 0 is TRUE.
		ArrayList<Predicate> ps2 = new ArrayList<Predicate>();				
		ps2.add(new Predicate(PredicateComparator.BIT_TRUE, (short) 1, (short) 15));
		ps2.add(new Predicate(PredicateComparator.BIT_TRUE, (short) 0, (short) 0));
		SortOrder order2 = new SortOrder();
		order2.setSortByColumnValue((short) 1);
		index.add(new Index((short) 1, "key_", ps2, order2));	

		startServerAndClient(new IndexedDatabaseServer(port, index));
		for (int i = 0; i < 20; i++) {			
			Record rec = new Record();
			rec.columns = new HashMap<Short, TimestampedValue>();
			rec.columns.put((short) 0, newTimeStampedValue(100, i, i));
			rec.columns.put((short) 1, newTimeStampedValue(100, 256-i, 256-i));
			client.setRecord("key_"+i, rec);
			Assert.assertEquals(rec, client.getRecord("key_"+i));
		}

		QueryIndexResult result1 = client.queryIndex(
				(short) 0, 20, new ScanContinuation());
		Assert.assertEquals(10, result1.matchingKeys.size());
		for (int i = 0; i < 10; i++) {
			Assert.assertEquals("key_" + 2*i, result1.matchingKeys.get(i));
		}

		// Test the order of query index results.
		QueryIndexResult result2 = client.queryIndex(
				(short) 1, 20, new ScanContinuation());
		Assert.assertEquals(10, result2.matchingKeys.size());
		for (int i = 0; i < 10; i++) {
			Assert.assertEquals("key_" + (19-2*i), result2.matchingKeys.get(i));
		}
	}

	/** Test continuations. */
	@Test
	public void testContinuations() throws Exception {
		ArrayList<Index> index = new ArrayList<Index>();
		// The index has two predicates that check if the 0th bit in column 0
		// value is FALSE and 8th bit in column 1 is FALSE.
		ArrayList<Predicate> ps = new ArrayList<Predicate>();		
		ps.add(new Predicate(PredicateComparator.BIT_FALSE, (short) 0, (short) 0));
		ps.add(new Predicate(PredicateComparator.BIT_FALSE, (short) 1, (short) 8));
		SortOrder order = new SortOrder();
		order.setSortByColumnValue((short) 0);
		index.add(new Index((short) 0, "key_", ps, order));
		
		startServerAndClient(new IndexedDatabaseServer(port, index));
		for (int i = 0; i < 20; i++) {			
			Record rec = new Record();
			rec.columns = new HashMap<Short, TimestampedValue>();
			rec.columns.put((short) 0, newTimeStampedValue(100, i, i));
			rec.columns.put((short) 1, newTimeStampedValue(100, 256-i, 256-i));
			client.setRecord("key_"+i, rec);
			Assert.assertEquals(rec, client.getRecord("key_"+i));
		}
		
		// Ask for five results.
		ScanContinuation continuation = new ScanContinuation();
		QueryIndexResult result = client.queryIndex((short) 0, 5, continuation);
		Assert.assertEquals(5, result.matchingKeys.size());
		Assert.assertEquals("key_8", result.scanContinuation.lastRecord.key);
		for (int i = 0; i < 5; i++) {
			Assert.assertEquals("key_" + 2*i, result.matchingKeys.get(i));
		}

		// Ask for three more results.
		result = client.queryIndex((short) 0, 3, result.scanContinuation);
		Assert.assertEquals(3, result.matchingKeys.size());
		Assert.assertEquals("key_14", result.scanContinuation.lastRecord.key);
		for (int i = 0; i < 3; i++) {
			Assert.assertEquals("key_" + 2*(i+5), result.matchingKeys.get(i));
		}

		// Ask for 10 results.
		result = client.queryIndex((short) 0, 10, result.scanContinuation);
		Assert.assertEquals(2, result.matchingKeys.size());
		Assert.assertEquals("key_18", result.scanContinuation.lastRecord.key);
		for (int i = 0; i < 2; i++) {
			Assert.assertEquals("key_" + 2*(i+8), result.matchingKeys.get(i));
		}
	}
}
