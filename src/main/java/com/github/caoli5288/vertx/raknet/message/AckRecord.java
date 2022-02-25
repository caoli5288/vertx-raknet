package com.github.caoli5288.vertx.raknet.message;

import com.github.caoli5288.vertx.raknet.util.Utils;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class AckRecord implements Serializable {

    private boolean single;
    private int sequence;
    private int sequence2;

    @Override
    public void decode(@NotNull ByteBuf buf) {
        single = buf.readBoolean();
        sequence = buf.readUnsignedMediumLE();
        if (!single) {
            sequence2 = buf.readUnsignedMediumLE();
        }
    }

    @Override
    public @NotNull ByteBuf encode() {
        ByteBuf buf = Utils.buffer();
        encode(buf, this);
        return buf;
    }

    public static void encode(ByteBuf buf, AckRecord record) {
        buf.writeBoolean(record.single);
        buf.writeMediumLE(record.sequence);
        if (!record.single) {
            buf.writeMediumLE(record.sequence2);
        }
    }
}
