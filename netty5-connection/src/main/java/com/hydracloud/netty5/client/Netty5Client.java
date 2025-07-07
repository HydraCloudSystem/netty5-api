package com.hydracloud.netty5.client;

import com.hydracloud.netty5.Netty5ChannelInitializer;
import com.hydracloud.netty5.Netty5ChannelUtils;
import com.hydracloud.netty5.Netty5ClientChannel;
import com.hydracloud.netty5.Netty5Component;
import io.netty5.bootstrap.Bootstrap;
import io.netty5.channel.ChannelOption;
import io.netty5.channel.SimpleChannelInboundHandler;
import io.netty5.channel.epoll.Epoll;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
public final class Netty5Client extends Netty5Component {
    private final Netty5ClientChannel.Identity identity;
    private final Map<String, String> authProperty;
    private final ScheduledExecutorService reconnectScheduler = Executors.newScheduledThreadPool(1);
    @Setter
    private Netty5ClientChannel thisChannel;
    private Bootstrap bootstrap;

    public Netty5Client(@NotNull String hostname,
                        int port,
                        @NotNull Netty5ClientChannel.Identity identity,
                        @Nullable Map<String, String> authProperty) {
        super(0, hostname, port);
        this.identity = identity;
        this.authProperty = authProperty == null ? new HashMap<>() : authProperty;
    }

    @Override
    public void initialize() throws Exception {
         bootstrap = new Bootstrap()
                .group(bossGroup())
                .channelFactory(Netty5ChannelUtils::createChannelFactory)
                .handler(new Netty5ChannelInitializer(this.identity) {
                    @Override
                    public SimpleChannelInboundHandler<?> handler() {
                        return new Netty5ClientHandler(Netty5Client.this);
                    }
                })
                .option(ChannelOption.AUTO_READ, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.IP_TOS, 24)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000);

        if (Epoll.isTcpFastOpenClientSideAvailable()) {
            bootstrap.option(ChannelOption.TCP_FASTOPEN_CONNECT, true);
        }

        connect();
    }

    private void connect() {
        bootstrap.connect(hostname(), port()).addListener(future -> {
            if (future.isSuccess()) {
                return;
            }
            scheduleReconnect();
            this.connectionFuture(null);
        });
    }

    private void scheduleReconnect() {
        reconnectScheduler.schedule(() -> {
            if (this.connectionState() == ConnectionState.DISCONNECTED ||
                    this.connectionState() == ConnectionState.SEASON_CLOSED) {
                System.out.println("Attempting to reconnect...");
                connect();
            }
        }, 5, TimeUnit.SECONDS);
    }
}
