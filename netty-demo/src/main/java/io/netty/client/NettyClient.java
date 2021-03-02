package io.netty.client;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 *  client端
 */
public class NettyClient {

        public static void main(String[] args) {
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap
                        // 1.指定线程模型
                        .group(workerGroup)
                        // 2.指定 IO 类型为 NIO
                        .channel(NioSocketChannel.class)
                        // 3.IO 处理逻辑
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) {
                                ch.pipeline().addLast(new NettyClientHandler());
                            }
                        });

                ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 50070).sync();

                channelFuture.channel().closeFuture().sync();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

}
