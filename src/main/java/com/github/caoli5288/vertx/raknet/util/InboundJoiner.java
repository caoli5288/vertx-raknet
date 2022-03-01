package com.github.caoli5288.vertx.raknet.util;

import com.github.caoli5288.vertx.raknet.message.Frame;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

public class InboundJoiner {

    private final Map<Integer, Joiner> joiners = new HashMap<>();
    private int sequence = -1;

    public void handle(Frame frame) {
        int fs = frame.getSplitterId();
        int c = Utils.toUInt24(fs - sequence);
        if (c < 1 || c > 65535) {// duplicated segment
            return;
        }
        Joiner jo = joiners.computeIfAbsent(fs, __ -> new Joiner(frame.getSplitSize()))
                .handle(frame);
        if (jo.isComplete()) {
            frame.setBody(jo.join());
            frame.setSplit(false);
            if (c == 1) {// bump mSplitterId and cleanup
                joiners.remove(fs);
                sequence = fs;
                cleanup();
            }
        }
    }

    private void cleanup() {
        if (joiners.isEmpty()) {
            return;
        }
        for (; isComplete(joiners.get(sequence + 1)); sequence++) {
            joiners.remove(sequence + 1);
        }
    }

    static boolean isComplete(Joiner joiner) {
        return joiner != null && joiner.isComplete();
    }

    static class Joiner {

        private final Frame[] frames;
        private int c;

        Joiner(int size) {
            frames = new Frame[size];
        }

        public Joiner handle(Frame frame) {
            if (!isComplete()) {
                int splitId = frame.getSplitId();
                Frame old = frames[splitId];
                if (old == null) {
                    frames[splitId] = frame;
                    c++;
                }
            }
            return this;
        }

        public ByteBuf join() {
            ByteBuf buf = Utils.buffer();
            for (Frame value : frames) {
                buf.writeBytes(value.getBody());
            }
            return buf;
        }

        public boolean isComplete() {
            return c == frames.length;
        }
    }
}
