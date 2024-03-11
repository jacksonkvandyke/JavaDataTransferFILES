import java.io.Serializable;

public class Packet implements Serializable {

    private String fileName = "";
    private int packetSequence = 0;
    private byte packetData[];
    private boolean isDirectory = false;
    private boolean lastPacket = false;

    public Packet(String fileName, int sequence, byte data[], long totalData){
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

    public void SetDirectory(){
        this.isDirectory = true;
    }

    public boolean CheckDirectory(){
        return this.isDirectory;
    }

    public void SetLast(){
        this.lastPacket = true;
    }

    public boolean CheckLast(){
        return this.lastPacket;
    }
}
