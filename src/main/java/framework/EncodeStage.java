package framework;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.*;

public class EncodeStage  extends AbstractStage {
    private final String header = "HTTP/1.1 200 OK\n"+
            "Content-Type: text/html;charset=UTF-8\n"+
            "framework.Server: SEDA Web framework.Server/1.1\n\n";
    private final String template = "<!DOCTYPE html>"+
            "<html><head>"+
            "<title>Welcome to SEDA Web framework.Server (WWS)</title>"+
            "</head><body>%s</body></html>";
    public EncodeStage(){
        StageMap.getInstance().stageMap.put("encode",this);
    }
    @Override
    public void doJob(ArrayList<Event> elist){
        Runnable task = new HandleThread(elist);
        pool.execute(task);
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
                    if(e.type == Event.Type.Encode) {
                        Event event = new Event(e.key, Event.Type.Write);
                        event.Packet = header+String.format(template,e.Packet);
                        StageMap.getInstance().stageMap.get("write").Enqueue(event);
                    }
                }
            }catch(Exception e){
                //do nothing
            }
        }
    }
}
