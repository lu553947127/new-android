package com.ktw.fly.bean.event;

/**
 * Created by Administrator on 2017/6/26 0026.
 */
public class MessageUploadChatRecord {
    public String chatIds;

    /**
     * @see com.ktw.fly.ui.message.ChatActivity
     */
    public MessageUploadChatRecord(String chatIds) {
        this.chatIds = chatIds;
    }
}