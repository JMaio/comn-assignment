#!/bin/bash
FILENAME=`date +"comn-2a-test-%Y%m%d-%H%M%S.py"`
echo " writing results to $FILENAME"
echo "------------------------------"

echo "stats = {" >> $FILENAME

for tx_delay in 5 25 100
do
    echo "transmission delay = $tx_delay"
    echo "$tx_delay: {" >> $FILENAME

    ipfw pipe 100 config delay ${tx_delay}ms plr 0.005 bw 10Mbits/s
    ipfw pipe 200 config delay ${tx_delay}ms plr 0.005 bw 10Mbits/s

    retry_timeout=`expr $tx_delay \* 4`
    echo "retry timeout = $retry_timeout"
    for window_sz in 1 2 4 8 16 32 64 128 256
    do
        echo "window_sz = $window_sz"
        echo "$window_sz: [" >> $FILENAME
        # run 5 times per timeout
        for n in {1..5}
        do
            # Sender2a <RemoteHost> <Port> <Filename> <RetryTimeout> <WindowSize>
            echo $n
            # java Sender2a localhost 100 test.jpg $retry_timeout $window_sz
            # log output of command (not command itself) into the filename, separate with a comma
            echo "\"`java Sender2a localhost 100 test.jpg $retry_timeout $window_sz`\"," >> $FILENAME
            # wait for receiver to sync
            sleep 5
        done
        echo "-------------------"
        echo "]," >> $FILENAME
    done
    echo "}," >> $FILENAME
done

# for timeout in 5 10 15 20 25 30 40 50 75 100
# do
#     echo "timeout = $timeout"
#     echo "$timeout: [" >> $FILENAME
#     # run 5 times per timeout
#     for n in {1..5}
#     do
#         # Sender1b <RemoteHost> <Port> <Filename> <RetryTimeout>
#         echo $n
#         echo "\"`java Sender1b localhost 100 test.jpg $timeout`\"," >> $FILENAME
#         clear; java Sender2a localhost 100 test.jpg 20 12
#         # wait for receiver to sync
#         sleep 2
#     done
#     echo "-------------------"
#     echo "]," >> $FILENAME
# done

echo "}" >> $FILENAME

