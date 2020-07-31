
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.*;

public class ReadStage implements StageAPI {
    public final Integer ThreadSize = 5;
    public final Integer BatchSize = 1;
    public ThreadPoolExecutor pool = new ThreadPoolExecutor(1,ThreadSize,50,TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(2* ThreadSize));

    public final String lock = "nomeaning";
    public LinkedBlockingQueue<Event>  BatchingQ = new LinkedBlockingQueue<Event>(2 * BatchSize);
    public ReadStage(){
        StageMap.getInstance().stageMap.put("read",this);
    }
    @Override
    public void Enqueue(Event e){
        synchronized (lock){
            Runnable task;
            BatchingQ.offer(e);
            if(BatchingQ.size() == BatchSize){
                ArrayList<Event> elist = new ArrayList<>();
                try {
                    for (int i = 0; i < BatchSize; i++) {
                        elist.add(i,BatchingQ.poll());
                    }
                }catch(Exception ex){
                    //do nothing
                }
                task = new HandleThread(elist);
                pool.execute(task);
            }
        }
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
                    if(e.type == Event.Type.Read){
                        System.out.println("Async " + e.Packet);
                        Event event = new Event(e.key, Event.Type.ReadRepsonse);
                        event.Packet = e.Packet;
                        StageMap.getInstance().stageMap.get("app").Enqueue(event);
                    }
                }
            }catch(Exception e){
                //do nothing
            }
        }
    }

}
