package com.iimm.miliao.bean.redpacket;

import android.support.annotation.NonNull;

/**
 * MrLiu253@163.com
 *
 * @time 2019-12-17
 */
public class H5Bean {

    /**
     * out_trade_no : 861576563312927
     * money : 2
     * sign : 4481424554f5decb427f7a62e99c4c1a
     * return_url : TuanZhang
     * pid : 2019100113
     * type : 2
     * userid : 10000039
     */

    private String out_trade_no;
    private String money;
    private String sign;
    private String return_url;
    private String userip;
    private String pid;
    private String type;
    private String notify_url;
    private String userid;


    @NonNull
    @Override
    public String toString() {
        return "out_trade_no=" + out_trade_no + "&money=" + money + "&sign=" + sign + "&return_url=" + return_url +
                "&userip=" + userip + "&pid=" + pid + "&type=" + type + "&notify_url=" + notify_url + "&userid=" + userid;
    }

    public String getOut_trade_no() {
        return out_trade_no == null ? "" : out_trade_no;
    }

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    public String getMoney() {
        return money == null ? "" : money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getSign() {
        return sign == null ? "" : sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getReturn_url() {
        return return_url == null ? "" : return_url;
    }

    public void setReturn_url(String return_url) {
        this.return_url = return_url;
    }

    public String getUserip() {
        return userip == null ? "" : userip;
    }

    public void setUserip(String userip) {
        this.userip = userip;
    }

    public String getPid() {
        return pid == null ? "" : pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getType() {
        return type == null ? "" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNotify_url() {
        return notify_url == null ? "" : notify_url;
    }

    public void setNotify_url(String notify_url) {
        this.notify_url = notify_url;
    }

    public String getUserid() {
        return userid == null ? "" : userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
