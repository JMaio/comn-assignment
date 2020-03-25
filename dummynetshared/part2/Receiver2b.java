import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;

/* Joao Maio s1621503 */

// java Receiver2b <Port> <Filename> <WindowSize>
//      <Port> is the port number which the receiver will use for receiving
//              messages from the sender.
//      <Filename> is the name to use for the received file to save on local disk.
//      <WindowSize> should be a positive integer.
// For example: java Receiver2b 54321 rfile 10

/**
 * Receiver2b
 */
public class Receiver2b {

    static int port;
    static UDPServer server;

    static String filename;

    static int dataPacketSize = 1024;

    static int windowSize;

    public static void receiveFile() throws IOException {
        File file = new File(filename);
        // empty file
        if (file.exists()) {
            file.delete();
            file.createNewFile();
        }

        // TODO: this should probably be kept in memory until all SEQs are received
        // FileOutputStream fos = new FileOutputStream(filename);
        
        // "rw" :	Open for reading and writing. 
        // If the file does not already exist then an attempt will be made to create it.
        // this might impact performance but it's done in advance of 2b, where it may come in handy
        RandomAccessFile raf = new RandomAccessFile(file, "rw");

        boolean last = false;
        int base = -1;

        // until final packet is received
        while (!last) {
            // receive the next packet
            DatagramPacket p = server.receivePacket();
            CustomUDPPacketData c = CustomUDPPacketData.fromDatagramPacket(p);
            // System.out.println(c);
            
            CustomACKMessage ack = new CustomACKMessage(c.seq);
            // System.out.println(ack);

            if (c.seq > base && c.seq <= base + windowSize) {
                // pkt in window size
                // send ack relating to this packet
                server.sendPacket(ack.toByteArray(), p.getAddress(), p.getPort());
                // seek to corresponding part of the file
                raf.seek(dataPacketSize * c.seq);
                // void write(byte[] b, int off, int len)
                // Writes len bytes from the specified byte array starting at offset off to this file.
                raf.write(c.data);
                // lastSeq++;
                base++;
                last = c.last;

            } else if (c.seq < base) {
                // already received, still need to send ack
                server.sendPacket(ack.toByteArray(), p.getAddress(), p.getPort());
                // System.out.println("got out of order pkt: " + c.seq);
            }
            // otherwise, ignore the packet

        }

        raf.close();
    }

    public static void parseArgs(String[] args) throws Exception {
        // Check how many arguments were passed in
        if (args.length != 3) {
            throw new Exception("incorrect usage - correct usage is:\njava Receiver2b <Port> <Filename> <WindowSize>");
        }
        // attempt to convert arguments - exit if error
        try {
            port = Integer.parseInt(args[0]);
            filename = args[1];
            windowSize = Integer.parseInt(args[2]);
            // System.out.println(String.format("listening on port '%d' and writing to file '%s'", port, filename));
        } catch (Exception e) {
            throw new Exception("argument parse error:" + e);
        }
    }

    public static void main(String[] args) {
        try {
            parseArgs(args);

            server = new UDPServer(port);

            receiveFile();
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(0);
    }
}