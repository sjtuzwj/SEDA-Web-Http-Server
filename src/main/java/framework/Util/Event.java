package framework.Util;

import framework.Util.HttpUtil.RequestType;
import framework.Util.HttpUtil.ResponseType;

import java.nio.channels.SelectionKey;

public class Event {
    public enum Type{Read,Write,ReadRepsonse,WriteResponse,Flush,Decode,Encode};
    public Type type;
    public RequestType requestType;
    public ResponseType responseType;
    public SelectionKey key;
    public String Packet;
    public Event(SelectionKey key,Type type){
        this.type = type;
        this.key = key;
    }
}
