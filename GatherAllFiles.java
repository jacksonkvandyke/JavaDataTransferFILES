import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GatherAllFiles {
    //This class gathers all files and prepares them to be sent by converting them to packets
    File userPrompt = null;
    long progress = 0;
    long totalSize = 0;
    int requiredFiles = 0;
    int currentFiles = 0;
    List<File> directories = new ArrayList<File>();
    List<FileToPackets> convertedFiles = new ArrayList<FileToPackets>();

    GatherAllFiles(File userPrompt){
        //Stores userPrompt
        this.userPrompt = userPrompt;
        
        //Checks for directories or files
        if (userPrompt.isDirectory()){
            //Get total directory size
            GetDirectorySize calculator = new GetDirectorySize(this);
            this.totalSize = calculator.CalculateSize();
            System.out.print(this.totalSize);

            //Start thread to get all files
            OpenDirectory threadObject = new OpenDirectory(userPrompt.getAbsolutePath(), this);
            Thread thread = new Thread(threadObject);
            thread.start();
        }

        if (userPrompt.isFile()){
            //Get total file size
            this.totalSize = userPrompt.length();
            System.out.print(this.totalSize);

            //Start thread for single file
            OpenFile threadObject = new OpenFile(userPrompt.getAbsolutePath(), this);
            Thread thread = new Thread(threadObject);
            thread.start();

        }

        //Start file wait on thread
        WaitCompletion waitObject = new WaitCompletion(this);
        Thread waitThread = new Thread(waitObject);
        waitThread.start();
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
    GatherAllFiles parent = null;

    OpenDirectory(String userPrompt, GatherAllFiles parent) {
        this.directory = userPrompt;
        this.parent = parent;
    }

    public void run() {
        //Get all files in directory then convert each to packets
        File directoryFile = new File(this.directory);
        File fileList[] = directoryFile.listFiles();

        //Call packet creation on each file or open start operation on directory if directory
        for (int i = 0; i < fileList.length; i++){
            if (fileList[i].isDirectory()){
                //Start thread to get all files
                OpenDirectory threadObject = new OpenDirectory(fileList[i].getAbsolutePath(), this.parent);
                Thread thread = new Thread(threadObject);
                thread.start();
                continue;
            }

            //Convert file to packets and update file size
            OpenFile threadObject = new OpenFile(fileList[i].getAbsolutePath(), this.parent);
            Thread thread = new Thread(threadObject);
            thread.start();
            this.parent.requiredFiles += 1;

        }

        //Set files after completion
        this.parent.directories.add(directoryFile);
    }
}

class OpenFile extends Thread{

    String path = "";
    GatherAllFiles parent = null;

    OpenFile(String userPrompt, GatherAllFiles parent) {
        this.path = userPrompt;
        this.parent = parent;
    }

    public void run() {
        //Convert single file and set attributes
        FileToPackets convertedFile = new FileToPackets(this.path);

        //Set converted files
        parent.convertedFiles.add(convertedFile);

        //Wait for file to finish processing
        while (true){
            if (convertedFile.packets.length == convertedFile.maxPackets){
                this.parent.currentFiles += 1;
                return;
            }

            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){
                System.out.println(e);
            }

        }

    }

}

class WaitCompletion extends Thread{

    GatherAllFiles parent = null;

    WaitCompletion(GatherAllFiles parent){
        this.parent = parent;
    }

    public void run(){
        //Wait for files to finish processing
        while (true){
            if (parent.requiredFiles == parent.currentFiles && parent.requiredFiles != 0){
                parent.progress = 100;
                return;
            }

            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){
                System.out.println(e);
            }

        }
    }

}
