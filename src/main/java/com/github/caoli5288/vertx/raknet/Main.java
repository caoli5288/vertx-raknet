package com.github.caoli5288.vertx.raknet;

import com.github.caoli5288.vertx.raknet.message.Reliability;
import com.github.caoli5288.vertx.raknet.message.UserData;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        RakNet net = RakNet.create(Vertx.vertx(), new RakNetOptions());
        net.listen("0.0.0.0", 20001, event -> {
            event.result().connectHandler(session -> {
                session.pingHandler(pong -> pong.setInfo("MCPE;MOTD;390;1.14.60;0;10;13253860892328930865;MOTD2;Survival;"));
                session.dataHandler(data -> {
                    System.out.println(Arrays.toString(data.getBytes()));
                });
                session.connectedHandler(__ -> {
                    System.out.println("Connected");
                    net.getVertx().setPeriodic(1000, a -> {
                        UserData data = new UserData();
                        data.setBody(Buffer.buffer("hello").getByteBuf());
                        session.send(data, Reliability.RELIABLE_ORDERED);
                    });
                });
            });
        });
    }
}
