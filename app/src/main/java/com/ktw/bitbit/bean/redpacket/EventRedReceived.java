package com.ktw.bitbit.bean.redpacket;

public class EventRedReceived {
    private OpenRedpacket openRedpacket;
    private RushRedPacket rushRedPacket;

    public EventRedReceived(OpenRedpacket openRedpacket) {
        this.openRedpacket = openRedpacket;
    }

    public EventRedReceived(RushRedPacket rushRedPacket) {
        this.rushRedPacket = rushRedPacket;
    }

    public RushRedPacket getRushRedPacket() {
        return rushRedPacket;
    }

    public void setRushRedPacket(RushRedPacket rushRedPacket) {
        this.rushRedPacket = rushRedPacket;
    }

    public OpenRedpacket getOpenRedpacket() {
        return openRedpacket;
    }

    public void setOpenRedpacket(OpenRedpacket openRedpacket) {
        this.openRedpacket = openRedpacket;
    }
}
