package com.github.retrooper.chat.client;

import com.github.retrooper.chat.message.ChatMessage;
import com.github.retrooper.chat.network.ChatClientHandler;
import com.github.retrooper.chat.network.ChatMessageDecoder;
import com.github.retrooper.chat.network.ChatMessageEncoder;
import com.github.retrooper.chat.network.ChatServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Scanner;

public class Client {
    private String username;

    public Client(String username) {
        this.username = username;
    }

    private Channel serverChannel;

    public boolean isConnectionOpen() {
        return serverChannel.isOpen();
    }

    public void connect(String host, int port) throws Exception {
        System.out.println("Connecting to " + host + ":" + port + "...");
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(worker)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast("decoder", new ChatMessageDecoder())
                                    .addLast("handler", new ChatClientHandler()) //Client specific
                                    .addLast("encoder", new ChatMessageEncoder());
                        }
                    });

            ChannelFuture f = bootstrap.connect(host, port).sync();
            System.out.println("Connected...");
            serverChannel = f.channel();
            Scanner scanner = new Scanner(System.in);
            //While the connection is still open
            while (isConnectionOpen()) {
                String line = scanner.nextLine();
                //Send a message packet to the server

                //TODO THREADING ISSUE - could improve
                serverChannel.writeAndFlush(new ChatMessage(username, line));
            }

            System.err.println("The server shut down! Closing program...");

            //Wait 10 seconds
            Thread.sleep(10000L);

        }
        finally {
            worker.shutdownGracefully();
        }
    }
}
