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

    //Await files command strings
    String command = "";
    String inCommand = "";

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
        awaitThread awaitObject = new awaitThread(this, renderer);
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

    void awaitFiles(){
        //Create the await file thread to wait for commands
        awaitFileThread awaitObject = new awaitFileThread(this.socket.getLocalPort() + 1, this);
        Thread thread = new Thread(awaitObject);
        thread.start();

    }

    void connectThreads(){
        //Create the threads and await for connection
        ExecutorService threads = Executors.newFixedThreadPool(this.maxCores);
        for (int i = 0; i < this.maxCores; i++){
            Runnable thread = new outputThread(this.socket.getLocalPort() + i + 1);
            threads.execute(thread);

        }

    }

}

class awaitThread extends Thread{

    renderer renderer = null;
    hostConnection connection;
    ServerSocket serverSocket = null;
    Socket socket = null;
    DataInputStream inputStream = null;
    DataOutputStream outputStream = null;

    public awaitThread(hostConnection connection, renderer renderer){
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
            connection.awaitFiles();
        }catch(IOException i){
            System.out.println(i);
        }

        //Set max cores
        this.connection.setCores(Math.max(cores, otherCores));

    }
}

class awaitFileThread extends Thread{

    private int port = 0;
    private ServerSocket serverSocket = null;
    private Socket socket = null;
    hostConnection connection = null;
    InputStream inputStream = null;
    OutputStream outputStream = null;
    
    public awaitFileThread(int port, hostConnection connection){
        this.port = port;
        this.connection = connection;
    }

    public void run(){
        //Create thread socket and await connection
        try{
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(connection.address, port));
        }catch(IOException i){
            System.out.println(i);
        }

        //Wait for connection then accept
        try{
            socket = serverSocket.accept();

            //Assign input and output streams
            this.inputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();
        }catch(IOException i){
            System.out.println(i);
        }

        //Continously read from input stream and write commands to output stream
        while (true){
            //Read data from input stream
            try{
                if (this.inputStream.available() > 0){
                    connection.inCommand = this.inputStream.readAllBytes().toString();
                    System.out.println(connection.inCommand);
                    connection.inCommand = "";
                }
            }catch (IOException e){
                System.out.println(e);

            }

            //Send commands through output stream
            try{
                if (connection.command != ""){
                    this.outputStream.write(connection.command.getBytes());
                    this.outputStream.flush();
                    connection.command = "";
                }
            }catch (IOException e){
                System.out.println(e);

            }

            System.out.print(connection.command);

            //Sleep for short moment to reduce CPU use
            try{
                Thread.sleep(1000);
            }catch (InterruptedException e){
                System.out.println(e);
            }

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
            outputStream = new  DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        }catch(IOException i){
            System.out.println(i);
        }

    }
    
}

