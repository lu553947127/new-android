package com.ktw.fly.call;

import com.ktw.fly.bean.message.ChatMessage;

/**
 * Created by Administrator on 2017/6/26 0026.
 */
public class MessageHangUpPhone {
    public final ChatMessage chatMessage;

    public MessageHangUpPhone(ChatMessage message) {
        this.chatMessage = message;
    }
}