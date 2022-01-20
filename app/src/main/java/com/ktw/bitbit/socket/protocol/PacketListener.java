package com.ktw.bitbit.socket.protocol;

public interface PacketListener {
    void onAfterSent(Packet packet, boolean isSentSuccess) throws Exception;
}
