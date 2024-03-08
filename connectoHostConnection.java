import java.io.*;
import java.net.*;

public class connectoHostConnection {
    //Initialize variables
    connectoHostConnection connection = this;
    Socket socket = null;
    String address = "127.0.0.1";
    int maxCores = 0;

    OutputByteBuffer outBuffer = new OutputByteBuffer();

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
        //Create the output Threads and await for connection
        for (int i = 0; i < this.maxCores; i++){
            //Output thread
            outputThread output = new outputThread(this.socket.getPort() + i + 1, outBuffer);
            Thread outThread = new Thread(output);
            outThread.start();
        }

        //Sleep so all output threads can be made on both sides
        try{
            Thread.sleep(1000);
        }catch (InterruptedException e){
            System.out.print(e);
        }

        //Create the input threads
        for (int i = this.maxCores; i < this.maxCores * 2; i++){
            //Input thread
            inputThread input = new inputThread(this.socket.getPort() + i);
            Thread inThread = new Thread(input);
            inThread.start();
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
            System.out.printf("Input Socket connected on port: %d\n", this.port);

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
                System.out.print("Empty");
                if (this.inputStream.available() != 0){
                    //Read from input stream
                    Packet inPacket = (Packet) this.inputStream.readObject();
                    this.assembler.SavePacket(inPacket);
                    System.out.print(inPacket.getFilename());
                }else{
                    System.out.print("Empty");
                }
            }catch (IOException | ClassNotFoundException e){
                System.out.print(e);
            }
        }
    }
    
}

class outputThread implements Runnable{

    private int port = 0;
    private Socket socket = null;

    ObjectOutputStream outputStream = null;
    OutputByteBuffer outBuffer;
    
    public outputThread(int port, OutputByteBuffer outBuffer){
        this.port = port;
        this.outBuffer = outBuffer;
    }

    public void run(){
        //Create thread socket and connect to host socket
        socket = new Socket();

        //Wait for connection then accept
        try{
            socket.connect(new InetSocketAddress("127.0.0.1", this.port));
            System.out.printf("Output Socket connected on port: %d\n", this.port);

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
            try{
                if (!this.outBuffer.packets.isEmpty()){
                    Packet sendPacket = this.outBuffer.getPacket();
                    this.outputStream.writeObject(sendPacket);
                }else{
                    return;
                }
            }catch (IOException e){
                System.out.print(e);
            }
        }
    }
}
