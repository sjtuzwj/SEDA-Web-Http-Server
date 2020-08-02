package framework;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.*;

public class FlushStage implements StageAPI {
    public final Integer ThreadSize = 5;
    public final Integer BatchSize = 1;

    public ThreadPoolExecutor pool = new ThreadPoolExecutor(1,ThreadSize,50,TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(2* ThreadSize));

    public final String lock = "nomeaning";
    public LinkedBlockingQueue<Event>  BatchingQ = new LinkedBlockingQueue<Event>(2 * BatchSize);
    public FlushStage(){
        StageMap.getInstance().stageMap.put("flush",this);
    }
    @Override
    public void Enqueue(Event e){
        synchronized (lock){
            Runnable task;
            BatchingQ.add(e);
            if(BatchingQ.size() == BatchSize){
                System.out.println("New Flusher");
                ArrayList<Event> elist = new ArrayList<>(BatchSize);
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
                    if(e.type == Event.Type.Flush){
                        ByteBuffer sendBuffer = (ByteBuffer) e.key.attachment();
                        SocketChannel channel = (SocketChannel)e.key.channel();
                        sendBuffer.flip();
                        if(sendBuffer.hasRemaining())
                            channel.write(sendBuffer);
                        sendBuffer.clear();
                    }
                }
            }catch(Exception e){
                //do nothing
            }
        }
    }

}
