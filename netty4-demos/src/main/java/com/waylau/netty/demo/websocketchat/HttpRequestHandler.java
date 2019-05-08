package com.waylau.netty.demo.websocketchat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * 处理 Http 请求
 * @author waylau.com
 * @date 2015-3-26 
 */
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> { //1
    private final String wsUri;
    private static final File INDEX;

    static {
        URL location = HttpRequestHandler.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            String path = location.toURI() + "WebsocketChatClient.html";
            path = !path.contains("file:") ? path : path.substring(5);
            INDEX = new File(path);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to locate WebsocketChatClient.html", e);
        }
    }

    public HttpRequestHandler(String wsUri) {
        this.wsUri = wsUri;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (wsUri.equalsIgnoreCase(request.getUri())) {
            // 升级websocket握手,页面进行socket连接时走这里
            // request.retain()是为了让计数加一，因为除了SimpleChannelInboundHandler的channelRead要realease一次，
            // WebSocketServerProtocolHandshakeHandler的channelRead里会调用WebSocketServerProtocolHandler.forbiddenHttpRequestResponder()释放一次，
            // 只在传入对象为FullHttpRequest时进行释放，并且write出去到下一个out-handler，否则跳到下一个in-handler
            ctx.fireChannelRead(request.retain());                  //2
        } else {
            //一般通过 post 上传大数据时，才会使用到 100-continue 协议
            if (HttpHeaders.is100ContinueExpected(request)) {
                //正确情况下，收到请求后，返回 100 或错误码。
                //如果在发送 100-continue 前收到了 post 数据（客户端提前发送 post 数据），则不发送 100 响应码(略去)。
                send100Continue(ctx);                               //3
            }

            RandomAccessFile file = new RandomAccessFile(INDEX, "r");//4

            HttpResponse response = new DefaultHttpResponse(request.getProtocolVersion(), HttpResponseStatus.OK);
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");

            boolean keepAlive = HttpHeaders.isKeepAlive(request);

            if (keepAlive) {                                        //5
                response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, file.length());
                response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            }
            ctx.write(response);                    //6 分两步传输 第一步是响应头 第二步是下面的文件内容

            if (ctx.pipeline().get(SslHandler.class) == null) {     //7 是否加密压缩
                ctx.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));
            } else {
                ctx.write(new ChunkedNioFile(file.getChannel()));
            }
            ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);           //8 LastHttpContent.EMPTY_LAST_CONTENT标识响应结束
            if (!keepAlive) {//如果不需要长连接则传输完成后关闭channel
                future.addListener(ChannelFutureListener.CLOSE);        //9
            }
            
            file.close();
        }
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        ctx.writeAndFlush(response);
    }

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
    	Channel incoming = ctx.channel();
		System.out.println("Client:"+incoming.remoteAddress()+"异常");
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
	}
}
