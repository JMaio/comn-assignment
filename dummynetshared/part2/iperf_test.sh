FILENAME=`date +"comn-iperf-test-%Y%m%d-%H%M%S.py"`
LOGS=`date +"iperf-s-log-%Y%m%d-%H%M%S.txt"`
LOGC=`date +"iperf-c-log-%Y%m%d-%H%M%S.txt"`

clear

# create log
touch $LOGS
touch $LOGC

echo " writing results to $FILENAME"
echo "------------------------------"
echo "stats = {" >> $FILENAME

PATTERN=\\d+\\.\\d+

for tx_delay in 25
do
    echo "transmission delay = ${tx_delay}ms"

    ipfw pipe 100 config delay ${tx_delay}ms plr 0.005 bw 10Mbits/s
    ipfw pipe 200 config delay ${tx_delay}ms plr 0.005 bw 10Mbits/s

    retry_timeout=`expr $tx_delay \* 4`
    echo "retry timeout = $retry_timeout"

    for window_sz in 1 2 4 8 16 32 # 64 128 256
    do
        echo "window_sz = $window_sz"
        echo "$window_sz: [" >> $FILENAME

        # enable server
        iperf \
            -s                  `# server ("send") mode using -s flag` \
            -w ${window_sz}KB   `# window size` \
            -f KB               `# format in KB` \
            > $LOGS             `# pipe to server log` \
            2> /dev/null        `# discard stderr` \
            &                   `# run in background` \
            # -M 1KB              `# maximum segment (packet) size` \
        
        # run 5 times per timeout
        for n in {1..5}
        do
            echo $n
            RESULT="$(iperf \
                -c localhost        `# client mode using -c flag` \
                -M 1KB              `# maximum segment (packet) size` \
                -w ${window_sz}KB   `# window size` \
                -n 900KB            `# set number of bytes to transmit ` \
                -F test.jpg         `# file to send` \
                -f KB               `# format in KB` \
                2> /dev/null
            )"
                # -t 100              `# set timeout to allow transfer completion ` \
                # -y                  `# output comma-separated values`
                # -t 300              `# set timeout to allow transfer completion ` \
            
            THRU=$(echo $RESULT \
                | grep -oP "$PATTERN KBytes/sec" \
                | grep -oP "$PATTERN")

            echo "$RESULT" >> $LOGC
            echo $THRU
            echo "${THRU}," >> $FILENAME
            # echo $RESULT | grep 'KB'
            # wait for receiver to sync
            sleep 5
        done

        pkill iperf # kill iperf server
        sleep 5

        echo "-------------------"
        echo "]," >> $FILENAME

    done
done

echo "}" >> $FILENAME
