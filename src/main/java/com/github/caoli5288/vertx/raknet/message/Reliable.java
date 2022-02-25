package com.github.caoli5288.vertx.raknet.message;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum Reliable {

    UNRELIABLE(false, false, false),
    UNRELIABLE_SEQUENCED(false, true, true),
    RELIABLE(true, false, false),
    RELIABLE_ORDERED(true, true, false),
    RELIABLE_SEQUENCED(true, true, true),
    UNRELIABLE_ACK(false, false, false),
    RELIABLE_ACK(true, false, false),
    RELIABLE_ORDERED_ACK(true, true, false);

    private static final Map<Integer, Reliable> BY_ID = new HashMap<>();

    static {
        for (Reliable mode : Reliable.values()) {
            BY_ID.put(mode.ordinal(), mode);
        }
    }

    private final boolean reliable;
    private final boolean ordered;
    private final boolean sequenced;

    Reliable(boolean reliable, boolean ordered, boolean sequenced) {
        this.reliable = reliable;
        this.ordered = ordered;
        this.sequenced = sequenced;
    }

    public static Reliable valueOf(int id) {
        return BY_ID.get(id);
    }
}
