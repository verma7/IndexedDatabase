IndexedDatabase
===============

A multi-threaded indexed database using Thrift and Java's concurrent collections.

How to run
----------

To generate thrift files, run `ant thrift`

To build the project, run `ant compile`

To run single threaded test, run `ant test`

To run concurrent test with different parameters, run
` ant ctest -Dnum.columns=<num_columns> -Dnum.records=<num_records> -Dnum.threads=<num_threads> `

To run all of the above, run ` ant`

You can change the varying parameters in tester.sh.
To run concurrent tests with varying parameters, run `sh tester.sh`
