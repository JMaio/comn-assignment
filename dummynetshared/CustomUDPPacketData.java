import java.net.DatagramPacket;

/* Joao Maio s1621503 */

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
    public boolean last;

    public byte[] data;

    private static final int headerSize = 3;

    public CustomUDPPacketData(int seq, boolean last, byte[] data) {
        this.seq = seq;
        this.last = last;
        this.data = data;
    }

    public static CustomUDPPacketData fromDatagramPacket(DatagramPacket p) {
        byte[] raw = p.getData();
        // copy data over - get data size from packet
        // https://piazza.com/class/k5kzwbadzbk3aj?cid=13
        byte[] d = new byte[p.getLength() - headerSize];
        System.arraycopy(raw, headerSize, d, 0, d.length);

        CustomUDPPacketData cpkt = new CustomUDPPacketData(((raw[1] & 0xFF) << 8) | (raw[0] & 0xFF),
                raw[2] == 0 ? true : false, d);

        return cpkt;
    }

    public byte[] toByteArray() {
        // header => | seq number (2Bytes) | last packet (1Byte) |
        byte[] header = new byte[3];
        // convert seq to 2 bytes
        // https://stackoverflow.com/questions/1735840/how-do-i-split-an-integer-into-2-byte-binary
        header[0] = (byte) (seq & 0xFF);
        header[1] = (byte) ((seq >> 8) & 0xFF);
        header[2] = (byte) (last ? 0 : 1);

        // concatenate header with data
        // https://stackoverflow.com/questions/5513152/easy-way-to-concatenate-two-byte-arrays
        byte[] pkt = new byte[header.length + data.length];
        System.arraycopy(header, 0, pkt, 0, header.length);
        System.arraycopy(data, 0, pkt, header.length, data.length);

        return pkt;
    }

    public String getByteSeq() {
        return String.format("%8s %8s (%d)", Integer.toBinaryString(seq & 0xFF),
                Integer.toBinaryString((seq >> 8) & 0xFF), seq);
    }

    @Override
    public String toString() {
        return String.format("CustomUDPPacketData(seq=%d, last=%b, size=%d)", seq, last, data.length);
    }

}