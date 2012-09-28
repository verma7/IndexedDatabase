package memdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import memdb.autogen.Index;
import memdb.autogen.IndexedDatabase;
import memdb.autogen.InvalidIndex;
import memdb.autogen.QueryIndexResult;
import memdb.autogen.Record;
import memdb.autogen.RecordNotFound;
import memdb.autogen.ScanContinuation;
import memdb.autogen.ServiceException;
import memdb.autogen.TimestampedValue;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

/** Implementation of an in-memory Indexed Database. */
public class IndexedDatabaseImpl implements IndexedDatabase.Iface {
	/** A ConcurrentHashMap to maintain a hashtable mapping keys to records.
	 * It provides efficient atomic insertions, updates and deletes.
	 * It does not allow null keys or values. 
	 */
	private ConcurrentHashMap<String, Record> keyValueMap;

	/** A list of query indexes. */
	private List<QueryIndex> indexes;

	private static Logger logger = Logger.getLogger(IndexedDatabaseImpl.class);

	/** Creates a new IndexedDatabase maintaining the given indexes. 
	 * @param indexMetadata A list of indexes to be maintained.
	 */
	public IndexedDatabaseImpl(ArrayList<Index> indexMetadata) {
		keyValueMap = new ConcurrentHashMap<String, Record>();
		indexes = new ArrayList<QueryIndex>();
		for (int i = 0; i < indexMetadata.size(); i++) {
			indexes.add(new QueryIndex(i, this, indexMetadata.get(i)));
		}
		// Set up basic logging using log4j.
		BasicConfigurator.configure();
		logger.setLevel(Level.WARN);
	}

	/** Creates a new IndexedDatabase maintaining no indexes. */
	public IndexedDatabaseImpl() {
		this(new ArrayList<Index>());
	}

	/** Returns the current value of the record with the given key.
	 *  @throws ServiceException if a "null" key is passed. 
	 *  @throws RecordNotFound exception if record with the given key is not
	 *  found.
	 *  @return Record with the given key.
	 **/
	@Override
	public Record getRecord(String key) throws RecordNotFound, ServiceException,
			TException {
		// ConcurrentHashMap does not allow keys with "null" values.		
		if (key == null) {
			throw new ServiceException("Null key is not allowed.");
		}

		// If the value returned by the ConcurrentHashMap is null, then the record
		// with the given key does not exist.
		Record value = keyValueMap.get(key);
		if (value == null) {
			throw new RecordNotFound("Record with key: " + key + " not found.");
		} else {
			logger.info("getRecord(" + key + ") = " + value);
			return value;
		}
	}

	/** Returns the index records for a given index.
	 * @param indexId Identifier of the Index.
	 * @param pageLimit Maximum number of records in the result.
	 * @param scanContinuation Specifies a previous continuation, so that
	 *  subsequent records can queried.
	 * @return QueryIndexResult containing at most pageLimit number of records
	 * starting from the previous scanContinuation satisfying the conditions
	 * specified by the index.
	 * @throws InvalidIndex If indexId is out of range.
	 * @throws ServiceException
	 * @throws TException 
	 * */
	@Override
	public QueryIndexResult queryIndex(short indexId, int pageLimit,
			ScanContinuation scanContinuation) throws InvalidIndex, ServiceException,
			TException {
		if (indexId < 0 || indexId >= indexes.size()) {
			throw new InvalidIndex("IndexId : " + indexId + " is out of range.");
		}
		QueryIndexResult result = indexes.get(indexId).queryIndex(
				pageLimit, scanContinuation); 
		logger.info("queryIndex(" + indexId + ") = " + result);
		return result;
	}

	/** Updates all the indexes for the given key.
	 * This function is idempotent and commutative.
	 * @param key Key whose indexes need to be updated. */
	private void updateAllIndexes(String key) throws RecordNotFound,
			ServiceException, TException, InterruptedException {
		for (QueryIndex index : indexes) {
			index.updateIndex(key);
		}
	}
	
	/**
	 * Updates a record specified by key with the given value.
	 * A record update merges with an existing record value on a per-column basis:
	 * columns with higher timestamps win merge conflicts.
	 * Also updates any relevant indexes.
	 * This function is idempotent and commutative.
	 * 
	 * @param key Key of the record.
	 * @param newRecord New record.
	 * @throws ServiceException if the key or the newRecord is null.
	 */
	@Override
	public void setRecord(String key, Record newRecord) throws ServiceException,
	TException, RecordNotFound {
		// Do not allow null keys or records.
		if (key == null || newRecord == null) {
			throw new ServiceException("Cannot store null keys or records");
		}
		logger.info("setRecord(" + key + ") = " + newRecord);

		Record oldRecord = keyValueMap.get(key);
		if (oldRecord == null) {
			// If the oldRecord does not exist, try inserting a new record.
			if (keyValueMap.putIfAbsent(key, newRecord) == null) {
				// Update indices if succeeded.
				try {
					updateAllIndexes(key);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				// Retry if failed.
				setRecord(key, newRecord);
			}
		} else {
			// Create a fresh record.
			Record freshRecord = new Record();
			freshRecord.columns = new HashMap<Short, TimestampedValue>();

			// Merge records on a per-column basis.
			for (Short columnId : newRecord.getColumns().keySet()) {
				if (oldRecord.columns.containsKey(columnId)) {
					TimestampedValue oldTValue = oldRecord.columns.get(columnId);
					TimestampedValue newTValue = newRecord.columns.get(columnId);
					// Column value with a later timestamp wins. 
					if (newTValue.timestamp > oldTValue.timestamp) {  
						freshRecord.columns.put(columnId, newTValue);
					} else if (newTValue.timestamp < oldTValue.timestamp){
						freshRecord.columns.put(columnId, oldTValue);
					} else {
						// Break ties arbitrarily and consistently by comparing values.
						// Record with larger column value wins. 
						if (newTValue.value.compareTo(oldTValue.value) > 0) {
							freshRecord.columns.put(columnId, newTValue);	
						} else {
							freshRecord.columns.put(columnId, oldTValue);
						}
					}
				} else {
					// Add column values from the new record that are not present in the
					// old record. 
					freshRecord.columns.put(columnId, newRecord.columns.get(columnId));
				}
			}

			// Add all column values from the old record that are not present in the
			// new record.
			for (Short columnId : oldRecord.getColumns().keySet()) {
				if (!newRecord.columns.containsKey(columnId)) {
					freshRecord.columns.put(columnId, oldRecord.columns.get(columnId));
				}
			}

			// Try replacing the old value with the fresh value.
			if (keyValueMap.replace(key, oldRecord, freshRecord)) {
				try {
					// Update all indices if succeeded.
					updateAllIndexes(key);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				// Retry if failed.
				setRecord(key, newRecord);
			}
		}
	}
}
