package framework;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;


public class Client {

    ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
    ByteBuffer readBuffer = ByteBuffer.allocate(1024);

    public void start() throws IOException {
        // 打开socket通道
        SocketChannel sc = SocketChannel.open();
        //设置为非阻塞
        sc.configureBlocking(false);
        //连接服务器地址和端口
        sc.connect(new InetSocketAddress("localhost", 8001));
        //打开选择器
        Selector selector = Selector.open();
        //注册连接服务器socket的动作
        sc.register(selector, SelectionKey.OP_CONNECT);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            //选择一组键，其相应的通道已为 I/O 操作准备就绪。
            //此方法执行处于阻塞模式的选择操作。
            selector.select();
            //返回此选择器的已选择键集。
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = keys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();
                // 判断此通道上是否正在进行连接操作。
                if (key.isConnectable()) {
                    sc.finishConnect();
                    sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    System.out.println("server connected...");
                    break;
                }
                else if (key.isReadable()){//读取数据
                    System.out.print("receive message:");
                    SocketChannel client = (SocketChannel) key.channel();
                    //将缓冲区清空以备下次读取
                    readBuffer.clear();
                    int num = client.read(readBuffer);
                    System.out.println(new String(readBuffer.array(),0, num));
                }
                else if (key.isWritable()) { //写数据
                    System.out.print("please input message:");
                    String message = scanner.nextLine();
                    //ByteBuffer writeBuffer = ByteBuffer.wrap(message.getBytes());
                    writeBuffer.clear();
                    writeBuffer.put(message.getBytes());
                    //将缓冲区各标志复位,因为向里面put了数据标志被改变要想从中读取数据发向服务器,就要复位
                    writeBuffer.flip();
                    sc.write(writeBuffer);

                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new Client().start();
    }
}
