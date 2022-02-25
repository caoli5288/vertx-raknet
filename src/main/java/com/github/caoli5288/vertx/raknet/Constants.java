package com.github.caoli5288.vertx.raknet;

import java.net.InetSocketAddress;

public final class Constants {

    public static final int SERVER_PROTOCOL_VERSION = 10;
    public static final byte[] OFFLINE_MESSAGE = new byte[]{(byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0xfe, (byte) 0xfe, (byte) 0xfe, (byte) 0xfe, (byte) 0xfd, (byte) 0xfd, (byte) 0xfd, (byte) 0xfd, (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78};

    public static final int ID_CONNECTED_PING = 0;
    public static final int ID_UNCONNECTED_PING = 1;
    public static final int ID_CONNECTED_PONG = 3;
    public static final int ID_OPEN_CONNECTION_REQUEST_1 = 5;
    public static final int ID_OPEN_CONNECTION_REPLY_1 = 6;
    public static final int ID_OPEN_CONNECTION_REQUEST_2 = 7;
    public static final int ID_OPEN_CONNECTION_REPLY_2 = 8;
    public static final int ID_CONNECTION_REQUEST = 9;
    public static final int ID_CONNECTION_REQUEST_ACCEPTED = 16;
    public static final int ID_NEW_INCOMING_CONNECTION = 19;
    public static final int ID_DISCONNECT = 21;
    public static final int ID_INCOMPATIBLE_PROTOCOL_VERSION = 25;
    public static final int ID_RN_UNCONNECTED_PONG = 28;
    public static final int ID_RN_FRAME_SET_PACKET_START = 128;
    public static final int ID_RN_FRAME_SET_PACKET_END = 139;
    public static final int ID_RN_N_ACK = 160;
    public static final int ID_RN_ACK = 192;
    public static final int ID_USER_PACKET = 254;

    public static final int UDP_OVERHEAD = 28;
    public static final int OPEN_CONNECTION_REQUEST_OVERHEAD = UDP_OVERHEAD + 18;
    public static final int FRAME_SET_OVERHEAD = Constants.UDP_OVERHEAD + 27;

    public static final int MTU_MAX = 1400;
    public static final int MTU_MIN = 576;

    public static final InetSocketAddress SYSTEM_ADDRESS = new InetSocketAddress(0);
}
