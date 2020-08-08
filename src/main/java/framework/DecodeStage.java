package framework;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.*;

public class DecodeStage  extends AbstractStage {
    public DecodeStage(){
        StageMap.getInstance().stageMap.put("decode",this);
    }
    @Override
    public void doJob(ArrayList<Event> elist){
        Runnable task = new HandleThread(elist);
        pool.execute(task);
    }
    private HttpType parseHttpType(String type){
        switch (type) {
            case "GET": return HttpType.GET;
            case "POST": return HttpType.POST;
            case "PUT": return HttpType.PUT;
            case "DELETE": return HttpType.DELETE;
            case "CONNECT": return HttpType.CONNECT;
            case "OPTIONS": return HttpType.OPTIONS;
            case "HEAD": return HttpType.HEAD;
            case "TRACE": return HttpType.TRACE;
        }
        return HttpType.ERROR;
    }

    class HandleThread implements Runnable{
        ArrayList<Event> elist;
        public HandleThread(ArrayList<Event> elist){
            this.elist = elist;
        }
        @Override
        public void run() {
            try {
                Event e;
                for (int i = 0; i < BatchSize; i++) {
                    e = elist.get(i);
                    if(e.type == Event.Type.Decode) {
                        String str = e.Packet;
                        Event event = new Event(e.key, Event.Type.ReadRepsonse);
                        String lines[] = str.split("\\r?\\n");
                        String param[] = lines[0].split(" ");
                        event.httpType = parseHttpType(param[0]);
                        event.Packet = param[1];
                        StageMap.getInstance().stageMap.get("app").Enqueue(event);
                    }
                }
            }catch(Exception e){
                //do nothing
            }
        }
    }
}
