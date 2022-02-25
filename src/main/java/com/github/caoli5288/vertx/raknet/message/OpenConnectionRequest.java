package com.github.caoli5288.vertx.raknet.message;

import com.github.caoli5288.vertx.raknet.Constants;
import com.github.caoli5288.vertx.raknet.util.RakNetCodecs;
import com.github.caoli5288.vertx.raknet.util.Utils;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Data
public class OpenConnectionRequest implements Serializable {

    private int version;
    private int mtu;

    public OpenConnectionRequest() {
    }

    public OpenConnectionRequest(ByteBuf buf) {
        decode(buf);
    }

    @Override
    public void decode(@NotNull ByteBuf buf) {
        buf.skipBytes(1);// id
        Utils.checkState(Arrays.equals(RakNetCodecs.readMagic(buf), Constants.OFFLINE_MESSAGE));
        version = buf.readByte();
        int s = buf.readableBytes();
        mtu = s + Constants.OPEN_CONNECTION_REQUEST_OVERHEAD;
        buf.skipBytes(s);
    }

    @Override
    public @NotNull ByteBuf encode() {
        ByteBuf buf = Utils.buffer();
        buf.writeByte(Constants.ID_OPEN_CONNECTION_REQUEST_1);
        buf.writeBytes(Constants.OFFLINE_MESSAGE);
        buf.writeByte(version);
        buf.writeZero(mtu - Constants.OPEN_CONNECTION_REQUEST_OVERHEAD);
        return buf;
    }
}
