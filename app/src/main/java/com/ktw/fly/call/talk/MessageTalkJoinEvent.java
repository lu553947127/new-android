package com.ktw.fly.call.talk;

import com.ktw.fly.bean.message.ChatMessage;

public class MessageTalkJoinEvent {
    public ChatMessage chatMessage;

    public MessageTalkJoinEvent(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }
}
