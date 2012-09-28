namespace java memdb.autogen

typedef i64 Timestamp

// A binary value with an associated updated-at timestamp.
struct TimestampedValue {
  1: optional binary value
  2: required Timestamp timestamp
}

// A Record consists of a map of column identifier (integer) to TimestampedValue.
struct Record {
  1: required map<i16, TimestampedValue> columns
}

union SortOrder {
  # index of column value within a record, byte-order, ascending
  1: i16 sortByColumnValue
}

enum PredicateComparator {
  BIT_FALSE  = 0
  BIT_TRUE   = 1
}

struct Predicate {
  # Which comparison operator?
  1: required PredicateComparator comparator

  # Which column to compare?
  2: required i16 columnId

  # Which bit of the column value to compare?
  3: required i16 bit
}

struct Index {
  # Unique identifier for this index.
  1: required i16 id

  # Record key prefix that this index applies to.
  2: required string keyPrefix

  # Index predicates, ANDed together.
  3: required list<Predicate> conjunctivePredicates

  # Sort order for the index entries.
  4: required SortOrder sortOrder
}

# Added by Abhishek Verma
# Record of a Query Index.
struct QueryIndexRecord {
  # Column value on which this index is sorted.
  1: required TimestampedValue value
  # Key of the record that this value belongs to.
  2: required string key
}

// A cursor for continuing scan results.
struct ScanContinuation {
  # Last record in the previous QueryIndexResult.
  1: optional QueryIndexRecord lastRecord
}

struct QueryIndexResult {
  # Keys that matched this index query.
  1: required list<string> matchingKeys

  # Cursor that can be used to continue this scan.
  2: optional ScanContinuation scanContinuation
}

exception RecordNotFound {
  1: string message
}

exception InvalidIndex {
  1: string message
}

exception ServiceException {
  1: string message
}

// The service will have a set of statically defined indexes.
// const list<Index> INDEXES = [...]

service IndexedDatabase {
  /**
   * Get the current value of a record identified by the given key.
   */
  Record getRecord(1: string key)
    throws (1: RecordNotFound notFound,
            2: ServiceException serviceException);

  /**
   * Update a record with the given value.
   * A record update merges with an existing record value on a per-column basis:
   * columns with higher timestamps win merge conflicts.
   *
   * This call should also update any relevant indexes.
   */
  void setRecord(1: string key, 2: Record value)
    throws (1: ServiceException serviceException,
    		2: RecordNotFound notFound)

  /**
   * Query the given index. Return at most pageLimit records.
   * Use the given ScanContinuation as a starting point for the scan.
   */
  QueryIndexResult queryIndex(
    1: i16 indexId,
    2: i32 pageLimit,
    3: ScanContinuation scanContinuation)
    throws (1: InvalidIndex invalidIndex,
            2: ServiceException serviceException)
}

