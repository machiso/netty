package io.netty.server;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * server端
 */
public class NettyServer {

    public static void main(String[] args) throws InterruptedException {

        //EventLoopGroup本质上是一组线程池，可以从里面获取线程，并且会管理线程的生命周期的一个组件
        //默认的线程的数量为当前机器的CPU数 * 2
        //每个线程都有自己的一个selector选择器，用来轮询就绪的事件
        EventLoopGroup bossGroup  = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();

        bootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)

                .childHandler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {

                        ch.pipeline().addLast(new NettyServerHandler());

                    }
                });

        ChannelFuture channelFuture = bootstrap.bind(8000).sync();

        channelFuture.channel().closeFuture().sync();

    }

}
