import java.io.*;
import java.net.*;

public class hostConnection{
    //Initialize variables
    renderer renderer = null;
    ServerSocket socket = null;
    String address = "127.0.0.1";
    int maxCores = 0;

    OutputByteBuffer outBuffer = new OutputByteBuffer();

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

    OutputByteBuffer getBuffer(){
        return this.outBuffer;
    }

    void connectThreads(){
        //Create the threads and await for connection
        for (int i = this.maxCores / 2; i < this.maxCores; i++){
            //Output thread
            hostOutputThread output = new hostOutputThread(this.socket.getLocalPort() + i + 1, this.outBuffer);
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
        for (int i = 0; i < this.maxCores / 2; i++){
            //Input thread
            hostInputThread input = new hostInputThread(this.socket.getLocalPort() + i + 1);
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

        //Connect threads
        connection.connectThreads();
    }
}

class hostInputThread extends Thread{

    private int port = 0;
    private ServerSocket serverSocket = null;
    private Socket socket = null;

    ObjectOutputStream outputStream = null;
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
            System.out.printf("Input Socket connected on port: %d\n", this.port);

            //Assign input and output streams
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.flush();
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
                if ((inPacket != null) && (inPacket.getData().length != 0)){
                    this.assembler.AddPacket(inPacket);
                }
            }catch (IOException | ClassNotFoundException e){
                System.out.print(e);
            }
        }
    }
    
}

class hostOutputThread extends Thread{

    private int port = 0;
    private ServerSocket serverSocket = null;
    private Socket socket = null;

    ObjectOutputStream outputStream = null;
    ObjectInputStream inputStream = null;

    OutputByteBuffer outBuffer;
    
    public hostOutputThread(int port, OutputByteBuffer outBuffer){
        this.port = port;
        this.outBuffer = outBuffer;
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
            System.out.printf("Output Socket connected on port: %d\n", this.port);

            //Assign input and output streams
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(null);
            outputStream.flush();
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
            try{
                if (!this.outBuffer.packets.isEmpty()){
                    Packet sendPacket = this.outBuffer.getPacket();
                    if (sendPacket != null){
                        this.outputStream.writeObject(sendPacket);
                        this.outputStream.flush();
                    }
                }else{
                    while(true){
                        
                    }
                }
            }catch (IOException e){
                System.out.print(e);
            }
        }
    }
    
}

