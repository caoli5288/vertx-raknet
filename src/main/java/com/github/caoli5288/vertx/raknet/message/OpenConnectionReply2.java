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
public class OpenConnectionReply2 implements Serializable {

    private long guid;
    private InetSocketAddress address;
    private int mtu;

    public OpenConnectionReply2(ByteBuf buf) {
        decode(buf);
    }

    @Override
    public void decode(@NotNull ByteBuf buf) {
        buf.skipBytes(1);
        // check magic
        Utils.checkState(Arrays.equals(RakNetCodecs.readMagic(buf), Constants.OFFLINE_MESSAGE));
        guid = buf.readLong();
        address = RakNetCodecs.readAddress(buf);
        mtu = buf.readUnsignedShort();
        buf.skipBytes(1);
    }

    @Override
    public @NotNull ByteBuf encode() {
        ByteBuf buf = Utils.buffer();
        buf.writeByte(Constants.ID_OPEN_CONNECTION_REPLY_2);
        buf.writeBytes(Constants.OFFLINE_MESSAGE);
        buf.writeLong(guid);
        RakNetCodecs.writeAddress(buf, address);
        buf.writeShort(mtu);
        buf.writeBoolean(false);
        return buf;
    }
}
