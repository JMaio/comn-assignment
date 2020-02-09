#!/bin/bash

# 10 timeouts
for i in {1..10}
do
    # run 5 times per timeout
    for j in {1..5}
    do
        java Receiver1b 100 test_recv.jpg
    done
done

