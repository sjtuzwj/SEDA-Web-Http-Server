import java.nio.channels.SelectionKey;

public class Event {
    enum Type{Read,Write,ReadRepsonse,WriteResponse};
    public Type type;
    public SelectionKey key;
    public String Packet;
    public Event(SelectionKey key,Type type){
        this.type = type;
        this.key = key;
    }
}
