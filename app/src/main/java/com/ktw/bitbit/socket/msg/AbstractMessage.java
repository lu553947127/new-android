package com.ktw.bitbit.socket.msg;


/**
 * @author lidaye
 */

public abstract class AbstractMessage {

    public MessageHead messageHead;

    public MessageHead getMessageHead() {
        return messageHead;
    }

    public void setMessageHead(MessageHead messageHead) {
        this.messageHead = messageHead;
    }

    public abstract String toString();

}
