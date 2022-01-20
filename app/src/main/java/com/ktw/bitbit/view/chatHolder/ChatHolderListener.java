package com.ktw.bitbit.view.chatHolder;

import android.graphics.Bitmap;
import android.view.View;

import com.ktw.bitbit.bean.message.ChatMessage;

public interface ChatHolderListener {

    void onItemClick(View v, AChatHolderInterface aChatHolderInterface, ChatMessage mdata);

    void onItemLongClick(View v, AChatHolderInterface aChatHolderInterface, ChatMessage mdata);

    void onChangeInputText(String text);

    void onCompDownVoice(ChatMessage message);

    Bitmap onLoadBitmap(String key, int width, int height);

    void onReplayClick(View v, AChatHolderInterface aChatHolderInterface, ChatMessage mdata);
}
