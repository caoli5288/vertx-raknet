package com.github.caoli5288.vertx.raknet.util;

import com.github.caoli5288.vertx.raknet.Constants;
import io.netty.buffer.ByteBuf;
import lombok.SneakyThrows;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class RakNetCodecs {// codecs

    public static String readString(ByteBuf buf) {
        byte[] bytes = new byte[buf.readShort()];
        buf.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void writeString(ByteBuf buf, String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(bytes.length);
        buf.writeBytes(bytes);
    }

    public static byte[] readMagic(ByteBuf buf) {
        byte[] magic = new byte[16];
        buf.readBytes(magic);
        return magic;
    }

    public static void writeMagic(ByteBuf buf) {
        buf.writeBytes(Constants.OFFLINE_MESSAGE);
    }

    @SneakyThrows
    public static InetSocketAddress readAddress(ByteBuf buf) {
        int type = buf.readByte();
        if (type == 4) {
            int j = ~buf.readInt();
            byte[] ip = ByteBuffer.allocate(4).putInt(j).array();
            int port = buf.readUnsignedShort();
            return new InetSocketAddress(InetAddress.getByAddress(ip), port);
        }
        Utils.checkState(type == 6);
        buf.skipBytes(2); //family
        int port = buf.readUnsignedShort();
        buf.skipBytes(4); //flow info
        byte[] ip = new byte[16];
        buf.readBytes(ip);
        buf.skipBytes(4); //scope id;
        return new InetSocketAddress(InetAddress.getByAddress(ip), port);
    }

    public static void writeAddress(ByteBuf buf, InetSocketAddress address) {
        InetAddress ip = address.getAddress();
        if (ip instanceof Inet4Address) {
            buf.writeByte(4);
            int j = ByteBuffer.wrap(ip.getAddress()).getInt();
            buf.writeInt(~j);
            buf.writeShort(address.getPort());
        } else {
            Utils.checkState(ip instanceof Inet6Address);
            buf.writeShort(10); //family AF_INET6
            buf.writeShort(address.getPort());
            buf.writeInt(0); //flow info
            buf.writeBytes(ip.getAddress());
            buf.writeInt(0); //scope id
        }
    }
}
