package com.github.caoli5288.vertx.raknet.message;

import com.github.caoli5288.vertx.raknet.Constants;
import com.github.caoli5288.vertx.raknet.util.RakNetCodecs;
import com.github.caoli5288.vertx.raknet.util.Utils;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

@Data
public class NewIncomingConnection implements Serializable {

    private InetSocketAddress address;
    private long time;
    private long time2;

    public NewIncomingConnection(ByteBuf data) {
        decode(data);
    }

    public NewIncomingConnection() {
    }

    @Override
    public void decode(@NotNull ByteBuf buf) {
        buf.skipBytes(1);
        address = RakNetCodecs.readAddress(buf);
        for (int i = 0; i < 10; i++) {
            RakNetCodecs.readAddress(buf);
        }
        time = buf.readLong();
        time2 = buf.readLong();
    }

    @Override
    public @NotNull ByteBuf encode() {
        ByteBuf buf = Utils.buffer();
        buf.writeByte(Constants.ID_NEW_INCOMING_CONNECTION);
        RakNetCodecs.writeAddress(buf, address);
        for (int i = 0; i < 10; i++) {
            RakNetCodecs.writeAddress(buf, Constants.SYSTEM_ADDRESS);
        }
        buf.writeLong(time);
        buf.writeLong(time2);
        return buf;
    }
}
