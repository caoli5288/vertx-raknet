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
public class UnconnectedPong implements Serializable {

    private String info = "";
    private long time;
    private long guid;

    public UnconnectedPong(ByteBuf buf) {
        decode(buf);
    }

    @Override
    public void decode(@NotNull ByteBuf buf) {
        buf.skipBytes(1);
        time = buf.readLong();
        guid = buf.readLong();
        Utils.checkState(Arrays.equals(RakNetCodecs.readMagic(buf), Constants.OFFLINE_MESSAGE));
        info = RakNetCodecs.readString(buf);
    }

    @Override
    public @NotNull ByteBuf encode() {
        ByteBuf buf = Utils.buffer();
        buf.writeByte(Constants.ID_RN_UNCONNECTED_PONG);
        buf.writeLong(time);
        buf.writeLong(guid);
        RakNetCodecs.writeMagic(buf);
        RakNetCodecs.writeString(buf, info);
        return buf;
    }
}
