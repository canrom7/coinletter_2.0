package com.iimm.miliao.bean;

public class BandUploadResult {

    /**
     * currentTime : 1564823746887
     * data : {"msg":"该账号已绑定其他用户,请选择未绑定账户或联系客服","code":"6"}
     * resultCode : 1
     */

    private String currentTime;
    private DataBean data;
    private int resultCode;

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public static class DataBean {
        /**
         * msg : 该账号已绑定其他用户,请选择未绑定账户或联系客服
         * code : 6
         */

        private String msg;
        private String code;

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}
