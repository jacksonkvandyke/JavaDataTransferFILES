import java.io.Serializable;

public class Packet implements Serializable {

    private String fileName = "";
    private long totalPackets = 0;
    private int packetSequence = 0;
    private byte packetData[];

    public Packet(String fileName, long maxPackets, int sequence, byte data[]){
        this.fileName = fileName;
        this.totalPackets = maxPackets;
        this.packetSequence = sequence;
        this.packetData = data;
    }

    public String getFilename() {
        return this.fileName;
    }

    public long getTotalPackets() {
        return this.totalPackets;
    }

    public int getSequence() {
        return this.packetSequence;
    }

    public byte[] getData() {
        return this.packetData;
    }
}
