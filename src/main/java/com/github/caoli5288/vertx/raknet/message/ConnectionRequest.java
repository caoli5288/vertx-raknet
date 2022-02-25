package com.github.caoli5288.vertx.raknet.message;

import com.github.caoli5288.vertx.raknet.Constants;
import com.github.caoli5288.vertx.raknet.util.Utils;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class ConnectionRequest implements Serializable {

    private long guid;
    private long time;

    public ConnectionRequest(ByteBuf buf) {
        decode(buf);
    }

    public ConnectionRequest() {
    }

    @Override
    public void decode(@NotNull ByteBuf buf) {
        buf.skipBytes(1);
        guid = buf.readLong();
        time = buf.readLong();
        buf.skipBytes(1);// security
    }

    @Override
    public @NotNull ByteBuf encode() {
        ByteBuf buf = Utils.buffer();
        buf.writeByte(Constants.ID_CONNECTION_REQUEST);
        buf.writeLong(guid);
        buf.writeLong(time);
        buf.writeBoolean(false);
        return buf;
    }
}
