package com.iimm.miliao.bean;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.iimm.miliao.BuildConfig;
import com.iimm.miliao.Reporter;

import java.util.List;

/**
 * @编写人： 未知
 * @时间： 2016/4/28 10:43
 * @说明： tanx补注释
 * @功能： 在所有的接口初始化之前，会向服务器获取接口配置，该类保存获取的配置
 **/
public class ConfigBean {
    private String ftpHost;    // ftp(无用)
    private String ftpUsername;// ftp用户名(无用)
    private String ftpPassword;// ftp密码(无用)
    private String androidAppUrl;// AndroidApp下载地址
    private String androidExplain;
    private String androidVersion;// 版本号
    private String androidDisable;// 禁用版本号，包括这个和更低版本，
    private String apiUrl;// Api的服务器地址
    private String uploadUrl;// 上传的服务器地址
    private String downloadUrl;// 头像以外的东西的下载地址
    private String downloadAvatarUrl;// 下载头像的前缀
    private String XMPPHost;  // xmpp主机
    private String XMPPDomain;// xmpp群聊的域名
    private int xmppPingTime; // 每隔xmppPingTime秒ping一次服务器
    private int XMPPTimeout;  // Xmpp超时时长(服务器针对客户端的超时时长)
    private int isOpenCluster;    // 是否开启集群
    private int isOpenReceipt = 1;// 是否请求回执
    private int hideSearchByFriends = 1;// 是否隐藏好友搜索功能 0:隐藏 1：开启
    /**
     * 注册邀请码   registerInviteCode
     * 0:关闭
     * 1:开启一对一邀请（一码一用，且必填）
     * <p>
     * 2:开启一对多邀请（一码多用，选填项），该模式下客户端需要把用户自己的邀请码显示出来
     */
    private String address;
    private int registerInviteCode;
    private int nicknameSearchUser = 2; //昵称搜索用户  0 :关闭       1:精确搜索    2:模糊搜索   默认模糊搜索
    private int regeditPhoneOrName;// 0：使用手机号注册，1：使用用户名注册
    private int isCommonFindFriends = 0;// 普通用户是否能搜索好友 0:允许 1：不允许
    private int isCommonCreateGroup = 0;// 普通用户是否能建群 0:允许 1：不允许
    private int displayRedPacket;//是否开启红包功能 0:隐藏 1：开启
    private int isOpenPositionService = 0;// 是否开启位置相关服务 0：开启 1：关闭
    private int isOpenGoogleFCM = 0;// 是否打开Android Google推送 1：开启 0：关闭
    private String popularAPP;// 热门应用  lifeCircle  生活圈，  videoMeeting 视频会议，  liveVideo 视频直播，  shortVideo 短视频， peopleNearby 附近的人
    private String isOpenRegister;// 是否开放注册，
    private String isOpenSMSCode; // 是否需要短信验证码，
    private String jitsiServer;// jitsi的前缀地址
    private int isQestionOpen;//是否需要密保问题 1需要
    // 关于页面的信息，
    private String companyName;
    private String copyright;
    private String website = "http://example.com/im-download.html";
    private String headBackgroundImg;
    private int fileValidTime = -1;// 文件保存时长，默认永久
    private int isUserSignRedPacket; //是否有签到红包
    private List<NodeInfo> nodesInfoList;//登录节点信息
    private int isNodesStatus;//是否支持多节点登录。0 不支持。1 支持
    private int isOpenOSStatus;//是否开启了obs 服务，1 开启  0 未开启

    private String accessKeyId; //ak  obs 用户 唯一 表示  ，已通过 RSA 加密
    private String accessSecretKey;// sk   obs 密钥  配合 ak 使用  已通过 RSA 加密
    private String bucketName;// 桶 名称  服务器提供，所有上传文件存入 此桶  当isOpenOBSStatus 为0时 此值无效
    private String endPoint;//访问点   当isOpenOBSStatus 为0时 此值无效
    private String location;// 访问区域 服务器已为桶配置 无需配置   当isOpenOBSStatus 为0时 此值无效
    private int osType; //Obs 类型  当isOpenOBSStatus 为 1 时  此值为1 即华为云   此值为2 即腾讯云  当isOpenOBSStatus 为0时 此值无效
    private String osName;// 当前Obs 名称  当isOpenOBSStatus 为0时 此值无效
    private String osAppId;//只有腾讯云COS 才会用到
    private TabBarConfigListBean tabBarConfigList;
    private int isWithdrawToAdmin;//是否开启提现到台
    private String minWithdrawToAdmin; //提现到后台，最小金额 单位 是 元

    private int isOpenTwoBarCode = 1; //是否显示 个人信息页的二维码 条目  0 不显示 1.显示
    private int isOpenTelnum = 1;//是否显示个人信息页的手机号条目 ，0不显示 1.显示
    private String wechatAppId;
    private int wechatPayStatus;//是否支持微信充值
    private int wechatWithdrawStatus;//是否支持微信提现
    private int aliPayStatus;//是否支持支付宝充值
    private int aliWithdrawStatus;//是否支持支付宝提现
    //这里为什么有两个银行卡充值判断值？就是这么操蛋...做兼容的 服务器更新后 isYunPayOpen 废弃
    private int yunPayStatus;//是否支持银行卡充值
    private int isYunPayOpen;  //是否支持银行卡充值
    private int qqLoginStatus;//是否支持QQ登录 1：开启 2：关闭
    private int wechatLoginStatus;//是否支持微信登录 1：开启 2：关闭
    private String qqLoginAppId;//QQId
    private String transferRate;//费率
    private String wechatLoginAppId;
    private int hmPayStatus;
    private int hmWithdrawStatus;
    private int isWeiBaoStatus;//是否开通云钱包，1开通
    public String weiBaoMaxTransferAmount;//微钱包最大提现金额
    public String weiBaoMaxRedPacketAmount;//微红包最大金额
    public String weiBaoMinTransferAmount;//微钱包最小提现金额
    public String weiBaoTransferRate;//微提现费率
    private int isAudioStatus;//是否开启音视频  0 关闭 1开启
    private String maxSendRedPagesAmount;//群红包最大金额
    private int isDelAfterReading;//是否开启阅后即焚
    private int isEnableCusServer;//是否开启联系客服  0关闭  1开启
    private String cusServerUrl;//联系客服地址

    public int getIsEnableCusServer() {
        return isEnableCusServer;
    }

    public void setIsEnableCusServer(int isEnableCusServer) {
        this.isEnableCusServer = isEnableCusServer;
    }

    public String getCusServerUrl() {
        return cusServerUrl == null ? "" : cusServerUrl;
    }

    public void setCusServerUrl(String cusServerUrl) {
        this.cusServerUrl = cusServerUrl;
    }

    public int getIsDelAfterReading() {
        return isDelAfterReading;
    }

    public void setIsDelAfterReading(int isDelAfterReading) {
        this.isDelAfterReading = isDelAfterReading;
    }

    public String getMaxSendRedPagesAmount() {
        return maxSendRedPagesAmount == null ? "500.00" : maxSendRedPagesAmount;
    }

    public void setMaxSendRedPagesAmount(String maxSendRedPagesAmount) {
        this.maxSendRedPagesAmount = maxSendRedPagesAmount;
    }

    public int getIsAudioStatus() {
        return isAudioStatus;
    }

    public void setIsAudioStatus(int isAudioStatus) {
        this.isAudioStatus = isAudioStatus;
    }

    public String getWeiBaoMaxTransferAmount() {
        return weiBaoMaxTransferAmount == null ? "" : weiBaoMaxTransferAmount;
    }

    public void setWeiBaoMaxTransferAmount(String weiBaoMaxTransferAmount) {
        this.weiBaoMaxTransferAmount = weiBaoMaxTransferAmount;
    }

    public String getWeiBaoMaxRedPacketAmount() {
        return weiBaoMaxRedPacketAmount == null ? "" : weiBaoMaxRedPacketAmount;
    }

    public void setWeiBaoMaxRedPacketAmount(String weiBaoMaxRedPacketAmount) {
        this.weiBaoMaxRedPacketAmount = weiBaoMaxRedPacketAmount;
    }

    public String getWeiBaoMinTransferAmount() {
        return weiBaoMinTransferAmount == null ? "" : weiBaoMinTransferAmount;
    }

    public void setWeiBaoMinTransferAmount(String weiBaoMinTransferAmount) {
        this.weiBaoMinTransferAmount = weiBaoMinTransferAmount;
    }

    public String getWeiBaoTransferRate() {
        return weiBaoTransferRate == null ? "" : weiBaoTransferRate;
    }

    public void setWeiBaoTransferRate(String weiBaoTransferRate) {
        this.weiBaoTransferRate = weiBaoTransferRate;
    }

    public int getIsWeiBaoStatus() {
        return isWeiBaoStatus;
    }

    public void setIsWeiBaoStatus(int isWeiBaoStatus) {
        this.isWeiBaoStatus = isWeiBaoStatus;
    }

    public int getHmPayStatus() {
        return hmPayStatus;
    }

    public void setHmPayStatus(int hmPayStatus) {
        this.hmPayStatus = hmPayStatus;
    }

    public int getHmWithdrawStatus() {
        return hmWithdrawStatus;
    }

    public void setHmWithdrawStatus(int hmWithdrawStatus) {
        this.hmWithdrawStatus = hmWithdrawStatus;
    }

    public String getWechatLoginAppId() {
        return wechatLoginAppId == null ? "" : wechatLoginAppId;
    }

    public void setWechatLoginAppId(String wechatLoginAppId) {
        this.wechatLoginAppId = wechatLoginAppId;
    }

    public String getTransferRate() {
        return transferRate == null ? "" : transferRate;
    }

    public void setTransferRate(String transferRate) {
        this.transferRate = transferRate;
    }

    public String getQqLoginAppId() {
        return TextUtils.isEmpty(qqLoginAppId) ? BuildConfig.QQ_APP_ID : qqLoginAppId;
    }

    public void setQqLoginAppId(String qqLoginAppId) {
        this.qqLoginAppId = qqLoginAppId;
    }

    public int getQqLoginStatus() {
        return qqLoginStatus;
    }

    public void setQqLoginStatus(int qqLoginStatus) {
        this.qqLoginStatus = qqLoginStatus;
    }

    public int getWechatLoginStatus() {
        return wechatLoginStatus;
    }

    public void setWechatLoginStatus(int wechatLoginStatus) {
        this.wechatLoginStatus = wechatLoginStatus;
    }

    public int getIsYunPayOpen() {
        return isYunPayOpen;
    }

    public void setIsYunPayOpen(int isYunPayOpen) {
        this.isYunPayOpen = isYunPayOpen;
    }

    public int getIsQestionOpen() {
        return isQestionOpen;
    }

    public void setIsQestionOpen(int isQestionOpen) {
        this.isQestionOpen = isQestionOpen;
    }

    public int getIsOpenTwoBarCode() {
        return isOpenTwoBarCode;
    }

    public void setIsOpenTwoBarCode(int isOpenTwoBarCode) {
        this.isOpenTwoBarCode = isOpenTwoBarCode;
    }

    public int getIsOpenTelnum() {
        return isOpenTelnum;
    }

    public void setIsOpenTelnum(int isOpenTelnum) {
        this.isOpenTelnum = isOpenTelnum;
    }

    public int getIsWithdrawToAdmin() {
        return isWithdrawToAdmin;
    }

    public void setIsWithdrawToAdmin(int isWithdrawToAdmin) {
        this.isWithdrawToAdmin = isWithdrawToAdmin;
    }

    public String getMinWithdrawToAdmin() {
        return minWithdrawToAdmin == null ? "" : minWithdrawToAdmin;
    }

    public void setMinWithdrawToAdmin(String minWithdrawToAdmin) {
        this.minWithdrawToAdmin = minWithdrawToAdmin;
    }

    public int getIsUserSignRedPacket() {
        return isUserSignRedPacket;
    }

    public void setIsUserSignRedPacket(int isUserSignRedPacket) {
        this.isUserSignRedPacket = isUserSignRedPacket;
    }

    public TabBarConfigListBean getTabBarConfigList() {
        return tabBarConfigList;
    }

    public void setTabBarConfigList(TabBarConfigListBean tabBarConfigList) {
        this.tabBarConfigList = tabBarConfigList;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getFtpHost() {
        return ftpHost;
    }

    public void setFtpHost(String ftpHost) {
        this.ftpHost = ftpHost;
    }

    public String getFtpUsername() {
        return ftpUsername;
    }

    public void setFtpUsername(String ftpUsername) {
        this.ftpUsername = ftpUsername;
    }

    public String getFtpPassword() {
        return ftpPassword;
    }

    public void setFtpPassword(String ftpPassword) {
        this.ftpPassword = ftpPassword;
    }

    public String getAndroidAppUrl() {
        return androidAppUrl;
    }

    public void setAndroidAppUrl(String androidAppUrl) {
        this.androidAppUrl = androidAppUrl;
    }

    public String getAndroidExplain() {
        return androidExplain;
    }

    public void setAndroidExplain(String androidExplain) {
        this.androidExplain = androidExplain;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
    }

    public String getAndroidDisable() {
        return androidDisable;
    }

    public void setAndroidDisable(String androidDisable) {
        this.androidDisable = androidDisable;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getDownloadAvatarUrl() {
        return downloadAvatarUrl;
    }

    public void setDownloadAvatarUrl(String downloadAvatarUrl) {
        this.downloadAvatarUrl = downloadAvatarUrl;
    }

    public String getXMPPHost() {
        return XMPPHost;
    }

    public void setXMPPHost(String xMPPHost) {
        XMPPHost = xMPPHost;
    }

    public String getXMPPDomain() {
        return XMPPDomain;
    }

    public void setXMPPDomain(String xMPPDomain) {
        XMPPDomain = xMPPDomain;
    }

    public int getXmppPingTime() {
        return xmppPingTime;
    }

    public void setXmppPingTime(int xmppPingTime) {
        this.xmppPingTime = xmppPingTime;
    }

    public int getXMPPTimeout() {
        return XMPPTimeout;
    }

    public void setXMPPTimeout(int XMPPTimeout) {
        this.XMPPTimeout = XMPPTimeout;
    }

    public int getIsOpenCluster() {
        return isOpenCluster;
    }

    public void setIsOpenCluster(int isOpenCluster) {
        this.isOpenCluster = isOpenCluster;
    }

    public int getIsOpenReceipt() {
        return isOpenReceipt;
    }

    public void setIsOpenReceipt(int isOpenReceipt) {
        this.isOpenReceipt = isOpenReceipt;
    }

    public String getIsOpenRegister() {
        return isOpenRegister;
    }

    public void setIsOpenRegister(String isOpenRegister) {
        this.isOpenRegister = isOpenRegister;
    }

    public String getIsOpenSMSCode() {
        return isOpenSMSCode;
    }

    public void setIsOpenSMSCode(String isOpenSMSCode) {
        this.isOpenSMSCode = isOpenSMSCode;
    }

    public String getJitsiServer() {
        return jitsiServer;
    }

    public void setJitsiServer(String jitsiServer) {
        this.jitsiServer = jitsiServer;
    }

    public int getFileValidTime() {
        return fileValidTime;
    }

    public void setFileValidTime(int fileValidTime) {
        this.fileValidTime = fileValidTime;
    }

    public int getHideSearchByFriends() {
        return hideSearchByFriends;
    }

    public void setHideSearchByFriends(int hideSearchByFriends) {
        this.hideSearchByFriends = hideSearchByFriends;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getRegisterInviteCode() {
        return registerInviteCode;
    }

    public void setRegisterInviteCode(int registerInviteCode) {
        this.registerInviteCode = registerInviteCode;
    }

    public int getNicknameSearchUser() {
        return nicknameSearchUser;
    }

    public void setNicknameSearchUser(int nicknameSearchUser) {
        this.nicknameSearchUser = nicknameSearchUser;
    }

    public int getRegeditPhoneOrName() {

        return regeditPhoneOrName;
    }

    public void setRegeditPhoneOrName(int regeditPhoneOrName) {
        this.regeditPhoneOrName = regeditPhoneOrName;
    }

    public int getIsCommonFindFriends() {
        return isCommonFindFriends;
    }

    public void setIsCommonFindFriends(int isCommonFindFriends) {
        this.isCommonFindFriends = isCommonFindFriends;
    }

    public int getIsCommonCreateGroup() {
        return isCommonCreateGroup;
    }

    public void setIsCommonCreateGroup(int isCommonCreateGroup) {
        this.isCommonCreateGroup = isCommonCreateGroup;
    }

    public int getDisplayRedPacket() {
        return displayRedPacket;
    }

    public void setDisplayRedPacket(int displayRedPacket) {
        this.displayRedPacket = displayRedPacket;
    }

    public int getIsOpenPositionService() {
        return isOpenPositionService;//服务器返回的数据，1是关闭，0是开启
    }

    public void setIsOpenPositionService(int isOpenPositionService) {
        this.isOpenPositionService = isOpenPositionService;
    }

    public String getHeadBackgroundImg() {
        return headBackgroundImg;
    }

    public void setHeadBackgroundImg(String headBackgroundImg) {
        this.headBackgroundImg = headBackgroundImg;
    }

    public int getIsOpenGoogleFCM() {
        return isOpenGoogleFCM;
    }

    public void setIsOpenGoogleFCM(int isOpenGoogleFCM) {
        this.isOpenGoogleFCM = isOpenGoogleFCM;
    }

    public String getPopularAPP() {
        return popularAPP;
    }

    public void setPopularAPP(String popularAPP) {
        this.popularAPP = popularAPP;
    }

    public List<NodeInfo> getNodesInfoList() {
        return nodesInfoList;
    }

    public void setNodesInfoList(List<NodeInfo> nodesInfoList) {
        this.nodesInfoList = nodesInfoList;
    }

    public int getIsNodesStatus() {
        return isNodesStatus;
    }

    public void setIsNodesStatus(int isNodesStatus) {
        this.isNodesStatus = isNodesStatus;
    }

    public int getIsOpenOSStatus() {
        return isOpenOSStatus;
    }

    public void setIsOpenOSStatus(int isOpenOSStatus) {
        this.isOpenOSStatus = isOpenOSStatus;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessSecretKey() {
        return accessSecretKey;
    }

    public void setAccessSecretKey(String accessSecretKey) {
        this.accessSecretKey = accessSecretKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getOsType() {
        return osType;
    }

    public void setOsType(int osType) {
        this.osType = osType;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsAppId() {
        return osAppId;
    }

    public String getWechatAppId() {
        return TextUtils.isEmpty(wechatAppId) ? (TextUtils.isEmpty(getWechatLoginAppId()) ? BuildConfig.WX_APP_KEY : getWechatLoginAppId()) : wechatAppId;
    }

    public void setWechatAppId(String wechatAppId) {
        this.wechatAppId = wechatAppId;
    }

    public int getWechatPayStatus() {
        return wechatPayStatus;
    }

    public void setWechatPayStatus(int wechatPayStatus) {
        this.wechatPayStatus = wechatPayStatus;
    }

    public int getWechatWithdrawStatus() {
        return wechatWithdrawStatus;
    }

    public void setWechatWithdrawStatus(int wechatWithdrawStatus) {
        this.wechatWithdrawStatus = wechatWithdrawStatus;
    }

    public int getAliPayStatus() {
        return aliPayStatus;
    }

    public void setAliPayStatus(int aliPayStatus) {
        this.aliPayStatus = aliPayStatus;
    }

    public int getAliWithdrawStatus() {
        return aliWithdrawStatus;
    }

    public void setAliWithdrawStatus(int aliWithdrawStatus) {
        this.aliWithdrawStatus = aliWithdrawStatus;
    }

    public void setOsAppId(String osAppId) {
        this.osAppId = osAppId;
    }

    public PopularApp getPopularAPPBean() {
        PopularApp popularAppBean = null;
        try {
            popularAppBean = JSON.parseObject(popularAPP, PopularApp.class);
        } catch (Exception e) {
            Reporter.unreachable(e);
        }
        if (popularAppBean == null) {
            popularAppBean = new PopularApp();
        }
        return popularAppBean;
    }

    public int getYunPayStatus() {
        return yunPayStatus;
    }

    public void setYunPayStatus(int yunPayStatus) {
        this.yunPayStatus = yunPayStatus;
    }

    // 热门应用  lifeCircle  生活圈，  videoMeeting 视频会议，  liveVideo 视频直播，  shortVideo 短视频， peopleNearby 附近的人
    public static class PopularApp {
        public int lifeCircle = 1;
        public int videoMeeting = 1;
        public int liveVideo = 1;
        public int shortVideo = 1;
        public int peopleNearby = 1;
        public int scan = 1;
    }

    public static class TabBarConfigListBean {
        /**
         * tabBarId : 5d2ebab68e62bad6c8e5b3ee
         * tabBarLinkUrl : https://www.layui.com/demo/laytpl.html
         * tabBarName : 发现
         * tabBarNum : 1
         * tabBarStatus : 1
         * tabBarUpdateTime : 1563343542
         */

        private String tabBarId;
        private String tabBarImg;
        private String tabBarImg1;
        private String tabBarLinkUrl;
        private String tabBarName;
        private int tabBarNum;
        private int tabBarStatus;
        private String tabBarUpdateTime;

        public String getTabBarImg1() {
            return tabBarImg1 == null ? "" : tabBarImg1;
        }

        public void setTabBarImg1(String tabBarImg1) {
            this.tabBarImg1 = tabBarImg1;
        }

        public String getTabBarUpdateTime() {
            return tabBarUpdateTime == null ? "" : tabBarUpdateTime;
        }

        public void setTabBarUpdateTime(String tabBarUpdateTime) {
            this.tabBarUpdateTime = tabBarUpdateTime;
        }

        public String getTabBarId() {
            return tabBarId == null ? "" : tabBarId;
        }

        public void setTabBarId(String tabBarId) {
            this.tabBarId = tabBarId;
        }

        public String getTabBarImg() {
            return tabBarImg == null ? "" : tabBarImg;
        }

        public void setTabBarImg(String tabBarImg) {
            this.tabBarImg = tabBarImg;
        }

        public String getTabBarLinkUrl() {
            return tabBarLinkUrl == null ? "" : tabBarLinkUrl;
        }

        public void setTabBarLinkUrl(String tabBarLinkUrl) {
            this.tabBarLinkUrl = tabBarLinkUrl;
        }

        public String getTabBarName() {
            return tabBarName == null ? "" : tabBarName;
        }

        public void setTabBarName(String tabBarName) {
            this.tabBarName = tabBarName;
        }

        public int getTabBarNum() {
            return tabBarNum;
        }

        public void setTabBarNum(int tabBarNum) {
            this.tabBarNum = tabBarNum;
        }

        public int getTabBarStatus() {
            return tabBarStatus;
        }

        public void setTabBarStatus(int tabBarStatus) {
            this.tabBarStatus = tabBarStatus;
        }
    }

}
