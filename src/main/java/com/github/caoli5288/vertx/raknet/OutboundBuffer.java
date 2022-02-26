package com.github.caoli5288.vertx.raknet;

import com.github.caoli5288.vertx.raknet.message.Ack;
import com.github.caoli5288.vertx.raknet.message.AckRecord;
import com.github.caoli5288.vertx.raknet.message.Frame;
import com.github.caoli5288.vertx.raknet.message.FrameSetPacket;
import com.github.caoli5288.vertx.raknet.message.NAck;
import com.github.caoli5288.vertx.raknet.message.Reliable;
import com.github.caoli5288.vertx.raknet.util.Utils;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class OutboundBuffer {

    private final Map<Integer, FrameSetPacket> cache = new HashMap<>();
    private final RakNetSession session;
    private final int mtu;
    private int seq;
    private int frameId;
    private int splitterId;

    public OutboundBuffer(RakNetSession session, int mtu) {
        this.session = session;
        this.mtu = mtu;
    }

    public void send(ByteBuf buf) {
        int dataSize = mtu - Constants.FRAME_SET_OVERHEAD;
        if (dataSize >= buf.readableBytes()) {
            FrameSetPacket fp = new FrameSetPacket();
            fp.setSequence(seq());
            Frame frame = frame();
            frame.setBody(buf);
            fp.setFrames(Collections.singletonList(frame));
            send(fp);
        } else {
            int count = Utils.ceil(buf.readableBytes(), dataSize);
            for (int i = 0; i < count; i++) {
                FrameSetPacket fp = new FrameSetPacket();
                fp.setSequence(seq());
                Frame frame = frame();
                // fragmented
                frame.setSplit(true);
                frame.setSplitterId(splitterId++);
                frame.setSplitId(i);
                frame.setSplitSize(count);
                // body
                if (buf.readableBytes() <= dataSize) {
                    frame.setBody(buf.readSlice(buf.readableBytes()));
                } else {
                    frame.setBody(buf.readSlice(dataSize));
                }
                fp.setFrames(Collections.singletonList(frame));
                send(fp);
            }
        }
    }

    @NotNull
    private Frame frame() {
        int j = frameId++;
        Frame frame = new Frame();
        frame.setReliable(Reliable.RELIABLE);
        frame.setId(j);
        return frame;
    }

    private int seq() {
        int old = seq;
        seq = Utils.toUInt24(seq + 1);
        return old;
    }

    private void send(FrameSetPacket fp) {
        cache.put(fp.getSequence(), fp);
        session.send0(fp.encode());
    }

    void sendOld(int old) {
        FrameSetPacket fp = cache.remove(old);
        if (fp != null) {
            fp.setSequence(seq());
            send(fp);
        }
    }

    public void handle(NAck nAck) {
        for (AckRecord record : nAck.getRecords()) {
            if (record.isSingle()) {
                sendOld(record.getSequence());
            } else {
                int j = record.getSequence2();
                for (int i = record.getSequence(); i <= j; i++) {
                    sendOld(i);
                }
            }
        }
    }

    public void handle(Ack ack) {
        for (AckRecord record : ack.getRecords()) {
            if (record.isSingle()) {
                cache.remove(record.getSequence());
            } else {
                int j = record.getSequence2();
                for (int i = record.getSequence(); i <= j; i++) {
                    cache.remove(i);
                }
            }
        }
    }
}
