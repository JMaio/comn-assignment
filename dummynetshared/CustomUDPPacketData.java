import java.net.DatagramPacket;

/**
 * CustomUDPPacket
 */
public class CustomUDPPacketData {

    // custom udp packet for COMN
    // ------ header -------
    // seq: 2 bytes
    // eof: 1 byte
    // ------- data --------
    // data: 1024 bytes


    public int seq;
    public int eof;

    public byte[] data;

    public CustomUDPPacketData(int seq, int eof, byte[] data) {
        this.seq = seq;
        this.eof = eof;
        this.data = data;    
    }

    public static CustomUDPPacketData fromDatagramPacket(DatagramPacket p) {
        byte[] raw = p.getData();
        // copy data over
        byte[] d = new byte[1024];
        System.arraycopy(raw, 0, d, 0, d.length);

        CustomUDPPacketData cpkt = new CustomUDPPacketData(
            (int) ((raw[1] << 8) | raw[0]), 
            (int) raw[2], 
            d);

        return cpkt;
    }

    public byte[] toByteArray() {
        // header => | seq number (2Bytes) | last packet (1Byte) |
        byte[] header = new byte[3];
        // convert seq to 2 bytes
        // https://stackoverflow.com/questions/1735840/how-do-i-split-an-integer-into-2-byte-binary
        header[0] = (byte) (seq & 0xFF);
        header[1] = (byte) ((seq >> 8) & 0xFF);
        header[2] = (byte) eof;

        // concatenate header with data
        // https://stackoverflow.com/questions/5513152/easy-way-to-concatenate-two-byte-arrays
        byte[] pkt = new byte[header.length + data.length];
        System.arraycopy(header, 0, pkt, 0, header.length);
        System.arraycopy(data, 0, pkt, header.length, data.length);

        return pkt;
    }
    
}