package com.github.caoli5288.vertx.raknet.message;

import com.github.caoli5288.vertx.raknet.Constants;
import com.github.caoli5288.vertx.raknet.util.RakNetCodecs;
import com.github.caoli5288.vertx.raknet.util.Utils;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

@Data
@NoArgsConstructor
public class ConnectionRequestAccepted implements Serializable {

    private InetSocketAddress address;// client address
    private long time;// client time
    private long time2;

    public ConnectionRequestAccepted(ByteBuf buf) {
        decode(buf);
    }

    @Override
    public void decode(@NotNull ByteBuf buf) {
        buf.skipBytes(1);// id
        address = RakNetCodecs.readAddress(buf);
        buf.readShort();
        for (int i = 0; i < 10; i++) {
            RakNetCodecs.readAddress(buf);// skip
        }
        time = buf.readLong();
        time2 = buf.readLong();
    }

    @Override
    public @NotNull ByteBuf encode() {
        ByteBuf buf = Utils.buffer();
        buf.writeByte(Constants.ID_CONNECTION_REQUEST_ACCEPTED);
        RakNetCodecs.writeAddress(buf, address);
        buf.writeShort(0);// ?
        for (int i = 0; i < 10; i++) {
            RakNetCodecs.writeAddress(buf, Constants.SYSTEM_ADDRESS);
        }
        buf.writeLong(time);
        buf.writeLong(time2);
        return buf;
    }
}
