package com.iimm.miliao.view.mucChatHolder;

import android.graphics.Bitmap;
import android.view.View;

import com.iimm.miliao.bean.message.ChatMessage;

public interface ChatHolderListener {

    void onItemLongClick(View v, AChatHolderInterface aChatHolderInterface, ChatMessage mdata);

    void onItemClick(View v, AChatHolderInterface aChatHolderInterface, ChatMessage mdata);

    void onReplayClick(View v, AChatHolderInterface aChatHolderInterface, ChatMessage mdata);

    void onCompDownVoice(ChatMessage message);

    void onChangeInputText(String text);

    Bitmap onLoadBitmap(String key, int width, int height);
}
