package com.ktw.fly.call.talk;

import com.ktw.fly.bean.message.ChatMessage;

public class MessageTalkLeftEvent {
    public ChatMessage chatMessage;

    public MessageTalkLeftEvent(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }
}
