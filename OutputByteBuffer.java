import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public class OutputByteBuffer {

    BlockingQueue<Packet> packets = new ArrayBlockingQueue<Packet>(10);

    ExecutorService executors = null;

    public void killThreads(){
        if (executors != null){
            executors.shutdown();
        }
    }
    
}
