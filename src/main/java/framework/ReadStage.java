package framework;

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
                    if(e.type == Event.Type.Read){
                        SocketChannel socketChannel = (SocketChannel) e.key.channel();
                        // Clear out our read buffer so it's ready for new data
                        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                        // Attempt to read off the channel
                        int numRead;
                        try {
                            numRead = socketChannel.read(readBuffer);
                        } catch (IOException ex) {
                            // The remote forcibly closed the connection, cancel
                            // the selection key and close the channel.
                            e.key.cancel();
                            socketChannel.close();
                            return;
                        }
                        if(numRead == -1){
                            e.key.cancel();
                            socketChannel.close();
                            return;
                        }
                        e.key.interestOps(e.key.interestOps() | SelectionKey.OP_READ);
                        String str = new String(readBuffer.array(), 0, numRead);
                        System.out.println("Async " + str);
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
