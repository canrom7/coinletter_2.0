package com.iimm.miliao.bean.redpacket;

import java.io.Serializable;

/**
 * MrLiu253@163.com
 *
 * @time 2020-01-09
 */
public class ReceiversBean implements Serializable {


    /**
     * amount : 0.01
     * completeDateTime : 2020-01-09 10:18:31
     * nickname : 戴全美
     * userId : 10000017
     */

    private String amount;
    private String completeDateTime;
    private String nickname;
    private String userId;
    private boolean isLucky;

    public boolean isLucky() {
        return isLucky;
    }

    public void setLucky(boolean lucky) {
        isLucky = lucky;
    }

    public String getAmount() {
        return amount == null ? "" : amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCompleteDateTime() {
        return completeDateTime == null ? "" : completeDateTime;
    }

    public void setCompleteDateTime(String completeDateTime) {
        this.completeDateTime = completeDateTime;
    }

    public String getNickname() {
        return nickname == null ? "" : nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUserId() {
        return userId == null ? "" : userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
