package com.github.caoli5288.vertx.raknet.message;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum Reliability {

    UNRELIABLE(false, false, false),
    UNRELIABLE_SEQUENCED(false, true, true),
    RELIABLE(true, false, false),
    RELIABLE_ORDERED(true, true, false),
    RELIABLE_SEQUENCED(true, true, true),
    UNRELIABLE_ACK(false, false, false),
    RELIABLE_ACK(true, false, false),
    RELIABLE_ORDERED_ACK(true, true, false);

    private static final List<Reliability> BY_ID;

    static {
        BY_ID = Arrays.asList(values());
    }

    private final boolean reliable;
    private final boolean ordered;
    private final boolean sequenced;

    Reliability(boolean reliable, boolean ordered, boolean sequenced) {
        this.reliable = reliable;
        this.ordered = ordered;
        this.sequenced = sequenced;
    }

    public static Reliability valueOf(int id) {
        return BY_ID.get(id);
    }
}
