import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class fileAssembler {
    String currentDirectory = null;
    List<Packet> packets = Collections.synchronizedList(new ArrayList<Packet>(50));

    //Start file assembler on object creation
    fileAssembler(){
        SavePackets savepackets = new SavePackets(this);
        Thread thread = new Thread(savepackets);
        thread.start();
    }

    synchronized void AddPacket(Packet packet){
        if (packets.size() < 50){
            packets.add(packet);
        }
    }

    Packet GetPacket(){
        if (this.packets.size() > 0){
            return this.packets.remove(0);
        }
        return null;
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

                //Return if null packet
                if (packet == null){
                    continue;
                }

                //Create file if it hasn't already been created
                File file = new File(packet.getFilename());

                if (!file.exists()){
                    try{
                        //Create file and write empty data
                        file.getParentFile().mkdirs();
                        file.createNewFile();
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
