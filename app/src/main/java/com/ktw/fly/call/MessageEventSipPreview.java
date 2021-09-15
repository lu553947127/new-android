package com.ktw.fly.call;

import com.ktw.fly.bean.Friend;
import com.ktw.fly.bean.message.ChatMessage;

/**
 * Created by Administrator on 2017/6/26 0026.
 */
public class MessageEventSipPreview {
    public final int number;
    public final String userid;
    public final boolean isvoice;
    public final Friend friend;
    public ChatMessage message;

    public MessageEventSipPreview(int number, String userid, boolean isvoice, Friend friend, ChatMessage message) {
        this.number = number;
        this.userid = userid;
        this.isvoice = isvoice;
        this.friend = friend;
        this.message = message;
    }
}