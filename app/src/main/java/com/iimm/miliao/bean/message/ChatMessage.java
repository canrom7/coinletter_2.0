package com.iimm.miliao.bean.message;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.db.dao.ChatMessageDaoImpl;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.timer.ItemTimer;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToolUtils;
import com.iimm.miliao.xmpp.ListenerManager;
import com.iimm.miliao.xmpp.XmppReceiptImpl;
import com.iimm.miliao.xmpp.util.ImHelper;

/**
 * @author WangQX
 * @date 2019/8/2 0002 16:06
 * description:
 */
@DatabaseTable(daoClass = ChatMessageDaoImpl.class)
public class ChatMessage extends XmppMessage implements Parcelable {
    public static final Creator<ChatMessage> CREATOR = new Creator<ChatMessage>() {
        @Override
        public ChatMessage createFromParcel(Parcel source) {
            ChatMessage message = new ChatMessage();
            message._id = source.readInt();
            message.content = source.readString();
            message.filePath = source.readString();
            message.fileSize = source.readInt();
            message.fromUserId = source.readString();
            message.toUserId = source.readString();
            message.fromUserName = source.readString();
          /*  boolean[] val = {message.isDownload, message.isMySend, message.isRead, message.isUpload};
            source.readBooleanArray(val);*/
            message.location_x = source.readString();
            message.location_y = source.readString();
            message.messageState = source.readInt();
            message.objectId = source.readString();
            message.packetId = source.readString();
            message.sipDuration = source.readInt();
            message.sipStatus = source.readInt();
            message.timeLen = source.readInt();
            message.timeReceive = source.readInt();
            // message.timeSend = source.readLong();
            message.timeSend = source.readDouble();
            message.deleteTime = source.readLong();
            message.type = source.readInt();

            // TODO 6.
            message.isReadDel = source.readInt();
            message.isEncrypt = source.readInt();

            message.fromId = source.readString();
            message.toUserId = source.readString();
            return message;
        }

        @Override
        public ChatMessage[] newArray(int size) {
            return new ChatMessage[size];
        }
    };
    // 多选，是否选中
    public boolean isMoreSelected;
    @DatabaseField
    private String fromUserId;

    @DatabaseField
    private String toUserId;

    @DatabaseField
    private String fromUserName;// 发送者名称
    /**
     * 在不同的消息类型里，代表不同的含义：<br/>
     * {@link Constants#TYPE_TEXT} 文字 <br/>
     * {@link Constants#TYPE_IMAGE} 图片的Url<br/>
     * {@link Constants#TYPE_VOICE} 语音的Url <br/>
     * {@link Constants#TYPE_LOCATION} 地理<br/>
     * {@link Constants#TYPE_GIF} Gif图的名称 <br/>
     * {@link Constants#TYPE_TIP} 系统提示的字<br/>
     * {@link Constants#TYPE_FILE} 文件的url<br/>
     */
    @DatabaseField
    private String content;

    @DatabaseField
    private String location_x;// 1.当为地理位置时，有效 2.特殊：当为图片时，该值为图片的宽度

    @DatabaseField
    private String location_y;// 1.当为地理位置时，有效 2.特殊：当为图片时，该值为图片的高度

    @DatabaseField
    private int fileSize;// 当为图片、语音消息时，此节点有效。图片、语音文件的大小

    @DatabaseField
    private int timeLen;// 当为语音消息时，此节点有效。语音信息的长度

    /* 本地额外存数数据 */
    @DatabaseField(generatedId = true)
    private int _id;

    @DatabaseField
    private int timeReceive;// 接收到消息回执的时间

    @DatabaseField
    private String filePath;// 为语音视频图片文件的 本地路径（IOS端叫fileName），注意本地文件可能清除了，此节点代表的数据不一定有效

    @DatabaseField
    private boolean isUpload;// 当为图片和语音类型是，此节点有效，代表是否上传完成，默认false。isMySend=true，此节点有效，

    @DatabaseField
    private int uploadSchedule;// 上传进度

    @DatabaseField
    private boolean isDownload;// 当为图片和语音类型是，此节点有效，代表是否下载完成，默认false。isMySend=false,此节点有效

    @DatabaseField
    private int messageState;// 只有当消息是我发出的，此节点才有效。消息的发送状态,默认值=0，代表发送中

    @DatabaseField
    private boolean sendRead; // 代表我是否发送过已读， false代表我未读这条， true代表我已读这条消息

    @DatabaseField
    private int sipStatus;// 语音或者视频通话的状态，本地数据库存储即可

    @DatabaseField
    private int sipDuration;// 语音或者视频通话的时间，本地数据库存储即可

    // TODO 1.
    @DatabaseField
    private int isReadDel;

    @DatabaseField
    private int isEncrypt;

    // //////推送特有的//////
    @DatabaseField
    private String objectId;// 用于商务圈推送，代表哪一条公共消息

    @DatabaseField
    private int reSendCount;

    @DatabaseField
    private int readPersons;

    @DatabaseField
    private long readTime;

    // Smack Message 下的fromId与toId;
    @DatabaseField
    private String fromId;
    @DatabaseField
    private String toId;

    // 消息到期时间(当前时间+消息保存天数=到期时间)
    @DatabaseField
    private long deleteTime;
    // 群组，消息已读人数
    private boolean showMucRead;
    private boolean isGroup;

    private boolean isLoadRemark;

    @DatabaseField(defaultValue = "0")
    // 该条消息是否过期
    // TODO 目前群组消息有一个问题，即消息过期了，本地在下次打开应用删除该条消息之后，调用XMPP加群获取离线消息有可能又将该消息拉下来了，添加该标志位记录过期消息，在群组查询时就过滤掉这种消息
    private int isExpired;

    public ChatMessage() {
    }

    public ChatMessage(String jsonData) {
        parserJsonData(jsonData);
    }

    public boolean isLoadRemark() {
        return isLoadRemark;
    }

    public void setLoadRemark(boolean loadRemark) {
        isLoadRemark = loadRemark;
    }

    public long getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(long deleteTime) {
        this.deleteTime = deleteTime;
    }

    public boolean isShowMucRead() {
        return showMucRead;
    }

    public void setShowMucRead(boolean showMucRead) {
        this.showMucRead = showMucRead;
    }

    public boolean isMoreSelected() {
        return isMoreSelected;
    }

    public void setMoreSelected(boolean moreSelected) {
        isMoreSelected = moreSelected;
    }

    public boolean isSendRead() {
        return sendRead;
    }

    public void setSendRead(boolean sendRead) {
        this.sendRead = sendRead;
    }

    public int getReadPersons() {
        return readPersons;
    }

    public void setReadPersons(int readPersons) {
        this.readPersons = readPersons;
    }

    public long getReadTime() {
        return readTime;
    }

    public void setReadTime(long readTime) {
        this.readTime = readTime;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getLocation_x() {
        return location_x;
    }

    public void setLocation_x(String location_x) {
        this.location_x = location_x;
    }

    public String getLocation_y() {
        return location_y;
    }

    public void setLocation_y(String location_y) {
        this.location_y = location_y;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getTimeLen() {
        return timeLen;
    }

    public void setTimeLen(int timeLen) {
        this.timeLen = timeLen;
    }

    public int getMessageState() {
        return messageState;
    }

    public void setMessageState(int messageState) {
        this.messageState = messageState;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public int getReSendCount() {
        return reSendCount;
    }

    public void setReSendCount(int reSendCount) {
        this.reSendCount = reSendCount;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public void setUpload(boolean isUpload) {
        this.isUpload = isUpload;
    }

    public int getUploadSchedule() {
        return uploadSchedule;
    }

    public void setUploadSchedule(int uploadSchedule) {
        this.uploadSchedule = uploadSchedule;
    }

    public boolean isDownload() {
        return isDownload;
    }

    public void setDownload(boolean isDownload) {
        this.isDownload = isDownload;
    }

    public int getTimeReceive() {
        return timeReceive;
    }

    public void setTimeReceive(int timeReceive) {
        this.timeReceive = timeReceive;
    }

//    public boolean getIsReadDel() {
//        return isReadDel == 1;
//    }

    public boolean getIsReadDel() {
        return isReadDel != 0;
    }

    public int getIsReadDelByInt() {
        return isReadDel;
    }


    public void setIsReadDel(int isReadDel) {
        this.isReadDel = isReadDel;
    }

    public int getIsEncrypt() {
        return isEncrypt;
    }

    public void setIsEncrypt(int isEncrypt) {
        this.isEncrypt = isEncrypt;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public int getIsExpired() {
        return isExpired;
    }

    public void setIsExpired(int isExpired) {
        this.isExpired = isExpired;
    }

    /**
     * 解析接收到的消息
     *
     * @param jsonData
     */
    private void parserJsonData(String jsonData) {
        try {
            JSONObject jObject = JSON.parseObject(jsonData);
            type = ToolUtils.getIntValueFromJSONObject(jObject, "type");
            timeSend = ToolUtils.getDoubleFromJSONObject(jObject, "timeSend");
            deleteTime = ToolUtils.getLongValueFromJSONObject(jObject, "deleteTime");
            fromUserId = ToolUtils.getStringValueFromJSONObject(jObject, "fromUserId");
            toUserId = ToolUtils.getStringValueFromJSONObject(jObject, "toUserId");
            fromUserName = ToolUtils.getStringValueFromJSONObject(jObject, "fromUserName");
            content = ToolUtils.getStringValueFromJSONObject(jObject, "content");
            location_x = ToolUtils.getStringValueFromJSONObject(jObject, "location_x");
            location_y = ToolUtils.getStringValueFromJSONObject(jObject, "location_y");
            fileSize = ToolUtils.getIntValueFromJSONObject(jObject, "fileSize");
            timeLen = ToolUtils.getIntValueFromJSONObject(jObject, "timeLen");
            filePath = ToolUtils.getStringValueFromJSONObject(jObject, "fileName");// 增加解析文件路径
            objectId = ToolUtils.getStringValueFromJSONObject(jObject, "objectId");
            packetId = ToolUtils.getStringValueFromJSONObject(jObject, "messageId");
            // TODO 3. 0.正常1.阅后即焚0.未加密1.加密
            isReadDel = ToolUtils.getIsEncrype(jObject, "isReadDel");
            isEncrypt = ToolUtils.getIsEncrype(jObject);

            isMySend = false;
            isDownload = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String toJsonString() {
        String msg = "";
        JSONObject object = new JSONObject();
        object.put("type", this.type);
        object.put("messageId", this.packetId);
        // object.put("toUserType", 1);
        object.put("timeSend", this.timeSend);
        object.put("deleteTime", this.deleteTime);

        if (isReadDel != 0) {
            object.put("isReadDel", this.isReadDel);
        }

        if (isEncrypt != 0) {
            object.put("isEncrypt", this.isEncrypt);
        }

        if (!TextUtils.isEmpty(this.fromUserId)) {
            object.put("fromUserId", this.fromUserId);
        }

        if (!TextUtils.isEmpty(this.toUserId)) {
            object.put("toUserId", this.toUserId);
        }

        if (!TextUtils.isEmpty(this.fromUserName)) {
            object.put("fromUserName", this.fromUserName);
        }

        if (!TextUtils.isEmpty(this.content)) {
            object.put("content", this.content);
        }

        if (!TextUtils.isEmpty(this.location_x)) {
            object.put("location_x", this.location_x);
        }

        if (!TextUtils.isEmpty(this.location_y)) {
            object.put("location_y", this.location_y);
        }

        if (!TextUtils.isEmpty(this.objectId)) {
            object.put("objectId", this.objectId);
        }

        if (this.fileSize > 0) {
            object.put("fileSize", this.fileSize);
        }
        // 增加filePath
        if (!TextUtils.isEmpty(this.filePath)) {
            object.put("fileName", this.filePath);
        }
        if (this.timeLen > 0) {
            object.put("timeLen", this.timeLen);
        }

        msg = object.toString();
        return msg;
    }

    public boolean validate() {
        return type != 0 && !TextUtils.isEmpty(fromUserId) && !TextUtils.isEmpty(fromUserName) && timeSend != 0;
    }

    public boolean isExpired() {
        return deleteTime != 0 && deleteTime != -1 && deleteTime < TimeUtils.time_current_time();
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeString(content);
        dest.writeString(filePath);
        dest.writeInt(fileSize);
        dest.writeString(fromUserId);
        dest.writeString(toUserId);
        dest.writeString(fromUserName);
        dest.writeString(location_x);
        dest.writeString(location_y);
        dest.writeInt(messageState);
        dest.writeString(objectId);
        dest.writeString(packetId);
        dest.writeInt(sipDuration);
        dest.writeInt(sipStatus);
        dest.writeInt(timeLen);
        dest.writeInt(timeReceive);
        // dest.writeLong(timeSend);
        dest.writeDouble(timeSend);
        dest.writeLong(deleteTime);
        dest.writeInt(type);

        // TODO 5.
        dest.writeInt(isReadDel);
        dest.writeInt(isEncrypt);

        dest.writeString(fromId);
        dest.writeString(toUserId);
    }

    public ChatMessage clone(boolean isGroupChat) {
        String json = this.toJsonString();
        return new ChatMessage(json);
    }

    @Override
    public void onTimeStart() {
        if (!TextUtils.isEmpty(packetId) && XmppReceiptImpl.mReceiptMsgMap.get(packetId) != null) {
            XmppReceiptImpl.ReceiptObj receiptObj = XmppReceiptImpl.mReceiptMsgMap.get(packetId);
            Friend friend = FriendDao.getInstance().getFriend(CoreManager.requireSelf(MyApplication.getInstance()).getUserId(), receiptObj.toUserId);
            if (friend != null && friend.getRoomFlag() != 0) {//群组
                ImHelper.reSendMucChatMessage(receiptObj.toUserId, (ChatMessage) receiptObj.msg);
            } else {
                ImHelper.reSendChatMessage(receiptObj.toUserId, (ChatMessage) receiptObj.msg);
            }
        }
    }

    @Override
    public void onTimeOut() {
        if (!TextUtils.isEmpty(packetId) && XmppReceiptImpl.mReceiptMsgMap.get(packetId) != null) {
            XmppReceiptImpl.ReceiptObj receiptObj = XmppReceiptImpl.mReceiptMsgMap.get(packetId);
            if (receiptObj.msg.getType() != Constants.TYPE_CURRENT_USER_INFO_UPDATE
                    && receiptObj.msg.getType() != Constants.TYPE_FRIEND_INFO_UPDATE
                    && receiptObj.msg.getType() != Constants.TYPE_FRIEND_LABEL_INFO_UPDATE
                    && receiptObj.msg.getType() != Constants.TYPE_FRIEND_OTHER_INFO_UPDATE
                    && receiptObj.msg.getType() != Constants.TYPE_FRIEND_READDEL_UPDATE
                    && receiptObj.msg.getType() != Constants.TYPE_FRIEND_ADD_DELETE
            ) {
                ListenerManager.getInstance().notifyMessageSendStateChange(MyApplication.getLoginUserId(), receiptObj.toUserId, packetId,
                        Constants.MESSAGE_SEND_FAILED);
            }
            XmppReceiptImpl.mReceiptMsgMap.remove(packetId);
        }
    }

    public void startTimer() {
        startTimer(resendCount);
    }

    public void startTimer(final int reCount) {
        if (itemTimer == null) {
            itemTimer = new ItemTimer(resendTimePeriod, reCount, this);
        }
        if (!itemTimer.isValid()) {
            itemTimer.start();
        }
    }

    public void stopTimer() {
        if (itemTimer != null) {
            itemTimer.stop();
        }
        itemTimer = null;
    }
}