package com.ktw.bitbit.bean.event;

import com.ktw.bitbit.bean.message.ChatMessage;

public class EventTransfer {
    private ChatMessage chatMessage;

    public EventTransfer(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }
}
