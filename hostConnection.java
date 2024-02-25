import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class hostConnection{
    //Initialize variables
    renderer renderer = null;
    ServerSocket socket = null;
    String address = "127.0.0.1";
    int maxCores = 0;
    FileToPackets assembledPackets = null;

    //Command packet


    public hostConnection(renderer renderer){
        //Set up server socket and bind to address and port
        try{
            socket = new ServerSocket();
            socket.bind(new InetSocketAddress(address, 50000));
            System.out.printf("Socket created on port: %d\n", socket.getLocalPort());
        }catch(IOException i){
            System.out.println(i);
            return;
        }

        //Create awaitThread
        hostawaitThread awaitObject = new hostawaitThread(this, renderer);
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
        for (int i = 0; i < this.maxCores / 2; i += 2){
            //Output thread
            Runnable outThread = new hostoutputThread(this.socket.getLocalPort() + i + 1);
            threads.execute(outThread);

            //Input thread
            Runnable inThread = new hostinputThread(this.socket.getLocalPort() + i + 2);
            threads.execute(inThread);

        }

    }

}

class hostawaitThread extends Thread{

    renderer renderer = null;
    hostConnection connection;
    ServerSocket serverSocket = null;
    Socket socket = null;
    DataInputStream inputStream = null;
    DataOutputStream outputStream = null;

    public hostawaitThread(hostConnection connection, renderer renderer){
        this.renderer = renderer;
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

        //Send message to renderer to set to Connected Page
        renderer.hostPage.frame.setVisible(false);
        renderer.ConnectedSessionPage();

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
            System.out.print(otherCores);
        }catch(IOException i){
            System.out.println(i);
        }

        //Set max cores and start transfer threads
        this.connection.setCores(Math.max(cores, otherCores));
        connection.connectThreads();

    }
}

class hostinputThread extends Thread{

    private int port = 0;
    private ServerSocket serverSocket = null;
    private Socket socket = null;
    DataInputStream inputStream = null;
    
    public hostinputThread(int port){
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

class hostoutputThread extends Thread{

    private int port = 0;
    private ServerSocket serverSocket = null;
    private Socket socket = null;
    DataOutputStream outputStream = null;
    
    public hostoutputThread(int port){
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
            outputStream = new  DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        }catch(IOException i){
            System.out.println(i);
        }

    }
    
}

