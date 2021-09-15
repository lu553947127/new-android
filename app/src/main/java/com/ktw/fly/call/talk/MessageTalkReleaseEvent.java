package com.ktw.fly.call.talk;

import com.ktw.fly.bean.message.ChatMessage;

public class MessageTalkReleaseEvent {
    public ChatMessage chatMessage;

    public MessageTalkReleaseEvent(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }
}
