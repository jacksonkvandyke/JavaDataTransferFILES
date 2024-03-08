import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class fileAssembler {
    String currentDirectory = null;
    File file = null;

    synchronized void SavePacket(Packet packet, File file){
        //Create file if it hasn't already been created

        if (!file.exists()){
            try{
                //Create file and write empty data
                file = new File(packet.getFilename());
                file.createNewFile();
                System.out.println("File successfully created!");

                FileOutputStream currentWriter = new FileOutputStream(file.getAbsolutePath());
                currentWriter.write(new byte[packet.getTotalPackets() * 1024]);
                currentWriter.flush();
                currentWriter.close();
            }catch (IOException e){
                System.out.print(e);
            }
        }
    }

}
