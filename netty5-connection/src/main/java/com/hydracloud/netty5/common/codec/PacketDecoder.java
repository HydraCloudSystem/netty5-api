package com.hydracloud.netty5.common.codec;

import com.hydracloud.netty5.Netty5ClientChannel;
import com.hydracloud.netty5.common.packet.Packet;
import io.netty5.buffer.Buffer;
import io.netty5.channel.ChannelHandlerContext;
import io.netty5.handler.codec.ByteToMessageDecoder;

public final class PacketDecoder extends ByteToMessageDecoder {
    private final Netty5ClientChannel.Identity identity;

    public PacketDecoder(Netty5ClientChannel.Identity identity) {
        this.identity = identity;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, Buffer in) {
        var buffer = new CodecBuffer(in);
        var className = buffer.readString();

        try {
            var readableBytes = buffer.readInt();
            var content = new CodecBuffer(in.copy(in.readerOffset(), readableBytes, true));
            in.skipReadableBytes(readableBytes);

            var packet = (Packet) Class.forName(className).getConstructor(CodecBuffer.class).newInstance(content);
            buffer.resetBuffer();
            ctx.fireChannelRead(packet);
        } catch (Exception e) {
            System.err.println((identity != null ? "[identity: " + identity.name() + "]" : "") + "Error while decoding packet" + className);
            e.printStackTrace();
        }
    }
}
