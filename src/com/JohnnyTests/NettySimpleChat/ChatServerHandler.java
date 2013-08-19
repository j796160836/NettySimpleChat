package com.JohnnyTests.NettySimpleChat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler implementation for the echo server.
 */
@Sharable
public class ChatServerHandler extends ChannelInboundHandlerAdapter
{
	private static final Logger logger = Logger
			.getLogger(ChatServerHandler.class.getName());

	// 存放所有Client端的連線
	private static final ChannelGroup channels = new DefaultChannelGroup(
			GlobalEventExecutor.INSTANCE);

	private ChannelMatcher NotMyself;

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception
	{
		super.channelActive(ctx);
		// 當有Client端連接上來，就把連線放入ChannelGroup
		channels.add(ctx.channel());

		// 對該Client印出歡迎訊息
		ctx.channel().writeAndFlush(
				String.format("=== Welcome to chat room! ===\n"
						+ "Username: %s\nOnline users: %d\n"
						+ "=============================\n\n", ctx.channel()
						.remoteAddress(), channels.size()));

		NotMyself = new ChannelMatcher()
		{
			@Override
			public boolean matches(Channel channel)
			{
				// 比對所有的Client連線，只要不是自己回傳true
				return !channel.equals(ctx.channel());
			}
		};

		// 通知其他用戶，使用者上線 （發送訊息給非自己的其他Client）
		channels.flushAndWrite(String.format(
				"[SERVER] - User %s has joined!\nOnline user:%d\n", ctx
						.channel().remoteAddress(), channels.size()), NotMyself);

		System.out.print(String.format(
				"[SERVER] - User %s has joined!\nOnline user:%d\n", ctx
						.channel().remoteAddress(), channels.size()));
	}

	@Override
	public void channelInactive(final ChannelHandlerContext ctx)
			throws Exception
	{
		// 通知其他用戶，使用者離線
		channels.flushAndWrite(String.format(
				"[SERVER] - User %s has left!\nOnline user:%d\n\n", ctx
						.channel().remoteAddress(), channels.size()));

		System.out.print(String.format(
				"[SERVER] - User %s has left!\nOnline user:%d\n\n", ctx
						.channel().remoteAddress(), channels.size()));
		channels.remove(ctx.channel());
		super.channelInactive(ctx);
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg)
			throws Exception
	{
		System.out.println("ChatServerHandler : channelRead " + msg.toString());

		// 當收到訊息，則轉送資料給大家
		channels.flushAndWrite(
				String.format("[%s] - %s", ctx.channel().remoteAddress(),
						msg.toString()), NotMyself);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception
	{
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	{
		// Close the connection when an exception is raised.
		logger.log(Level.WARNING, "Unexpected exception from downstream.",
				cause);
		ctx.close();
	}
}
