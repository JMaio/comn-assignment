import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/* Joao Maio s1621503 */

// java Sender1a <RemoteHost> <Port> <Filename>
// 
// <RemoteHost> is IP address or host name for the corresponding receiver.
//      Note that if both sender and receiver run on the same machine,
//      <RemoteHost> can be specified as either 127.0.0.1 or localhost.
// <Port> is the port number used by the receiver.
// <Filename> is the file to transfer.
// 
// For example: java Sender1a localhost 54321 sfile

/**
 * Sender1a
 * @author Joao Maio (s1621503)
 */
public class Sender1a {

    static String remoteHost;
    static int port;
    
    static UDPClient client;

    static String filename;

    static int dataPacketSize = 1024;

    public static void sendFile() throws Exception {
        
        FileInputStream fis = new FileInputStream(filename);
        int filesize = fis.available();
        BufferedInputStream bis = new BufferedInputStream(fis, dataPacketSize);
        
        // https://stackoverflow.com/questions/1074228/is-there-any-java-function-or-util-class-which-does-rounding-this-way-func3-2
        // Divide x by n rounding up
        // int res = (x+n-1)/n
        
        // end of file packet = total size / packet size
        byte eof = (byte) ((filesize + dataPacketSize - 1) / dataPacketSize);
        System.out.println("packets to transmit = " + (int) eof);

        System.out.println("--------");
        
        for (int seq = 0; seq * dataPacketSize < filesize; seq++) {

            // // header => | seq number (2Bytes) | last packet (1Byte) |
            // byte[] header = new byte[3];
            // // convert seq to 2 bytes
            // // https://stackoverflow.com/questions/1735840/how-do-i-split-an-integer-into-2-byte-binary
            // header[0] = (byte) (seq & 0xFF);
            // header[1] = (byte) ((seq >> 8) & 0xFF);
            // header[2] = eof;

            // this is the data in the packet
            byte[] data = new byte[dataPacketSize];
            // buffer the input for the next packet
            bis.read(data);
            
            CustomUDPPacketData pkt = new CustomUDPPacketData(seq, eof, data);
            // // concatenate header with data
            // // https://stackoverflow.com/questions/5513152/easy-way-to-concatenate-two-byte-arrays
            // byte[] pkt = new byte[header.length + data.length];
            // System.arraycopy(header, 0, pkt, 0, header.length);
            // System.arraycopy(data, 0, pkt, header.length, data.length);

            client.sendPacket(pkt.toByteArray());
            
            // In the sender code, insert, at a minimum, a 10ms gap (i.e., sleep for 10ms)
            // after each packet transmission.
            // https://stackoverflow.com/questions/24104313/how-do-i-make-a-delay-in-java
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.out.println(String.format("sent: %3s", seq));
        }
        fis.close();
    }

    public static void parseArgs(String[] args) throws Exception {
        // Check how many arguments were passed in
        if(args.length != 3) {
            throw new Exception("incorrect usage - correct usage is:\njava Sender1a <RemoteHost> <Port> <Filename>");
        }
        // attempt to convert arguments - exit if error
        try {
            remoteHost = args[0];
            port = Integer.parseInt(args[1]);
            filename = args[2];
            System.out.println(String.format("sending file '%s' to remote host --> %s:%d", filename, remoteHost, port));
        } catch (Exception e) {
            throw new Exception("argument parse error:" + e);
        }
    }

    public static void main(String[] args) {
        try {
            parseArgs(args);

            client = new UDPClient(remoteHost, port);
            // packetSize = client.maxPacketSize;

            sendFile();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        
    }
}