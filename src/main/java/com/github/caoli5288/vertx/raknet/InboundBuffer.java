package com.github.caoli5288.vertx.raknet;

import com.github.caoli5288.vertx.raknet.message.Ack;
import com.github.caoli5288.vertx.raknet.message.AckRecord;
import com.github.caoli5288.vertx.raknet.message.Frame;
import com.github.caoli5288.vertx.raknet.message.FrameSetPacket;
import com.github.caoli5288.vertx.raknet.message.NAck;
import com.github.caoli5288.vertx.raknet.message.Reliable;
import com.github.caoli5288.vertx.raknet.util.FrameJoiner;
import com.github.caoli5288.vertx.raknet.util.InboundOrder;
import com.github.caoli5288.vertx.raknet.util.Utils;
import io.vertx.core.Handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class InboundBuffer implements Handler<FrameSetPacket> {

    private final Map<Integer, FrameJoiner> joiners = new HashMap<>();
    private final List<InboundOrder> orders = new ArrayList<>();
    private final InboundOrder sequencer = new InboundOrder();
    private final RakNetSession session;
    private int mSequence = -1;

    public InboundBuffer(RakNetSession session) {
        this.session = session;
        for (int i = 0; i < 8; i++) {
            orders.add(new InboundOrder());
        }
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
        if (frame.isSplit()) {
            FrameJoiner joiner = joiners.computeIfAbsent(frame.getSplitterId(), s -> new FrameJoiner(frame.getSplitSize()));
            joiner.join(frame);
            if (!frame.isSplit()) {
                joiners.remove(frame.getSplitterId());
                handle2(frame);
            }
        } else {
            handle2(frame);
        }
    }

    private void handle2(Frame frame) {
        InboundOrder order = getOrder(frame);
        if (order == null) {
            session.handle0(frame.getBody());
        } else {
            List<Frame> frames = order.handle(frame);
            if (!frames.isEmpty()) {
                for (Frame f : frames) {
                    session.handle0(f.getBody());
                }
            }
        }
    }

    private InboundOrder getOrder(Frame frame) {
        Reliable reliable = frame.getReliable();
        if (reliable.isOrdered()) {
            return orders.get(frame.getChannel());
        }
        if (reliable.isSequenced()) {
            return sequencer;
        }
        return null;
    }
}
