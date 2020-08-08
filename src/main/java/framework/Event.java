package framework;

import java.nio.channels.SelectionKey;

public class Event {
    enum Type{Read,Write,ReadRepsonse,WriteResponse,Flush,Decode,Encode};
    public Type type;
    public HttpType httpType;
    public SelectionKey key;
    public String Packet;
    public Event(SelectionKey key,Type type){
        this.type = type;
        this.key = key;
    }
}
