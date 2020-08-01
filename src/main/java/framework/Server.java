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
               else if (key.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    // Clear out our read buffer so it's ready for new data
                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    // Attempt to read off the channel
                    int numRead;
                    try {
                        numRead = socketChannel.read(readBuffer);
                    } catch (IOException ex) {
                        // The remote forcibly closed the connection, cancel
                        // the selection key and close the channel.
                        key.cancel();
                        socketChannel.close();
                        return;
                    }
                    if(numRead == -1){
                        close(key);
                        continue;
                    }
                    String str = new String(readBuffer.array(), 0, numRead);
                    Event event = new Event(key, Event.Type.Read);
                    event.Packet = str;
                    System.out.println("Sync "+str);
                    StageMap.getInstance().stageMap.get("read").Enqueue(event);
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
        clientChannel.register(subSelector, SelectionKey.OP_READ);
        System.out.println("a new client connected "+clientChannel.getRemoteAddress());
    }

    private void close(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        SocketAddress addr  = clientChannel.getRemoteAddress();
        clientChannel.close();
        System.out.println("a client closed "+addr);
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
        new Server().start();
    }
}
