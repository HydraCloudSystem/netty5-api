package com.hydracloud.netty5.common.packet;

import com.hydracloud.netty5.common.codec.CodecBuffer;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Setter
@Getter
public abstract class RespondPacket extends Packet implements CodecBuffer.WriteReadStream {
    protected UUID queryId;

    public RespondPacket() {
        super();
    }

    public RespondPacket(@NotNull CodecBuffer buffer) {
        super(buffer);
        this.queryId = buffer.readUniqueId();
        this.readBuffer(buffer);
    }
}
