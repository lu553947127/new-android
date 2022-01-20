package com.ktw.bitbit.call.talk;

import com.ktw.bitbit.bean.message.ChatMessage;

public class MessageTalkOnlineEvent {
    public ChatMessage chatMessage;

    public MessageTalkOnlineEvent(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }
}
