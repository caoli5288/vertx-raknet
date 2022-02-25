package com.github.caoli5288.vertx.raknet.message;

import com.github.caoli5288.vertx.raknet.Constants;
import com.github.caoli5288.vertx.raknet.util.Utils;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class ConnectedPong implements Serializable {

    private long time;

    public ConnectedPong(ByteBuf buf) {
        decode(buf);
    }

    public ConnectedPong() {
    }

    @Override
    public void decode(@NotNull ByteBuf buf) {
        buf.skipBytes(1);
        time = buf.readLong();
    }

    @Override
    public @NotNull ByteBuf encode() {
        ByteBuf buf = Utils.buffer();
        buf.writeByte(Constants.ID_CONNECTED_PONG);
        buf.writeLong(time);
        return buf;
    }
}
