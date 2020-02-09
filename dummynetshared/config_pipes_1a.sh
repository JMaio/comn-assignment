#!/bin/bash
ipfw pipe 100 config delay 5ms bw 10Mbits/s
ipfw pipe 200 config delay 5ms bw 10Mbits/s
