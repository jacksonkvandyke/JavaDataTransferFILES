import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class connectoHostConnection {
    //Initialize variables
    connectoHostConnection connection = this;
    Socket socket = null;
    String address = "127.0.0.1";
    int maxCores = 0;
    FileToPackets assembledPackets = null;

    List<Packet> dataStream = new ArrayList<Packet>();

    connectoHostConnection(String address, int port){
        //Create socket and link streams
        socket = new Socket();
        System.out.println("Socket successfully created.");

        //Connect to ensure connection
        try{
            socket.connect(new InetSocketAddress(address, port));
            this.address = address;
            System.out.println(socket.getPort());

            //Call thread to create Data Threads
            connectThread object = new connectThread(connection, socket);
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
        assembledPackets = new FileToPackets(location);

    }

    void connectThreads(){
        //Create the threads and await for connection
        ExecutorService threads = Executors.newFixedThreadPool(this.maxCores);
        for (int i = 0; i < this.maxCores; i += 2){
            //Output thread
            Runnable outThread = new outputThread(this.socket.getPort() + i + 1, dataStream);
            threads.execute(outThread);

            //Input thread
            Runnable inThread = new inputThread(this.socket.getPort() + i + 2);
            threads.execute(inThread);

        }

    }

}

class connectThread extends Thread{

    connectoHostConnection connection = null;
    Socket socket = null;
    DataInputStream inputStream = null;
    DataOutputStream outputStream = null;

    public connectThread(connectoHostConnection connection, Socket socket){
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
            System.out.print(otherCores);
            //Sleep for one second before continuing connection to ensure the host has finished setting up connections
            try{
                Thread.sleep(1000);
            }catch (InterruptedException e){
                System.out.println(e);
            }
        }catch(IOException i){
            System.out.println(i);
        }
        
        //Set max cores and start transfer threads
        connection.setCores((int) ( Math.max(cores, otherCores) / 2));

        //Sleep for one second to allow host threads to be created
        try{
            Thread.sleep(1000);
        }catch(InterruptedException e){
            System.out.print(e);
        }

        //Start transfer threads
        connection.connectThreads();

    }
    
}

class inputThread extends Thread{

    private int port = 0;
    private Socket socket = null;
    
    ObjectInputStream inputStream = null;
    fileAssembler assembler = null;
    
    public inputThread(int port){
        this.port = port;
        this.assembler = new fileAssembler();
    }

    public void run(){
        //Create thread socket and connect to host socket
        socket = new Socket();

        //Wait for connection then accept
        try{
            socket.connect(new InetSocketAddress("127.0.0.1", this.port));
            System.out.printf("Socket connected on port: %d\n", this.port);

            //Assign input and output streams
            inputStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        }catch(IOException i){
            System.out.println(i);
        }

        //Start data transfer
        dataTransfer();

    }

    void dataTransfer(){
        while(true){
            try {
                //Read from input stream
                Packet inPacket = (Packet) this.inputStream.readObject();
                System.out.print(inPacket.getFilename());
                this.assembler.SavePacket(inPacket);

            }catch (IOException | ClassNotFoundException e){
                System.out.print(e);
            }
        }
    }
    
}

class outputThread extends Thread{

    private int port = 0;
    private Socket socket = null;

    List<Packet> dataStream = null;
    ObjectOutputStream outputStream = null;
    
    public outputThread(int port, List<Packet> dataStream){
        this.port = port;
        this.dataStream = dataStream;
    }

    public void run(){
        //Create thread socket and connect to host socket
        socket = new Socket();

        //Wait for connection then accept
        try{
            socket.connect(new InetSocketAddress("127.0.0.1", this.port));
            System.out.printf("Socket connected on port: %d\n", this.port);

            //Assign input and output streams
            outputStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        }catch(IOException i){
            System.out.println(i);
        }

        //Start data transfer
        dataTransfer();

    }

    void dataTransfer(){
        while(true){
            //Write data to output stream
            if (this.dataStream.size() > 0){
                try{
                    this.outputStream.writeObject(this.dataStream.remove(0));
                    this.outputStream.flush();
                }catch (IOException e){
                    System.out.print(e);
                }
            }
        }
    }
    
}
