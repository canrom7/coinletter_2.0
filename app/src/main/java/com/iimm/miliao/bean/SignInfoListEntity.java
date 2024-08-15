package com.iimm.miliao.bean;

/**
 * Created by Administrator on 2019/3/6 0006.
 */

public class SignInfoListEntity {
    /**
     * id : 5c7faac16867a778bbb81c03
     * userId : 10044
     * signDate : 1551801600000
     * device : 865967030010700
     * signIP : 117.136.104.211
     * signAward : money:2.28
     * status : 1
     * createDate : 1551870657056
     * updateDate : 1551870657056
     */

    private String id;
    private String userId;
    private String signDate;
    private String device;
    private String signIP;
    private String signAward;
    private String status;
    private String createDate;
    private String updateDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSignDate() {
        return signDate;
    }

    public void setSignDate(String signDate) {
        this.signDate = signDate;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getSignIP() {
        return signIP;
    }

    public void setSignIP(String signIP) {
        this.signIP = signIP;
    }

    public String getSignAward() {
        return signAward;
    }

    public void setSignAward(String signAward) {
        this.signAward = signAward;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }
}
