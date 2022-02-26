package com.github.caoli5288.vertx.raknet;

import com.github.caoli5288.vertx.raknet.message.Ack;
import com.github.caoli5288.vertx.raknet.message.ConnectedPing;
import com.github.caoli5288.vertx.raknet.message.ConnectedPong;
import com.github.caoli5288.vertx.raknet.message.ConnectionRequest;
import com.github.caoli5288.vertx.raknet.message.ConnectionRequestAccepted;
import com.github.caoli5288.vertx.raknet.message.Disconnect;
import com.github.caoli5288.vertx.raknet.message.FrameSetPacket;
import com.github.caoli5288.vertx.raknet.message.IncompatibleProtocol;
import com.github.caoli5288.vertx.raknet.message.NAck;
import com.github.caoli5288.vertx.raknet.message.NewIncomingConnection;
import com.github.caoli5288.vertx.raknet.message.OpenConnectionReply;
import com.github.caoli5288.vertx.raknet.message.OpenConnectionReply2;
import com.github.caoli5288.vertx.raknet.message.OpenConnectionRequest;
import com.github.caoli5288.vertx.raknet.message.OpenConnectionRequest2;
import com.github.caoli5288.vertx.raknet.message.UnconnectedPing;
import com.github.caoli5288.vertx.raknet.message.UnconnectedPong;
import com.github.caoli5288.vertx.raknet.message.UserData;
import com.github.caoli5288.vertx.raknet.util.Utils;
import io.netty.buffer.ByteBuf;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.net.SocketAddress;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class RakNetSession implements Handler<DatagramPacket> {

    private final RakNet net;
    @Getter
    private final SocketAddress address;
    @Getter
    private final Mode mode;
    @Getter
    private State state = State.NEW;
    @Getter
    private int version = Constants.SERVER_PROTOCOL_VERSION;
    // handlers
    private Handler<UnconnectedPong> pingHandler;
    private Handler<Buffer> dataHandler;
    private Handler<RakNetSession> connectedHandler;
    private Handler<RakNetSession> closedHandler;
    // IOs
    private RakNetConnector connector;
    private InboundBuffer inbound;
    private OutboundBuffer outbound;

    RakNetSession(RakNet net, SocketAddress address, Mode mode) {
        this.net = net;
        this.address = address;
        this.mode = mode;// TODO check client mode in some session
    }

    @Override
    public void handle(DatagramPacket packet) {
        // parse packets
        ByteBuf data = packet.data().getByteBuf();
        int f = data.getUnsignedByte(0);
        switch (f) {
            case Constants.ID_UNCONNECTED_PING:// 1
                handle(new UnconnectedPing(data));
                break;
            case Constants.ID_OPEN_CONNECTION_REQUEST_1:// 5
                handle(new OpenConnectionRequest(data));
                break;
            case Constants.ID_OPEN_CONNECTION_REPLY_1:// 6
                handle(new OpenConnectionReply(data));
                break;
            case Constants.ID_OPEN_CONNECTION_REQUEST_2:// 7
                handle(new OpenConnectionRequest2(data));
                break;
            case Constants.ID_OPEN_CONNECTION_REPLY_2:// 7
                handle(new OpenConnectionReply2(data));
                break;
            case Constants.ID_DISCONNECT:// 21
                handle(new Disconnect(data));
                break;
            case Constants.ID_INCOMPATIBLE_PROTOCOL_VERSION:// 25
                handle(new IncompatibleProtocol(data));
                break;
            case Constants.ID_RN_UNCONNECTED_PONG:// 28
                handle(new UnconnectedPong(data));
                break;
            case Constants.ID_RN_FRAME_SET_PACKET_START:// start
            case 129:
            case 130:
            case 131:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case Constants.ID_RN_FRAME_SET_PACKET_END:// end
                handle(new FrameSetPacket(data));
                break;
            case Constants.ID_RN_N_ACK:// 160
                handle(new NAck(data));
            case Constants.ID_RN_ACK:// 192
                handle(new Ack(data));
        }
        Utils.checkState(data.readableBytes() == 0);
    }

    private void handle(Ack ack) {
        outbound.handle(ack);
    }

    private void handle(NAck nAck) {
        outbound.handle(nAck);
    }

    private void handle(IncompatibleProtocol protocol) {
        if (connector != null) {
            connector.fail("Server protocol version is " + protocol.getVersion());
        }
        close();// close
    }

    private void handle(UnconnectedPong pong) {
        if (pingHandler != null) {
            pingHandler.handle(pong);
        }
    }

    private void handle(Disconnect disconnect) {
        close();
    }

    private void handle(FrameSetPacket packet) {
        if (state == State.ESTABLISHED || state == State.CONNECTING) {
            inbound.handle(packet);
        }
    }

    private void handle(OpenConnectionRequest2 request2) {
        Utils.checkState(state == State.SYNC && mode == Mode.SERVER);
        state = State.CONNECTING;
        // reply
        int mtu = request2.getMtu();
        OpenConnectionReply2 reply2 = new OpenConnectionReply2();
        reply2.setGuid(net.getOptions().getGuid());
        reply2.setAddress(Utils.resolve(address));
        reply2.setMtu(request2.getMtu());
        send0(reply2.encode());
        // setup IO buffers
        inbound = new InboundBuffer(this);
        outbound = new OutboundBuffer(this, mtu);
    }

    private void handle(OpenConnectionReply2 reply2) {
        Utils.checkState(state == State.SYNC && mode == Mode.CLIENT);
        state = State.CONNECTING;
        // setup IO buffers
        inbound = new InboundBuffer(this);
        outbound = new OutboundBuffer(this, reply2.getMtu());
        ConnectionRequest request = new ConnectionRequest();
        request.setGuid(getOptions().getGuid());
        request.setTime(System.currentTimeMillis() / 1000);
        outbound.send(request.encode());
    }

    private void handle(OpenConnectionRequest request) {
        Utils.checkState(state == State.NEW && mode == Mode.SERVER);
        // Cannot connect to old server
        if (request.getVersion() > Constants.SERVER_PROTOCOL_VERSION) {
            IncompatibleProtocol protocol = new IncompatibleProtocol();
            protocol.setGuid(getOptions().getGuid());
            protocol.setVersion(version);
            send0(protocol.encode());
            close();
        } else {
            state = State.SYNC;
            // Use client version for compatible codecs
            version = request.getVersion();
            OpenConnectionReply reply = new OpenConnectionReply();
            reply.setGuid(net.getOptions().getGuid());
            reply.setMtu(request.getMtu());
            send0(reply.encode());
        }
    }

    private void handle(OpenConnectionReply reply) {
        Utils.checkState(state == State.OPEN && mode == Mode.CLIENT);
        state = State.SYNC;
        connector.setMtu(reply.getMtu());
        connector.setCd(2);
        connector.connect2();
    }

    private void handle(UnconnectedPing ping) {
        UnconnectedPong pong = new UnconnectedPong();
        pong.setGuid(net.getOptions().getGuid());
        pong.setTime(ping.getTime());
        if (pingHandler != null) {
            pingHandler.handle(pong);
        }
        send0(pong.encode());
    }

    void handle0(ByteBuf data) {
        int f = data.getUnsignedByte(0);
        switch (f) {
            case Constants.ID_CONNECTED_PING:// 0
                handle0(new ConnectedPing(data));
                break;
            case Constants.ID_CONNECTED_PONG:// 3
                break;
            case Constants.ID_CONNECTION_REQUEST:// 9
                handle0(new ConnectionRequest(data));
                break;
            case Constants.ID_CONNECTION_REQUEST_ACCEPTED:// 16
                handle0(new ConnectionRequestAccepted(data));
                break;
            case Constants.ID_NEW_INCOMING_CONNECTION:// 19
                handle0(new NewIncomingConnection(data));
                break;
            case Constants.ID_DISCONNECT:// 21
                close();
                break;
            case Constants.ID_USER_PACKET:
                handle0(new UserData(data));
        }
    }

    private void handle0(UserData packet) {
        Utils.checkState(state == State.ESTABLISHED, "State " + state);
        if (dataHandler != null) {
            dataHandler.handle(Buffer.buffer(packet.getBody()));
        }
    }

    private void handle0(NewIncomingConnection handshake) {
        Utils.checkState(state == State.CONNECTING && mode == Mode.SERVER);
        state = State.ESTABLISHED;
        if (connectedHandler != null) {
            connectedHandler.handle(this);
        }
    }

    private void handle0(ConnectedPing ping) {
        ConnectedPong pong = new ConnectedPong();
        pong.setTime(System.currentTimeMillis() / 1000);
        outbound.send(pong.encode());
    }

    private void handle0(ConnectionRequestAccepted handshake) {
        Utils.checkState(state == State.CONNECTING && mode == Mode.CLIENT);
        state = State.ESTABLISHED;
        NewIncomingConnection pong = new NewIncomingConnection();
        pong.setAddress(handshake.getAddress());
        pong.setTime(handshake.getTime2());
        pong.setTime2(System.currentTimeMillis() / 1000);
        connector.complete();
    }

    private void handle0(ConnectionRequest request) {
        Utils.checkState(state == State.CONNECTING && mode == Mode.SERVER);
        ConnectionRequestAccepted handshake = new ConnectionRequestAccepted();
        handshake.setAddress(Utils.resolve(address));
        handshake.setTime(request.getTime());
        handshake.setTime2(System.currentTimeMillis() / 1000);
        outbound.send(handshake.encode());
    }

    void send0(ByteBuf buf) {
        net.send(address, buf, event -> {
            if (!event.succeeded()) {
                close();
            }
        });
    }

    public void send(@NotNull UserData data) {
        Utils.checkState(state == State.ESTABLISHED);
        outbound.send(data.encode());
    }

    public void send(@NotNull Buffer buffer) {
        UserData data = new UserData();
        data.setBody(buffer.getByteBuf());
        send(data);
    }

    public RakNetSession connect(@NotNull Handler<AsyncResult<RakNetSession>> handler) {
        Utils.checkState(state == State.NEW, "State is " + state);
        state = State.OPEN;
        connector = new RakNetConnector(this, handler);
        connector.connect();
        return this;
    }

    public RakNetSession ping(@NotNull Handler<AsyncResult<UnconnectedPong>> handler) {
        // check state is NEW
        Utils.checkState(state == State.NEW, "State is " + state);
        Promise<UnconnectedPong> f = Promise.promise();
        // ping handler
        pingHandler = event -> {
            if (f.tryComplete(event)) {
                handler.handle(f.future());
            }
        };
        // setup timeout task
        net.getVertx().setTimer(4000, event -> {
            if (f.tryFail("timeout")) {
                handler.handle(f.future());
            }
        });
        UnconnectedPing ping = new UnconnectedPing();
        ping.setTime(System.currentTimeMillis() / 1000);
        ping.setGuid(net.getOptions().getGuid());
        send0(ping.encode());
        return this;
    }

    public RakNetSession pingHandler(Handler<UnconnectedPong> pingHandler) {
        this.pingHandler = pingHandler;
        return this;
    }

    public RakNetSession dataHandler(Handler<Buffer> dataHandler) {
        this.dataHandler = dataHandler;
        return this;
    }

    public RakNetSession connectedHandler(Handler<RakNetSession> connectedHandler) {
        this.connectedHandler = connectedHandler;
        return this;
    }

    public RakNetSession closedHandler(Handler<RakNetSession> closedHandler) {
        this.closedHandler = closedHandler;
        return this;
    }

    public void close() {
        if (state == State.CLOSED) {
            return;
        }
        send0(new Disconnect().encode());
        state = State.CLOSED;
        net.close(address);
        if (closedHandler != null) {
            closedHandler.handle(this);
        }
    }

    public Vertx getVertx() {
        return net.getVertx();
    }

    public RakNetOptions getOptions() {
        return net.getOptions();
    }

    public boolean isClosed() {
        return state == State.CLOSED;
    }

    public enum State {// ESTABLISHED
        NEW,
        OPEN,
        SYNC,
        CONNECTING,
        ESTABLISHED,
        CLOSED
    }

    public enum Mode {
        CLIENT,
        SERVER
    }
}
