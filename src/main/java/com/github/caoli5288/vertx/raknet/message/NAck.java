package com.github.caoli5288.vertx.raknet.message;

import com.github.caoli5288.vertx.raknet.Constants;
import com.github.caoli5288.vertx.raknet.util.Utils;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class NAck implements Serializable {

    private List<AckRecord> records;

    public NAck(ByteBuf buf) {
        decode(buf);
    }

    @Override
    public void decode(@NotNull ByteBuf buf) {
        buf.skipBytes(1);// id
        int c = buf.readShort();
        records = new ArrayList<>();
        for (int i = 0; i < c; i++) {
            AckRecord record = new AckRecord();
            record.decode(buf);
            records.add(record);
        }
    }

    @Override
    public @NotNull ByteBuf encode() {
        ByteBuf buf = Utils.buffer();
        buf.writeByte(Constants.ID_RN_N_ACK);
        buf.writeShort(records.size());
        for (AckRecord record : records) {
            AckRecord.encode(buf, record);
        }
        return buf;
    }
}
