FILENAME=`date +"comn-iperf-test-%Y%m%d-%H%M%S.py"`
LOG=`date +"iperf-log-%Y%m%d-%H%M%S.txt"`

clear

# create log
touch $LOG

echo " writing results to $FILENAME"
echo "------------------------------"
echo "stats = {" >> $FILENAME

PATTERN=\\d+\\.\\d+

for tx_delay in 25
do
    echo "transmission delay = ${tx_delay}ms"

    ipfw pipe 100 config delay ${tx_delay}ms plr 0.05 bw 10Mbits/s
    ipfw pipe 200 config delay ${tx_delay}ms plr 0.05 bw 10Mbits/s

    retry_timeout=`expr $tx_delay \* 4`
    echo "retry timeout = $retry_timeout"

    for window_sz in 1 2 4 8 16 32 # 64 128 256
    do
        echo "window_sz = $window_sz"
        echo "$window_sz: [" >> $FILENAME

        # enable server
        iperf \
            -s                  `# server ("send") mode using -s flag` \
            -M 1KB              `# maximum segment (packet) size` \
            -w ${window_sz}KB   `# window size` \
            >> /dev/null        `# ignore output` \
            &                   `# run in background`
            # -f K                `# format in KB` \
        
        # run 5 times per timeout
        for n in {1..5}
        do
            echo $n
            RESULT="$(iperf \
                -c localhost        `# client mode using -c flag` \
                -M 1KB              `# maximum segment (packet) size` \
                -w ${window_sz}K    `# window size` \
                -F test.jpg         `# file to send` \
                -n 900K             `# set number of bytes to transmit ` \
                -f K                `# format in KB` \
            )"
                # -y                  `# output comma-separated values`
                # -t 300              `# set timeout to allow transfer completion ` \
            
            THRU=$(echo $RESULT \
                | grep -oP "$PATTERN KBytes/sec" \
                | grep -oP "$PATTERN")

            echo "$RESULT" >> $LOG
            echo $THRU
            echo "${THRU}," >> $FILENAME
            # echo $RESULT | grep 'KB'
            # wait for receiver to sync
            sleep 3
        done

        pkill iperf # kill iperf server
        sleep 5

        echo "-------------------"
        echo "]," >> $FILENAME

    done
done

echo "}" >> $FILENAME
