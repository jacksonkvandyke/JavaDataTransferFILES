import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class hostConnection{
    //Initialize variables
    renderer renderer = null;
    ServerSocket socket = null;
    String address = "127.0.0.1";
    int maxCores = 0;

    List<Packet> packets = Collections.synchronizedList(new ArrayList<Packet>(50));

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

    List<Packet> getPackets(){
        return this.packets;
    }

    void connectThreads(){
        //Create the threads and await for connection
        for (int i = 0; i < this.maxCores * 2; i += 2){
            //Output thread
            hostOutputThread output = new hostOutputThread(this.socket.getLocalPort() + i + 1, packets);
            Thread outThread = new Thread(output);
            outThread.start();

            //Input thread
            hostInputThread input = new hostInputThread(this.socket.getLocalPort() + i + 2);
            Thread inThread = new Thread(input);
            inThread.start();
            
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

class hostInputThread extends Thread{

    private int port = 0;
    private ServerSocket serverSocket = null;
    private Socket socket = null;

    ObjectInputStream inputStream = null;
    fileAssembler assembler = null;
    
    public hostInputThread(int port){
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

class hostOutputThread implements Runnable{

    private int port = 0;
    private ServerSocket serverSocket = null;
    private Socket socket = null;

    ObjectOutputStream outputStream = null;
    List<Packet> packets = null;
    
    public hostOutputThread(int port, List<Packet> packets){
        this.port = port;
        this.packets = packets;
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
            outputStream = new ObjectOutputStream(socket.getOutputStream());
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

