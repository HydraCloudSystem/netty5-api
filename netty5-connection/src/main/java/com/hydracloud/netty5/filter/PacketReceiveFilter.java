package com.hydracloud.netty5.filter;

import com.hydracloud.netty5.Netty5ClientChannel;
import com.hydracloud.netty5.common.packet.Packet;
import org.jetbrains.annotations.NotNull;

public abstract class PacketReceiveFilter extends Filter<PacketReceiveFilter.FilterValue> {
    public record FilterValue(@NotNull Packet recievedPacket, @NotNull Netty5ClientChannel senderChannel) {
    }
}
