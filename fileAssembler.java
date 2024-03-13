import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class fileAssembler {
    String currentDirectory = null;
    BlockingQueue<Packet> packets = new ArrayBlockingQueue<Packet>(10);
    String downloadLocation;

    //Start file assembler on object creation
    fileAssembler(){
        SavePackets savepackets = new SavePackets(this);
        Thread thread = new Thread(savepackets);
        thread.start();
    }
}

class SavePackets extends Thread{

    fileAssembler assembler = null;

    SavePackets(fileAssembler assembler){
        this.assembler = assembler;
    }

    public void run(){
        while (true){
            //Get current packet
            try{
                Packet packet = this.assembler.packets.take();

                //Return if null packet
                if (packet == null){
                    continue;
                }

                //Check if file is directory and create directory
                if (packet.CheckDirectory()){
                    Path directoryPath = Paths.get(assembler.downloadLocation + packet.getFilename());
                    Files.createDirectory(directoryPath);

                }

                //Create file if it hasn't already been created
                File file = new File(assembler.downloadLocation + packet.getFilename());

                if (!file.exists()){
                    try{
                        //Create file and write empty data
                        if (file.getParentFile() != null){
                            file.getParentFile().mkdirs();
                        }
                        file.createNewFile();
                    }catch (IOException e){
                        System.out.print(e);
                    }
                }

                //Write data to file
                RandomAccessFile currentWriter = new RandomAccessFile(file.getAbsolutePath(), "rw");
                currentWriter.seek(packet.getSequence() * 1024);
                currentWriter.write(packet.getData());
                currentWriter.close();
                
            }catch (InterruptedException | IOException e){
                System.out.print(e);
            }
        }
    }
}
