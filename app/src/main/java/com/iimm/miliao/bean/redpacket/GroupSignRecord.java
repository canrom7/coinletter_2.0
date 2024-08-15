package com.iimm.miliao.bean.redpacket;

import com.iimm.miliao.bean.GroupSignReward;

import java.util.List;

public class GroupSignRecord {
    /*
    *
    * count	1
    id	"5d6348e719d406c00c8c85ae"
    nickName	""
    roomId	""
    roomJid	"72ea3ce29a894fde8a97594fa40aaea6"
    roomSignInGift	[]
    serialCount	1
    signInTime	1566787815490
    status	1
    userId
    * */
    private String count;
    private String id;
    private String nickName;
    private String roomId;
    private String roomJid;
    private String serialCount;
    private String signInTime;
    private String userId;
    private boolean ichecked;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private List<GroupSignReward> roomSignInGift;

    public List<GroupSignReward> getRoomSignInGift() {
        return roomSignInGift;
    }

    public void setRoomSignInGift(List<GroupSignReward> roomSignInGift) {
        this.roomSignInGift = roomSignInGift;
    }

    public boolean isIchecked() {
        return ichecked;
    }

    public void setIchecked(boolean ichecked) {
        this.ichecked = ichecked;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomJid() {
        return roomJid;
    }

    public void setRoomJid(String roomJid) {
        this.roomJid = roomJid;
    }

    public String getSerialCount() {
        return serialCount;
    }

    public void setSerialCount(String serialCount) {
        this.serialCount = serialCount;
    }

    public String getSignInTime() {
        return signInTime;
    }

    public void setSignInTime(String signInTime) {
        this.signInTime = signInTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String status;
}
