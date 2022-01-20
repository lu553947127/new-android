package com.ktw.bitbit.call;

import com.ktw.bitbit.bean.message.ChatMessage;

/**
 * Created by Administrator on 2017/6/26 0026.
 */
public class MessageCallingEvent {
    public ChatMessage chatMessage;

    public MessageCallingEvent(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }
}