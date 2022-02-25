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

    public void handle(Frame frame) {
        map.put(frame.getFragmentId(), frame);
    }

    public boolean isComplete() {
        return map.size() == count;
    }

    public ByteBuf join() {
        ByteBuf buffer = Utils.buffer();
        for (int i = 0; i < count; i++) {
            buffer.writeBytes(map.get(i).getBody());
        }
        return buffer;
    }
}
