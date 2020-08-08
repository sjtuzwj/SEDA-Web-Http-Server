package framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;

public class AppStage extends AbstractStage {
    public AppStage(){
        StageMap.getInstance().stageMap.put("app",this);
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
                    if (e.type == Event.Type.ReadRepsonse) {
                        System.out.println("APP Read " + e.Packet);
                        HashMap<String,String >params = RestfulParser.parse(e.Packet);
                        Event event = new Event(e.key, Event.Type.Encode);
                        event.Packet = Dispatcher.dispatch(e.httpType,params);
                        //event.Packet = params.toString();
                        StageMap.getInstance().stageMap.get("encode").Enqueue(event);
                    }
                    else if(e.type == Event.Type.WriteResponse){
                        System.out.println("Write Done");
                    }
                }
            }catch(Exception e){
                //do nothing
            }
        }
}

}
