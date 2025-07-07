package com.hydracloud.netty5;

import com.hydracloud.netty5.client.Netty5ClientPacketTransmitter;
import com.hydracloud.netty5.common.codec.CodecBuffer;
import com.hydracloud.netty5.common.packet.Packet;
import io.netty5.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

@Getter
@AllArgsConstructor
public final class Netty5ClientChannel {
    @Setter
    private Identity identity;
    private final Channel channel;
    @Setter
    private Netty5ClientPacketTransmitter transmitter;

    public void sendPacket(@NotNull Packet packet) {
        channel.writeAndFlush(packet);
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Identity implements CodecBuffer.WriteReadStream {
        private String name;
        private UUID uuid;

        @Override
        public void writeBuffer(@NotNull CodecBuffer codecBuffer) {
            codecBuffer.writeString(name)
                    .writeUniqueId(uuid);
        }

        @Override
        public void readBuffer(@NotNull CodecBuffer codecBuffer) {
            this.name = codecBuffer.readString();
            this.uuid = codecBuffer.readUniqueId();
        }
    }

    public record AuthType(Netty5ClientChannel clientChannel, Map<String, String> authProperty) {
    }

}
