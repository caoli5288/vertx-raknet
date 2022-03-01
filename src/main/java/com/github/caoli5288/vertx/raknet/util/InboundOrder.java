package com.github.caoli5288.vertx.raknet.util;

import com.github.caoli5288.vertx.raknet.message.Frame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InboundOrder {

    private final Map<Integer, Frame> frames = new HashMap<>();
    private int sequence = -1;

    @Nullable
    public List<Frame> handle(@NotNull Frame frame) {
        int fs = frame.getSequenceId();
        int c = Utils.toUInt24(fs - sequence);
        if (c < 1 || c > 65535) {
            return null;
        }
        if (c == 1) {// yes
            List<Frame> out = new ArrayList<>();
            out.add(frame);
            for (; frames.containsKey(fs + 1); fs++) {
                out.add(frames.remove(fs + 1));
            }
            sequence = fs;
            return out;
        } else {
            frames.put(fs, frame);
        }
        return null;
    }
}
