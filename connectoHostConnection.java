import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class connectoHostConnection {
    //Initialize variables
    connectoHostConnection connection = this;
    Socket socket = null;
    String address = null;
    int maxCores = 0;

    OutputByteBuffer outBuffer = new OutputByteBuffer();

    String downloadLocation = "";
    boolean receivingFiles = false;
    long totalReceivingData = 0;
    long recievedData = 0;

    connectoHostConnection(String address, int port){
        //Create socket and link streams
        socket = new Socket();
        System.out.println("Socket successfully created.");
        try{
            Thread.sleep(1000);
        }catch(InterruptedException e){
            System.out.print(e);
        }

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

    void connectThreads(){
        //Create the threads and await for connection
        ExecutorService executors = Executors.newFixedThreadPool(this.maxCores * 2);

        //Output threads
        for (int i = this.maxCores; i < this.maxCores * 2; i++){
            outputThread output = new outputThread(this.socket.getPort() + i + 1, this.outBuffer, socket.getInetAddress());
            Thread outThread = new Thread(output);
            executors.execute(outThread);
        }

        //Sleep so all output threads can be made on both sides
        try{
            Thread.sleep(1000);
        }catch (InterruptedException e){
            System.out.print(e);
        }

        //Input thread
        for (int i = 0; i < this.maxCores; i++){
        inputThread input = new inputThread(this.socket.getPort() + i + 1, this, socket.getInetAddress());
        Thread inThread = new Thread(input);
        executors.execute(inThread);
        }

        //Add services to OutputBuffer
        outBuffer.executors = executors;

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
        connection.setCores((int) (Math.min(cores, otherCores)));

        //Short sleep to allow host threads to be created
        try{
            Thread.sleep(1000);
        }catch (InterruptedException e){
            System.out.print(e);
        }

        //Connect threads
        connection.connectThreads();
    }
}

class inputThread extends Thread{

    private int port = 0;
    private Socket socket = null;
    private connectoHostConnection parent = null;
    private InetAddress hostAddress = null;
    
    private ObjectOutputStream outputStream = null;
    private ObjectInputStream inputStream = null;
    private fileAssembler assembler = null;
    
    public inputThread(int port, connectoHostConnection parent, InetAddress hostAddress){
        this.port = port;
        this.parent = parent;
        this.hostAddress = hostAddress;
        this.assembler = new fileAssembler();
    }

    public void run(){
        //Create thread socket and connect to host socket
        socket = new Socket();

        //Wait for connection then accept
        try{
            socket.connect(new InetSocketAddress(hostAddress, this.port));
            System.out.printf("Input Socket connected on port: %d\n", this.port);

            //Assign input and output streams
            outputStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            outputStream.flush();
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
                if (inPacket != null){
                    //Set download location of assembler
                    this.assembler.downloadLocation = this.parent.downloadLocation;

                    //Recieve packet
                    synchronized(this.parent){
                        this.parent.recievedData += inPacket.getDataLength();
                        System.out.printf("Recieved data: %d", this.parent.recievedData);
                        this.assembler.packets.put(inPacket);

                        //Create starter data
                        if (this.parent.receivingFiles == false){
                            this.parent.receivingFiles = true;
                            this.parent.totalReceivingData = inPacket.getTotalData();
                        }

                        //Check if all data recieved
                        if (this.parent.recievedData == this.parent.totalReceivingData){
                            this.parent.receivingFiles = false;
                            this.parent.recievedData = 0;
                        }
                    }
                }

            }catch (IOException | ClassNotFoundException | InterruptedException e){
                System.out.print(e);
            }
        }
    }
    
}

class outputThread extends Thread{

    private int port = 0;
    private Socket socket = null;
    private InetAddress hostAddress = null;

    private ObjectOutputStream outputStream = null;
    private ObjectInputStream inputStream = null;

    private OutputByteBuffer outBuffer;
    
    public outputThread(int port, OutputByteBuffer outBuffer, InetAddress hostAddress){
        this.port = port;
        this.outBuffer = outBuffer;
        this.hostAddress = hostAddress;
    }

    public void run(){
        //Create thread socket and connect to host socket
        socket = new Socket();

        //Wait for connection then accept
        try{
            socket.connect(new InetSocketAddress(hostAddress, this.port));
            System.out.printf("Output Socket connected on port: %d\n", this.port);

            //Assign input and output streams
            outputStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            outputStream.flush();
            inputStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        }catch(IOException i){
            System.out.println(i);
        }

        //Start data transfer
        dataTransfer();

    }

    void dataTransfer(){
        while(true){
            try{
                Packet sendPacket = this.outBuffer.packets.take();
                if (sendPacket != null){
                    this.outputStream.writeObject(sendPacket);
                    this.outputStream.flush();
                }

            }catch (IOException | InterruptedException e){
                System.out.print(e);
            }
        }
    }
}
