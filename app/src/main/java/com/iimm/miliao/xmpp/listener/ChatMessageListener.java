package com.iimm.miliao.xmpp.listener;

import com.iimm.miliao.bean.message.ChatMessage;

public interface ChatMessageListener {
    // 消息发送状态的回调
    void onMessageSendStateChange(int messageState, String msgId);

    // 消息来临时的回调
    boolean onNewMessage(String fromUserId, ChatMessage message, boolean isGroupMsg);
}
