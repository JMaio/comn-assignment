#!/bin/bash

# 10 timeouts
for tx_delay in 5 25 100
do
    echo "transmission delay = $tx_delay"

    for window_sz in 1 2 4 8 16 32 64 128 256
    do
        echo "window_sz = $window_sz"
        # run 5 times per timeout
        for n in {1..5}
        do
            echo $n
            java Receiver2a 100 test_recv.jpg
            sleep 1
        done
    done
done

