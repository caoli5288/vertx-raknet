package com.github.caoli5288.vertx.raknet;

import com.github.caoli5288.vertx.raknet.message.Ack;
import com.github.caoli5288.vertx.raknet.message.AckRecord;
import com.github.caoli5288.vertx.raknet.message.Frame;
import com.github.caoli5288.vertx.raknet.message.FrameSetPacket;
import com.github.caoli5288.vertx.raknet.message.NAck;
import com.github.caoli5288.vertx.raknet.util.FrameJoiner;
import com.github.caoli5288.vertx.raknet.util.Utils;
import io.vertx.core.Handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class InboundBuffer implements Handler<FrameSetPacket> {

    private final Map<Integer, FrameJoiner> joiners = new HashMap<>();
    private final RakNetSession session;
    private int mSequence = -1;

    public InboundBuffer(RakNetSession session) {
        this.session = session;
    }

    @Override
    public void handle(FrameSetPacket packet) {
        int sq = packet.getSequence();
        int c = Utils.toUInt24(sq - mSequence);
        if (c < 1 || c > 0xffff) {// duplicate?
            return;
        }
        if (c == 1) {// yes
            for (Frame frame : packet.getFrames()) {
                handle(frame);
            }
        } else {
            // send n-ack
            AckRecord record = new AckRecord();
            record.setSequence(Utils.toUInt24(mSequence + 1));
            record.setSequence2(Utils.toUInt24(sq - 1));
            NAck nAck = new NAck();
            nAck.setRecords(Collections.singletonList(record));
            session.send0(nAck.encode());
        }
        mSequence = sq;
        // TODO buffered ack
        AckRecord record = new AckRecord();
        record.setSingle(true);
        record.setSequence(sq);
        Ack ack = new Ack();
        ack.setRecords(Collections.singletonList(record));
        session.send0(ack.encode());
    }

    private void handle(Frame frame) {
        if (frame.isFragmented()) {
            FrameJoiner joiner = joiners.computeIfAbsent(frame.getSplitterId(), s -> new FrameJoiner(frame.getFragmentSize()));
            joiner.handle(frame);
            if (joiner.isComplete()) {
                joiners.remove(frame.getSplitterId());
                session.handle0(joiner.join());
            }
        } else {// TODO buffered
            session.handle0(frame.getBody());
        }
    }
}
