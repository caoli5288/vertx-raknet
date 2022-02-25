package com.github.caoli5288.vertx.raknet;

import com.github.caoli5288.vertx.raknet.message.OpenConnectionRequest;
import com.github.caoli5288.vertx.raknet.message.OpenConnectionRequest2;
import com.github.caoli5288.vertx.raknet.util.Utils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
class RakNetConnector {

    private final RakNetSession session;
    private final Handler<AsyncResult<RakNetSession>> handler;
    @Setter
    private int mtu = Constants.MTU_MAX;
    @Setter
    private int cd = 2;

    public void connect() {
        OpenConnectionRequest request = new OpenConnectionRequest();
        request.setVersion(session.getVersion());
        request.setMtu(mtu);
        session.send0(request.encode());
        session.getVertx().setTimer(1000, c -> {
            if (!session.isClosed() && session.getState() == RakNetSession.State.OPEN) {
                if (cd > 0) {
                    cd--;
                    connect();
                } else if (mtu != Constants.MTU_MIN) {
                    mtu = Math.max(Constants.MTU_MIN, mtu - 200);
                    cd = 2;
                    connect();
                } else {// timeout
                    handler.handle(Future.failedFuture("timeout"));
                }
            }
        });
    }

    public void connect2() {
        OpenConnectionRequest2 request2 = new OpenConnectionRequest2();
        request2.setAddress(Utils.resolve(session.getAddress()));
        request2.setMtu(mtu);
        request2.setGuid(session.getOptions().getGuid());
        session.send0(request2.encode());
        session.getVertx().setTimer(1000, c -> {
            if (!session.isClosed() && session.getState() == RakNetSession.State.SYNC) {
                if (cd > 0) {
                    cd--;
                    connect2();
                } else {// timeout
                    handler.handle(Future.failedFuture("timeout"));
                }
            }
        });
    }

    public void fail(String msg) {
        handler.handle(Future.failedFuture(msg));
    }

    public void complete() {// delegate to upstream handler
        handler.handle(Future.succeededFuture(session));
    }
}
