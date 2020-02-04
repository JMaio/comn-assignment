import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

/* Joao Maio s1621503 */

// The receiver should store the transmitted data
// (after removing header from packet) into a local file

// Receiver program must be named as specified below and must accept the
// following options from the command line:

// java Receiver1a <Port> <Filename>
//      <Port> is the port number which the receiver will use for receiving messages from the sender.
//      <Filename> is the name to use for the received file to save on local disk.

// For example: java Receiver1a 54321 rfile
// Expected output: A successfully transferred file to the receiver; both sent and
// received files must be identical at a binary level when checked using the “diff” command.

/**
 * Receiver1a
 */
public class Receiver1a {

    static int port;
    static UDPServer server;

    static String filename;

    static int dataPacketSize = 1024;

    public static DatagramPacket waitAndGetInitial() throws IOException {
        DatagramPacket p = null;
        while (p == null) {

            p = server.receivePacket();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return p;
    }

    public static void receiveFile() throws IOException {
        File file = new File(filename);
        //create file if not exists
        if (!file.exists()) {
            file.createNewFile();
        }

        // TODO: this should probably be kept in memory until all SEQs are received
        FileOutputStream fos = new FileOutputStream(filename);
        
        boolean last = false;

        // until final packet is received
        while (!last) {
            // receive the next packet
            DatagramPacket p = server.receivePacket();
            CustomUDPPacketData c = CustomUDPPacketData.fromDatagramPacket(p);

            System.out.println(c);
            // System.out.println("got: " + c);
            
            // write this to the file
            fos.write(c.data);

            last = c.last;
        }

        fos.close();
    }

    public static void parseArgs(String[] args) throws Exception {
        // Check how many arguments were passed in
        if(args.length != 2) {
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

            // DatagramPacket i = waitAndGetInitial();
            // System.out.println(String.format("initial packet received: size=%d", i.getLength()));

            receiveFile();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }   
    }
}