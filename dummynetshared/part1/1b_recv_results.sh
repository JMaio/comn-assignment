#!/bin/bash

# 10 timeouts
for timeout in 5 10 15 20 25 30 40 50 75 100
do
    echo "timeout = $timeout"
    # run 5 times per timeout
    for n in {1..5}
    do
        echo $n
        java Receiver1b 100 test_recv.jpg
        sleep 2
    done
done

