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
    int maxCores = 0;
    FileToPackets assembledPackets = null;

    //Command strings
    String command = "";
    String inCommand = "";

    connectoHostConnection(String address, int port){
        //Create socket and link streams
        socket = new Socket();
        System.out.println("Socket successfully created.");

        //Connect to ensure connection
        try{
            socket.connect(new InetSocketAddress(address, port));
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

    void awaitFiles(){
        //Create the await file thread to wait for commands
        awaitFileThread awaitObject = new awaitFileThread(this.socket.getLocalPort(), this);
        Thread thread = new Thread(awaitObject);
        thread.start();

    }

    void connectThreads(){
        //Create the threads and await for connection
        ExecutorService threads = Executors.newFixedThreadPool(this.maxCores);
        for (int i = 0; i < this.maxCores; i++){
            Runnable thread = new inputThread(this.socket.getLocalPort() + i + 1);
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
            //Sleep for one second before continuing connection to ensure the host has finished setting up connections
            try{
                Thread.sleep(1000);
            }catch (InterruptedException e){
                System.out.println(e);
            }

            socket.close();
            connection.awaitFiles();
        }catch(IOException i){
            System.out.println(i);
        }
        
        //Set max cores
        connection.setCores(Math.max(cores, otherCores));

    }
    
}

class awaitFileThread extends Thread{

    private int port = 0;
    private ServerSocket serverSocket = null;
    private Socket socket = null;
    connectoHostConnection connection = null;
    DataInputStream inputStream = null;
    DataOutputStream outputStream = null;
    
    public awaitFileThread(int port, connectoHostConnection connection){
        this.port = port;
        this.connection = connection;
    }

    public void run(){
        //Create thread socket and await connection
        try{
            serverSocket = new ServerSocket(this.port);
        }catch(IOException i){
            System.out.println(i);
        }

        //Wait for connection then accept
        try{
            socket = serverSocket.accept();
            System.out.printf("Socket connected on port: %d\n", this.port);

            //Assign input and output streams
            inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            outputStream =  new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        }catch(IOException i){
            System.out.println(i);
        }

        //Continously read from input stream and write commands to output stream
        while (true){
            //Read data from input stream
            try{
                connection.inCommand = inputStream.readAllBytes().toString();
                System.out.println(connection.inCommand);
            }catch (IOException e){
                System.out.println(e);

            }

            //Send commands through output stream
            try{
                outputStream.write(Byte.parseByte(connection.command));;
            }catch (IOException e){
                System.out.println(e);

            }

            //Reset incoming and outgoing streams
            connection.inCommand = "";
            connection.command = "";

            //Sleep for short moment to reduce CPU use
            try{
                Thread.sleep(1000);
            }catch (InterruptedException e){
                System.out.println(e);
            }       

    }
    
}

class inputThread extends Thread{

    private int port = 0;
    private ServerSocket serverSocket = null;
    private Socket socket = null;
    DataInputStream inputStream = null;
    
    public inputThread(int port){
        this.port = port;
    }

    public void run(){
        //Create thread socket and await connection
        try{
            serverSocket = new ServerSocket(this.port);
        }catch(IOException i){
            System.out.println(i);
        }

        //Wait for connection then accept
        try{
            socket = serverSocket.accept();
            System.out.printf("Socket connected on port: %d\n", this.port);

            //Assign input and output streams
            inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        }catch(IOException i){
            System.out.println(i);
        }

    }
    
}

class outputThread extends Thread{

    private int port = 0;
    private ServerSocket serverSocket = null;
    private Socket socket = null;
    DataOutputStream outputStream = null;
    
    public outputThread(int port){
        this.port = port;
    }

    public void run(){
        //Create thread socket and await connection
        try{
            serverSocket = new ServerSocket(this.port);
        }catch(IOException i){
            System.out.println(i);
        }

        //Wait for connection then accept
        try{
            socket = serverSocket.accept();
            System.out.printf("Socket connected on port: %d\n", this.port);

            //Assign input and output streams
            outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        }catch(IOException i){
            System.out.println(i);
        }

    }
    
}
