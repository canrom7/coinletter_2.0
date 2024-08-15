package com.iimm.miliao.bean;

import com.iimm.miliao.bean.message.ChatMessage;

import java.util.List;

public class ForwardResultOfGroup {
    private Friend mFriend;
    private List<ChatMessage> messages;
    private String failureMessage;


    public Friend getFriend() {
        return mFriend;
    }

    public void setFriend(Friend friend) {
        mFriend = friend;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }
}
