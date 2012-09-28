# Script for concurrent testing over varying parameters

NUM_TRIALS=3
TMP_DIR=/tmp/db.$$
NUM_COLUMNS=5
NUM_RECORDS_PER_THREAD=10

mkdir -p $TMP_DIR
ant clean compile

for tries in `seq 1 $NUM_TRIALS`; do
	for threads in `seq 100 100 1000`; do
		records=`echo $NUM_RECORDS_PER_THREAD*$threads | bc`
		echo "Trial $tries : Running concurrent test with $NUM_COLUMNS columns, $threads threads and $records records."
		ant ctest -Dnum.columns=$NUM_COLUMNS -Dnum.records=$records -Dnum.threads=$threads > $TMP_DIR/log
		grep -i indexes $TMP_DIR/log
	done
done

rm -rf $TMP_DIR
