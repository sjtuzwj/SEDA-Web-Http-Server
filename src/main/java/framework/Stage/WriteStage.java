package framework.Stage;

import framework.Util.Event;
import framework.Util.ReadWriteBuffer;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class WriteStage extends AbstractStage {
    public WriteStage(){
        StageMap.getInstance().stageMap.put("write",this);
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
                    if(e.type == Event.Type.Write){
                        System.out.println("Write "+e.Packet);
                        ByteBuffer sendBuffer =((ReadWriteBuffer) e.key.attachment()).writeBuffer;
                        SocketChannel channel = (SocketChannel)e.key.channel();
                        sendBuffer.put(e.Packet.getBytes());
                        e.key.interestOps(e.key.interestOps() | SelectionKey.OP_WRITE);
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
