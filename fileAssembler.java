import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class fileAssembler {
    String currentDirectory = null;
    boolean threadStarted = false;

    List<Packet> packets = Collections.synchronizedList(new ArrayList<Packet>(50));

    synchronized void AddPacket(Packet packet){
        if (packets.size() < 50){
            packets.add(packet);

            //Start save packet thread if not started
            if (!threadStarted){
                SavePackets savepackets = new SavePackets(this);
                Thread thread = new Thread(savepackets);
                thread.start();
                threadStarted = true;
            }
        }
    }

    Packet GetPacket(){
        return this.packets.remove(0);
    }
}

class SavePackets extends Thread{

    fileAssembler assembler = null;

    SavePackets(fileAssembler assembler){
        this.assembler = assembler;
    }

    public void run(){
        while (true){
            if (assembler.packets.size() > 0){
                //Get current packet
                Packet packet = assembler.GetPacket();

                //Create file if it hasn't already been created
                File file = new File(packet.getFilename());

                if (!file.exists()){
                    try{
                        //Create file and write empty data
                        file.createNewFile();
                        System.out.println("File successfully created!");
                    }catch (IOException e){
                        System.out.print(e);
                    }
                }

                //Write data to file
                try{
                    RandomAccessFile currentWriter = new RandomAccessFile(file.getAbsolutePath(), "rw");
                    currentWriter.seek(packet.getSequence() * 1024);
                    currentWriter.write(packet.getData());
                    currentWriter.close();
                }catch (IOException e){
                    System.out.print(e);
                }
            }
        }
    }
}
