package com.github.caoli5288.vertx.raknet;

import io.vertx.core.datagram.DatagramSocketOptions;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class RakNetOptions {

    private DatagramSocketOptions socketOptions = new DatagramSocketOptions();
    private long guid = 1234567890;

    public RakNetOptions copy() {
        RakNetOptions options = new RakNetOptions();
        options.setSocketOptions(socketOptions);
        options.setGuid(guid);
        return options;
    }
}
