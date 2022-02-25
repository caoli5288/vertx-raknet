package com.github.caoli5288.vertx.raknet.message;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public interface Serializable {

    void decode(@NotNull ByteBuf buf);

    @NotNull
    ByteBuf encode();
}
