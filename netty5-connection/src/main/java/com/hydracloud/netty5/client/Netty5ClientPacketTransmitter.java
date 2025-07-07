package com.hydracloud.netty5.client;

import com.hydracloud.netty5.Netty5ClientChannel;
import com.hydracloud.netty5.common.packet.Netty5PacketTransmitter;
import com.hydracloud.netty5.common.packet.Packet;
import io.netty5.channel.EventLoopGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class Netty5ClientPacketTransmitter extends Netty5PacketTransmitter {
    public Netty5ClientPacketTransmitter(@NotNull EventLoopGroup eventExecutors,
                                         @NotNull Consumer<Packet> packetConsumer) {
        super(eventExecutors, packetConsumer);
    }

    @Override
    public void callActions(@NotNull Packet packet, @Nullable Netty5ClientChannel sender) {
    }
}
