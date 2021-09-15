package com.ktw.fly.call;

import com.ktw.fly.bean.message.ChatMessage;

/**
 * 被邀请 加入会议
 */
public class MessageEventMeetingInvited {
    public final int type;
    public final ChatMessage message;

    public MessageEventMeetingInvited(int type, ChatMessage message) {
        this.type = type;
        this.message = message;
    }
}