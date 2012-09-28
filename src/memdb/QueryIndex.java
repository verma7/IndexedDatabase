package memdb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import memdb.autogen.Index;
import memdb.autogen.Predicate;
import memdb.autogen.PredicateComparator;
import memdb.autogen.QueryIndexRecord;
import memdb.autogen.QueryIndexResult;
import memdb.autogen.Record;
import memdb.autogen.RecordNotFound;
import memdb.autogen.ScanContinuation;
import memdb.autogen.ServiceException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

/** QueryIndex maintains the metadata and the index in the IndexedDatabase. s*/
public class QueryIndex {
	/** An instance of the IndexedDatabase to which this query index belongs. */
	private IndexedDatabaseImpl database;

	/** The id of this query index */
	private int id;

	/** Metadata about index that has to be maintained. */
	private Index metadata;

	/** The actual indexes are stored as a skip list set. Note that we use 
	 * SkipListSet({value, key}) instead of SkipListMap<value, key> since there
	 * can be multiple records with the same value. */
	private ConcurrentSkipListSet<QueryIndexRecord> indexRecords;

	/** For efficiently accessing indexRecords by key, we keep a map from the key
	 * to the QueryIndexRecord. */
	private ConcurrentHashMap<String, QueryIndexRecord> keyToIndexMap;

	/** Set (interface backed by a concurrent hashmap) of keys currently being
	 * updated in this index. */
	private Set<String> keysBeingUpdated;

	private static Logger logger = Logger.getLogger(QueryIndex.class);

	/** Creates a new query index.
	 * @param id The identifier for the query index.
	 * @param database The IndexedDatabase that this query index is a part of.
	 * @param metadata Metadata of the index that has to be maintained. */
	QueryIndex(int id, IndexedDatabaseImpl database, Index metadata) {
		this.id = id;
		this.database = database;
		this.metadata = metadata;
		indexRecords = new ConcurrentSkipListSet<QueryIndexRecord>();
		keyToIndexMap = new ConcurrentHashMap<String, QueryIndexRecord>();
		keysBeingUpdated = Collections.newSetFromMap(
				new ConcurrentHashMap<String, Boolean>());
		
		BasicConfigurator.configure();
		logger.setLevel(Level.WARN);
		logger.info("Created QueryIndex " + id);
	}

	/** Returns whether a given record satisfies all the predicates.
	 * @param record The input record. */
	private boolean checkPredicates(Record record) {
		for (Predicate predicate : metadata.conjunctivePredicates) {
			// Predicate is false if record is null.
			if (record == null) {
				return false;
			}

			// Predicate is false if the bit is out of range.
			byte[] buffer = record.columns.get(predicate.columnId).value.array();
			int bufferIndex = predicate.bit / 8;
			if (bufferIndex >= buffer.length) {
				return false;
			}

			// Predicate is false if the specified bit does not match the comparator. 
			int byteIndex = predicate.bit % 8;
			if ((buffer[bufferIndex] & (1 << byteIndex )) == 0) {
				if (predicate.comparator.equals(PredicateComparator.BIT_TRUE)) {
					return false;
				}
			} else {
				if (predicate.comparator.equals(PredicateComparator.BIT_FALSE)) {
					return false;
				}
			}
		}
		// Predicate is true otherwise.
		return true;
	}

	/** Updates the index for the record with the given key.
	 *  This function is idempotent. 
	 *  @param key */
	public void updateIndex(String key) throws RecordNotFound, ServiceException,
	TException, InterruptedException {
		// Check if the key starts with the index prefix.
		if (!key.startsWith(metadata.keyPrefix)) {
			return;
		}

		if (keysBeingUpdated.add(key)) {
			QueryIndexRecord newRecord = null;
			do {
				newRecord = null;
				// Get the record and check predicates.			
				Record record = database.getRecord(key);
				if (checkPredicates(record)) {
					// Create a new index record.
					newRecord = new QueryIndexRecord(
							record.columns.get(metadata.sortOrder.getSortByColumnValue()), key);
				}
			}
			// Atomically update the new index record, retry one more time if failed.
			while (!updateIndexRecordAtomically(key, newRecord));
			keysBeingUpdated.remove(key);
		}
	}

	/** Updates a query index record atomically and returns if it was successful.
	 * @param key Key of the record.
	 * @param newRecord New record. */
	public synchronized boolean updateIndexRecordAtomically(String key,
			QueryIndexRecord newRecord) throws RecordNotFound, ServiceException,
			TException {
		// Get a fresh record.
		Record freshRecord = database.getRecord(key);

		// Do not update if the fresh record does not match with the new record.
		if (newRecord != null && freshRecord != null && freshRecord.columns.get(
				metadata.sortOrder.getSortByColumnValue()).compareTo(newRecord.value) != 0) {
			logger.info("Failed to update record: Records mismatch " + newRecord);
			return false;
		}

		logger.info("Updating: Index(" + id + ") : " + key + " -> " + newRecord);

		// Simply return true if a more recent record already exists.
		QueryIndexRecord existingRecord = keyToIndexMap.get(key);
		if (existingRecord != null && newRecord != null && 
				existingRecord.value.timestamp > newRecord.value.timestamp) {
			logger.info("Failed to update record: Fresher record " + newRecord);
			return true;
		}

		// Delete the existing record if it exists and update the map.
		if (existingRecord != null && indexRecords.contains(existingRecord)) {
			if (!indexRecords.remove(existingRecord)) {
				logger.info("Failed to update record: Remove failed " + newRecord);
				return false;
			}
			if (existingRecord != keyToIndexMap.remove(key)) {
				return false;
			}
		}

		// Add new record and update the map.
		if (newRecord != null) {
			if (indexRecords.add(newRecord)) {
				keyToIndexMap.put(key, newRecord);
				return true;
			} else {
				logger.info("Failed to update record: Adding failed " + newRecord);
				return false;
			}
		}
		return true;
	}

	/** Returns the index records for this index.
	 * @param pageLimit Maximum number of records in the result.
	 * @param scanContinuation Specifies a previous continuation, so that
	 *  subsequent records can queried.
	 * @return QueryIndexResult containing at most pageLimit number of records
	 * starting from the previous scanContinuation satisfying the conditions
	 * specified by the index.
	 * @throws ServiceException If the last record specified by the 
	 * scanContinuation has been deleted. In this case, the client must retry with
	 * a new scanContinuation.
	 * @throws TException 
	 * */
	public QueryIndexResult queryIndex(int pageLimit,
			ScanContinuation scanContinuation) throws ServiceException,
			TException {
		QueryIndexResult result = new QueryIndexResult();
		result.matchingKeys = new ArrayList<String>();

		// Get the iterator to the first element.
		Iterator<QueryIndexRecord> indexItr = indexRecords.iterator();
		if (scanContinuation.lastRecord != null) {
			if (!indexRecords.contains(scanContinuation.lastRecord)) {
				throw new ServiceException("Last record in the continuation: "
						+ scanContinuation.lastRecord + " has been deleted. " + 
				"Retry queryIndex() with a new ScanContinuation.");
			}
			// Get the iterator to the element specified in the scanContinuation.
			indexItr = indexRecords.tailSet(
					scanContinuation.lastRecord, false).iterator();
		}
		int numResults = 0;
		result.scanContinuation = new ScanContinuation();
		// Iterate through sufficient results and maintain the last record for the 
		// continuation.
		while(indexItr.hasNext() && numResults < pageLimit) {
			QueryIndexRecord current = indexItr.next();
			result.scanContinuation.lastRecord = current;
			result.matchingKeys.add(numResults, current.key);
			numResults++;
		}
		return result;
	}
}
