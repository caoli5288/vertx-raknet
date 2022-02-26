package com.github.caoli5288.vertx.raknet;

import com.github.caoli5288.vertx.raknet.util.Utils;
import io.netty.buffer.ByteBuf;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.net.SocketAddress;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class RakNet {

    private final Map<SocketAddress, RakNetSession> sessions = new HashMap<>();
    private final DatagramSocket so;
    @Getter
    private final Vertx vertx;
    @Getter
    private final RakNetOptions options;
    // states
    @Getter
    private RakNetSession session;// client mode
    private Handler<RakNetSession> connectHandler;
    @Getter(AccessLevel.PACKAGE)
    private Context context;

    RakNet(Vertx vertx, RakNetOptions options) {
        this.vertx = vertx;
        this.options = options.copy();
        so = vertx.createDatagramSocket(options.getSocketOptions());
    }

    public RakNet listen(String host, int port, Handler<AsyncResult<RakNet>> handler) {
        Utils.checkState(so.localAddress() == null, "Connected so");
        so.listen(port, host, event -> {
            if (event.succeeded()) {
                context = vertx.getOrCreateContext();// instanceof EventLoopContext
                handler.handle(Future.succeededFuture(this));
                so.handler(this::handle);
            } else {
                handler.handle(Future.failedFuture(event.cause()));
            }
        });
        return this;
    }

    public RakNet open(String host, int port, Handler<AsyncResult<RakNetSession>> handler) {
        Utils.checkState(so.localAddress() == null, "Connected so");
        // bind
        so.listen(0, "0.0.0.0", event -> {
            if (event.succeeded()) {
                // bind success
                context = vertx.getOrCreateContext();
                session = new RakNetSession(this, SocketAddress.inetSocketAddress(port, host), RakNetSession.Mode.CLIENT);
                // handle data
                so.handler(session);
                handler.handle(Future.succeededFuture(session));
            } else {
                handler.handle(Future.failedFuture(event.cause()));
            }
        });
        return this;
    }

    public RakNet connectHandler(Handler<RakNetSession> connectHandler) {
        this.connectHandler = connectHandler;
        return this;
    }

    void close(SocketAddress client) {
        sessions.remove(client);
    }

    void send(SocketAddress client, ByteBuf buf, Handler<AsyncResult<Void>> handler) {
        so.send(Buffer.buffer(buf), client.port(), client.host(), handler);
    }

    private void handle(DatagramPacket packet) {
        sessions.computeIfAbsent(packet.sender(), client -> {
                    RakNetSession session = new RakNetSession(this, client, RakNetSession.Mode.SERVER);// call handler
                    if (connectHandler != null) {
                        connectHandler.handle(session);
                    }
                    return session;
                })
                .handle(packet);
    }

    public void close(Handler<AsyncResult<Void>> handler) {
        so.close(handler);
    }

    public static RakNet create(Vertx vertx, RakNetOptions options) {
        return new RakNet(vertx, options);
    }

    enum State {
        NEW, BIND, CONNECTED, LISTENING, CLOSE
    }
}
