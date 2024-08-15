package com.iimm.miliao.bean;

public class TransferMicro {

    /**
     * walletId : 33704400121831923
     * amount : 2
     * serialNumber : 1216909291043299328
     * merchantId : 890000595
     * requestId : 666588815904870400
     * hmac : 0KPan9N/xHC4zi14z3KjECvJDXi3BxvBQTF2ehZyWYZnmwEkmQb1QZUJ6uPajNYd4WYezVxKOUooCvKw+KEIa3H673j478ux15LENlah1GvFBCbd8Xx3Netd5LoyrC34HRjxpZDT8gFFJMdOdb5LEq3yCliV9GoWy2XXY92u7TS/OQ3ABF9Mj0u4Lzu6OCop51yLPlITrgdrFk76OazpGF9DkSJvhUyAY1lCv/higtfbgaK27zN/yTsUk9aULAlX2s6/sWL39VaworQaF8TyGK0VXYGrDrAeXGgmGHNnMynYy/t0sa6dMDNuY3W/Lzj9NyN76DHxY8dZTZtPFhGdJA==
     * orderStatus : INIT
     * currency : CNY
     * targetWalletId : 33704400121831926
     * createDateTime : 2020-01-14 10:25:58
     * status : SUCCESS
     * token : 20200114582880401683287883730944
     */

    private String walletId;
    private String amount;
    private String serialNumber;
    private String merchantId;
    private String requestId;
    private String hmac;
    private String orderStatus;
    private String currency;
    private String targetWalletId;
    private String createDateTime;
    private String status;
    private String token;
    private String debitDateTime;
    private String completeDateTime;
    private String userId;//发送人id
    private String targetUserId;//接收人id
    private String remark;//备注

    public String getUserId() {
        return userId == null ? "" : userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTargetUserId() {
        return targetUserId == null ? "" : targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getRemark() {
        return remark == null ? "" : remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCompleteDateTime() {
        return completeDateTime == null ? "" : completeDateTime;
    }

    public void setCompleteDateTime(String completeDateTime) {
        this.completeDateTime = completeDateTime;
    }

    public String getDebitDateTime() {
        return debitDateTime == null ? "" : debitDateTime;
    }

    public void setDebitDateTime(String debitDateTime) {
        this.debitDateTime = debitDateTime;
    }

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

    public String getOrderStatus() {
        return orderStatus == null ? "" : orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getCurrency() {
        return currency == null ? "" : currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTargetWalletId() {
        return targetWalletId == null ? "" : targetWalletId;
    }

    public void setTargetWalletId(String targetWalletId) {
        this.targetWalletId = targetWalletId;
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
