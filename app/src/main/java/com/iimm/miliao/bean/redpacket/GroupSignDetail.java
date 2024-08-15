package com.iimm.miliao.bean.redpacket;

import com.iimm.miliao.bean.GroupSignReward;

import java.util.List;

public class GroupSignDetail {
    public String serialCount;
    private int status;
    private List<GroupSignReward> roomSignInGift;

    public String getSerialCount() {
        return serialCount;
    }

    public void setSerialCount(String serialCount) {
        this.serialCount = serialCount;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<GroupSignReward> getRoomSignInGift() {
        return roomSignInGift;
    }

    public void setRoomSignInGift(List<GroupSignReward> roomSignInGift) {
        this.roomSignInGift = roomSignInGift;
    }
}
