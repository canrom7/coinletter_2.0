package com.iimm.miliao.ui.account;

/**
 * MrLiu253@163.com
 * 微信登陆授权成功的回调
 *
 * @time 2019-08-06
 */
public class LoginWXBean {
    public final String wxCode;

    public LoginWXBean(String wxCode) {
        this.wxCode = wxCode;
    }
}
