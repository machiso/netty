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

        //初始化ServerSocketChannel并且注册到selector选择器上面去，让选择器监听OP_ACCEPT事件
        init();

        listen();
    }


    private static void init(){
        ServerSocketChannel serverSocketChannel = null;

        try {

            //初始化selector选择器
            selector = Selector.open();

            //初始化serversocketchannel
            serverSocketChannel = ServerSocketChannel.open();

            //设置为非阻塞模式
            serverSocketChannel.configureBlocking(false);

            //监听某个端口
            serverSocketChannel.socket().bind(new InetSocketAddress(9000),100);

            //将serverSocketChannel注册到selector上面去，并且让selector监听ServerSocketChannel的OP_ACCEPT事件
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

                //ServerSocketChannel.accept()
                //监听新进来的连接，通过 ServerSocketChannel.accept() 方法监听新进来的连接。
                // 当 accept()方法返回的时候,它返回一个包含新进来的连接的SocketChannel。
                // 因此, accept()方法会一直阻塞到有新连接到达。
                //ServerSocketChannel可以设置成非阻塞模式。在非阻塞模式下，accept() 方法会立刻返回，如果还没有新进来的连接,返回的将是null。
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
