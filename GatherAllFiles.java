import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GatherAllFiles {
    //This class gathers all files and prepares them to be sent by converting them to packets
    File userPrompt = null;
    long progress = 0;
    long totalSize = 0;
    long sentBytes = 0;

    ExecutorService gatherFilesExecutor = Executors.newFixedThreadPool(1);

    GatherAllFiles(File userPrompt){
        //Stores userPrompt
        this.userPrompt = userPrompt;
        
        //Checks for directories or files
        if (userPrompt.isDirectory()){
            //Get total directory size
            GetDirectorySize calculator = new GetDirectorySize(this);
            this.totalSize = calculator.CalculateSize();
        }

        if (userPrompt.isFile()){
            //Get total file size
            this.totalSize = userPrompt.length();
        }
    }

    void StartTransfer(hostConnection hostConnection, connectoHostConnection toConnection){
        //Check if host or client
        OutputByteBuffer outBuffer = null;
        if (hostConnection != null){
            outBuffer = hostConnection.outBuffer;
        }else{
            outBuffer = toConnection.outBuffer;
        }

        //Start thread to get all files
        gatherFilesExecutor.execute(new ProcessFiles(userPrompt.getAbsolutePath(), userPrompt.getName(), this, outBuffer));
    }
}

class GetDirectorySize extends Thread{

    GatherAllFiles parent = null;
    long totalSize = 0;
    
    GetDirectorySize(GatherAllFiles parent){
        this.parent = parent;
    }

    long CalculateSize(){
        //Go through all files and directories to get total size
        File fileList[] = parent.userPrompt.listFiles();

        //Loop through all files and add the size
        for (int i = 0; i < fileList.length; i++){
            //Open directory if directory
            if (fileList[i].isDirectory()){
                OpenDirectory(fileList[i]);
                continue;
            }

            //Add file size if file
            if (fileList[i].isFile()){
                this.totalSize += fileList[i].length();
                continue;
            }

        }
        return totalSize;
    }

    void OpenDirectory(File file){
        //Go through all files and directories to get total size
        File fileList[] = file.listFiles();

        //Loop through all files and add the size
        for (int i = 0; i < fileList.length; i++){
            //Open directory if directory
            if (fileList[i].isDirectory()){
                OpenDirectory(fileList[i]);
                continue;
            }

            //Add file size if file
            if (fileList[i].isFile()){
                this.totalSize += fileList[i].length();
                continue;
            }

        }
    }

}

class ProcessFiles extends Thread{
    //This class opens the specified directory and checks for any files to send
    String userInput = "";
    String fileName = "";
    GatherAllFiles parent = null;
    OutputByteBuffer outBuffer = null;

    ProcessFiles(String userInput, String fileName, GatherAllFiles parent, OutputByteBuffer outBuffer) {
        this.userInput = userInput;
        this.fileName = fileName;
        this.parent = parent;
        this.outBuffer = outBuffer;
    }

    public void run() {
        File userFile = new File(this.userInput);
        ReadDirectory(userFile, fileName, parent, outBuffer);
    }

    void ReadDirectory(File userInput, String fileName, GatherAllFiles parent, OutputByteBuffer outBuffer2){
        //Get all files in directory then convert each to packets
        File fileList[] = userInput.listFiles();

        //Call packet creation on each file or open start operation on directory if directory
        if (fileList != null){
            for (int i = 0; i < fileList.length; i++){
                if (fileList[i].isDirectory()){
                    //Create new directory name
                    String newfileName = fileName + "/" + fileList[i].getName();
    
                    //Start process to get all files
                    ReadDirectory(fileList[i], newfileName, this.parent, this.outBuffer);
                    continue;
                }
                //Convert file to packets and update file size
                String newfileName = fileName + "/" + fileList[i].getName();
                ReadFile(fileList[i].getAbsolutePath(), newfileName, this.parent, this.outBuffer);
            }
        }else {
            ReadFile(userInput.getAbsolutePath(), fileName, this.parent, this.outBuffer);
        }
    }

    void ReadFile(String userPrompt, String filename, GatherAllFiles parent, OutputByteBuffer outBuffer) {
         //Convert single file and set attributes
         FileToPackets convertedFile = new FileToPackets(userPrompt, filename);
         System.out.printf("Getting file: %s", userPrompt);

         //Add files to outputStream until depleted
         while (convertedFile.processingFile == true){
             //Check if data can be added to stream
            try{
                System.out.print(convertedFile.packets.size());
                Packet retrievedPacket = convertedFile.packets.take();
                System.out.print("Took Packet");

                if (retrievedPacket != null){
                    this.outBuffer.packets.put(retrievedPacket); 
                    retrievedPacket = null;
                }
            }catch (InterruptedException e){
                System.out.print(e);
            }
        }
        this.parent.sentBytes += convertedFile.fileSize;
        System.out.print(this.parent.sentBytes);
    }
}
