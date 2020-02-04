import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/* Joao Maio s1621503 */

// java Sender1b <RemoteHost> <Port> <Filename> <RetryTimeout>
//      <RemoteHost> is IP address or host name for the corresponding receiver.
//          Note that if both sender and receiver run on the same machine,
//          <RemoteHost> can be specified as either 127.0.0.1 or localhost.
//      <Port> is the port number used by the receiver.
//      <Filename> is the file to transfer.
//      <RetryTimeout> should be a positive integer in the millisecond unit

// For example: java Sender1b localhost 54321 sfile 10

// the sender must output number of retransmissions and throughput (in Kbytes/second) only
// in a single line; no other terminal output should be displayed; the following output
// implies that the number of retransmissions is 10 and the throughput is 200 Kbytes/second:
// 10 200

/**
 * Sender1b
 * 
 * @author Joao Maio (s1621503)
 */
public class Sender1b {

    static String remoteHost;
    static int port;

    static UDPClient client;

    static String filename;

    static int retryTimeout;

    static int dataPacketSize = 1024;

    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static Future<Boolean> waitForACK(final int seq) {
        // if ack is correct, return true
        return executorService.submit(new Callable<Boolean>() {
            public Boolean call() throws IOException {
                CustomACKMessage ack = CustomACKMessage.fromDatagramPacket(client.receivePacket());
                return ack.seq == seq;
            }
        });
    }

    public static void sendFile() throws Exception {

        File file = new File(filename);
        long filesize = file.length();
        System.out.println("file size = " + filesize);

        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis, dataPacketSize);

        // https://stackoverflow.com/questions/1074228/is-there-any-java-function-or-util-class-which-does-rounding-this-way-func3-2
        // Divide x by n rounding up
        // int res = (x+n-1)/n
        int last = (int) ((filesize + dataPacketSize - 1) / dataPacketSize);

        for (int seq = 0; seq * dataPacketSize < filesize; seq++) {
            // this is the data in the packet -- at most, the data packet size,
            // could also be shorter if available data is lower than maxSize
            byte[] data = new byte[Math.min(dataPacketSize, bis.available())];
            // buffer the input for the next packet
            bis.read(data);

            CustomUDPPacketData pkt = new CustomUDPPacketData(seq, (seq + 1) == last ? true : false, data);

            boolean match = false;
            // send the packet every time 
            do {
                client.sendPacket(pkt.toByteArray());
                try {
                    match = waitForACK(seq).get(retryTimeout, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {}
            } while (!match);

            // {
            //     // start timer
            //     // executorService.submit(Sender1b::waitForACK, );
            //     // DatagramPacket rcv = null;
    
            //     // Timeout before re-transmitting
            //     // https://stackoverflow.com/questions/24104313/how-do-i-make-a-delay-in-java
            //     client.receivePacket();
            // }


            System.out.println(String.format("sent: %s", pkt));
        }

        fis.close();
    }

    public static void parseArgs(String[] args) throws Exception {
        // Check how many arguments were passed in
        if (args.length != 4) {
            throw new Exception(
                    "incorrect usage - correct usage is:\njava Sender1b <RemoteHost> <Port> <Filename> <RetryTimeout>");
        }
        // attempt to convert arguments - exit if error
        try {
            remoteHost = args[0];
            port = Integer.parseInt(args[1]);
            filename = args[2];
            retryTimeout = Integer.parseInt(args[3]);

            System.out.println(String.format("sending file '%s' to remote host --> %s:%d [timeout=%dms]", filename,
                    remoteHost, port, retryTimeout));
        } catch (Exception e) {
            throw new Exception("argument parse error:" + e);
        }
    }

    public static void main(String[] args) {
        try {
            parseArgs(args);

            client = new UDPClient(remoteHost, port);

            sendFile();

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.exit(0);
    }
}