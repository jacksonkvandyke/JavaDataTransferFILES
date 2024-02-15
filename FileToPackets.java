import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileToPackets{

    Packet packets[] = null;
    File currentFile = null;
    InputStream fileInput = null;
    long maxPackets = 0;
    long fileSize = 0;

    public FileToPackets(String location, int maxCores){
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
        int sequenceNumber = 0;
        maxPackets = (long) Math.max(1, Math.ceil(fileSize / 1024));
        packets = new Packet[(int) maxPackets];

        //Create packet assembler threads
        ExecutorService threads = Executors.newFixedThreadPool(maxCores);
        while (packets.length < maxPackets){
            Runnable thread = new ReadPacketThread(fileInput, sequenceNumber, packets);
            threads.execute(thread);

        }

        //Print bytes
        System.out.println(packets.length);

    }

}

class Packet {

    private int packetSequence = 0;
    private byte packetData[];

    public Packet(int sequence, byte data[]){
        this.packetSequence = sequence;
        this.packetData = data;

    }

    public int getSequence() {
        return this.packetSequence;

    }

    public byte[] getData() {
        return this.packetData;

    }
    
}

class ReadPacketThread extends Thread{

    InputStream fileInput = null;
    int sequenceNumber = 0;
    Packet packets[] = null;

    public ReadPacketThread(InputStream fileInput, int sequenceNumber, Packet packets[]){
        this.fileInput = fileInput;
        this.sequenceNumber = sequenceNumber;
        this.packets = packets;

    }

    public void run(){
        //Read from file and add to buffer
        byte packetBuffer[] = new byte[1024];

        //Read data from file and put it into packetBuffer
            try{
                fileInput.read(packetBuffer);
            }catch (IOException i){
                System.out.print(i);

            }

            //Create packet and increment sequence
            if (packetBuffer.length > 0){
                Packet packet = new Packet(sequenceNumber, packetBuffer);
                packetBuffer = new byte[1024];
                packets[sequenceNumber] = packet;

            }

    }
}