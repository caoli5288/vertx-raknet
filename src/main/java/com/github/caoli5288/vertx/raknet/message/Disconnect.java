package com.github.caoli5288.vertx.raknet.message;

import com.github.caoli5288.vertx.raknet.Constants;
import com.github.caoli5288.vertx.raknet.util.Utils;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class Disconnect implements Serializable {

    public Disconnect() {
    }

    public Disconnect(ByteBuf buf) {
        decode(buf);
    }

    @Override
    public void decode(@NotNull ByteBuf buf) {
        buf.skipBytes(1);// id
    }

    @Override
    public @NotNull ByteBuf encode() {
        ByteBuf buf = Utils.buffer();
        buf.writeByte(Constants.ID_DISCONNECT);
        return buf;
    }
}
