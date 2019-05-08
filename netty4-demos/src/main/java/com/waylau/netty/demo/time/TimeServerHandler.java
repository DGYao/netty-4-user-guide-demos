package com.waylau.netty.demo.time;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class TimeServerHandler extends ChannelInboundHandlerAdapter {
	
	@Override
    public void channelActive(final ChannelHandlerContext ctx) { // (1)
       /* final ByteBuf time = ctx.alloc().buffer(4); // (2)
        time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));
        *//**
         * ChannelHandlerContext.write() (和 writeAndFlush() )方法会返回一个 ChannelFuture 对象，
         * 一个 ChannelFuture 代表了一个还没有发生的 I/O 操作。
         * 这意味着任何一个请求操作都不会马上被执行，因为在 Netty 里所有的操作都是异步的。
         * 举个例子下面的代码中在消息被发送之前可能会先关闭连接。
         *//*
        final ChannelFuture f = ctx.writeAndFlush(time); // (3)
        //预定义监听代码 f.addListener(ChannelFutureListener.CLOSE);
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                //assert:如果它的条件返回错误,则终止程序执行
                assert f == future;
                //监听写操作完成后才关闭连接
                ctx.close();
            }
        }); // (4)*/
        ChannelFuture f = ctx.writeAndFlush(new UnixTime());
        f.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
