package com.iimm.miliao.bean;

import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.bean.message.MucRoom;

public class EventNewNotice {
    private String text;
    private String roomJid;

    public EventNewNotice(MucRoom.Notice notice, ChatMessage chatMessage) {
        this.text = notice.getText();
        this.roomJid = chatMessage.getObjectId();
    }

    public String getText() {
        return text;
    }

    public String getRoomJid() {
        return roomJid;
    }
}
