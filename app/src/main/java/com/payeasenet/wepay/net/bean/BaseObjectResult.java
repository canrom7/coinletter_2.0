package com.payeasenet.wepay.net.bean;

/**
 * MrLiu253@163.com
 *
 * @time 2020-02-13
 */
public class BaseObjectResult<T> {

    private int resultCode;
    private String resultMsg;
    private T data;

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg == null ? "" : resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
