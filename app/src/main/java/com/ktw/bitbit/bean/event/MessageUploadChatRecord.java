package com.ktw.bitbit.bean.event;

/**
 * Created by Administrator on 2017/6/26 0026.
 */
public class MessageUploadChatRecord {
    public String chatIds;

    /**
     * @see com.ktw.bitbit.ui.message.ChatActivity
     */
    public MessageUploadChatRecord(String chatIds) {
        this.chatIds = chatIds;
    }
}