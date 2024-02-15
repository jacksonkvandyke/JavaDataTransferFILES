import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class hostConnection{
    //Initialize variables
    ServerSocket socket = null;
    DataOutputStream outputStream = null;
    int maxCores = 0;
    FileToPackets assembledPackets = null;

    public hostConnection(){
        //Set up server socket and bind to address and port
        try{
            socket = new ServerSocket();
            socket.bind(new InetSocketAddress("127.0.0.1", 50000));
            System.out.printf("Socket created on port: %d", socket.getLocalPort());
        }catch(IOException i){
            System.out.println(i);
            return;
        }

        //Create awaitThread
        awaitThread awaitObject = new awaitThread(this);
        Thread thread = new Thread(awaitObject);
        thread.start();

    }

    void setCores(int cores){
        this.maxCores = cores;

    }

    void createPackets(String location){
        System.out.println(maxCores);
        assembledPackets = new FileToPackets(location, this.maxCores);

    }

    void connectThreads(){
        //Create the threads and await for connection
        ExecutorService threads = Executors.newFixedThreadPool(this.maxCores);
        for (int i = 0; i < this.maxCores; i++){
            Runnable thread = new outgoingDataThread(this.socket.getLocalPort() + i + 1);
            threads.execute(thread);

        }

    }

}

class awaitThread extends Thread{

    hostConnection connection;
    ServerSocket serverSocket = null;
    Socket socket = null;
    DataInputStream inputStream = null;
    DataOutputStream outputStream = null;

    public awaitThread(hostConnection connection){
        this.connection = connection;
        this.serverSocket = connection.socket;

    }

    public void run(){
        //Await for connection
        try{
            socket = serverSocket.accept();

            //Assign input and output streams
            inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            outputStream = new  DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        }catch (IOException i){
            System.out.println(i);
            return;
        }

        //Get max threads after successful connection
        int cores = Runtime.getRuntime().availableProcessors();
        int otherCores = 0;

        //Send cores and get connected cores
        try{
            outputStream.writeInt(cores);
            outputStream.flush();
        }catch(IOException i){
            System.out.println(i);
        }
        try{
            otherCores = inputStream.readInt();
        }catch(IOException i){
            System.out.println(i);
        }

        //Set max cores
        int maxCores = Math.max(cores, otherCores);
        System.out.println(maxCores);
        this.connection.setCores(maxCores);

    }
}

class outgoingDataThread extends Thread{

    private int port = 0;
    private ServerSocket socket = null;
    
    public outgoingDataThread(int port){
        this.port = port;
        
    }

    public void run(){
        //Create thread socket and await connection
        try{
            socket = new ServerSocket(this.port);
        }catch(IOException i){
            System.out.println(i);
        }

        //Wait for connection then accept
        try{
            socket.accept();
            System.out.printf("Socket connected on port: %d\n", this.port);
        }catch(IOException i){
            System.out.println(i);
        }

    }
    
}

