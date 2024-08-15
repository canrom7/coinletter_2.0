package com.iimm.miliao.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * MrLiu253@163.com
 *
 * @time 2020-03-18
 */
public class DynamicWithdrawalBean implements Serializable {


    /**
     * withdrawWayKeyId : e3148998645a46fab46fc3b391ff2cc1
     * withdrawWayName : 支付宝
     * withdrawWaySort : 0
     * withdrawWayStatus : 1
     * withdrawWayTime : 1584500825
     * withdrawKeyDetails : [{"withdrawId":"5e719089aa534928c032e3d6","withdrawKeyTime":1584500873,"withdrawName":"姓名","withdrawStatus":"1"},{"withdrawId":"5e719089aa534928c032e3d7","withdrawKeyTime":1584500873,"withdrawName":"手机号","withdrawStatus":"1"},{"withdrawId":"5e719089aa534928c032e3d8","withdrawKeyTime":1584500873,"withdrawName":"备注","withdrawStatus":"0"}]
     */

    private String withdrawWayKeyId;
    private String withdrawWayName;
    private int withdrawWaySort;
    private int withdrawWayStatus;
    private String withdrawWayTime;
    private List<WithdrawKeyDetailsBean> withdrawKeyDetails;

    public String getWithdrawWayKeyId() {
        return withdrawWayKeyId == null ? "" : withdrawWayKeyId;
    }

    public void setWithdrawWayKeyId(String withdrawWayKeyId) {
        this.withdrawWayKeyId = withdrawWayKeyId;
    }

    public String getWithdrawWayName() {
        return withdrawWayName == null ? "" : withdrawWayName;
    }

    public void setWithdrawWayName(String withdrawWayName) {
        this.withdrawWayName = withdrawWayName;
    }

    public int getWithdrawWaySort() {
        return withdrawWaySort;
    }

    public void setWithdrawWaySort(int withdrawWaySort) {
        this.withdrawWaySort = withdrawWaySort;
    }

    public int getWithdrawWayStatus() {
        return withdrawWayStatus;
    }

    public void setWithdrawWayStatus(int withdrawWayStatus) {
        this.withdrawWayStatus = withdrawWayStatus;
    }

    public String getWithdrawWayTime() {
        return withdrawWayTime == null ? "" : withdrawWayTime;
    }

    public void setWithdrawWayTime(String withdrawWayTime) {
        this.withdrawWayTime = withdrawWayTime;
    }

    public List<WithdrawKeyDetailsBean> getWithdrawKeyDetails() {
        if (withdrawKeyDetails == null) {
            return new ArrayList<>();
        }
        return withdrawKeyDetails;
    }

    public void setWithdrawKeyDetails(List<WithdrawKeyDetailsBean> withdrawKeyDetails) {
        this.withdrawKeyDetails = withdrawKeyDetails;
    }

    public static class WithdrawKeyDetailsBean implements Serializable {
        /**
         * withdrawId : 5e719089aa534928c032e3d6
         * withdrawKeyTime : 1584500873
         * withdrawName : 姓名
         * withdrawStatus : 1
         */

        private String withdrawId;
        private String withdrawKeyTime;
        private String withdrawName;
        private int withdrawStatus;

        public String getWithdrawId() {
            return withdrawId == null ? "" : withdrawId;
        }

        public void setWithdrawId(String withdrawId) {
            this.withdrawId = withdrawId;
        }

        public String getWithdrawKeyTime() {
            return withdrawKeyTime == null ? "" : withdrawKeyTime;
        }

        public void setWithdrawKeyTime(String withdrawKeyTime) {
            this.withdrawKeyTime = withdrawKeyTime;
        }

        public String getWithdrawName() {
            return withdrawName == null ? "" : withdrawName;
        }

        public void setWithdrawName(String withdrawName) {
            this.withdrawName = withdrawName;
        }

        public int getWithdrawStatus() {
            return withdrawStatus;
        }

        public void setWithdrawStatus(int withdrawStatus) {
            this.withdrawStatus = withdrawStatus;
        }
    }
}
