import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

// sample code from slides
// https://www.inf.ed.ac.uk/teaching/courses/comn/lecture-notes/chapter_2.pdf

class UDPClient extends UDPInterface {

    public final String host;
    public final InetAddress IPAddress;

    public UDPClient(String host, int port) throws Exception {
        this.host = host;
        UDPInterface.port = port;

        socket = new DatagramSocket();
        IPAddress = InetAddress.getByName(host);
    }

    @Override
    public void sendPacket(byte[] sendData, InetAddress ip, int p) throws IOException {
        DatagramPacket pkt = new DatagramPacket(sendData, sendData.length, ip, p); 
        socket.send(pkt);
    }

    public void sendPacket(byte[] sendData) throws IOException {
        sendPacket(sendData, IPAddress, port);
    }

}