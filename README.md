IndexedDatabase
===============

A multi-threaded indexed database using Thrift and Java's concurrent collections.

Overview
------
IndexedDatabase contains multiple records. Each record has a unique key (string) and multiple columns 
(some of which may be empty) which contain timestamped binary values.

IndexedDatabase also maintains indices over records that satisfy given predicates (which is a conjunction over different bits of the column) in a particular order.

API
---
Three operations have to be supported:
# setRecord(String key, Record record): Atomically updates the record with the given key. Records are merged on a per column basis: column value with a later timestamp wins.
# getRecord(String key): Atomically returns the current record for the given key.
# getQueryIndex(int id, int pageLimit, ScanContinuation cont): Returns atmost pageLimit number of records of the index id starting from the given ScanContinuation. This operation need not be atomic with respect to setRecord and getRecord.

Implementation
--------------
The implementation uses concurrent Java collections.
# Maintain a concurrent hashmap <key, record>.
# Maintain a concurrent skip list set of (QueryIndexRecord, key) and a concurrent hashmap <key, QueryIndexRecord>.

How to run
----------
To generate thrift files, run ` ant thrift `

To build the project, run ` ant compile `

To run single threaded test, run ` ant test `

To run concurrent test with different parameters, run
` ant ctest -Dnum.columns=<num_columns> -Dnum.records=<num_records> -Dnum.threads=<num_threads> `

To run all of the above, run ` ant `

You can change the varying parameters in tester.sh.
To run concurrent tests with varying parameters, run ` sh tester.sh `
