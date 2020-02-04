import java.net.DatagramPacket;

/* Joao Maio s1621503 */

/**
 * CustomACKMessage
 */
public class CustomACKMessage {

    public final int seq;

    public CustomACKMessage(int seq) {
        this.seq = seq;
    }

    public static CustomACKMessage fromDatagramPacket(DatagramPacket p) {
        // ack message should be 2 bytes
        assert p.getLength() == 2;
        byte[] raw = p.getData();
        int seq = ((raw[1] & 0xFF) << 8) | (raw[0] & 0xFF);
        
        return new CustomACKMessage(seq);
    }
    
    public byte[] toByteArray() {
        // ack => | seq number (2Bytes) |
        byte[] a = new byte[2];

        a[0] = (byte) (seq & 0xFF);
        a[1] = (byte) ((seq >> 8) & 0xFF);

        return a;
    }
}