package com.iimm.miliao.bean;

public class MicroTransferReceive {


    /**
     * serialNumber : 1217006066815078400
     * merchantId : 890000595
     * hmac : 0LbOOltSnZr0DFEGP81JlSzj9lLSPE+w+50Cz2k26NN0Q0SjFaN4ZVgMDzuwO3iLTbRrrlhcwmGMM9+CNTQ5zskhoE0LrY0/iOIyZoV1snVUhszhxCPw/5J+8qZkWrVyxA3/YLrFvOsuiJe8jXBVCICvmR4UzNbhL9XYWwV/0TQZPL0pBf6ego6poQUFz0xYUAXwOAmpjXBUSymYCtevXhzOdf9jb3K4KqSxYB+H8ToYr4croxPdEbKnNfcMZRgfVgMPnP20iZ9jJg4etfyMozlliDczSQ0wZhLbGf0Yedy01hI2Gdj4eoq0apkbmQ/WYN6y9RCBJlzpxcQDPv7IDg==
     * confirmStatus : SUCCESS
     * status : SUCCESS
     */

    private String serialNumber;
    private String merchantId;
    private String hmac;
    private String confirmStatus;
    private String status;

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

    public String getHmac() {
        return hmac == null ? "" : hmac;
    }

    public void setHmac(String hmac) {
        this.hmac = hmac;
    }

    public String getConfirmStatus() {
        return confirmStatus == null ? "" : confirmStatus;
    }

    public void setConfirmStatus(String confirmStatus) {
        this.confirmStatus = confirmStatus;
    }

    public String getStatus() {
        return status == null ? "" : status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
