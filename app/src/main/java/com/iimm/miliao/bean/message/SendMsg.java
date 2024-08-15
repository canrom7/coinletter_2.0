package com.iimm.miliao.bean.message;

import com.j256.ormlite.field.DatabaseField;

public class SendMsg {
    @DatabaseField
    private String msgContent;

    @DatabaseField
    private boolean isWhetherToForward;
    @DatabaseField
    private String whetherToForwardDevice;


    public String getMsgContent() {
        return msgContent == null ? "" : msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    public boolean isWhetherToForward() {
        return isWhetherToForward;
    }

    public void setWhetherToForward(boolean whetherToForward) {
        isWhetherToForward = whetherToForward;
    }

    public String getWhetherToForwardDevice() {
        return whetherToForwardDevice == null ? "" : whetherToForwardDevice;
    }

    public void setWhetherToForwardDevice(String whetherToForwardDevice) {
        this.whetherToForwardDevice = whetherToForwardDevice;
    }
}
