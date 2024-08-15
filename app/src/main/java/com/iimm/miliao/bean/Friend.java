package com.iimm.miliao.bean;

import android.text.TextUtils;

import com.alibaba.fastjson.annotation.JSONField;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable
public class Friend implements Serializable {
    private static final long serialVersionUID = -6859528031175998594L;
    @DatabaseField(generatedId = true)
    private int _id;

    @DatabaseField(canBeNull = false)
    private String ownerId; // 属于哪个用户的id

    @DatabaseField(canBeNull = false)
    private String userId; // 用户id或者聊天室id

    @DatabaseField(canBeNull = false)
    @JSONField(name = "nickname")
    private String nickName;// 用户昵称或者聊天室名称

    @DatabaseField
    private String description;// 签名

    @DatabaseField
    private long timeCreate;// 创建好友关系的时间

    @DatabaseField(defaultValue = "0")
    private int unReadNum; // 未读消息数量

    @DatabaseField
    private String content;// 最后一条消息内容

    @DatabaseField
    private int type;// 最后一条消息类型

    @DatabaseField
    private long timeSend;// 最后一条消息发送时间

    @DatabaseField(defaultValue = "0")
    private int roomFlag;// 0朋友 1群组 510（我的联系人群组）

    @DatabaseField(defaultValue = "0")
    private int companyId; // 0表示不是公司

    @DatabaseField
    private int status;// -1:黑名单；0：陌生人；1:单方关注；2:互为好友；8:系统号；9:非显示系统号

    @DatabaseField
    private String privacy;// 隐私

    @DatabaseField
    private String remarkName;// 备注

    @DatabaseField
    private String describe;// 描述，

    @DatabaseField
    private int version;// 本地表的版本

    @DatabaseField
    private String roomId;// 仅仅当roomFlag==1，为群组的时候才有效

    @DatabaseField
    private String roomCreateUserId;// 仅仅当roomFlag==1，为群组的时候才有效

    @DatabaseField
    private String roomMyNickName;// 我在这个群组的昵称

    @DatabaseField
    private long roomTalkTime;// 在这个群组的禁言时间(群禁言)
    @DatabaseField
    private long roomMyTalkTime;// 我在这个群组的禁言时间(个人被禁言)

    @DatabaseField(defaultValue = "0")
    private long topTime;
    // 0:正常 1:被踢出该群组 2:该群已被解散 3:该群已被后台锁定
    @DatabaseField(defaultValue = "0")
    private int groupStatus;
    // 是否为设备，如果为设备，该Friend的userId为android || ios || pc...，之后需要在各个地方判断
    @DatabaseField(defaultValue = "0")
    private int isDevice;
    // 消息免打扰 0:未设置 1:已设置
    @DatabaseField(defaultValue = "0")
    private int offlineNoPushMsg;
    @DatabaseField(defaultValue = "0")
    private double chatRecordTimeOut;//0 || -1 消息永久保存 单位：day
    @DatabaseField(defaultValue = "0")
    private long downloadTime;// 最后一次同步消息的时间
    @DatabaseField(defaultValue = "0")
    private int isAtMe;// 是否有人@我 0 正常 1 @我 2 @全体成员

    private boolean isSelect; //标志 此对象 是否被选中

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public long getTopTime() {
        return topTime;
    }

    public void setTopTime(long topTime) {
        this.topTime = topTime;
    }

    public int getGroupStatus() {
        return groupStatus;
    }

    public void setGroupStatus(int groupStatus) {
        this.groupStatus = groupStatus;
    }

    public int getIsDevice() {
        return isDevice;
    }

    public void setIsDevice(int isDevice) {
        this.isDevice = isDevice;
    }

    public int getOfflineNoPushMsg() {
        return offlineNoPushMsg;
    }

    public void setOfflineNoPushMsg(int offlineNoPushMsg) {
        this.offlineNoPushMsg = offlineNoPushMsg;
    }

    public double getChatRecordTimeOut() {
        return chatRecordTimeOut;
    }

    public void setChatRecordTimeOut(double chatRecordTimeOut) {
        this.chatRecordTimeOut = chatRecordTimeOut;
    }

    public long getDownloadTime() {
        return downloadTime;
    }

    public void setDownloadTime(long downloadTime) {
        this.downloadTime = downloadTime;
    }

    public int getIsAtMe() {
        return isAtMe;
    }

    public void setIsAtMe(int isAtMe) {
        this.isAtMe = isAtMe;
    }

    public String getRoomId() {
        return roomId == null ? "" : roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomCreateUserId() {
        return roomCreateUserId;
    }

    public void setRoomCreateUserId(String roomCreateUserId) {
        this.roomCreateUserId = roomCreateUserId;
    }

    public String getRemarkName() {
        return remarkName == null ? "" : remarkName;
    }

    public void setRemarkName(String remarkName) {
        this.remarkName = remarkName;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName == null ? "" : nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTimeCreate() {
        return timeCreate;
    }

    public void setTimeCreate(long timeCreate) {
        this.timeCreate = timeCreate;
    }

    public int getUnReadNum() {
        return unReadNum;
    }

    public void setUnReadNum(int unReadNum) {
        this.unReadNum = unReadNum;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTimeSend() {
        return timeSend;
    }

    public void setTimeSend(long timeSend) {
        this.timeSend = timeSend;
    }

    public int getRoomFlag() {
        return roomFlag;
    }

    public void setRoomFlag(int roomFlag) {
        this.roomFlag = roomFlag;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getRoomMyNickName() {
        return roomMyNickName;
    }

    public void setRoomMyNickName(String roomMyNickName) {
        this.roomMyNickName = roomMyNickName;
    }

    public long getRoomTalkTime() {
        return roomTalkTime;
    }

    public void setRoomTalkTime(long roomTalkTime) {
        this.roomTalkTime = roomTalkTime;
    }

    public long getRoomMyTalkTime() {
        return roomMyTalkTime;
    }

    public void setRoomMyTalkTime(long roomMyTalkTime) {
        this.roomMyTalkTime = roomMyTalkTime;
    }

    /* 快捷方法，获取在好友列表中显示的名称 */
    public String getShowName() {
        if (!TextUtils.isEmpty(remarkName)) {
            return remarkName.trim();
        } else if (!TextUtils.isEmpty(nickName)) {
            return nickName.trim();
        } else {
            return "";
        }
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }
}
