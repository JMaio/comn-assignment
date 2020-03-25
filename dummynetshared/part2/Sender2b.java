import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.util.Timer;
import java.util.TimerTask;

/* Joao Maio s1621503 */

// java Sender2b <RemoteHost> <Port> <Filename> <RetryTimeout> <WindowSize>
//      <RemoteHost> is IP address or host name for the corresponding receiver.
//          Note that if both sender and receiver run on the same machine,
//          <RemoteHost> can be specified as either 127.0.0.1 or localhost.
//      <Port> is the port number used by the receiver.
//      <Filename> is the file to transfer.
//      <RetryTimeout> should be a positive integer in the millisecond unit.
//      <WindowSize> should be a positive integer.

// For example: java Sender2b localhost 54321 sfile 10 5

// The sender must output throughput (in Kbytes/second) only in a single line;
// no other terminal output should be displayed; 
// the following output implies that the throughput is 200 Kbytes/second:
// 200


/**
 * Sender2b
 * 
 * @author Joao Maio (s1621503)
 */
public class Sender2b {

    static String remoteHost;
    static int port;

    static UDPClient client;

    static String filename;
    static long filesize;

    static int retryTimeout;
    // choose 10 retries as default
    final static int maxRetries = 10;
    static int retries = 0;

    static int windowSize;

    static int dataPacketSize = 1024;

    // the base of the window, accessible from within threads
    // treat this as last good sent packet
    public static int base = 0;
    // next packet to send
    public static int nextSeqNum = 0;

    public static Object lock = new Object();


    // measures the interval between first message transmission time 
    // and acknowledgement receipt time for last message
    public static long sendFile() throws Exception {

        File file = new File(filename);
        filesize = file.length();

        FileInputStream fis = new FileInputStream(file);
        final BufferedInputStream bis = new BufferedInputStream(fis, dataPacketSize);

        // https://stackoverflow.com/questions/1074228/is-there-any-java-function-or-util-class-which-does-rounding-this-way-func3-2
        // Divide x by n rounding up
        // int res = (x+n-1)/n
        final int last = (int) ((filesize + dataPacketSize - 1) / dataPacketSize) - 1;

        final CustomUDPPacketData[] pkts = new CustomUDPPacketData[last+1];
        final CustomACKMessage[] acks = new CustomACKMessage[last+1];
        
        // runnable for sending packets asynchronously
        final Runnable threadedTx = new Runnable() {
            @Override
            public void run() {
                // until transfer is complete
                while (base <= last && retries < maxRetries) {
                    // if nextSeqNum is within window, create the packet
                    if (nextSeqNum < base + windowSize && nextSeqNum <= last) {
                        // keep creating new packets to send, loop through
                        // un-ack'd packets
                        synchronized (lock) {
                            try {
                                CustomUDPPacketData pkt = pkts[nextSeqNum];
                                // packet not yet created
                                if (pkt == null) {
                                    // okay to use buffered input stream because packets will always be created in order
                                    byte[] data = new byte[Math.min(dataPacketSize, bis.available())];
                                    bis.read(data);
                                    pkt = new CustomUDPPacketData(nextSeqNum, nextSeqNum == last, data);
                                    // System.out.println("create pkt: " + pkt);
                                    pkts[nextSeqNum] = pkt;
                                }
                                // packet not yet ack'd
                                // System.out.println("ack " + nextSeqNum + " = " + acks[nextSeqNum]);
                                if (acks[nextSeqNum] == null) {
                                    // (re-)send next packet
                                    client.sendPacket(pkt.toByteArray());
                                    System.out.println("sent #" + nextSeqNum);
                                }
                                nextSeqNum++;
                            } catch (Exception e) {
                                // System.out.println("send exception: " + e);
                            }
                            // no need to handle retries here - reset nextSeqNum from receiver thread
                        }
                    }
                }
            }
        };
        
        // runnable for processing acks asynchronously
        final Runnable threadedRx = new Runnable() {
            @Override
            public void run() {
                // until transfer is complete
                while (base <= last && retries < maxRetries) {
                    // if waiting for a packet
                    // only prioritise this if window size reached
                    if (base < nextSeqNum) {
                        // only lock if there is a packet to receive
                        synchronized (lock) {
                            // next pkt should be base+1
                            int lastBase = base;
                            try {
                                DatagramPacket p = client.receivePacket();
                                CustomACKMessage ack = CustomACKMessage.fromDatagramPacket(p);

                                // save this ack. it should be used to tell whether a packet
                                // hase been sent and properly received
                                acks[ack.seq] = ack;
                                // move window forward if this is the base packet
                                if (ack.seq == base) {
                                    // System.out.println(ack);
                                    base++;
                                    // System.out.println("base = " + base);
                                }
                                retries = 0;

                            } catch (Exception e) {
                                // ack not received, keep trying to send packet
                                // restart from base
                                nextSeqNum = base;
                                // System.out.println("rx exception: " + e);
                                // System.out.println("nextSeqNum = " + base);
                            }
                        }
                    }
                }
            }
        };
        
        Thread tx = new Thread(threadedTx);
        Thread rx = new Thread(threadedRx);

        // get starting time
        long t0 = System.currentTimeMillis();

        tx.start();
        rx.start();

        // wait until receiver thread has completed
        // meaning last ack has been received
        // terminates early if no ack is received after 10 retries
        synchronized (rx) {
            rx.wait();
        }
        
        // if (retries == maxRetries) {
        //     System.out.println("max retries exceeded");
        // }

        long t1 = System.currentTimeMillis();

        fis.close();

        // time taken to send
        return t1 - t0;
    }

    public static void parseArgs(String[] args) throws Exception {
        // Check how many arguments were passed in
        if (args.length != 5) {
            throw new Exception(
                "incorrect usage - correct usage is:\njava Sender2b <RemoteHost> <Port> <Filename> <RetryTimeout> <WindowSize>"
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
            // System.out.println("timeout: " + retryTimeout + "ms");
            // System.out.println("window size: " + windowSize);

            long time = sendFile();

            // 1 ms == 1s ; 1KB = 1024 B ;
            // https://www.technicalkeeda.com/java-tutorials/get-file-size-in-java
            double throughput = ((double) filesize / 1024) / ((double) time / 1000);

            System.out.println(String.format("%f", throughput));

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.exit(0);
    }
}