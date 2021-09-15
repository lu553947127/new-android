package com.ktw.fly.call;

import com.ktw.fly.bean.message.ChatMessage;

/**
 * Created by Administrator on 2017/6/26 0026.
 */
public class MessageEventClicAudioVideo {
    public final ChatMessage event;
    public final int isauido;

    public MessageEventClicAudioVideo(ChatMessage event, int misauido) {
        this.event = event;
        this.isauido = misauido;
    }
}