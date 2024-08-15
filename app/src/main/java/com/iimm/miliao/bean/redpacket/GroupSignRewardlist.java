package com.iimm.miliao.bean.redpacket;

import java.util.List;

public class GroupSignRewardlist {
    private String roomJid;
    private List<GroupSignRecord> data;

    public String getRoomJid() {
        return roomJid;
    }

    public void setRoomJid(String roomJid) {
        this.roomJid = roomJid;
    }

    public List<GroupSignRecord> getData() {
        return data;
    }

    public void setData(List<GroupSignRecord> data) {
        this.data = data;
    }
}
