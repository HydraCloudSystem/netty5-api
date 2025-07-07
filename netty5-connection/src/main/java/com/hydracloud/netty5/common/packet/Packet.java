package com.hydracloud.netty5.common.packet;

import com.hydracloud.netty5.common.codec.CodecBuffer;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public abstract class Packet {
    protected CodecBuffer buffer;

    public Packet() {
        this.buffer = CodecBuffer.allocate();
    }

    public Packet(@NotNull CodecBuffer buffer) {
        this.buffer = buffer;
    }
}
