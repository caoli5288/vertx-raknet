package com.github.caoli5288.vertx.raknet.util;

import com.github.caoli5288.vertx.raknet.message.Frame;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

public class FrameJoiner {

    private final Map<Integer, Frame> map = new HashMap<>();
    private final int count;

    public FrameJoiner(int count) {
        this.count = count;
    }

    public void join(Frame frame) {
        map.put(frame.getSplitId(), frame);
        if (map.size() == count) {
            ByteBuf buf = Utils.buffer();
            for (int i = 0; i < count; i++) {
                buf.writeBytes(map.get(i).getBody());
            }
            frame.setSplit(false);
            frame.setBody(buf);
        }
    }
}
