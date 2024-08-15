package com.iimm.miliao.bean;

import android.text.TextUtils;

/**
 * Created by Administrator on 2019/3/5 0005.
 * 签到信息返回的数据
 */

public class SignInfoEntity {

    /**
     * sevenCount : 1
     * signCount : 2
     * signStatus : 1
     * signAward : money:9.07
     * seriesSignCount : 1
     */

    private String sevenCount;
    private String signCount;
    private String signStatus;
    private String signAward;
    private String seriesSignCount;

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
