package io.netty.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] requestBytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(requestBytes);

        String request = new String(requestBytes, "UTF-8");
        System.out.println("服务端接收到的请求:"+request);

        String response = "";
        ByteBuf responseBuf = Unpooled.copiedBuffer(response.getBytes());
        ctx.write(responseBuf);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
