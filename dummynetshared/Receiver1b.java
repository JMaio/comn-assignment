import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

/* Joao Maio s1621503 */

// java Receiver1b <Port> <Filename>
//      <Port> is the port number which the receiver will use for receiving
//      messages from the sender.
//      <Filename> is the name to use for the received file to save on local disk.

// For example: java Receiver1b 54321 rfile


/**
 * Receiver1b
 */
public class Receiver1b {

    static int port;
    static UDPServer server;

    static String filename;

    static int dataPacketSize = 1024;

    public static void receiveFile() throws IOException {
        File file = new File(filename);
        // create file if not exists
        if (!file.exists()) {
            file.createNewFile();
        }

        // TODO: this should probably be kept in memory until all SEQs are received
        FileOutputStream fos = new FileOutputStream(filename);

        boolean last = false;
        int lastSeq = -1;

        // until final packet is received
        while (!last) {
            // receive the next packet
            DatagramPacket p = server.receivePacket();
            CustomUDPPacketData c = CustomUDPPacketData.fromDatagramPacket(p);

            // System.out.println(c);

            CustomACKMessage ack = new CustomACKMessage(c.seq);

            server.sendPacket(ack.toByteArray(), p.getAddress(), p.getPort());
            // System.out.println(ack);

            // if this is not a duplicate packet
            if (c.seq == lastSeq + 1) {
                // write this to the file
                fos.write(c.data);
                lastSeq++;
            }
            
            last = c.last;
        }

        fos.close();
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
            System.out.println(String.format("listening on port '%d' and writing to file '%s'", port, filename));
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