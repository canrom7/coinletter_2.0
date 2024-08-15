package com.iimm.miliao.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BlanceInfo implements Serializable{

    private List<MethodBean> alipayMethod;
    private List<MethodBean> bankCardMethod;
    private List<MethodBean> otherMethod;

    public List<MethodBean> getOtherMethod() {
        if (otherMethod == null) {
            return new ArrayList<>();
        }
        return otherMethod;
    }

    public void setOtherMethod(List<MethodBean> otherMethod) {
        this.otherMethod = otherMethod;
    }

    public List<MethodBean> getAlipayMethod() {
        if (alipayMethod == null) {
            return new ArrayList<>();
        }
        return alipayMethod;
    }

    public void setAlipayMethod(List<MethodBean> alipayMethod) {
        this.alipayMethod = alipayMethod;
    }

    public List<MethodBean> getBankCardMethod() {
        if (bankCardMethod == null) {
            return new ArrayList<>();
        }
        return bankCardMethod;
    }

    public void setBankCardMethod(List<MethodBean> bankCardMethod) {
        this.bankCardMethod = bankCardMethod;
    }

    public static class MethodBean implements Serializable {
        /**
         * addAlipayTime : 1574216007
         * alipayId : 5dd4a1474b7024fc900004e6
         * alipayName : 啊哈
         * alipayNumber : 123456
         */

        private int type;//1支付宝  5银行卡
        private String alipayId;
        private String alipayName;
        private String alipayNumber;

        private String bankCardNo;
        private String bankId;
        private String bankName;
        private String bankUserName;
        private String remarks;
        private String subBankName;

        private String otherId;
        private String otherNode1;
        private String otherNode2;
        private String otherNode3;
        private String otherNode4;
        private String otherNode5;

        private boolean select;
        private int withdrawWaySort;
        private String withdrawWayName;
        private List<DynamicWithdrawalBean.WithdrawKeyDetailsBean> withdrawKeyDetails;

        public List<DynamicWithdrawalBean.WithdrawKeyDetailsBean> getWithdrawKeyDetails() {
            if (withdrawKeyDetails == null) {
                return new ArrayList<>();
            }
            return withdrawKeyDetails;
        }

        public void setWithdrawKeyDetails(List<DynamicWithdrawalBean.WithdrawKeyDetailsBean> withdrawKeyDetails) {
            this.withdrawKeyDetails = withdrawKeyDetails;
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

        public boolean isSelect() {
            return select;
        }

        public void setSelect(boolean select) {
            this.select = select;
        }

        public MethodBean(int type ,String name,int sort,List<DynamicWithdrawalBean.WithdrawKeyDetailsBean> details) {
            this.type = type;
            this.withdrawWayName = name;
            this.withdrawWaySort = sort;
            this.withdrawKeyDetails = details;
        }

        public MethodBean() {
        }

        public String getOtherId() {
            return otherId == null ? "" : otherId;
        }

        public void setOtherId(String otherId) {
            this.otherId = otherId;
        }

        public String getOtherNode1() {
            return otherNode1 == null ? "" : otherNode1;
        }

        public void setOtherNode1(String otherNode1) {
            this.otherNode1 = otherNode1;
        }

        public String getOtherNode2() {
            return otherNode2 == null ? "" : otherNode2;
        }

        public void setOtherNode2(String otherNode2) {
            this.otherNode2 = otherNode2;
        }

        public String getOtherNode3() {
            return otherNode3 == null ? "" : otherNode3;
        }

        public void setOtherNode3(String otherNode3) {
            this.otherNode3 = otherNode3;
        }

        public String getOtherNode4() {
            return otherNode4 == null ? "" : otherNode4;
        }

        public void setOtherNode4(String otherNode4) {
            this.otherNode4 = otherNode4;
        }

        public String getOtherNode5() {
            return otherNode5 == null ? "" : otherNode5;
        }

        public void setOtherNode5(String otherNode5) {
            this.otherNode5 = otherNode5;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getAlipayId() {
            return alipayId == null ? "" : alipayId;
        }

        public void setAlipayId(String alipayId) {
            this.alipayId = alipayId;
        }

        public String getAlipayName() {
            return alipayName == null ? "" : alipayName;
        }

        public void setAlipayName(String alipayName) {
            this.alipayName = alipayName;
        }

        public String getAlipayNumber() {
            return alipayNumber == null ? "" : alipayNumber;
        }

        public void setAlipayNumber(String alipayNumber) {
            this.alipayNumber = alipayNumber;
        }

        public String getBankCardNo() {
            return bankCardNo == null ? "" : bankCardNo;
        }

        public void setBankCardNo(String bankCardNo) {
            this.bankCardNo = bankCardNo;
        }

        public String getBankId() {
            return bankId == null ? "" : bankId;
        }

        public void setBankId(String bankId) {
            this.bankId = bankId;
        }

        public String getBankName() {
            return bankName == null ? "" : bankName;
        }

        public void setBankName(String bankName) {
            this.bankName = bankName;
        }

        public String getBankUserName() {
            return bankUserName == null ? "" : bankUserName;
        }

        public void setBankUserName(String bankUserName) {
            this.bankUserName = bankUserName;
        }

        public String getRemarks() {
            return remarks == null ? "" : remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }

        public String getSubBankName() {
            return subBankName == null ? "" : subBankName;
        }

        public void setSubBankName(String subBankName) {
            this.subBankName = subBankName;
        }
    }
}
