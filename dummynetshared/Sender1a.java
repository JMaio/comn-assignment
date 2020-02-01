import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;

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
    static String filename;

    static int packetSize = 8;

    public static void sendPacket() {}


    public static void sendFile() throws Exception {
        
        File f = new File(filename);
        
        // https://funnelgarden.com/java_read_file/#New_IO_Reading_Bytes
        // read all bytes at once (limited to 2GB)
        byte[] fBytes = Files.readAllBytes(f.toPath());
        
        // https://stackoverflow.com/questions/1074228/is-there-any-java-function-or-util-class-which-does-rounding-this-way-func3-2
        // Divide x by n rounding up
        // int res = (x+n-1)/n
        
        // end of file packet = total size / packet size
        byte eof = (byte) ((fBytes.length + packetSize - 1) / packetSize);
        System.out.println("packets to transmit = " + (int) eof);

        System.out.println("--------");
        
        for (int seq = 0; seq * packetSize < fBytes.length; seq++) {


            byte[] header = new byte[3];

            // convert seq to 2 bytes
            // https://stackoverflow.com/questions/1735840/how-do-i-split-an-integer-into-2-byte-binary
            header[0] = (byte) (seq & 0xFF);
            header[1] = (byte) ((seq >> 8) & 0xFF);
            header[2] = eof;

            // System.out.println(String.format("header = %2s %2s %2s", (int)header[0], (int)header[1], (int)header[2]));
            
            // calculate offset in file bytes
            int offset = seq * packetSize;

            // System.out.println(seq + " --------");
            // this is the data in the packet
            byte[] data = Arrays.copyOfRange(fBytes, offset, offset+packetSize);
            
            // TODO: send the data
            
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

            sendFile();
            
        } catch (Exception e) {
            System.err.println(e);
            System.exit(0);
        }
        
    }
}