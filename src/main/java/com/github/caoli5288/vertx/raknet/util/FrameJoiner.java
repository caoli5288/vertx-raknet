package com.github.caoli5288.vertx.raknet.util;

import com.github.caoli5288.vertx.raknet.message.Frame;
import io.netty.buffer.ByteBuf;

public class FrameJoiner {

    private final Frame[] frames;
    private int c;

    public FrameJoiner(int count) {
        frames = new Frame[count];
    }

    public void join(Frame frame) {
        int splitId = frame.getSplitId();
        Frame old = frames[splitId];
        if (old != null) {
            return;
        }
        frames[splitId] = frame;
        c++;
        if (c == frames.length) {// yes
            ByteBuf buf = Utils.buffer();
            for (Frame value : frames) {
                buf.writeBytes(value.getBody());
            }
            frame.setSplit(false);
            frame.setBody(buf);
        }
    }
}
