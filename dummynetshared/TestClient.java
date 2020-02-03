import java.net.DatagramPacket;

class TestClient {
    public static void main(String[] args) throws Exception {
        
        UDPClient cl = new UDPClient("localhost", 9876);

        cl.sendPacket("hello world from udp client".getBytes());

        // Thread.sleep(1000);

        DatagramPacket r = cl.receivePacket();

        String modifiedSentence = new String(r.getData()); 

        System.out.println(modifiedSentence);

    }
}