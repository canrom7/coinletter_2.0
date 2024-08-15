package com.iimm.miliao.bean;

public class ShareBean {
    private String appId;
    private String appSecret;

    // 分享的类型 目前只支持 [链接]
    private int shareType;

    private String appName;
    private String appIcon;

    // 标题
    private String title;
    // 内容
    private String subTitle;
    // 链接地址
    private String url;
    // 链接图片地址 如不填，使用icon地址
    private String imageUrl;

    public ShareBean() {
    }

    public ShareBean(String appId, String appSecret, String appName, String appIcon, String title, String subTitle, String url, String imageUrl) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.appName = appName;
        this.appIcon = appIcon;
        this.title = title;
        this.subTitle = subTitle;
        this.url = url;
        this.imageUrl = imageUrl;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public int getShareType() {
        return shareType;
    }

    public void setShareType(int shareType) {
        this.shareType = shareType;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(String appIcon) {
        this.appIcon = appIcon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
