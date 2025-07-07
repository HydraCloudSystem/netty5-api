package com.hydracloud.netty5.server;

import com.hydracloud.netty5.Netty5ClientChannel;
import com.hydracloud.netty5.TriConsumer;
import com.hydracloud.netty5.common.packet.Netty5PacketTransmitter;
import com.hydracloud.netty5.common.packet.Packet;
import com.hydracloud.netty5.common.packet.RequestPacket;
import com.hydracloud.netty5.common.packet.RespondPacket;
import io.netty5.channel.EventLoopGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class Netty5ServerPacketTransmitter extends Netty5PacketTransmitter {
    private final TriConsumer<RequestPacket, Class<Packet>, Consumer<Packet>> requestPacketConsumer;

    public Netty5ServerPacketTransmitter(EventLoopGroup eventExecutors,
                                         Consumer<Packet> packetConsumer,
                                         TriConsumer<RequestPacket, Class<Packet>, Consumer<Packet>> requestPacketConsumer) {
        super(eventExecutors, packetConsumer);
        this.requestPacketConsumer = requestPacketConsumer;
    }

    @Override
    public <P extends Packet> CompletableFuture<Packet> queryPacket(@NotNull RequestPacket requestPacket, Class<P> packet) {
        return null;
    }

    @Override
    public <P extends Packet> P queryPacketDirect(@NotNull RequestPacket requestPacket, Class<P> packetClass) {
        return null;
    }

    @Override
    public <P extends Packet> void queryPacket(@NotNull RequestPacket requestPacket, Class<P> packet, Consumer<P> callback) {
        // todo send to every connected client
    }

    public <R extends RequestPacket> void callResponder(@NotNull R request, @NotNull Netty5ClientChannel sender) {
        if (responders().containsKey(request.getClass())) {
            responders().get(request.getClass()).forEach((key, respondPacketFunction) -> {
                var respondPacket = respondPacketFunction.apply(request);
                respondPacket.queryId(request.queryId());
                respondPacket.buffer().writeUniqueId(request.queryId());
                respondPacket.writeBuffer(respondPacket.buffer());
                sender.transmitter().publishPacket(respondPacket);
            });
        } else {
            sender.transmitter().callResponder(request);
        }
    }

    @Override
    public void call(@NotNull Packet packet, @Nullable Netty5ClientChannel sender) {
        if (packet instanceof RespondPacket respondPacket) {
            if (directRequests().containsKey(respondPacket.queryId())) {
                directRequests().put(respondPacket.queryId(), packet);
            }
            if (futureRequests().containsKey(respondPacket.queryId())) {
                futureRequests().get(respondPacket.queryId()).accept(packet);
                futureRequests().remove(respondPacket.queryId());
            }
        }

        if (packet instanceof RequestPacket requestPacket) {
            if (sender != null) {
                this.callResponder(requestPacket, sender);
            } else {
                throw new RuntimeException("Sender cannot be null by QueryPacket to Server");
            }
        }

        this.callActions(packet, sender);

        if (listener().containsKey(packet.getClass())) {
            listener().get(packet.getClass()).forEach((key, packetConsumer) -> packetConsumer.accept(sender, packet));
        }
    }

    @Override
    public void publishPacket(@NotNull Packet packet) {
        packetConsumer().accept(packet);
    }

    @Override
    public void callActions(@NotNull Packet packet, @Nullable Netty5ClientChannel sender) {
    }
}
