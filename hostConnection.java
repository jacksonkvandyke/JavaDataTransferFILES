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

    outputThread transferThreads[] = null;

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
        assembledPackets = new FileToPackets(location);

    }

    void connectThreads(){
        //Create thread list
        this.transferThreads = new outputThread[(int) Math.ceil(this.maxCores / 2)];

        //Create the threads and await for connection
        ExecutorService threads = Executors.newFixedThreadPool(this.maxCores);
        for (int i = 0; i < this.maxCores; i += 2){
            //Output thread
            outputThread output = new outputThread(this.socket.getLocalPort() + i + 1);
            Runnable outThread = output;
            threads.execute(outThread);

            //Input thread
            Runnable inThread = new inputThread(this.socket.getLocalPort() + i + 2);
            threads.execute(inThread);

            //Add output thread to transferThreads array
            if (i == 0){
                this.transferThreads[0] = output;
            }

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
        this.connection.setCores((int) ( Math.max(cores, otherCores) / 2));
        connection.connectThreads();

    }
}

class inputThread extends Thread{

    private int port = 0;
    private ServerSocket serverSocket = null;
    private Socket socket = null;

    ObjectInputStream inputStream = null;
    fileAssembler assembler = null;
    
    public inputThread(int port){
        this.port = port;
        this.assembler = new fileAssembler();
    }

    public void run(){
        //Create thread socket and await connection
        try{
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress("127.0.0.1", this.port));
        }catch(IOException i){
            System.out.println(i);
        }

        //Wait for connection then accept
        try{
            socket = serverSocket.accept();

            //Assign input and output streams
            inputStream = new ObjectInputStream(socket.getInputStream());
        }catch(IOException i){
            System.out.println(i);
            return;
        }

        //Start data transfer
        dataTransfer();

    }

    void dataTransfer(){
        while(true){
            try {
                //Read from input stream
                Packet inPacket = (Packet) this.inputStream.readObject();
                this.assembler.SavePacket(inPacket);
                System.out.print(inPacket.getFilename());

            }catch (IOException | ClassNotFoundException e){
                System.out.print(e);
            }

            //Slight slowdown to allow reads
            try{
                Thread.sleep(10);
            }catch(InterruptedException e){
                System.out.println(e);
            }
        }
    }
    
}

class outputThread implements Runnable{

    private int port = 0;
    private ServerSocket serverSocket = null;
    private Socket socket = null;

    ObjectOutputStream outputStream = null;
    Packet currentPacket = null;
    
    public outputThread(int port){
        this.port = port;
    }

    public void setPacket(Packet packet){
        this.currentPacket = packet;
    }

    public Packet getPacket(){
        return this.currentPacket;
    }

    public void run(){
        //Create thread socket and await connection
        try{
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress("127.0.0.1", this.port));
        }catch(IOException i){
            System.out.println(i);
        }

        //Wait for connection then accept
        try{
            socket = serverSocket.accept();

            //Assign input and output streams
            outputStream = new  ObjectOutputStream(socket.getOutputStream());
        }catch(IOException i){
            System.out.println(i);
            return;
        }

        //Start data transfer
        dataTransfer();

    }

    void dataTransfer(){
        while(true){
            try{
                this.outputStream.writeObject(currentPacket);
                this.outputStream.flush();
                this.currentPacket = null;
            }catch (IOException e){
                System.out.print(e);
            }

            //Slight slowdown to allow reads
            try{
                Thread.sleep(10);
            }catch(InterruptedException e){
                System.out.println(e);
            }
        }
    }
    
}

