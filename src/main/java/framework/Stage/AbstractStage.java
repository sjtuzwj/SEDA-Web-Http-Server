package framework.Stage;

import framework.Util.Event;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class AbstractStage implements  StageAPI {

    public final Integer ThreadSize = 5;
    public final Integer BatchSize = 1;
    public ThreadPoolExecutor pool = new ThreadPoolExecutor(1,ThreadSize,50, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(2* ThreadSize));

    public final String lock = "nomeaning";
    public LinkedBlockingQueue<Event> BatchingQ = new LinkedBlockingQueue<Event>(2 * BatchSize);

    @Override
    public void Enqueue(Event e){
        synchronized (lock){
            BatchingQ.add(e);
            if(BatchingQ.size() == BatchSize){
                ArrayList<Event> elist = new ArrayList<>(BatchSize);
                try {
                    for (int i = 0; i < BatchSize; i++) {
                        elist.add(i,BatchingQ.poll());
                    }
                }catch(Exception ex){
                    //do nothing
                }
                doJob(elist);
            }
        }
    }
}
