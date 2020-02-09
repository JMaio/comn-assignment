// sample code from slides
// https://www.inf.ed.ac.uk/teaching/courses/comn/lecture-notes/chapter_2.pdf

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/* Joao Maio s1621503 */

abstract class UDPInterface {

    public static int port;
    public DatagramSocket socket;
    // max defined here for convenience -- should probably be specified in 
    // client / server in future
    public final int maxPacketSize = 1027;


    abstract public void sendPacket(byte[] sendData, InetAddress ipAddress, int port) throws Exception;

    DatagramPacket receivePacket() throws IOException {
        byte[] data = new byte[maxPacketSize];
        DatagramPacket p = new DatagramPacket(data, maxPacketSize);

        socket.receive(p);
        return p;
    };
    
}