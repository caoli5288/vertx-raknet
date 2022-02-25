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
public class IncompatibleProtocol implements Serializable {

    private int version;
    private long guid;

    public IncompatibleProtocol(ByteBuf buf) {
        decode(buf);
    }

    @Override
    public void decode(@NotNull ByteBuf buf) {
        buf.skipBytes(1);
        version = buf.readByte();
        Utils.checkState(Arrays.equals(RakNetCodecs.readMagic(buf), Constants.OFFLINE_MESSAGE));
        guid = buf.readLong();
    }

    @Override
    public @NotNull ByteBuf encode() {
        ByteBuf buf = Utils.buffer();
        buf.writeByte(Constants.ID_INCOMPATIBLE_PROTOCOL_VERSION);
        buf.writeByte(version);
        buf.writeBytes(Constants.OFFLINE_MESSAGE);
        buf.writeLong(guid);
        return buf;
    }
}
