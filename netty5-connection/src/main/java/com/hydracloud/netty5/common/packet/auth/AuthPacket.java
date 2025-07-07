package com.hydracloud.netty5.common.packet.auth;

import com.hydracloud.netty5.Netty5ClientChannel;
import com.hydracloud.netty5.common.codec.CodecBuffer;
import com.hydracloud.netty5.common.packet.Packet;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Getter
public class AuthPacket extends Packet {
    protected final Netty5ClientChannel.Identity identity;
    protected final Map<String, String> properties;

    public AuthPacket(@NotNull Netty5ClientChannel.Identity identity, @NotNull Map<String, String> properties) {
        this.identity = identity;
        this.properties = properties;
        buffer.writeStream(this.identity)
                .writeMap(this.properties, CodecBuffer::writeString, CodecBuffer::writeString);
    }

    public AuthPacket(@NotNull CodecBuffer buffer) {
        super(buffer);
        this.identity = buffer.readStream(new Netty5ClientChannel.Identity());
        this.properties = buffer.readMap(new HashMap<>(), buffer::readString, buffer::readString);
    }
}
