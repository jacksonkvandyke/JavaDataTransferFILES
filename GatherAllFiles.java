import java.io.File;

public class GatherAllFiles {
    //This class gathers all files and prepares them to be sent
    File userPrompt = null;
    int progress = 0;

    GatherAllFiles(File userPrompt){
        //Stores userPrompt
        this.userPrompt = userPrompt;

        if (userPrompt.isDirectory()){
            System.out.println("Is Directory");
        }
        if (userPrompt.isFile()){
            System.out.println("Is File");
        }
    }

    public int getProgress() {
        //Returns progress on conversion of files to packets
        return progress;
    }

}
