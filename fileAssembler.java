import java.io.File;
import java.io.FileOutputStream;
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

    List<Packet> GetPackets(){
        return this.packets;
    }
}

class SavePackets extends Thread{

    fileAssembler assembler = null;

    SavePackets(fileAssembler assembler){
        this.assembler = assembler;
    }

    public void run(){
        while (true){
            List<Packet> packets = assembler.GetPackets();

            if (packets.size() > 0){
                //Get current packet
                Packet packet = packets.remove(0);

                //Create file if it hasn't already been created
                File file = new File(packet.getFilename());

                if (!file.exists()){
                    try{
                        //Create file and write empty data
                        file.createNewFile();
                        System.out.println("File successfully created!");

                        FileOutputStream currentWriter = new FileOutputStream(file.getAbsolutePath());
                        currentWriter.write(new byte[(int) (packet.getTotalPackets() * 1024)]);
                        currentWriter.flush();
                        currentWriter.close();
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
