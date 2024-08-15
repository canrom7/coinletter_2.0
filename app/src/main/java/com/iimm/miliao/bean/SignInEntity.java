package com.iimm.miliao.bean;

import android.text.TextUtils;

/**
 * Created by Administrator on 2019/3/6 0006.
 * 签到成功后返回的数据
 */

public class SignInEntity {

    /**
     * msg : 签到成功
     * sevenCount : 1
     * signCount : 1
     * signStatus : 1
     * signAward : money:10.46
     * seriesSignCount : 1
     */

    private String msg;
    private String sevenCount;
    private String signCount;
    private String signStatus;
    private String signAward;
    private String seriesSignCount;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSevenCount() {
        return sevenCount;
    }

    public void setSevenCount(String sevenCount) {
        this.sevenCount = sevenCount;
    }

    public String getSignCount() {
        return signCount;
    }

    public void setSignCount(String signCount) {
        this.signCount = signCount;
    }

    public String getSignStatus() {
        return signStatus;
    }

    public void setSignStatus(String signStatus) {
        this.signStatus = signStatus;
    }

    public String getSignAward() {
        if (!TextUtils.isEmpty(signAward) && signAward.contains("money:")) {
            return signAward.substring("money:".length(), signAward.length());
        } else {
            return "0";
        }
    }

    public void setSignAward(String signAward) {
        this.signAward = signAward;
    }

    public String getSeriesSignCount() {
        return seriesSignCount;
    }

    public void setSeriesSignCount(String seriesSignCount) {
        this.seriesSignCount = seriesSignCount;
    }
}
