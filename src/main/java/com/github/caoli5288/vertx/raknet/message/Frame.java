package com.github.caoli5288.vertx.raknet.message;

import com.github.caoli5288.vertx.raknet.util.Utils;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class Frame implements Serializable {

    private Reliable reliable;
    private int frameId;
    private int sequenceId;
    private int orderId;
    private int orderChannel;
    // fragmented
    private boolean fragmented;
    private int fragmentSize;
    private int splitterId;
    private int fragmentId;
    private ByteBuf body;

    @Override
    public void decode(@NotNull ByteBuf buf) {
        int flags = buf.readUnsignedByte();
        reliable = Reliable.valueOf(flags >> 5);
        fragmented = (flags & 16) == 16;
        int c = buf.readUnsignedShort() / 8;// body lengths
        if (reliable.isReliable()) {
            frameId = buf.readUnsignedMediumLE();
        }
        if (reliable.isSequenced()) {
            sequenceId = buf.readUnsignedMediumLE();
        }
        if (reliable.isOrdered()) {
            orderId = buf.readUnsignedMediumLE();
            orderChannel = buf.readByte();
        }
        if (fragmented) {
            fragmentSize = buf.readInt();
            splitterId = buf.readShort();
            fragmentId = buf.readInt();
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
        int flags = frame.getReliable().ordinal() << 5;
        if (frame.isFragmented()) {
            flags = Utils.setBitTo1(flags, 4);
        }
        buf.writeByte(flags);
        buf.writeShort(frame.getBody().readableBytes() << 3);
        if (frame.getReliable().isReliable()) {
            buf.writeMediumLE(frame.frameId);
        }
        if (frame.getReliable().isSequenced()) {
            buf.writeMediumLE(frame.sequenceId);
        }
        if (frame.getReliable().isOrdered()) {
            buf.writeMediumLE(frame.orderId);
            buf.writeByte(frame.orderChannel);
        }
        if (frame.isFragmented()) {
            buf.writeInt(frame.fragmentSize);
            buf.writeShort(frame.splitterId);
            buf.writeInt(frame.fragmentId);
        }
        buf.writeBytes(frame.getBody());
    }
}
