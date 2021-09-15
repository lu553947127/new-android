package com.ktw.fly.socket.protocol;

public interface PacketListener {
    void onAfterSent(Packet packet, boolean isSentSuccess) throws Exception;
}
