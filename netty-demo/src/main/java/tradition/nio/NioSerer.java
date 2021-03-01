package tradition.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author machi
 *
 * 使用原生NIO
 */
public class NioSerer {

    private static Selector selector;
    private static LinkedBlockingQueue<SelectionKey> requestQueue;
    private static ExecutorService executorService;

    public static void main(String[] args) {

        init();

        listen();
    }


    private static void init(){
        ServerSocketChannel serverSocketChannel = null;

        try {
            selector = Selector.open();

            serverSocketChannel = ServerSocketChannel.open();

            //非阻塞模式
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(9000),100);

            //serverSocketChannel
            serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void listen(){
        while (true){
            try {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()){
                    SelectionKey key = (SelectionKey)iterator.next();

                    iterator.remove();

                    handleRequest(key);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private static void handleRequest(SelectionKey key) throws IOException {
        SocketChannel channel = null;

        try {
            //连接请求
            if (key.isAcceptable()){
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                channel = serverSocketChannel.accept();

                channel.configureBlocking(false);

                //关注发送过来的read请求
                channel.register(selector,SelectionKey.OP_READ);
            }

            else if (key.isReadable()){

                channel = (SocketChannel) key.channel();

                ByteBuffer buffer = ByteBuffer.allocate(1024);
                int count = channel.read(buffer);
                if (count>0){
                    buffer.flip();
                    channel.register(selector,SelectionKey.OP_WRITE);
                }
            }

            else if (key.isWritable()){
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                buffer.put("收到".getBytes());
                buffer.flip();

                channel = (SocketChannel)key.channel();
                channel.write(buffer);
                channel.register(selector,SelectionKey.OP_WRITE);
            }
        }catch (Exception e){
            e.printStackTrace();
            if (channel!=null){
                channel.close();
            }
        }
    }
}
