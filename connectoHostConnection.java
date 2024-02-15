import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class connectoHostConnection {
    //Initialize variables
    connectoHostConnection connection = this;
    Socket socket = null;
    DataInputStream inputStream = null;
    DataOutputStream outputStream = null;
    int maxCores = 0;
    FileToPackets assembledPackets = null;

    connectoHostConnection(String address, int port){
        //Create socket and link streams
        socket = new Socket();
        System.out.println("Socket successfully created.");

        //Connect to ensure connection
        try{
            socket.connect(new InetSocketAddress(address, port));
            inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            System.out.println(socket.getPort());

            //Call thread to create Data Threads
            incomingConnectThread object = new incomingConnectThread(connection, socket);
            Thread thread = new Thread(object);
            thread.start();

        
        }catch(IOException i) {
            System.out.println(i);
            return;
        }

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
            Runnable thread = new dataThread(this.socket.getLocalPort() + i + 1);
            threads.execute(thread);

        }

    }

}

class incomingConnectThread extends Thread{

    connectoHostConnection connection = null;
    Socket socket = null;
    DataInputStream inputStream = null;
    DataOutputStream outputStream = null;

    public incomingConnectThread(connectoHostConnection connection, Socket socket){
        try{
            this.connection = connection;
            this.socket = socket;
            this.inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            this.outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            
        }catch(IOException i){
            System.out.println(i);

        }

    }
    
    public void run(){
        //Create dataThreads after successful connection
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
        connection.setCores(Math.max(cores, otherCores));

    }
    
}

class dataThread extends Thread{

    private int port = 0;
    private Socket socket = null;

    public dataThread(int port) {
        this.port = port;
        
    }

    public void run(){
        //Connect to thread sockets
        socket = new Socket();
        try{
            socket.connect(new InetSocketAddress("127.0.0.1", port));
            System.out.printf("Socket connected on port: %d\n", port);
        }catch(IOException i){
            System.out.println(i);

        }

    }

}
