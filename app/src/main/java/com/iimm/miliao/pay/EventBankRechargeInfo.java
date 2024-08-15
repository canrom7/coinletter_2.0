package com.iimm.miliao.pay;


import com.iimm.miliao.bean.message.ChatMessage;

/**
 * 银行卡充值结果
 */
public class EventBankRechargeInfo {
    private ChatMessage chatMessage;

    public EventBankRechargeInfo(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }
}
