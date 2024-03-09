import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public class OutputByteBuffer {

    BlockingQueue<Packet> packets = new ArrayBlockingQueue<Packet>(10);

    ExecutorService outputExecutor = null;
    ExecutorService inputExecutor = null;

    public void killThreads(){
        if (outputExecutor != null){
            outputExecutor.shutdown();
        }
        if (inputExecutor != null){
            inputExecutor.shutdown();
        }
    }
    
}
