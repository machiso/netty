package tradition.nio;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author machi
 *
 * 原生Nio构建client
 */
public class NioClient {

    public static void main(String[] args) {

        new Worker().start();
    }


    private static class Worker extends Thread{
        @Override
        public void run() {
            SocketChannel channel = null;
            Selector selector = null;

            try {

                //初始化socketchannel，并且设置为非阻塞
                //传统的socket模型是阻塞的，客户端一个连接请求需要服务端对应一个线程进行处理，
                // 如果很多请求的话，服务端端线程压力会很大，很容易造成宕机，cpu飙升
                channel = SocketChannel.open();
                channel.configureBlocking(false);
                channel.connect(new InetSocketAddress("localhost",9000));

                //初始化selector
                selector = Selector.open();

                //将socketChannel注册到selector上面去，后面让selector负责监听连接事件
                channel.register(selector, SelectionKey.OP_ACCEPT);

                while (true){
                    selector.select();

                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()){
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove();

                        if (key.isConnectable()){
                            channel = (SocketChannel)key.channel();

                            if (channel.isConnectionPending()){
                                channel.finishConnect();

                                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                                byteBuffer.put("你好".getBytes());
                                byteBuffer.flip();

                                channel.write(byteBuffer);
                            }

                            channel.register(selector,SelectionKey.OP_READ);
                        }

                        else if(key.isReadable()){  // 就说明服务器端返回了一条数据可以读了
                            channel = (SocketChannel) key.channel();

                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            int len = channel.read(buffer);  // 把数据写入buffer，position推进到读取的字节数数字

                            if(len > 0) {
                                System.out.println("[" + Thread.currentThread().getName()
                                        + "]收到响应：" + new String(buffer.array(), 0, len));
                                Thread.sleep(5000);
                                channel.register(selector, SelectionKey.OP_WRITE);
                            }
                        }

                        else if(key.isWritable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            buffer.put("你好".getBytes());
                            buffer.flip();

                            channel = (SocketChannel) key.channel();
                            channel.write(buffer);
                            channel.register(selector, SelectionKey.OP_READ);
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if (channel!=null){
                    try {
                        channel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (selector!=null){
                    try {
                        selector.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


}
