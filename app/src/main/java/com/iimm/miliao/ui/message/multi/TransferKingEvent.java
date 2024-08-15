package com.iimm.miliao.ui.message.multi;

public class TransferKingEvent {
    public String roomJid;
    public String toUserId;
    public String toUserName;

    public TransferKingEvent(String roomJid, String toUserId, String toUserName) {
        this.roomJid = roomJid;
        this.toUserId = toUserId;
        this.toUserName = toUserName;
    }
}
