package com.hydracloud.netty5.server;

import com.hydracloud.netty5.Netty5ChannelInitializer;
import com.hydracloud.netty5.Netty5ChannelUtils;
import com.hydracloud.netty5.Netty5ClientChannel;
import com.hydracloud.netty5.Netty5Component;
import com.hydracloud.netty5.filter.Filter;
import io.netty5.bootstrap.ServerBootstrap;
import io.netty5.channel.ChannelOption;
import io.netty5.channel.EventLoopGroup;
import io.netty5.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public final class Netty5Server extends Netty5Component {
    private final EventLoopGroup workerGroup = Netty5ChannelUtils.createEventLoopGroup(0);
    private final List<Filter<?>> filters = new ArrayList<>();
    private final Netty5ServerPacketTransmitter packetTransmitter;
    @Setter
    private List<Netty5ClientChannel> connections = new ArrayList<>();
    @Setter
    private Netty5ClientChannel.Identity serverIdentity;

    public Netty5Server(@NotNull String hostname, int port) {
        super(1, hostname, port);
        this.packetTransmitter = new Netty5ServerPacketTransmitter(bossGroup(), packet -> {

        }, (requestPacket, packetClass, consumer) -> {

        });
    }

    public <T extends Filter<?>> Netty5Server addFilter(@NotNull T filter) {
        this.filters.add(filter);
        return this;
    }

    @Override
    public void initialize() throws Exception {
        new ServerBootstrap()
                .group(bossGroup(), workerGroup)
                .channelFactory(Netty5ChannelUtils.buildChannelFactory())
                .childHandler(new Netty5ChannelInitializer(this.serverIdentity) {
                    @Override
                    public SimpleChannelInboundHandler<?> handler() {
                        return new Netty5ServerHandler(Netty5Server.this);
                    }
                })
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.IP_TOS, 24)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .bind(this.hostname(), this.port())
                .addListener(deployFuture())
                .addListener(future -> {
                    if (!future.isSuccess()) {
                        throw new RuntimeException(future.cause());
                    }
                });
    }

    @Override
    public void shutdownGracefully() {
        this.workerGroup.shutdownGracefully();
        super.shutdownGracefully();
    }

    public List<Netty5ClientChannel> clientChannel(@NotNull String name) {
        return this.connections.stream()
                .filter(clientChannel -> clientChannel.identity().name().equalsIgnoreCase(name))
                .toList();
    }

    public Netty5ClientChannel firstChannel(@NotNull String name) {
        return this.clientChannel(name).stream().findFirst().orElse(null);
    }

    public Netty5ClientChannel clientChannel(@NotNull UUID uuid) {
        return this.connections.stream()
                .filter(clientChannel -> clientChannel.identity().uuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }

}
