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

    public static void main(String[] args) {
        // Check how many arguments were passed in
        if(args.length != 3) {
            System.err.println("incorrect usage - correct usage is:");
            System.err.println("java Sender1a <RemoteHost> <Port> <Filename>");
            System.exit(0);
        }

        String remoteHost;
        int port;
        String filename;

        // attempt to convert arguments - exit if error
        try {
            remoteHost = args[0];
            port = Integer.parseInt(args[1]);
            filename = args[2];
            System.out.println(String.format("sending file '%s' to remote host --> %s:%d", filename, remoteHost, port));
        } catch (Exception e) {
            System.err.println("argument parse error:");
            System.err.println(e);
            System.exit(0);
        }





    }
}