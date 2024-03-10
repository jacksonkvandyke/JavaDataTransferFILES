import java.io.Serializable;

public class Packet implements Serializable {

    private String fileName = "";
    private int packetSequence = 0;
    private byte packetData[];

    public Packet(String fileName, int sequence, byte data[]){
        this.fileName = fileName;
        this.packetSequence = sequence;
        this.packetData = data;
    }

    public String getFilename() {
        return this.fileName;
    }

    public int getSequence() {
        return this.packetSequence;
    }

    public byte[] getData() {
        return this.packetData;
    }
}