import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileToPackets{

    Packet packets[] = null;
    File currentFile = null;
    String filename = "";
    InputStream fileInput = null;
    long maxPackets = 0;
    long fileSize = 0;
    boolean processingFile = true;

    int currentPackets = 0;
    int packetIterator = -1;

    public FileToPackets(String location, String filename){
        //Assign filename
        this.filename = filename;

        //Open file and create scanner
        try{
            currentFile = new File(location);
            fileInput = new FileInputStream(currentFile);

            //Get file size
            try{
                Path filePath = Paths.get(location);
                fileSize = Files.size(filePath);
            }catch(IOException i){
                System.out.println(i);
            }

        }catch(FileNotFoundException f){
            System.out.println(f);

        }

        //Create packet from bytes and add to packets list
        this.maxPackets = (long) Math.max(1, Math.ceil((double) fileSize / (double) 1024));
        this.packets = new Packet[(int) maxPackets];

        //Create packets
        ReadPacketThread threadObject = new ReadPacketThread(filename, fileInput, packets, this);
        Thread thread = new Thread(threadObject);
        thread.start();

    }

    public Packet GetPacket(){
        //Check if packet is available
        if (this.packetIterator < this.currentPackets - 1){
            //Check if iterator needs to be incremented
            this.packetIterator += 1;
            return this.packets[packetIterator];
        }
        return null;
    }

}

class Packet implements Serializable {

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

class ReadPacketThread extends Thread{

    String fileName = "";
    InputStream fileInput = null;
    int sequenceNumber = 0;
    Packet packets[] = null;

    FileToPackets parent;

    public ReadPacketThread(String fileName, InputStream fileInput, Packet packets[], FileToPackets parent){
        this.fileName = fileName;
        this.fileInput = fileInput;
        this.packets = packets;
        this.parent = parent;

    }

    public void run(){
        while (true){
            //Read from file and add to buffer
            byte packetBuffer[] = new byte[1024];
            int currentRead = 0;

            //Read data from file and put it into packetBuffer
            try{
                currentRead = fileInput.read(packetBuffer);
            }catch (IOException i){
                System.out.print(i);

            }

            //Check if read elements was less than packet buffers size
            if ((currentRead < 1024) && (currentRead > -1)){
                byte newBuffer[] = new byte[currentRead];

                //Add all elements to new buffer
                for (int i = 0; i < currentRead; i++){
                    newBuffer[i] = packetBuffer[i];
                }
                packetBuffer = newBuffer;
            }

            //Check if any data was returned
            if (currentRead == -1){
                this.parent.processingFile = false;
                break;
            }

            //Create packet, increment sequence, and increment fileSize to UI
            if ((packetBuffer.length > 0) && (parent.currentPackets < parent.maxPackets)){
                Packet packet = new Packet(fileName, parent.maxPackets, sequenceNumber, packetBuffer);
                packets[sequenceNumber] = packet;
                sequenceNumber += 1;
                parent.currentPackets += 1;

            }
        }
    }
}