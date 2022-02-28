package com.github.caoli5288.vertx.raknet.message;

import com.github.caoli5288.vertx.raknet.Constants;
import com.github.caoli5288.vertx.raknet.util.Utils;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Data
public class FrameSetPacket implements Serializable {

    private int sequence;
    private List<Frame> frames;

    public FrameSetPacket() {
    }

    public FrameSetPacket(ByteBuf buf) {
        decode(buf);
    }

    @Override
    public void decode(@NotNull ByteBuf buf) {
        buf.skipBytes(1);// id
        sequence = buf.readUnsignedMediumLE();
        frames = new ArrayList<>();
        while (buf.isReadable()) {
            Frame frame = new Frame();
            frame.decode(buf);
            frames.add(frame);
        }
    }

    @Override
    public @NotNull ByteBuf encode() {
        ByteBuf buf = Utils.buffer();
        buf.writeByte(Constants.ID_RN_FRAME_SET_PACKET_4);
        buf.writeMediumLE(sequence);
        for (Frame frame : frames) {
            Frame.encode(buf, frame);
        }
        return buf;
    }
}
