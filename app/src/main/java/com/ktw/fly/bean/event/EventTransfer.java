package com.ktw.fly.bean.event;

import com.ktw.fly.bean.message.ChatMessage;

public class EventTransfer {
    private ChatMessage chatMessage;

    public EventTransfer(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }
}
