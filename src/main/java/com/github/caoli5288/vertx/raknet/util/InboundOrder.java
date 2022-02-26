package com.github.caoli5288.vertx.raknet.util;

import com.github.caoli5288.vertx.raknet.message.Frame;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InboundOrder {

    private final Map<Integer, Frame> frames = new HashMap<>();
    private int sequence = -1;

    @NotNull
    public List<Frame> handle(@NotNull Frame frame) {
        int seq = frame.getSequenceId();
        int c = Utils.toUInt24(seq - sequence);
        if (c > 0 && c <= 65535) {
            if (c == 1) {// yes
                List<Frame> out = new ArrayList<>();
                out.add(frame);
                for (Frame f; (f = frames.get(seq + 1)) != null; seq++) {
                    out.add(f);
                }
                sequence = seq;
                return out;
            } else {
                frames.put(seq, frame);
            }
        }
        return Collections.emptyList();
    }
}
