#!/bin/bash
ipfw add pipe 100 in
ipfw add pipe 200 out
# 
ipfw pipe 100 config delay 5ms
ipfw pipe 200 config delay 5ms
