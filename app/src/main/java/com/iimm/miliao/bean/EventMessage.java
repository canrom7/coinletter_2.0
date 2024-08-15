package com.iimm.miliao.bean;

/**
 * Created by wangqixu on 2019/3/25.
 */
public class EventMessage {
    private int messageAmount;

    public EventMessage() {
    }

    public EventMessage(int messageAmount) {
        this.messageAmount = messageAmount;
    }

    public int getMessageAmount() {
        return messageAmount;
    }

    public void setMessageAmount(int messageAmount) {
        this.messageAmount = messageAmount;
    }
}