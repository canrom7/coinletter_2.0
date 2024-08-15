package com.iimm.miliao.bean.message;

import com.j256.ormlite.field.DatabaseField;

public class ReceiptMsg {
    @DatabaseField
    private String msgContent;


    public String getMsgContent() {
        return msgContent == null ? "" : msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }
}
