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
                file.createNewFile();
                System.out.println("File successfully created!");
            }catch (IOException e){
                System.out.print(e);
            }
        }

        try{
            //Create file writer and write components to file
            FileOutputStream currentWriter = new FileOutputStream(file.getAbsolutePath());
            currentWriter.write(packet.getData(), packet.getSequence() * 1024, packet.getData().length);
            currentWriter.flush();

            //Close file writer
            currentWriter.close();
        }catch(IOException e){
            System.out.print(e);
        }
    }

}
