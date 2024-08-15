package com.iimm.miliao.bean;

import java.util.List;

public class MyBankCardBean {

    /**
     * start : 0
     * total : 1
     * pageData : [{"bankUserName":"王*仓","bankNumber":"6221*****4587","bankName":"邮储银行","bankId":"5d5e5a7b8014ae4e2cb5629b"}]
     * pageSize : 10
     * pageIndex : 0
     * pageCount : 1
     */

    private int start;
    private int total;
    private int pageSize;
    private int pageIndex;
    private int pageCount;
    private List<PageDataBean> pageData;

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public List<PageDataBean> getPageData() {
        return pageData;
    }

    public void setPageData(List<PageDataBean> pageData) {
        this.pageData = pageData;
    }

    public static class PageDataBean {
        /**
         * bankUserName : 王*仓
         * bankNumber : 6221*****4587
         * bankName : 邮储银行
         * bankId : 5d5e5a7b8014ae4e2cb5629b
         */

        private String bankUserName;
        private String bankNumber;
        private String bankName;
        private String bankId;

        public String getBankUserName() {
            return bankUserName;
        }

        public void setBankUserName(String bankUserName) {
            this.bankUserName = bankUserName;
        }

        public String getBankNumber() {
            return bankNumber;
        }

        public void setBankNumber(String bankNumber) {
            this.bankNumber = bankNumber;
        }

        public String getBankName() {
            return bankName;
        }

        public void setBankName(String bankName) {
            this.bankName = bankName;
        }

        public String getBankId() {
            return bankId;
        }

        public void setBankId(String bankId) {
            this.bankId = bankId;
        }
    }
}
