import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class connectoHostConnection {
    //Initialize variables
    connectoHostConnection connection = this;
    Socket socket = null;
    String address = "127.0.0.1";
    int maxCores = 0;

    List<Packet> packets = Collections.synchronizedList(new ArrayList<Packet>(50));

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

    List<Packet> getPackets(){
        return this.packets;
    }

    void connectThreads(){
        //Create the threads and await for connection
        for (int i = 0; i < this.maxCores * 2; i += 2){
            //Output thread
            outputThread output = new outputThread(this.socket.getPort() + i + 1, packets);
            Thread outThread = new Thread(output);
            outThread.start();

            //Input thread
            inputThread input = new inputThread(this.socket.getPort() + i + 2);
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
    List<Packet> packets = null;
    
    public outputThread(int port, List<Packet> packets){
        this.port = port;
        this.packets = packets;
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
                if (!this.packets.isEmpty()){
                    this.outputStream.writeObject(this.packets.remove(0));
                    this.outputStream.flush();
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
