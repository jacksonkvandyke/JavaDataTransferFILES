import java.io.File;

public class GatherAllFiles {
    //This class gathers all files and prepares them to be sent by converting them to packets
    File userPrompt = null;
    long progress = 0;
    long totalSize = 0;
    int requiredFiles = 0;
    int currentFiles = 0;

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
        if (hostConnection != null){
            //Checks for directories or files and then send over socket as packets
            if (userPrompt.isDirectory()){
                //Start thread to get all files
                HostOpenDirectory threadObject = new HostOpenDirectory(hostConnection, userPrompt.getAbsolutePath(), this);
                Thread thread = new Thread(threadObject);
                thread.start();
            }

            if (userPrompt.isFile()){
                //Start thread for single file
                HostOpenFile threadObject = new HostOpenFile(hostConnection, userPrompt.getAbsolutePath(), this);
                Thread thread = new Thread(threadObject);
                thread.start();
            }
        }else{
            //Checks for directories or files and then send over socket as packets
            if (userPrompt.isDirectory()){
                //Start thread to get all files
                OpenDirectory threadObject = new OpenDirectory(toConnection, userPrompt.getAbsolutePath(), this);
                Thread thread = new Thread(threadObject);
                thread.start();
            }

            if (userPrompt.isFile()){
                //Start thread for single file
                OpenFile threadObject = new OpenFile(toConnection, userPrompt.getAbsolutePath(), this);
                Thread thread = new Thread(threadObject);
                thread.start();
        }

        //Start file wait on thread
        WaitCompletion waitObject = new WaitCompletion(this);
        Thread waitThread = new Thread(waitObject);
        waitThread.start();
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
                this.parent.requiredFiles += 1;
                continue;
            }

        }
    }

}

class HostOpenDirectory extends Thread{
    //This class opens the specified directory and checks for any files to send
    hostConnection connection = null;
    String directory = "";
    GatherAllFiles parent = null;

    HostOpenDirectory(hostConnection connection, String userPrompt, GatherAllFiles parent) {
        this.connection = connection;
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
                HostOpenDirectory threadObject = new HostOpenDirectory(this.connection, fileList[i].getAbsolutePath(), this.parent);
                Thread thread = new Thread(threadObject);
                thread.start();
                continue;
            }

            //Convert file to packets and update file size
            HostOpenFile threadObject = new HostOpenFile(this.connection, fileList[i].getAbsolutePath(), this.parent);
            Thread thread = new Thread(threadObject);
            thread.start();

        }
    }
}

class HostOpenFile extends Thread{

    hostConnection connection = null;
    String path = "";
    GatherAllFiles parent = null;

    HostOpenFile(hostConnection connection, String userPrompt, GatherAllFiles parent) {
        this.connection = connection;
        this.path = userPrompt;
        this.parent = parent;
    }

    public void run() {
        //Convert single file and set attributes
        FileToPackets convertedFile = new FileToPackets(this.path);

        //Add files to outputStream until depleted
        while ((convertedFile.packetIterator != convertedFile.maxPackets) || (convertedFile.currentPackets == 0)){
            //Check if data can be added to stream
            Packet retrievedPacket = convertedFile.GetPacket();
            hostOutputThread transferThreads[] = connection.getOutThreads();
            System.out.print(transferThreads);
            
            //Continue waiting until packet is added to thread
            while (retrievedPacket != null){
                if (retrievedPacket != null){
                    for (int i = 0; i < transferThreads.length; i++){
                        if (transferThreads[i].currentPacket == null){
                            transferThreads[i].setPacket(retrievedPacket);
                        }
                    }
                }
            }

            //Short sleep to allow computations
            try{
                Thread.sleep(10);
            }catch(InterruptedException e){
                System.out.println(e);
            }
        }
        parent.currentFiles += 1;
    }
}


class OpenDirectory extends Thread{
    //This class opens the specified directory and checks for any files to send
    connectoHostConnection connection = null;
    String directory = "";
    GatherAllFiles parent = null;

    OpenDirectory(connectoHostConnection connection, String userPrompt, GatherAllFiles parent) {
        this.connection = connection;
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
                OpenDirectory threadObject = new OpenDirectory(this.connection, fileList[i].getAbsolutePath(), this.parent);
                Thread thread = new Thread(threadObject);
                thread.start();
                continue;
            }

            //Convert file to packets and update file size
            OpenFile threadObject = new OpenFile(this.connection, fileList[i].getAbsolutePath(), this.parent);
            Thread thread = new Thread(threadObject);
            thread.start();

        }

    }
}

class OpenFile extends Thread{

    connectoHostConnection connection = null;
    String path = "";
    GatherAllFiles parent = null;

    OpenFile(connectoHostConnection connection, String userPrompt, GatherAllFiles parent) {
        this.connection = connection;
        this.path = userPrompt;
        this.parent = parent;
    }

    public void run() {
        //Convert single file and set attributes
        FileToPackets convertedFile = new FileToPackets(this.path);

        //Add files to outputStream until depleted
        while ((convertedFile.packetIterator != convertedFile.maxPackets) || (convertedFile.currentPackets == 0)){
            //Check if data can be added to stream
            Packet retrievedPacket = convertedFile.GetPacket();
            outputThread transferThreads[] = connection.getOutThreads();
            System.out.print(transferThreads);
            
            //Continue waiting until packet is added to thread
            while (retrievedPacket != null){
                if (retrievedPacket != null){
                    for (int i = 0; i < transferThreads.length; i++){
                        if (transferThreads[i].getPacket() == null){
                            transferThreads[i].setPacket(retrievedPacket);
                        }
                    }
                }
            }

            //Short sleep to allow computations
            try{
                Thread.sleep(10);
            }catch(InterruptedException e){
                System.out.println(e);
            }
        }
        parent.currentFiles += 1;
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
