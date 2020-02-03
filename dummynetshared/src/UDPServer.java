import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class UDPServer extends UDPInterface {

    public UDPServer(int port) throws Exception {
        this.port = port;
        socket = new DatagramSocket(port);
    }

    // private void dumpToFile(byte[] data) {
    //     File
    // }
    
    @Override
    public void sendPacket(byte[] sendData, InetAddress ipAddress, int port) throws IOException {
        DatagramPacket p = new DatagramPacket(sendData, sendData.length, ipAddress, port); 
        socket.send(p);
    }
    
    // @Override
    // public DatagramPacket receivePacket(int length) throws IOException {
    //     byte[] data = new byte[length];
    //     DatagramPacket p = new DatagramPacket(data, length);

    //     socket.receive(p);
    //     return p;
    // }
    
    public void serve() throws IOException {
        while (true) {
            DatagramPacket p = receivePacket();
            String r = new String(p.getData());

            System.out.println(r);

            sendPacket(r.getBytes(), p.getAddress(), p.getPort());
        }
    }

    public static void main(String args[]) throws Exception {
        
        DatagramSocket serverSocket = new DatagramSocket(9876);
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];

        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            
            String sentence = new String(receivePacket.getData());

            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            
            String capitalizedSentence = sentence.toUpperCase();
            sendData = capitalizedSentence.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            serverSocket.send(sendPacket);
        }

        // serverSocket.close();
    }
}