package com.waylau.netty.demo.time;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class TimeDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes()<4){
            return;
        }
//        list.add(byteBuf.readBytes(4));
        //使用pojo类型解码
        list.add(new UnixTime(byteBuf.readUnsignedInt()));
    }
}
