package com.iimm.miliao.bean.redpacket;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * MrLiu253@163.com
 *
 * @time 2020-01-08
 */
public class CloudQueryRedPacket implements Serializable {

    /**
     * packetType : 1
     * walletId : 33704400121831684
     * serialNumber : 1214832105738080256
     * packetCount : 1
     * singleAmount : 0.1
     * greeting : 恭喜发财，大吉大利!
     * orderStatus : 1
     * targetUserId : 10000017
     * userId : 10000016
     * createDateTime : 2020-01-08 16:51:59
     * debitDateTime : 2020-01-08 16:52:23
     * paymentType : BALANCE
     * merchantId : 890000595
     * receivers : []
     * requestId : 664511630784204800
     * hmac : lYSNSjjyfFCYiOD8t+dI2glf8KDN4JpuVH8LascrmG3rpLU1vpZD8wPrTAWEbOhfpa5Rm8GgG4+2YyqTbG7ZxL9RlOtNvHGNzf2QpklaMzR1zWBODWLlh+UOhiAcFmL/ALho283MGfvG5zQ/4HUFj6Yth8kySurqhcPVn11Q7brT7+8jOgjBtAlS2vlkAx6eKV/zLddK2bJcNafSWhmiCtYgUIzNYJ8G6GfsJJetTuIyy9cpVaZoI20maNkvEE9e4MCmwpKZQb1kivU2d2oa3I3YL7rihKaLx8KjnBLq/Jy8X8yWal+S0djxviL4pkQdXbdUIomsr2VjTLs5hM+6dA==
     * receiveWalletId : 33704400121831685
     * currency : CNY
     * status : SUCCESS
     */

    private int packetType;
    private String walletId;
    private String serialNumber;
    private int packetCount;//红包数量
    private String singleAmount;
    private String greeting;
    private int orderStatus;
    private String targetUserId;
    private String userId;
    private String createDateTime;
    private String debitDateTime;
    private String paymentType;
    private String merchantId;
    private String requestId;
    private String hmac;
    private String receiveWalletId;
    private String currency;
    private String status;
    private String completeDateTime;
    private String nickName;
    private String receivedAmount;
    private int receivedCount;//已领取金额
    private String amount;
    private List<ReceiversBean> receivers;

    public String getAmount() {
        return amount == null ? "" : amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public int getPacketCount() {
        return packetCount;
    }

    public void setPacketCount(int packetCount) {
        this.packetCount = packetCount;
    }

    public int getReceivedCount() {
        return receivedCount;
    }

    public void setReceivedCount(int receivedCount) {
        this.receivedCount = receivedCount;
    }

    public String getCompleteDateTime() {
        return completeDateTime == null ? "" : completeDateTime;
    }

    public void setCompleteDateTime(String completeDateTime) {
        this.completeDateTime = completeDateTime;
    }

    public String getNickName() {
        return nickName == null ? "" : nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getReceivedAmount() {
        return receivedAmount == null ? "" : receivedAmount;
    }

    public void setReceivedAmount(String receivedAmount) {
        this.receivedAmount = receivedAmount;
    }

    public List<ReceiversBean> getReceivers() {
        if (receivers == null) {
            return new ArrayList<>();
        }
        return receivers;
    }

    public void setReceivers(List<ReceiversBean> receivers) {
        this.receivers = receivers;
    }

    public int getPacketType() {
        return packetType;
    }

    public void setPacketType(int packetType) {
        this.packetType = packetType;
    }

    public String getWalletId() {
        return walletId == null ? "" : walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public String getSerialNumber() {
        return serialNumber == null ? "" : serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getSingleAmount() {
        return singleAmount == null ? "" : singleAmount;
    }

    public void setSingleAmount(String singleAmount) {
        this.singleAmount = singleAmount;
    }

    public String getGreeting() {
        return greeting == null ? "" : greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    public int getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(int orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getTargetUserId() {
        return targetUserId == null ? "" : targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getUserId() {
        return userId == null ? "" : userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCreateDateTime() {
        return createDateTime == null ? "" : createDateTime;
    }

    public void setCreateDateTime(String createDateTime) {
        this.createDateTime = createDateTime;
    }

    public String getDebitDateTime() {
        return debitDateTime == null ? "" : debitDateTime;
    }

    public void setDebitDateTime(String debitDateTime) {
        this.debitDateTime = debitDateTime;
    }

    public String getPaymentType() {
        return paymentType == null ? "" : paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
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

    public String getReceiveWalletId() {
        return receiveWalletId == null ? "" : receiveWalletId;
    }

    public void setReceiveWalletId(String receiveWalletId) {
        this.receiveWalletId = receiveWalletId;
    }

    public String getCurrency() {
        return currency == null ? "" : currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status == null ? "" : status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
