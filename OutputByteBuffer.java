import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public class OutputByteBuffer {

    BlockingQueue<Packet> packets = new ArrayBlockingQueue<Packet>(50);

    ExecutorService outputExecutor = null;
    ExecutorService inputExecutor = null;

    synchronized public Packet getPacket() throws InterruptedException{
        return this.packets.take();
    }

    synchronized public boolean addPacket(Packet packet){
        return this.packets.offer(packet);
    }

    public void killThreads(){
        if (outputExecutor != null){
            outputExecutor.shutdown();
        }
        if (inputExecutor != null){
            inputExecutor.shutdown();
        }
    }
    
}
