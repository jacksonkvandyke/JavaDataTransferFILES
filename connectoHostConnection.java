import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class connectoHostConnection {
    //Initialize variables
    connectoHostConnection connection = this;
    Socket socket = null;
    String address = "127.0.0.1";
    int maxCores = 0;

    outputThread transferThreads[] = null;

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

    outputThread[] getOutThreads(){
        return this.transferThreads;
    }

    void connectThreads(){
        //Create thread list
        this.transferThreads = new outputThread[(int) Math.ceil(this.maxCores) + 1];

        //Create the threads and await for connection
        for (int i = 0; i < this.maxCores * 2; i += 2){
            //Output thread
            outputThread output = new outputThread(this.socket.getPort() + i + 1);
            Thread outThread = new Thread(output);
            outThread.start();

            //Input thread
            inputThread input = new inputThread(this.socket.getPort() + i + 2);
            Thread inThread = new Thread(input);
            inThread.start();

            //Add output thread to transferThreads array
            if (i == 0){
                this.transferThreads[0] = output;
            }else{
                this.transferThreads[(int) Math.ceil(this.maxCores / 2)] = output;
            }

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
            inputStream = new ObjectInputStream(socket.getInputStream());
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

                //Check if packet was read
                if (inPacket != null){
                    this.assembler.SavePacket(inPacket);
                    System.out.print(inPacket.getFilename());
                }

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
        //Create thread socket and connect to host socket
        socket = new Socket();

        //Wait for connection then accept
        try{
            socket.connect(new InetSocketAddress("127.0.0.1", this.port));
            System.out.printf("Socket connected on port: %d\n", this.port);

            //Assign input and output streams
            outputStream = new ObjectOutputStream(socket.getOutputStream());
        }catch(IOException i){
            System.out.println(i);
        }

        //Start data transfer
        dataTransfer();

    }

    void dataTransfer(){
        while(true){
            try{
                if (this.currentPacket != null){
                    this.outputStream.writeObject(this.currentPacket);
                    this.outputStream.flush();
                    this.currentPacket = null;
                }
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
