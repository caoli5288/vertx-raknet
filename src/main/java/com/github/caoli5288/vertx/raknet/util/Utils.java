package com.github.caoli5288.vertx.raknet.util;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.impl.VertxByteBufAllocator;
import io.vertx.core.net.SocketAddress;

import java.net.InetSocketAddress;
import java.util.function.Consumer;

public class Utils {

    public static <T> T let(T obj, Consumer<T> consumer) {
        if (obj != null) {
            consumer.accept(obj);
        }
        return obj;
    }

    public static void checkState(boolean b, String msg) {
        if (!b) {
            throw new IllegalStateException(msg);
        }
    }

    public static void checkState(boolean b) {
        if (!b) {
            throw new IllegalStateException();
        }
    }

    public static ByteBuf buffer() {
        return VertxByteBufAllocator.DEFAULT.buffer();
    }

    public static int toUInt24(int i) {
        return i & 0xffffff;
    }

    public static InetSocketAddress resolve(SocketAddress address) {
        return new InetSocketAddress(address.host(), address.port());
    }

    /**
     * Set the specified bit to 1
     *
     * @param b Raw byte value
     * @param i bit index (From 0~7)
     * @return Final byte value
     */
    public static int setBitTo1(int b, int i) {
        b |= (1 << i);
        return b;
    }

    /**
     * Set the specified bit to 0
     *
     * @param b Raw byte value
     * @param i bit index (From 0~7)
     * @return Final byte value
     */
    public static int setBitTo0(int b, int i) {
        b &= ~(1 << i);
        return b;
    }

    public static byte[] getBytes(ByteBuf buf) {
        byte[] arr = new byte[buf.writerIndex()];
        buf.getBytes(0, arr);
        return arr;
    }

    public static int ceil(int i, int j) {
        int ceil = i / j;
        if (i % j != 0) {
            ceil = ceil + 1;
        }
        return ceil;
    }
}
