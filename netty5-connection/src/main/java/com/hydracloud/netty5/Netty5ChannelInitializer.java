package com.hydracloud.netty5;

import com.hydracloud.netty5.common.codec.PacketDecoder;
import com.hydracloud.netty5.common.codec.PacketEncoder;
import io.netty5.channel.Channel;
import io.netty5.channel.ChannelInitializer;
import io.netty5.channel.SimpleChannelInboundHandler;
import io.netty5.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty5.handler.codec.LengthFieldPrepender;

public abstract class Netty5ChannelInitializer extends ChannelInitializer<Channel> {
    private final Netty5ClientChannel.Identity identity;

    public Netty5ChannelInitializer(Netty5ClientChannel.Identity identity) {
        this.identity = identity;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        channel.pipeline()
                .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, Integer.BYTES, 0, Integer.BYTES))
                .addLast(new PacketDecoder(identity))
                .addLast(new LengthFieldPrepender(Integer.BYTES))
                .addLast(new PacketEncoder(identity))
                .addLast(handler());
    }

    public abstract SimpleChannelInboundHandler<?> handler();
}
