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

    // public UDPInterface() {}
    //  {
    //     srvHost = host;
    //     srvPort = port;
    //     // IPAddress = InetAddress.getByName(host);
    //     // clientSocket = new DatagramSocket();
    // }

    abstract public void sendPacket(byte[] sendData, InetAddress ipAddress, int port) throws Exception;
    //  {
    //     // DatagramPacket p = new DatagramPacket(sendData, sendData.length, ipAddress, port);
    //     // socket.send(p);
    // }

    DatagramPacket receivePacket() throws IOException {
        byte[] data = new byte[maxPacketSize];
        DatagramPacket p = new DatagramPacket(data, maxPacketSize);

        socket.receive(p);
        return p;
    };
    //  {
    //     // byte[] receiveData = new byte[length];
    //     // DatagramPacket receivePacket = new DatagramPacket(receiveData, length);
    //     // socket.receive(receivePacket);

    //     // return receiveData;
    // }

    // public void close() {
    //     // socket.close();
    // }
    
}