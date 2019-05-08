package com.waylau.netty.demo.heartbeat;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

/**
 * 说明：心跳服务器处理器
 *
 * @author <a href="http://www.waylau.com">waylau.com</a> 2015年11月6日
 */
public class HeartbeatServerHandler extends ChannelInboundHandlerAdapter {
	
	// Return a unreleasable view on the given ByteBuf
	// which will just ignore release and retain calls.
	private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled
			.unreleasableBuffer(Unpooled.copiedBuffer("Heartbeat",
					CharsetUtil.UTF_8));  

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {

		if (evt instanceof IdleStateEvent) {
			IdleStateEvent event = (IdleStateEvent) evt;
			String type = "";
			if (event.state() == IdleState.READER_IDLE) {
				type = "read idle";
			} else if (event.state() == IdleState.WRITER_IDLE) {
				type = "write idle";
			} else if (event.state() == IdleState.ALL_IDLE) {
				type = "all idle";
			}
			/**
			 * 1）duplicate方法：复制当前对象，复制后的对象与前对象共享缓冲区，且维护自己的独立索引
			 * 2）copy方法：复制一份全新的对象，内容和缓冲区都不是共享的
			 * 3）slice方法：获取调用者的子缓冲区，且与原缓冲区共享缓冲区
			 */
			//复制当前对象，复制后的对象与前对象共享缓冲区，且维护自己的独立索引，多个channel可以独立维护自己的byteBuf的写进度
			ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate()).addListener(
					ChannelFutureListener.CLOSE_ON_FAILURE);
 
			System.out.println( ctx.channel().remoteAddress()+"超时类型：" + type);
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}
}
