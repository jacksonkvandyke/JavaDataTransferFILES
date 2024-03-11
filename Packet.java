import java.io.Serializable;

public class Packet implements Serializable {

    private String fileName = "";
    private int packetSequence = 0;
    private byte packetData[];
    private int packetDataLength = 0;
    private long totalData = 0;
    private boolean isDirectory = false;

    public Packet(String fileName, int sequence, byte data[], int packetDataLength, long totalData){
        this.fileName = fileName;
        this.packetSequence = sequence;
        this.packetData = data;
        this.packetDataLength = packetDataLength;
        this.totalData = totalData;
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

    public int getDataLength() {
        return this.packetDataLength;
    }

    public long getTotalData(){
        return this.totalData;
    }

    public void SetDirectory(){
        this.isDirectory = true;
    }

    public boolean CheckDirectory(){
        return this.isDirectory;
    }
}
