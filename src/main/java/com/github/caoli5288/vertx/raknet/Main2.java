package com.github.caoli5288.vertx.raknet;

import com.whirvis.jraknet.RakNetPacket;
import com.whirvis.jraknet.client.RakNetClient;
import com.whirvis.jraknet.client.RakNetClientListener;
import com.whirvis.jraknet.peer.RakNetServerPeer;
import com.whirvis.jraknet.protocol.ConnectionType;

import java.net.InetSocketAddress;

public class Main2 {

    public static void main(String[] args) throws Exception {
        RakNetClient client = new RakNetClient();
        client.addListener(new RakNetClientListener() {
            @Override
            public void onConnect(RakNetClient client, InetSocketAddress address, ConnectionType connectionType) {

            }

            @Override
            public void handleMessage(RakNetClient client, RakNetServerPeer peer, RakNetPacket packet, int channel) {
                System.out.println(packet.toString());
            }
        });
        client.connect("localhost", 20001);
    }
}
