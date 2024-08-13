package com.github.retrooper.chat.network;

import com.github.retrooper.chat.message.ChatMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ChatClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ChatMessage chatMessage) {
            //Print to console
            System.out.println(chatMessage.sender() + ": " + chatMessage.message());
        }
    }
}
