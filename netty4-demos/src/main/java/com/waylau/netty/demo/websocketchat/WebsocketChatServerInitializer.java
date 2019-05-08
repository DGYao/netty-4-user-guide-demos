package com.waylau.netty.demo.websocketchat;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 服务端 ChannelInitializer
 * 
 * @author waylau.com
 * @date 2015-3-13
 */
public class WebsocketChatServerInitializer extends
		ChannelInitializer<SocketChannel> {	//1

	@Override
    public void initChannel(SocketChannel ch) throws Exception {//2
		 ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new HttpServerCodec());//in & out
		pipeline.addLast(new HttpObjectAggregator(64*1024));//in
		pipeline.addLast(new ChunkedWriteHandler());//in $ out
		pipeline.addLast(new HttpRequestHandler("/ws"));//in
		pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));//in
		pipeline.addLast(new TextWebSocketFrameHandler());//in

    }
}
