package com.github.caoli5288.vertx.raknet.message;

import com.github.caoli5288.vertx.raknet.util.Utils;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class Frame implements Serializable {

    private Reliability reliability;
    private int id;
    private int sequenceId;
    private int channel;
    // fragmented
    private boolean split;
    private int splitSize;
    private int splitterId;
    private int splitId;
    private ByteBuf body;

    @Override
    public void decode(@NotNull ByteBuf buf) {
        int flags = buf.readUnsignedByte();
        reliability = Reliability.valueOf(flags >> 5);
        split = (flags & 16) == 16;
        int c = buf.readUnsignedShort() / 8;// body lengths
        if (reliability.isReliable()) {
            id = buf.readUnsignedMediumLE();
        }
        if (reliability.isSequenced()) {
            sequenceId = buf.readUnsignedMediumLE();
        }
        if (reliability.isOrdered()) {
            sequenceId = buf.readUnsignedMediumLE();
            channel = buf.readByte();
        }
        if (split) {
            splitSize = buf.readInt();
            splitterId = buf.readShort();
            splitId = buf.readInt();
        }
        body = buf.readBytes(c);
    }

    @Override
    public @NotNull ByteBuf encode() {
        ByteBuf buf = Utils.buffer();
        encode(buf, this);
        return buf;
    }

    public static void encode(ByteBuf buf, Frame frame) {
        int flags = frame.getReliability().ordinal() << 5;
        if (frame.isSplit()) {
            flags = Utils.setBitTo1(flags, 4);
        }
        buf.writeByte(flags);
        buf.writeShort(frame.getBody().readableBytes() << 3);
        if (frame.getReliability().isReliable()) {
            buf.writeMediumLE(frame.getId());
        }
        if (frame.getReliability().isSequenced()) {
            buf.writeMediumLE(frame.sequenceId);
        }
        if (frame.getReliability().isOrdered()) {
            buf.writeMediumLE(frame.sequenceId);
            buf.writeByte(frame.channel);
        }
        if (frame.isSplit()) {
            buf.writeInt(frame.splitSize);
            buf.writeShort(frame.splitterId);
            buf.writeInt(frame.splitId);
        }
        buf.writeBytes(frame.getBody());
    }
}
