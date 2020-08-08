package framework.Stage;

import framework.Util.Event;
import framework.Util.ReadWriteBuffer;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.*;

public class FlushStage extends AbstractStage {
    public final Integer ThreadSize = 5;
    public final Integer BatchSize = 1;

    public ThreadPoolExecutor pool = new ThreadPoolExecutor(1,ThreadSize,50,TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(2* ThreadSize));
    public final String lock = "nomeaning";
    public LinkedBlockingQueue<Event>  BatchingQ = new LinkedBlockingQueue<Event>(2 * BatchSize);
    public FlushStage(){
        StageMap.getInstance().stageMap.put("flush",this);
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
                    if(e.type == Event.Type.Flush){
                        System.out.println("Flushed");
                        ByteBuffer sendBuffer =((ReadWriteBuffer) e.key.attachment()).writeBuffer;
                        sendBuffer.flip();
                        SocketChannel channel = (SocketChannel)e.key.channel();
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
