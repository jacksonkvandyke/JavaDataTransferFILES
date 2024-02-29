import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GatherAllFiles {
    //This class gathers all files and prepares them to be sent by converting them to packets
    File userPrompt = null;
    int progress = 0;
    int totalSize = 0;
    List<File> directories = new ArrayList<File>();
    List<FileToPackets> convertedFiles = new ArrayList<FileToPackets>();

    GatherAllFiles(File userPrompt){
        //Stores userPrompt
        this.userPrompt = userPrompt;
        
        //Checks for directories or files
        if (userPrompt.isDirectory()){
            //Start thread to get all files
            OpenDirectory threadObject = new OpenDirectory(userPrompt.getAbsolutePath(), this);
            Thread thread = new Thread(threadObject);
            thread.start();
        }

        if (userPrompt.isFile()){
            //Start thread for single file
            OpenFile threadObject = new OpenFile(userPrompt.getAbsolutePath(), this);
            Thread thread = new Thread(threadObject);
            thread.start();

        }
    }

    public int getProgress() {
        //Returns progress on conversion of files to packets
        return progress;
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
            OpenFile threadObject = new OpenFile(fileList[0].getAbsolutePath(), this.parent);
            Thread thread = new Thread(threadObject);
            thread.start();

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
        FileToPackets convertedFile = new FileToPackets(this.path, this.parent);

        //Set converted files
        parent.convertedFiles.add(convertedFile);

    }


}
