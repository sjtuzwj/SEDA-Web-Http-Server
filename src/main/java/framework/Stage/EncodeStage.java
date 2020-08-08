package framework.Stage;

import framework.Util.Event;
import framework.Util.HttpUtil.ResponseType;

import java.util.ArrayList;

public class EncodeStage  extends AbstractStage {
    private final String htmlHeader = "HTTP/1.1 200 OK\n"+
            "Content-Type: text/html;charset=UTF-8\n"+
            "Server: SEDA Web Server\n\n";
    private final String jsonHeader = "HTTP/1.1 200 OK\n"+
            "Content-Type: text/json;charset=UTF-8\n"+
            "Server: SEDA Web Server\n\n";
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
                        e.type = Event.Type.Write;
                        if(e.responseType == ResponseType.HTML)
                        e.Packet = htmlHeader+e.Packet;
                        if(e.responseType == ResponseType.JSON)
                            e.Packet = jsonHeader+e.Packet;
                        StageMap.getInstance().stageMap.get("write").Enqueue(e);
                    }
                }
            }catch(Exception e){
                //do nothing
            }
        }
    }
}
