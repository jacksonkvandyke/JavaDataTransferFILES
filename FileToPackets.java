import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileToPackets{

    Packet packets[] = null;
    File currentFile = null;
    File parentDirectory = null;
    InputStream fileInput = null;
    long maxPackets = 0;
    long fileSize = 0;

    int packetIterator = 0;
    int currentPackets = 0;

    public FileToPackets(String location){
        //Open file and create scanner
        try{
            currentFile = new File(location);
            parentDirectory = currentFile.getParentFile();
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
        this.maxPackets = (long) Math.max(1, Math.ceil(fileSize / 1024)) + 1;
        this.packets = new Packet[(int) maxPackets];

        //Create packets
        ReadPacketThread threadObject = new ReadPacketThread(currentFile.getName(), fileInput, packets, this);
        Thread thread = new Thread(threadObject);
        thread.start();

    }

    public Packet[] GetPackets(){
        return this.packets;
    }

}

class Packet implements Serializable {

    private String fileName = "";
    private int totalPackets = 0;
    private int packetSequence = 0;
    private byte packetData[];

    public Packet(String fileName, int sequence, byte data[]){
        this.fileName = fileName;
        this.totalPackets = data.length;
        this.packetSequence = sequence;
        this.packetData = data;

    }

    public String getFilename() {
        return this.fileName;
    }

    public int getTotalPackets() {
        return this.totalPackets;
    }

    public int getSequence() {
        return this.packetSequence;

    }

    public byte[] getData() {
        return this.packetData;

    }

    private void readObject(ObjectInputStream res) 
            throws IOException, 
                   ClassNotFoundException 
        { 
            res.defaultReadObject(); 
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

            //Read data from file and put it into packetBuffer
            try{
                fileInput.read(packetBuffer);
            }catch (IOException i){
                System.out.print(i);

            }

            //Check if end of file
            if (parent.currentPackets == parent.maxPackets){
                return;
            }

            //Create packet, increment sequence, and increment fileSize to UI
            if (packetBuffer.length > 0){
                Packet packet = new Packet(fileName, sequenceNumber, packetBuffer);
                packets[sequenceNumber] = packet;
                sequenceNumber += 1;
                parent.currentPackets += 1;

            }

            //Sleep reduces errors
            try{
                Thread.sleep(10);
            }catch(InterruptedException e){
                System.out.println(e);
            }
        }
    }
}