package framework;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
//https://www.cnblogs.com/gmhappy/p/11864094.html
public class Server {
    volatile private Selector mainSelector;
    volatile private Selector subSelector;
    public void mainStart() throws IOException {
        // 打开服务器套接字通道
        ServerSocketChannel ssc = ServerSocketChannel.open();
        // 服务器配置为非阻塞
        ssc.configureBlocking(false);
        // 进行服务的绑定
        ssc.bind(new InetSocketAddress("localhost", 8001));

        // 注册到mainSelector，等待连接
        ssc.register(mainSelector, SelectionKey.OP_ACCEPT);

        System.out.println("MainReactor Started... ");
        while (!Thread.currentThread().isInterrupted()) {
            mainSelector.select(100);
            Set<SelectionKey> keys = mainSelector.selectedKeys();
            Iterator<SelectionKey> keyIterator = keys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                if (!key.isValid()) {
                    continue;
                }
                if (key.isAcceptable()) {
                    accept(key);
                }
                keyIterator.remove(); //该事件已经处理，可以丢弃
            }
        }
    }

    public void subStart() throws IOException{

        System.out.println("SubReactor Started... ");
        while (!Thread.currentThread().isInterrupted()) {
            subSelector.select(100);
            Set<SelectionKey> keys =subSelector.selectedKeys();
            Iterator<SelectionKey> keyIterator = keys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                if (!key.isValid()) {
                    continue;
                }
               if (key.isReadable()) {
                    key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
                    Event event = new Event(key, Event.Type.Read);
                    StageMap.getInstance().stageMap.get("read").Enqueue(event);
                }
               if(key.isWritable()){
                   key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
                   Event event = new Event(key, Event.Type.Flush);
                   StageMap.getInstance().stageMap.get("flush").Enqueue(event);
               }
                keyIterator.remove(); //该事件已经处理，可以丢弃
            }
        }
    }

    public void start() throws IOException {
        mainSelector = Selector.open();
        subSelector = Selector.open();
        Thread sub = new Thread(new subReactor());
        sub.start();
        mainStart();
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = ssc.accept();
        clientChannel.configureBlocking(false);
        SelectionKey newkey = clientChannel.register(subSelector, SelectionKey.OP_READ);
        newkey.attach(new ReadWriteBuffer());
        System.out.println("a new client connected "+clientChannel.getRemoteAddress());
    }


    class subReactor implements Runnable {
        @Override
        public void run() {
            try {
                subStart();
            }catch(IOException io){
                System.out.println("sub IO Error");
            }
        }
    }


    public static void main(String[] args) throws IOException {
        System.out.println("server started...");
        new AppStage();
        new WriteStage();
        new ReadStage();
        new FlushStage();
        new DecodeStage();
        new EncodeStage();
        new Server().start();
    }
}
