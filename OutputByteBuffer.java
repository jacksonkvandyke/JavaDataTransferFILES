import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OutputByteBuffer {

    List<Packet> packets = Collections.synchronizedList(new ArrayList<Packet>(50));

    public Packet getPacket(){
        return this.packets.remove(0);
    }

    public void addPacket(Packet packet){
        this.packets.add(packet);
    }
    
}
