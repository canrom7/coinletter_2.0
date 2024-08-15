package com.iimm.miliao.bean.redpacket;

public class RedDialogBean {
    private String userId;
    private String userName;
    private String words;
    private String redId;
    private int redType;
    private int redVip;
    private boolean showGroup;


    public RedDialogBean(String userId, String userName, String words, String redId, int redType, int redVip, boolean showGroup) {
        this.userId = userId;
        this.userName = userName;
        this.words = words;
        this.redId = redId;
        this.redType = redType;
        this.redVip = redVip;
        this.showGroup = showGroup;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        this.words = words;
    }

    public String getRedId() {
        return redId;
    }

    public void setRedId(String redId) {
        this.redId = redId;
    }

    public int getRedType() {
        return redType;
    }

    public void setRedType(int redType) {
        this.redType = redType;
    }

    public int getRedVip() {
        return redVip;
    }

    public void setRedVip(int redVip) {
        this.redVip = redVip;
    }

    public boolean isShowGroup() {
        return showGroup;
    }

    public void setShowGroup(boolean showGroup) {
        this.showGroup = showGroup;
    }
}
