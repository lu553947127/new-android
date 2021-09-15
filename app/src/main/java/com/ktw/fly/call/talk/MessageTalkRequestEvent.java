package com.ktw.fly.call.talk;

import com.ktw.fly.bean.message.ChatMessage;

public class MessageTalkRequestEvent {
    public ChatMessage chatMessage;

    public MessageTalkRequestEvent(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }
}
