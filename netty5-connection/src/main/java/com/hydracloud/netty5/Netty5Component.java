package com.hydracloud.netty5;

import com.hydracloud.netty5.actions.Action;
import io.netty5.channel.Channel;
import io.netty5.channel.EventLoopGroup;
import io.netty5.util.concurrent.FutureListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Getter
public abstract class Netty5Component {
    @Setter(AccessLevel.PROTECTED)
    private CommunicationFuture<Void> connectionFuture = new CommunicationFuture<>();
    @Getter
    private final EventLoopGroup bossGroup;
    @Getter(AccessLevel.PROTECTED)
    private final String hostname;
    @Getter(AccessLevel.PROTECTED)
    private final int port;
    @Getter
    private final List<Action<?>> actions = new ArrayList<>();
    @Setter
    private ConnectionState connectionState = ConnectionState.UNDEFINED;

    public Netty5Component(int bossGroupThreads, @NotNull String hostname, int port) {
        this.bossGroup = Netty5ChannelUtils.createEventLoopGroup(bossGroupThreads);
        this.hostname = hostname;
        this.port = port;
    }

    public boolean isAlive() {
        return !bossGroup.isShutdown() && !bossGroup.isTerminated() && !bossGroup.isShuttingDown();
    }

    public void shutdownGracefully() {
        bossGroup.shutdownGracefully();
    }

    public FutureListener<? super Channel> deployFuture() {
        return it -> {
            if (it.isSuccess()) {
                connectionFuture.complete(null);
                it.getNow().closeFuture();
            } else {
                connectionFuture.completeExceptionally(it.cause());
            }
        };
    }

    public <T extends Action<?>> Netty5Component addAction(@NotNull T action) {
        actions.add(action);
        return this;
    }

    public abstract void initialize() throws Exception;

    public enum ConnectionState {
        UNDEFINED,
        CONNECTED,
        DISCONNECTED,
        SEASON_CLOSED
    }

    public static final class CommunicationFuture<E> extends CompletableFuture<E> {
        public CommunicationFuture() {
            this.exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            });
        }

        public E sync(E defaultValue, long secondTimeout) {
            try {
                return get(secondTimeout, TimeUnit.SECONDS);
            } catch (Exception e) {
                return defaultValue;
            }
        }

        public E sync(E defaultValue) {
            return sync(defaultValue, 5);
        }
    }

}
