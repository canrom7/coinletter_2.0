package com.iimm.miliao.bean.message;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.User;
import com.iimm.miliao.timer.ItemTimer;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToolUtils;
import com.iimm.miliao.xmpp.ListenerManager;
import com.iimm.miliao.xmpp.XmppReceiptImpl;

import java.io.Serializable;
import java.util.UUID;

/**
 * #define CALL_CENTER_USERID @"10000" //系统消息<br/>
 * #define FRIEND_CENTER_USERID @"10001" //新朋友 <br/>
 * #define BLOG_CENTER_USERID @"10003" //商务圈 <br/>
 * #define TEST_CENTER_USERID @"10004" //面试中心<br/>
 * 朋友中心
 */
@DatabaseTable
public class NewFriendMessage extends XmppMessage implements Cloneable, Serializable {
    private static final long serialVersionUID = -4231369003725583507L;

    public NewFriendMessage() {
    }

    public NewFriendMessage(String jsonData) {
        parserJsonData(jsonData);
    }

    @DatabaseField(generatedId = true)
    private int _id;

    @DatabaseField(canBeNull = false)
    private String ownerId; // 这个消息是属于哪个用户的

    @DatabaseField(canBeNull = false)
    private String userId; // 此新朋友消息针对的是哪个用户（一定是别人，不是自己）

    @DatabaseField // 默认值
    private int state = 20;

    @DatabaseField
    private String nickName;// 此新朋友消息针对的是哪个用户（一定是别人，不是自己）

    @DatabaseField
    private String content;// (打招呼的内容)

    @DatabaseField
    private boolean isRead;

    @DatabaseField
    private int companyId;// 此新朋友消息针对的是哪个用户,他的公司Id（一定是别人，不是自己）

    @DatabaseField(defaultValue = "0")
    private int unReadNum; // NewFriend 未读消息数量

    public int getUnReadNum() {
        return unReadNum;
    }

    public void setUnReadNum(int unReadNum) {
        this.unReadNum = unReadNum;
    }

    /* 下面5个只用于xmpp通讯时，生成json消息。在接受时，会自动转为上面的有效消息，所以不应该作为其他用途，不作为判断依据，不写入数据库 */
    private String fromUserId;
    private String fromUserName;
    private String toUserId;
    private String toUserName;
    private int fromCompanyId;

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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickname) {
        this.nickName = nickname;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    public String getToUserName() {
        return toUserName;
    }

    public int getFromCompanyId() {
        return fromCompanyId;
    }

    public void setFromCompanyId(int fromCompanyId) {
        this.fromCompanyId = fromCompanyId;
    }

    @Override
    public NewFriendMessage clone() {
        NewFriendMessage n = null;
        try {
            n = (NewFriendMessage) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return n;
    }

    private void parserJsonData(String jsonData) {
        try {
            JSONObject jObject = JSON.parseObject(jsonData);
            userId = ToolUtils.getStringValueFromJSONObject(jObject, "fromUserId");
            nickName = ToolUtils.getStringValueFromJSONObject(jObject, "fromUserName");
            toUserId = ToolUtils.getStringValueFromJSONObject(jObject, "toUserId");
            toUserName = ToolUtils.getStringValueFromJSONObject(jObject, "toUserName");
            companyId = ToolUtils.getIntValueFromJSONObject(jObject, "fromCompanyId");
            type = ToolUtils.getIntValueFromJSONObject(jObject, "type");
            timeSend = ToolUtils.getIntValueFromJSONObject(jObject, "timeSend");
            content = ToolUtils.getStringValueFromJSONObject(jObject, "content");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * fromUserId, fromUserName, fromCompanyId, timeSend, content, type,
     *
     * @return
     */
    public String toJsonString() {
        String msg = "";
        JSONObject object = new JSONObject();
        object.put("fromUserId", this.fromUserId);
        object.put("fromUserName", this.fromUserName);
        object.put("toUserId", this.toUserId);
        object.put("toUserName", this.toUserName);
        object.put("fromCompanyId", this.fromCompanyId);
        object.put("type", this.type);
        object.put("timeSend", this.timeSend);
        object.put("messageId", this.packetId);
        if (!TextUtils.isEmpty(this.content)) {
            object.put("content", this.content);
        }
        msg = object.toString();
        return msg;
    }

    /**
     * @param fromUser    应该是当前登陆的User
     * @param type
     * @param content
     * @param toUserId
     * @param toNickName
     * @param toCompanyId 此状态主要用于更新朋友关系。 发送加关注、加好友 此状态有效<br/>
     *                    发送打招呼 、解除关注、解除好友此状态无效，填Integer.MIN_VALUE<br/>
     *                    下面几个重载方法都遵循此原则<br/>
     * @return
     */
    public static NewFriendMessage createWillSendMessage(User fromUser, int type, String content, String toUserId, String toNickName, int toCompanyId) {
        NewFriendMessage message = new NewFriendMessage();
        message.setPacketId(ToolUtils.getUUID());
        // 首先是传输协议的字段，
        message.setFromUserId(fromUser.getUserId());
        message.setFromUserName(fromUser.getNickName());
        message.setToUserId(toUserId);
        message.setToUserName(toNickName);
        message.setFromCompanyId(fromUser.getCompanyId());
        message.setType(type);
        message.setContent(content);
        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
        // 本地数据库状态
        message.setOwnerId(fromUser.getUserId());
        message.setUserId(toUserId);
        message.setNickName(toNickName);
        message.setCompanyId(toCompanyId);
        message.setRead(true);
        message.setMySend(true);
        return message;
    }

    // 多点登陆，本地收到其他端发送的新朋友消息，本地也需要创建NewFriendMessage并存入本地
    public static NewFriendMessage createLocalMessage(User fromUser, int type, String content, String toUserId, String toNickName) {
        String packetId = UUID.randomUUID().toString().replace("-", "");
        NewFriendMessage message = new NewFriendMessage();
        message.setPacketId(packetId);
        // 首先是传输协议的字段，
        message.setFromUserId(fromUser.getUserId());
        message.setFromUserName(fromUser.getNickName());
        message.setToUserId(toUserId);
        message.setToUserName(toNickName);
        message.setFromCompanyId(fromUser.getCompanyId());
        message.setType(type);
        message.setContent(content);
        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
        // 本地数据库状态
        message.setOwnerId(fromUser.getUserId());
        message.setUserId(toUserId);
        message.setNickName(toNickName);
        message.setRead(true);
        message.setMySend(true);
        return message;
    }

    public static NewFriendMessage createWillSendMessage(User fromUser, int type, String content, User toUser) {
        return createWillSendMessage(fromUser, type, content, toUser.getUserId(), toUser.getNickName(), toUser.getCompanyId());
    }

    public static NewFriendMessage createWillSendMessage(User fromUser, int type, String content, Friend toFriend) {
        return createWillSendMessage(fromUser, type, content, toFriend.getUserId(), toFriend.getNickName(), toFriend.getCompanyId());
    }

    public static NewFriendMessage createWillSendMessage(User fromUser, int type, String content, NewFriendMessage existMessage) {
        return createWillSendMessage(fromUser, type, content, existMessage.getUserId(), existMessage.getNickName(), existMessage.getCompanyId());
    }

    @Override
    public void onTimeStart() {

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
                ListenerManager.getInstance().notifyNewFriendSendStateChange(receiptObj.toUserId, ((NewFriendMessage) receiptObj.msg),
                        Constants.MESSAGE_SEND_FAILED);
            }
            XmppReceiptImpl.mReceiptMsgMap.remove(packetId);
        }
    }

    public void startTimer() {
        startTimer(0);//新朋友消息，只发一次
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
