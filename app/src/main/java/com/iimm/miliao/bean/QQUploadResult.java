package com.iimm.miliao.bean;

public class QQUploadResult {

    /**
     * ret : 0
     * openid : 6C6B9064E0D9463F87AC74978A46B102
     * access_token : 6F1458BB1FAABFD3417BACEA1E451E9D
     * pay_token : A5E431A062D834A0097F8D51A2466C10
     * expires_in : 7776000
     * pf : desktop_m_qq-10000144-android-2002-
     * pfkey : 0cf2ee6c748652cdfe2371d0f81fa70b
     * msg :
     * login_cost : 233
     * query_authority_cost : 323
     * authority_cost : 1847
     * expires_time : 1571826833796
     */

    private int ret;
    private String openid;
    private String access_token;
    private String pay_token;
    private int expires_in;
    private String pf;
    private String pfkey;
    private String msg;
    private int login_cost;
    private int query_authority_cost;
    private int authority_cost;
    private long expires_time;

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public String getOpenid() {
        return openid == null ? "" : openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getAccess_token() {
        return access_token == null ? "" : access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getPay_token() {
        return pay_token == null ? "" : pay_token;
    }

    public void setPay_token(String pay_token) {
        this.pay_token = pay_token;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }

    public String getPf() {
        return pf == null ? "" : pf;
    }

    public void setPf(String pf) {
        this.pf = pf;
    }

    public String getPfkey() {
        return pfkey == null ? "" : pfkey;
    }

    public void setPfkey(String pfkey) {
        this.pfkey = pfkey;
    }

    public String getMsg() {
        return msg == null ? "" : msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getLogin_cost() {
        return login_cost;
    }

    public void setLogin_cost(int login_cost) {
        this.login_cost = login_cost;
    }

    public int getQuery_authority_cost() {
        return query_authority_cost;
    }

    public void setQuery_authority_cost(int query_authority_cost) {
        this.query_authority_cost = query_authority_cost;
    }

    public int getAuthority_cost() {
        return authority_cost;
    }

    public void setAuthority_cost(int authority_cost) {
        this.authority_cost = authority_cost;
    }

    public long getExpires_time() {
        return expires_time;
    }

    public void setExpires_time(long expires_time) {
        this.expires_time = expires_time;
    }
}
