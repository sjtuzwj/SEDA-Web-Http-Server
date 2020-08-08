package framework.Stage;

import framework.Util.Event;
import framework.Util.ReadWriteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.*;

public class ReadStage extends AbstractStage {
    public LinkedBlockingQueue<Event>  BatchingQ = new LinkedBlockingQueue<Event>(2 * BatchSize);
    public ReadStage(){
        StageMap.getInstance().stageMap.put("read",this);
    }
       public void doJob(ArrayList<Event> elist){
        Runnable task = new HandleThread(elist);
        pool.execute(task);
    }
    class HandleThread implements Runnable{
        ArrayList<Event> elist;
        public HandleThread(ArrayList<Event> elist){
            this.elist = elist;
        }
        public void run() {
            try {
                Event e;

                for (int i = 0; i < BatchSize; i++) {
                    e = elist.get(i);
                    if(e.type == Event.Type.Read){
                        SocketChannel socketChannel = (SocketChannel) e.key.channel();
                        // Clear out our read buffer so it's ready for new data
                        ByteBuffer readBuffer =((ReadWriteBuffer) e.key.attachment()).readBuffer;
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
                    Event event = new Event(e.key, Event.Type.Decode);
                    event.Packet = str;
                    StageMap.getInstance().stageMap.get("decode").Enqueue(event);
                    }
                }
            }catch(Exception e){
                //do nothing
            }
        }
    }
}
