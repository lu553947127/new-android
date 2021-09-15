package com.ktw.fly.xmpp.listener;

public interface ChatReceiptListener {

    void onReceiveReceipt(int state, String messageId);
}
