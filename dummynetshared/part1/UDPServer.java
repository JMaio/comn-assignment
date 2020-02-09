import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class UDPServer extends UDPInterface {

    public UDPServer(int port) throws Exception {
        UDPInterface.port = port;
        socket = new DatagramSocket(port);
    }
    
    @Override
    public void sendPacket(byte[] sendData, InetAddress ipAddress, int port) throws IOException {
        DatagramPacket p = new DatagramPacket(sendData, sendData.length, ipAddress, port); 
        socket.send(p);
    }
    
    public void serve() throws IOException {
        while (true) {
            DatagramPacket p = receivePacket();
            String r = new String(p.getData());

            System.out.println(r);

            sendPacket(r.getBytes(), p.getAddress(), p.getPort());
        }
    }
}