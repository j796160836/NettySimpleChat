package com.JohnnyTests.NettySimpleChat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ChatClientHandler extends SimpleChannelInboundHandler<String>
{

	@Override
	protected void channelRead0(ChannelHandlerContext arg0, String msg)
			throws Exception
	{
		System.out.println(msg);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception
	{
		System.out.println("Server offline!!!");
		ctx.close();
		ctx.channel().closeFuture().sync();
		System.exit(0);
	}
}
