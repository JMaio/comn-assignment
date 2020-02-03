// sample code from slides
// https://www.inf.ed.ac.uk/teaching/courses/comn/lecture-notes/chapter_2.pdf

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

abstract class UDPInterface {

    static int port;
    // static InetAddress IPAddress;
    static DatagramSocket socket;
    static int maxPacketSize = 1024;


    abstract public void sendPacket(byte[] sendData, InetAddress ipAddress, int port) throws Exception;

    DatagramPacket receivePacket() throws IOException {
        byte[] data = new byte[maxPacketSize];
        DatagramPacket p = new DatagramPacket(data, maxPacketSize);

        socket.receive(p);
        return p;
    };
    
}