package com.iimm.miliao.bean;

import com.iimm.miliao.bean.message.MucRoom;

/**
 * 将群组存入朋友表
 */
public class EventCreateGroupFriend {
    private MucRoom mucRoom;

    public EventCreateGroupFriend(MucRoom mucRoom) {
        this.mucRoom = mucRoom;
    }

    public MucRoom getMucRoom() {
        return mucRoom;
    }

    public void setMucRoom(MucRoom mucRoom) {
        this.mucRoom = mucRoom;
    }
}
