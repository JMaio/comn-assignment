FILENAME=`date +"comn-iperf-test-%Y%m%d-%H%M%S.py"`
echo " writing results to $FILENAME"
echo "------------------------------"

echo "stats = {" >> $FILENAME

for tx_delay in 25
do
    echo "transmission delay = ${tx_delay}ms"

    ipfw pipe 100 config delay ${tx_delay}ms plr 0.05 bw 10Mbits/s
    ipfw pipe 200 config delay ${tx_delay}ms plr 0.05 bw 10Mbits/s

    retry_timeout=`expr $tx_delay \* 4`
    echo "retry timeout = $retry_timeout"

    for window_sz in 1 2 4 8 16 32 64 128 256
    do
        echo "window_sz = $window_sz"
        echo "$window_sz: [" >> $FILENAME

        # enable server
        iperf \
            -s localhost        `# server ("send") mode using -s flag` \
            -M 1KB              `# maximum segment (packet) size` \
            -w ${window_sz}KB   `# window size` \
            >> /dev/null        `# ignore output` \
            &                   `# run in background`
            # -f K                `# format in KB` \
        
        # run 5 times per timeout
        for n in {1..5}
        do
            echo $n
            iperf \
                -c localhost        `# client mode using -c flag` \
                -M 1KB              `# maximum segment (packet) size` \
                -w ${window_sz}KB   `# window size` \
                -F test.jpg         `# file to send` \
                -t 100              `# set timeout to 100s (allow transfer completion) ` \
                -f K                `# format in KB`
            # wait for receiver to sync
            sleep 2
        done

        pkill iperf # kill iperf server

        echo "-------------------"
        echo "]," >> $FILENAME

    done
done

echo "}" >> $FILENAME
