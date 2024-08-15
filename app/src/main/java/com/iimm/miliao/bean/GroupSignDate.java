package com.iimm.miliao.bean;

public class GroupSignDate {
    /*
    *
    * "id":"5d64e9cd90fc166f946cd40b",
       "roomId":"",
            "roomJid":"72ea3ce29a894fde8a97594fa40aaea6",
            "signInDate":1566835200000,
            "userId":10000100
    * */
    private String id;
    private String roomId;
    private String roomJid;
    private String userId;

    public String getSignInDate() {
        return signInDate;
    }

    public void setSignInDate(String signInDate) {
        this.signInDate = signInDate;
    }

    private String signInDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}
