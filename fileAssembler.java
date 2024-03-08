import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class fileAssembler {
    String currentDirectory = null;

    void SavePacket(Packet packet){
        //Create file if it hasn't already been created
        File file = new File(packet.getFilename());

        if (!file.exists()){
            try{
                //Create file and write empty data
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
