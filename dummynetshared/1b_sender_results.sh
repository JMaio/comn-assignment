#!/bin/bash
FILENAME=`date +"comn-1b-test-%Y%m%d-%H%M%S.py"`
echo " writing results to $FILENAME"
echo "------------------------------"

echo "stats = {" >> $FILENAME

for timeout in 5 10 15 20 25 30 40 50 75 100
do
    echo "timeout = $timeout"
    echo "$timeout: [" >> $FILENAME
    # run 5 times per timeout
    for n in {1..5}
    do
        # Sender1b <RemoteHost> <Port> <Filename> <RetryTimeout>
        echo $n
        echo "\"`java Sender1b localhost 100 test.jpg $timeout`\"," >> $FILENAME
        # wait for receiver to sync
        sleep 2
    done
    echo "-------------------"
    echo "]," >> $FILENAME
done

echo "}" >> $FILENAME

