import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;

/* Joao Maio s1621503 */

// java Receiver2a <Port> <Filename>
//      <Port> is the port number which the receiver will use for receiving
//          messages from the sender.
//      <Filename> is the name to use for the received file to save on local disk.
// For example: java Receiver2a 54321 rfile

/**
 * Receiver1b
 */
public class Receiver2a {

    static int port;
    static UDPServer server;

    static String filename;

    static int dataPacketSize = 1024;

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
        RandomAccessFile raf = new RandomAccessFile(file, "rw");

        boolean last = false;
        int lastSeq = -1;

        // until final packet is received
        while (!last) {
            // receive the next packet
            DatagramPacket p = server.receivePacket();
            CustomUDPPacketData c = CustomUDPPacketData.fromDatagramPacket(p);

            CustomACKMessage ack = new CustomACKMessage(c.seq);
            System.out.println(ack);
            
            server.sendPacket(ack.toByteArray(), p.getAddress(), p.getPort());
            // System.out.println(ack);
            
            // In our GBN protocol, the receiver discards out-of-order packets (J. F. Kurose and K. W. Ross)
            // if this is not a duplicate packet
            if (c.seq == lastSeq + 1) {
                // seek to corresponding part of the file
                raf.seek(dataPacketSize * c.seq);
                // void write(byte[] b, int off, int len)
                // Writes len bytes from the specified byte array starting at offset off to this file.
                raf.write(c.data);
                lastSeq++;
            }
            
            last = c.last;
        }

        raf.close();
    }

    public static void parseArgs(String[] args) throws Exception {
        // Check how many arguments were passed in
        if (args.length != 2) {
            throw new Exception("incorrect usage - correct usage is:\njava Receiver1a <Port> <Filename>");
        }
        // attempt to convert arguments - exit if error
        try {
            port = Integer.parseInt(args[0]);
            filename = args[1];
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