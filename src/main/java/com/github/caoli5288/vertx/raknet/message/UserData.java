package com.github.caoli5288.vertx.raknet.message;

import com.github.caoli5288.vertx.raknet.Constants;
import com.github.caoli5288.vertx.raknet.util.Utils;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class UserData implements Serializable {

    private ByteBuf body;

    public UserData(ByteBuf data) {
        decode(data);
    }

    public UserData() {
    }

    @Override
    public void decode(@NotNull ByteBuf buf) {
        buf.skipBytes(1);
        body = buf.readBytes(buf.readableBytes());
    }

    @Override
    public @NotNull ByteBuf encode() {
        ByteBuf buf = Utils.buffer();
        buf.writeByte(Constants.ID_USER_PACKET);
        buf.writeBytes(body);
        return buf;
    }
}
