/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.JohnnyTests.NettySimpleChat.gui;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.JohnnyTests.NettySimpleChat.ChatClient;
import com.JohnnyTests.NettySimpleChat.ChatClientHandler;

/**
 * Simplistic telnet client.
 */
public class NettyTCPClient
{
	private final String host;
	private final int port;
	private EventLoopGroup group;
	private Channel ch;
	private ChannelInboundHandlerAdapter handler;

	public NettyTCPClient(String host, int port)
	{
		this.host = host;
		this.port = port;
		PropertyConfigurator.configure("log4j.properties");
	}

	public void startClient() throws Exception
	{
		// 載入Log資訊檔
		
		Logger logger = Logger.getLogger(NettyTCPClient.class);
		group = new NioEventLoopGroup();

		Bootstrap bootstrap = new Bootstrap().group(group)
				.channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<SocketChannel>()
				{
					@Override
					public void initChannel(SocketChannel ch) throws Exception
					{
						ch.pipeline().addLast("encoder", new StringEncoder());
						ch.pipeline().addLast("decoder", new StringDecoder());
						// and then business logic.
						if (handler != null)
							ch.pipeline().addLast("handler", handler);
					}
				});

		ChannelFuture f = bootstrap.connect(host, port).sync();
		ch = f.channel();
		System.out.println("Connected!");
	}

	public void sendMessage(String msg)
	{
		ch.writeAndFlush(msg);
	}

	public void stopClient()
	{
		// Wait until all messages are flushed before closing the channel.
		try
		{
			ch.close();

			ch.closeFuture().sync();
		} catch (InterruptedException e)
		{
		} finally
		{
			group.shutdownGracefully();
		}
	}

	public ChannelInboundHandlerAdapter getHandler()
	{
		return handler;
	}

	public void setHandler(ChannelInboundHandlerAdapter handler)
	{
		this.handler = handler;
	}
}
