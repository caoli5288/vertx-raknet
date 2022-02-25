package com.github.caoli5288.vertx.raknet.message;

import com.github.caoli5288.vertx.raknet.Constants;
import com.github.caoli5288.vertx.raknet.util.RakNetCodecs;
import com.github.caoli5288.vertx.raknet.util.Utils;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.Arrays;

@Data
@NoArgsConstructor
public class OpenConnectionRequest2 implements Serializable {

    private InetSocketAddress address;
    private int mtu;
    private long guid;
    private ByteBuf extras;

    public OpenConnectionRequest2(ByteBuf buf) {
        decode(buf);
    }

    @Override
    public void decode(@NotNull ByteBuf buf) {
        buf.skipBytes(1);// skip
        Utils.checkState(Arrays.equals(RakNetCodecs.readMagic(buf), Constants.OFFLINE_MESSAGE));
        address = RakNetCodecs.readAddress(buf);
        mtu = buf.readShort();
        guid = buf.readLong();
        extras = buf.readBytes(buf.readableBytes());
    }

    @Override
    public @NotNull ByteBuf encode() {
        ByteBuf buf = Utils.buffer();
        buf.writeByte(Constants.ID_OPEN_CONNECTION_REQUEST_2);
        buf.writeBytes(Constants.OFFLINE_MESSAGE);
        RakNetCodecs.writeAddress(buf, address);
        buf.writeShort(mtu);
        buf.writeLong(guid);
        if (extras != null) {
            buf.writeBytes(extras);
        }
        return buf;
    }
}
