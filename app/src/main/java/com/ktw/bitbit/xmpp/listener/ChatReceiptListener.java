package com.ktw.bitbit.xmpp.listener;

public interface ChatReceiptListener {

    void onReceiveReceipt(int state, String messageId);
}
