package com.iimm.miliao.bean.redpacket;

/**
 * MrLiu253@163.com
 * 云红包
 *
 * @time 2020-01-08
 */
public class CloudRedPacket {


    /**
     * walletId : 33704400121831684
     * amount : 0.1
     * serialNumber : 1214794282079240192
     * merchantId : 890000595
     * requestId : 664473804789125120
     * hmac : HXekQ8e38Jcamg9wNPEYKFjAiJtJbr354ycBbsUxodf+cKmyT1PMfI/issCSxzeIAoNZpsXyu3wJw6XlP/VRvYZOBG8pnt0gJqY2V7g+fM656w7V69ONMjIaY3Gv/g+jNfPwTRKu9G5C4PwjJ4o5ylfTUDpIjEAJaE+XmSaZ8KBJOEVRwlOk2BGmeXv+j1S3zCysjw+hkUOyp3uCLhylioPREMN3n8YQdxpXGlmGS23jZoPI/SEfw3mmZUGFjDHfVyHYmhUHWpre7WYtTBvr+h9BUSbk7cgHYW3BQVrJ6afYB+aZc01qvASGyxDy56sXdkZljVlqH8YY2WmA3vOGLA==
     * orderStatus : INIT
     * currency : CNY
     * createDateTime : 2020-01-08 14:21:41
     * status : SUCCESS
     * token : 20200108582890399568279355871232
     */

    private String walletId;
    private String amount;
    private String serialNumber;
    private String merchantId;
    private String requestId;
    private String hmac;
    private int orderStatus;
    private String currency;
    private String createDateTime;
    private String status;
    private String token;

    public String getWalletId() {
        return walletId == null ? "" : walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public String getAmount() {
        return amount == null ? "" : amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getSerialNumber() {
        return serialNumber == null ? "" : serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getMerchantId() {
        return merchantId == null ? "" : merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getRequestId() {
        return requestId == null ? "" : requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getHmac() {
        return hmac == null ? "" : hmac;
    }

    public void setHmac(String hmac) {
        this.hmac = hmac;
    }

    public int getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(int orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getCurrency() {
        return currency == null ? "" : currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCreateDateTime() {
        return createDateTime == null ? "" : createDateTime;
    }

    public void setCreateDateTime(String createDateTime) {
        this.createDateTime = createDateTime;
    }

    public String getStatus() {
        return status == null ? "" : status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getToken() {
        return token == null ? "" : token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
