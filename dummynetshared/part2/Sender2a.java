import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/* Joao Maio s1621503 */

// java Sender2a <RemoteHost> <Port> <Filename> <RetryTimeout> <WindowSize>
//      <RemoteHost> is IP address or host name for the corresponding receiver.
//          Note that if both sender and receiver run on the same machine,
//          <RemoteHost> can be specified as either 127.0.0.1 or localhost.
//      <Port> is the port number used by the receiver.
//      <Filename> is the file to transfer.
//      <RetryTimeout> should be a positive integer in the millisecond unit.
//      <WindowSize> should be a positive integer.

// For example: java Sender2a localhost 54321 sfile 10 5

// The sender must output throughput (in Kbytes/second) only in a single line;
// no other terminal output should be displayed; 
// the following output implies that the throughput is 200 Kbytes/second:
// 200


/**
 * Sender2a
 * 
 * @author Joao Maio (s1621503)
 */
public class Sender2a {

    static String remoteHost;
    static int port;

    static UDPClient client;

    static String filename;
    static long filesize;

    static int retryTimeout;
    // choose 10 retries as default
    final static int maxRetries = 10;
    static int totalRetries = 0;

    static int windowSize;

    static int dataPacketSize = 1024;

    // measures the interval between first message transmission time 
    // and acknowledgement receipt time for last message
    public static long sendFile() throws Exception {

        File file = new File(filename);
        filesize = file.length();
        // System.out.println("file size = " + filesize);

        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis, dataPacketSize);

        // https://stackoverflow.com/questions/1074228/is-there-any-java-function-or-util-class-which-does-rounding-this-way-func3-2
        // Divide x by n rounding up
        // int res = (x+n-1)/n
        int last = (int) ((filesize + dataPacketSize - 1) / dataPacketSize);

        // get starting time
        long t0 = System.currentTimeMillis();
        long t1 = t0;
        for (int seq = 0; seq * dataPacketSize < filesize; seq++) {
            // this is the data in the packet -- at most, the data packet size,
            // could also be shorter if available data is lower than maxSize
            byte[] data = new byte[Math.min(dataPacketSize, bis.available())];
            // buffer the input for the next packet
            bis.read(data);

            CustomUDPPacketData pkt = new CustomUDPPacketData(seq, (seq + 1) == last ? true : false, data);

            // create ack message with "-1" to be "invalid" message
            CustomACKMessage ack = new CustomACKMessage(-1);
            int retries = 0;

            do {
                // send data packet
                client.sendPacket(pkt.toByteArray());
                
                // System.out.println(String.format("sent: %s", pkt));
                
                try {
                    // receive the ack packet
                    DatagramPacket p = client.receivePacket();
                    ack = CustomACKMessage.fromDatagramPacket(p);
                    
                } catch (SocketTimeoutException e) {
                    retries++;
                    // if final packet and max retries exceeded, stop
                    if (seq + 1 == last && retries >= maxRetries) {
                        break;
                    }
                }

                // if incorrect seq, re-send
            } while (ack.seq != seq);

            if (ack.seq != seq && seq + 1 == last) {
                // if last ack was not received
                // ignore retransmissions to receive the last ACK
                // ignore the time it takes to receive the last 
                break;
            } else {
                // if valid ack packet, record system time
                t1 = System.currentTimeMillis();
                // tally retries
                totalRetries += retries;
            }
        }

        fis.close();

        // time taken to send
        return t1 - t0;
    }

    public static void parseArgs(String[] args) throws Exception {
        // Check how many arguments were passed in
        if (args.length != 5) {
            throw new Exception(
                "incorrect usage - correct usage is:\njava Sender2a <RemoteHost> <Port> <Filename> <RetryTimeout> <WindowSize>"
            );
        }
        // attempt to convert arguments - exit if error
        try {
            remoteHost = args[0];
            port = Integer.parseInt(args[1]);
            filename = args[2];
            retryTimeout = Integer.parseInt(args[3]);
            windowSize = Integer.parseInt(args[4]);

            // System.out.println(String.format("sending file '%s' to remote host --> %s:%d [timeout=%dms]", filename,
            //         remoteHost, port, retryTimeout));
        } catch (Exception e) {
            throw new Exception("argument parse error:" + e);
        }
    }

    public static void main(String[] args) {
        try {
            parseArgs(args);

            client = new UDPClient(remoteHost, port);
            client.socket.setSoTimeout(retryTimeout);

            long time = sendFile();

            // 1 ms == 1s ; 1KB = 1024 B ;
            // https://www.technicalkeeda.com/java-tutorials/get-file-size-in-java
            double throughput = ((double) filesize / 1024) / ((double) time / 1000);

            System.out.println(String.format("%d %f", totalRetries, throughput));

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.exit(0);
    }
}