import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OutputByteBuffer {

    List<Packet> packets = Collections.synchronizedList(new ArrayList<Packet>(50));
    List<Thread> threads = Collections.synchronizedList(new ArrayList<Thread>());

    synchronized public Packet getPacket(){
        return this.packets.remove(0);
    }

    synchronized public void addPacket(Packet packet){
        this.packets.add(packet);
    }

    public void addThread(Thread thread){
        this.threads.add(thread);
    }

    public void killThreads(){
        while (!threads.isEmpty()){
            threads.remove(0).interrupt();
        }
    }
    
}
