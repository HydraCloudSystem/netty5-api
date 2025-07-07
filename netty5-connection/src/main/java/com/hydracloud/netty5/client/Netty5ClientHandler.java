package com.hydracloud.netty5.client;

import com.hydracloud.netty5.Netty5ClientChannel;
import com.hydracloud.netty5.Netty5Component;
import com.hydracloud.netty5.common.packet.Packet;
import com.hydracloud.netty5.common.packet.auth.AuthPacket;
import io.netty5.channel.ChannelHandlerContext;
import io.netty5.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;

import java.io.IOException;

@AllArgsConstructor
public final class Netty5ClientHandler extends SimpleChannelInboundHandler<Packet> {
    private final Netty5Client client;

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, Packet packet) throws Exception {
        client.thisChannel().transmitter().call(packet, null);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().writeAndFlush(new AuthPacket(client.identity(), client.authProperty()));
        client.connectionState(Netty5Component.ConnectionState.CONNECTED);
        client.thisChannel(new Netty5ClientChannel(client.identity(), ctx.channel(),
                new Netty5ClientPacketTransmitter(
                        client.bossGroup(),
                        packet -> client.thisChannel().sendPacket(packet)
                )));
        client.connectionFuture().complete(null);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if ((!ctx.channel().isActive() || !ctx.channel().isOpen() || !ctx.channel().isWritable())) {
            client.connectionState(Netty5Component.ConnectionState.DISCONNECTED);
            ctx.channel().close();
        }
    }

    @Override
    public void channelExceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (!(cause instanceof IOException)) {
            if (cause.getMessage().equalsIgnoreCase("null")) return;
            System.err.println("[client: " + client.identity().name() + "] Exception caught: " + cause.getMessage());
        }
    }
}
