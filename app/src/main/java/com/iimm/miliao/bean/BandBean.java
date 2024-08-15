package com.iimm.miliao.bean;

import java.util.List;

/**
 * MrLiu253@163.com
 *
 * @time 2019-07-26
 */
public class BandBean {

    /**
     * currentTime : 1564131304292
     * data : [{"createTime":0,"id":"5d289261bf545e7e23cf3429","loginInfo":"oWVC21Pu4ZWmGcIYwr35QLwF0O-0","type":2,"userId":10000012},{"createTime":1564054904,"id":"5d399578bf545e768034abf1","loginInfo":"6C6B9064E0D9463F87AC74978A46B102","type":1,"userId":10000012}]
     * resultCode : 1
     */

    private long currentTime;
    private int resultCode;
    private List<DataBean> data;

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * createTime : 0
         * id : 5d289261bf545e7e23cf3429
         * loginInfo : oWVC21Pu4ZWmGcIYwr35QLwF0O-0
         * type : 2
         * userId : 10000012
         */

        private String createTime;
        private String id;
        private String loginInfo;
        private int type;
        private String userId;

        public String getCreateTime() {
            return createTime == null ? "" : createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public String getId() {
            return id == null ? "" : id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLoginInfo() {
            return loginInfo == null ? "" : loginInfo;
        }

        public void setLoginInfo(String loginInfo) {
            this.loginInfo = loginInfo;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getUserId() {
            return userId == null ? "" : userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
}
