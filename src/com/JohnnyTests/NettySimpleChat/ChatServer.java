package com.JohnnyTests.NettySimpleChat;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import org.apache.log4j.PropertyConfigurator;

/**
 * Echoes back any received data from a client.
 */
public class ChatServer
{

	private final int port;

	public ChatServer(int port)
	{
		this.port = port;
	}

	public void run() throws Exception
	{
		// 載入Log資訊檔
		PropertyConfigurator.configure("log4j.properties");
		// Configure the server.
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try
		{
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 100)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new ChannelInitializer<SocketChannel>()
					{
						@Override
						public void initChannel(SocketChannel ch)
								throws Exception
						{
							// 使用字串解碼器
							ch.pipeline().addLast(new StringDecoder());
							// 使用字串編碼器
							ch.pipeline().addLast(new StringEncoder());
							// 定義收到訊息的處理方式
							ch.pipeline().addLast(new ChatServerHandler());
						}
					});

			// Start the server.
			ChannelFuture f = b.bind(port).sync();
			System.out.println("=============================\n"
					+ "Server started at port " + port + "\n"
					+ "=============================\n");
			// Wait until the server socket is closed.
			f.channel().closeFuture().sync();

		} finally
		{
			// Shut down all event loops to terminate all threads.
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception
	{
		int port;
		if (args.length > 0)
		{
			port = Integer.parseInt(args[0]);
		} else
		{
			port = 9999;
		}
		new ChatServer(port).run();
	}
}
