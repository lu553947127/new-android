package com.ktw.fly.bean.event;

import com.ktw.fly.bean.message.ChatMessage;

/**
 * Created by Administrator on 2017/6/26 0026.
 */
public class MessageEventClickable {
    public final ChatMessage event;

    public MessageEventClickable(ChatMessage event) {
        this.event = event;
    }
}