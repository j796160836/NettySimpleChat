package com.JohnnyTests.NettySimpleChat;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.log4j.PropertyConfigurator;

public class ChatClient
{
	private final String host;
	private final int port;

	public static void main(String[] args) throws Exception
	{
		int port = 9999;
		String ip = "127.0.0.1";
		if (args.length >= 2)
		{
			ip = args[0];
			port = Integer.parseInt(args[1]);
		} else if (args.length == 1)
		{
			port = Integer.parseInt(args[0]);
		}
		new ChatClient(ip, port).run();
	}

	public ChatClient(String host, int port)
	{
		this.host = host;
		this.port = port;
	}

	public void run() throws Exception
	{
		// 載入Log資訊檔
		PropertyConfigurator.configure("log4j.properties");
		EventLoopGroup group = new NioEventLoopGroup();
		try
		{
			Bootstrap bootstrap = new Bootstrap().group(group)
					.channel(NioSocketChannel.class)
					.handler(new ChannelInitializer<SocketChannel>()
					{
						@Override
						protected void initChannel(SocketChannel ch)
								throws Exception
						{
							// 使用字串解碼器
							ch.pipeline().addLast(new StringDecoder());
							// 使用字串編碼器
							ch.pipeline().addLast(new StringEncoder());
							// 定義收到訊息的處理方式
							ch.pipeline().addLast(new ChatClientHandler());
						}
					});
			Channel channel = bootstrap.connect(host, port).sync().channel();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
			while (true)
			{
				// 迴圈聆聽鍵盤輸入
				String msg = in.readLine();
				if (msg.equalsIgnoreCase("quit") || msg.equalsIgnoreCase("bye"))
				{
					// 若符合特定字串，則斷線
					channel.close();
					break;
				}
				channel.writeAndFlush(msg + "\n");
			}

		} finally
		{
			group.shutdownGracefully();
		}
	}

}
