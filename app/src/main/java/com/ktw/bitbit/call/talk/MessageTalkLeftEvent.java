package com.ktw.bitbit.call.talk;

import com.ktw.bitbit.bean.message.ChatMessage;

public class MessageTalkLeftEvent {
    public ChatMessage chatMessage;

    public MessageTalkLeftEvent(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }
}
