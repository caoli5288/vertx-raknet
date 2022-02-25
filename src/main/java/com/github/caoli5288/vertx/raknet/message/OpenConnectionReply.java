package com.github.caoli5288.vertx.raknet.message;

import com.github.caoli5288.vertx.raknet.Constants;
import com.github.caoli5288.vertx.raknet.util.RakNetCodecs;
import com.github.caoli5288.vertx.raknet.util.Utils;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Data
@NoArgsConstructor
public class OpenConnectionReply implements Serializable {

    private long guid;
    private int mtu;

    public OpenConnectionReply(ByteBuf buf) {
        decode(buf);
    }

    @Override
    public void decode(@NotNull ByteBuf buf) {
        buf.skipBytes(1);
        // check magic
        Utils.checkState(Arrays.equals(RakNetCodecs.readMagic(buf), Constants.OFFLINE_MESSAGE));
        guid = buf.readLong();
        buf.readBoolean();
        mtu = buf.readUnsignedShort();
    }

    @Override
    public @NotNull ByteBuf encode() {
        ByteBuf buf = Utils.buffer();
        buf.writeByte(Constants.ID_OPEN_CONNECTION_REPLY_1);
        buf.writeBytes(Constants.OFFLINE_MESSAGE);
        buf.writeLong(guid);
        buf.writeBoolean(false);
        buf.writeShort(mtu);
        return buf;
    }
}
