package com.iimm.miliao.bean;

/**
 * MrLiu253@163.com
 *
 * @time 2020-03-18
 */
public class DeleteGroupMemberBean {


    /**
     * delSucceededUserId : 111,444
     * delFailedUserId : 222,333
     */

    private String delSucceedUserId;
    private String delFailedUserId;

    public String getDelSucceedUserId() {
        return delSucceedUserId == null ? "" : delSucceedUserId;
    }

    public void setDelSucceedUserId(String delSucceedUserId) {
        this.delSucceedUserId = delSucceedUserId;
    }

    public String getDelFailedUserId() {
        return delFailedUserId == null ? "" : delFailedUserId;
    }

    public void setDelFailedUserId(String delFailedUserId) {
        this.delFailedUserId = delFailedUserId;
    }
}
