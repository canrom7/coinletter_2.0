package com.iimm.miliao.bean;

public class BankRechargeStatusBean {

    /**
     * amount : 100.98
     * statusMsg : 请确认您已支付成功,如您已付款,请联系客服,核实处理;如你多次恶意提交虚假订单，系统将对您账户进行封禁处理
     * title : 充值通知
     * drawee :
     * payStatus : 6
     */

    private String amount;
    private String statusMsg;
    private String title;
    private String drawee;
    private String payStatus;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDrawee() {
        return drawee;
    }

    public void setDrawee(String drawee) {
        this.drawee = drawee;
    }

    public String getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
    }
}
