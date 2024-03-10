import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GatherAllFiles {
    //This class gathers all files and prepares them to be sent by converting them to packets
    File userPrompt = null;
    long progress = 0;
    long totalSize = 0;
    long sentBytes = 0;

    ExecutorService gatherFilesExecutor = Executors.newFixedThreadPool(2);

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

        //Checks for directories or files and then send over socket as packets
        if (userPrompt.isDirectory()){
            //Start thread to get all files
            OpenDirectory threadObject = new OpenDirectory(userPrompt.getAbsolutePath(), userPrompt.getName(), this, outBuffer);
            Thread thread = new Thread(threadObject);
            gatherFilesExecutor.execute(thread);
        }

        if (userPrompt.isFile()){
            //Start thread for single file
            OpenFile threadObject = new OpenFile(userPrompt.getAbsolutePath(), userPrompt.getName(), this, outBuffer);
            Thread thread = new Thread(threadObject);
            gatherFilesExecutor.execute(thread);
        }
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

class OpenDirectory extends Thread{
    //This class opens the specified directory and checks for any files to send
    String directory = "";
    String directoryname = "";
    GatherAllFiles parent = null;
    OutputByteBuffer outBuffer = null;

    OpenDirectory(String userPrompt, String directoryname, GatherAllFiles parent, OutputByteBuffer outBuffer) {
        this.directory = userPrompt;
        this.directoryname = directoryname;
        this.parent = parent;
        this.outBuffer = outBuffer;
    }

    public void run() {
        //Get all files in directory then convert each to packets
        File directoryFile = new File(this.directory);
        File fileList[] = directoryFile.listFiles();

        //Call packet creation on each file or open start operation on directory if directory
        for (int i = 0; i < fileList.length; i++){
            String newDirectoryName = "";
            String newFileName = "";

            if (fileList[i].isDirectory()){
                //Create new directory name
                newDirectoryName = this.directoryname + "/" + fileList[i].getName();

                //Start process to get all files
                OpenDirectory threadObject = new OpenDirectory(fileList[i].getAbsolutePath(), newDirectoryName, this.parent, outBuffer);
                Thread thread = new Thread(threadObject);
                this.parent.gatherFilesExecutor.execute(thread);
                continue;
            }

            //Convert file to packets and update file size
            newFileName = this.directoryname + "/" + fileList[i].getName();
            OpenFile threadObject = new OpenFile(fileList[i].getAbsolutePath(), newFileName, this.parent, this.outBuffer);
            Thread thread = new Thread(threadObject);
            this.parent.gatherFilesExecutor.execute(thread);
        }
    }
}

class OpenFile extends Thread{
    String path = "";
    String filename = "";
    GatherAllFiles parent = null;
    OutputByteBuffer outBuffer = null;

    OpenFile(String userPrompt, String filename, GatherAllFiles parent, OutputByteBuffer outBuffer) {
        this.path = userPrompt;
        this.filename = filename;
        this.parent = parent;
        this.outBuffer = outBuffer;
    }

    public void run() {
        //Convert single file and set attributes
        FileToPackets convertedFile = new FileToPackets(this.path, this.filename);
        this.parent.sentBytes += convertedFile.fileSize;

        //Add files to outputStream until depleted
        while (convertedFile.processingFile == true){
            //Check if data can be added to stream
            Packet retrievedPacket = convertedFile.GetPacket();
            
            //Continue waiting until packet is added to thread
            try{
                if (retrievedPacket != null){
                    this.outBuffer.packets.put(retrievedPacket); 
                    retrievedPacket = null;
                }
            }catch (InterruptedException e){
                System.out.print(e);
            }
        }
    }
}
