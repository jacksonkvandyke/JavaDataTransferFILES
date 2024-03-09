import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class OutputByteBuffer {

    List<Packet> packets = Collections.synchronizedList(new ArrayList<Packet>(50));

    ExecutorService outputExecutor = null;
    ExecutorService inputExecutor = null;

    synchronized public Packet getPacket(){
        if (this.packets.size() > 0){
            return this.packets.remove(0);
        }
        return null;
    }

    synchronized public void addPacket(Packet packet){
        this.packets.add(packet);
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
