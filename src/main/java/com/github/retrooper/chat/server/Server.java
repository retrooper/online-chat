package com.github.retrooper.chat.server;

import com.github.retrooper.chat.network.ChatMessageEncoder;
import com.github.retrooper.chat.network.ChatServerHandler;
import com.github.retrooper.chat.network.ChatMessageDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.ArrayList;
import java.util.List;

public class Server {

    public static Server INSTANCE;



    public List<Channel> clients = new ArrayList<>();

    public void start(int port) throws Exception {
        System.out.println("Starting server with port " + port);
        INSTANCE = this;
        //Handles incoming connections
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //Handles traffic
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<>() {
                        @Override
                        public void initChannel(Channel channel) throws Exception {
                            //Register client
                            clients.add(channel);

                            channel.pipeline().addLast("decoder", new ChatMessageDecoder())
                                    .addLast("handler", new ChatServerHandler()) //Server specific
                                    .addLast("encoder", new ChatMessageEncoder());
                        }
                    })
                    .option(ChannelOption.TCP_NODELAY,
                            true) //Should packets be sent when requested? (Packets won't be accumulated to send them at once)
                    .option(ChannelOption.SO_BACKLOG,
                            100) //Maximum amount of connections that could wait in a queue to connect to this server.
                    .option(ChannelOption.SO_KEEPALIVE,
                            true); //Should small data be circulated to efficiently detect if a client is offline?

            //Bind to this port, accept incoming connections.
            ChannelFuture f = bootstrap.bind(port).sync();

            System.out.println("Started server...");

            //Keep on running until the server socket is closed
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
