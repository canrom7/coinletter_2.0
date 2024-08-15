package com.iimm.miliao.call;

import com.iimm.miliao.bean.message.ChatMessage;

/**
 * Created by Administrator on 2017/6/26 0026.
 */
public class MessageHangUpPhone {
    public final ChatMessage chatMessage;

    public MessageHangUpPhone(ChatMessage message) {
        this.chatMessage = message;
    }
}