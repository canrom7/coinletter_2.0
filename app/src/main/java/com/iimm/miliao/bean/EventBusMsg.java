package com.iimm.miliao.bean;

/**
 * Created by wangqixu on 2019/3/25.
 */
public class EventBusMsg {
    private int messageType;
    private Object object;

    public EventBusMsg() {
    }

    public EventBusMsg(int ebsType) {
        this.messageType = ebsType;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}