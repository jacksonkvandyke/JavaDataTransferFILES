import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class connectoHostConnection {
    //Initialize variables
    Socket socket = null;
    int maxCores = 0;
    DataInputStream inputStream = null;
    DataOutputStream outputStream = null;

    connectoHostConnection(String address, int port){
        //Create socket and link streams
        socket = new Socket();
        System.out.println("Socket successfully created.");

        //Connect to ensure connection
        try{
            socket.connect(new InetSocketAddress(address, port));
            inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            System.out.println(socket.getPort());
        
        }catch(IOException i) {
            System.out.println(i);
            return;
        }

    }
    
    public static void main(String[] args){
        connectoHostConnection connection = new connectoHostConnection();

        //Call thread to create Data Threads
        incomingConnectThread object = new incomingConnectThread(connection.socket);
        Thread thread = new Thread(object);
        thread.start();

    }


}

class incomingConnectThread extends Thread{

    Socket socket = null;
    DataInputStream inputStream = null;
    DataOutputStream outputStream = null;

    public incomingConnectThread(Socket socket){
        try{
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
        }catch(IOException i){
            System.out.println(i);
        }
        
        //Set max cores
        int maxCores = Math.max(cores, otherCores);
        System.out.println(maxCores);

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException i) {
            System.out.println(i);
        }

        ExecutorService threads = Executors.newFixedThreadPool(maxCores);
        for (int i = 0; i < maxCores; i++){
            Runnable thread = new incomingDataThread(socket.getPort() + i + 1);
            threads.execute(thread);

        }

    }
    
}

class incomingDataThread extends Thread{

    private int port = 0;
    private Socket socket = null;

    public incomingDataThread(int port) {
        this.port = port;
        
    }

    public void run(){
        //Connect to thread sockets
        socket = new Socket();
        try{
            socket.connect(new InetSocketAddress("127.0.0.1", port));
            System.out.printf("Socket connected on port: %d\n", port);
        }catch(IOException i){
            System.out.println(i);

        }

    }

}
