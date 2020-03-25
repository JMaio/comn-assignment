#!/bin/bash

for tx_delay in 25
do
    echo "transmission delay = $tx_delay"

    for window_sz in 1 2 4 8 16 32
    do
        echo "window_sz = $window_sz"
        for n in {1..5}
        do
            echo $n
            java Receiver2b 100 test_recv.jpg $window_sz
            sleep 5
        done
    done
done

