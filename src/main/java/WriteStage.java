
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.prefs.BackingStoreException;

public class WriteStage implements StageAPI {
    public final Integer ThreadSize = 5;
    public final Integer BatchSize = 1;
    private final String header = "HTTP/1.1 200 OK\n"+
            "Content-Type: text/html;charset=UTF-8\n"+
            "Server: SEDA Web Server/1.1\n\n";
    private final String template = "<!DOCTYPE html>"+
            "<html><head>"+
            "<title>Welcome to SEDA Web Server (WWS)</title>"+
            "</head><body>%s</body></html>";

    public ThreadPoolExecutor pool = new ThreadPoolExecutor(1,ThreadSize,50,TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(2* ThreadSize));

    public final String lock = "nomeaning";
    public LinkedBlockingQueue<Event>  BatchingQ = new LinkedBlockingQueue<Event>(2 * BatchSize);
    public WriteStage(){
        StageMap.getInstance().stageMap.put("write",this);
    }
    @Override
    public void Enqueue(Event e){
        synchronized (lock){
            Runnable task;
            BatchingQ.add(e);
            if(BatchingQ.size() == BatchSize){
                System.out.println("New Writer");
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
                    if(e.type == Event.Type.Write){
                        System.out.println("Write " + String.format(template,e.Packet));
                        ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
                        SocketChannel channel = (SocketChannel) e.key.channel();
                        sendBuffer.clear();
                        sendBuffer.put((header+String.format(template,e.Packet)).getBytes());
                        sendBuffer.flip();
                        channel.write(sendBuffer);
                        Event event = new Event(e.key, Event.Type.WriteResponse);
                        StageMap.getInstance().stageMap.get("app").Enqueue(event);
                    }
                }
            }catch(Exception e){
                //do nothing
            }
        }
    }

}
