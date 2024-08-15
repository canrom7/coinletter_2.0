package com.iimm.miliao.xmpp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.BuildConfig;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.audio.NoticeVoicePlayer;
import com.iimm.miliao.bean.CodePay;
import com.iimm.miliao.bean.EventBusMsg;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.MassageDelete;
import com.iimm.miliao.bean.SpareMessage;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.broadcast.OtherBroadcast;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.ReceiptMsgDao;
import com.iimm.miliao.db.dao.login.MachineDao;
import com.iimm.miliao.pay.EventBankRechargeInfo;
import com.iimm.miliao.pay.EventPaymentSuccess;
import com.iimm.miliao.pay.EventReceiptSuccess;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.NotificationUtils;
import com.iimm.miliao.util.log.LogUtils;
import com.iimm.miliao.xmpp.spare.SpareChatEvent;
import com.iimm.miliao.xmpp.util.ImHelper;
import com.iimm.miliao.xmpp.util.XmppStringUtil;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.ChatStateListener;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.util.XmppStringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

/**
 * @Author: wangqx
 * @Date: 2019/8/1
 * @Description:描述
 */
public class XmppChatImpl implements IncomingChatMessageListener,
        OutgoingChatMessageListener, MessageListener, ChatStateListener, SpareChatEvent {
    private String TAG = "XmppChatImpl";
    private Map<String, String> mMsgIDMap = new ConcurrentHashMap<>();// 因为多点登录 转发 问题，先用一个简单的方法去重
    private static List<Message> list = new ArrayList<>();
    private static boolean isStart = false;
    public static int count = 0;

    @Override
    public void onOpen() {
        Log.i("SpareConnectionHelper", "备用连接已连接....");
    }

    @Override
    public void onMessage(String id, String event, String message) {
        Log.i("SpareConnectionHelper", "备用连接收到消息...." + message);
        try {
            if (Constants.SUPPORT_SECOND_CHANNEL && !TextUtils.equals(message, "ok") && !TextUtils.equals(message, "fail")) {
                SpareMessage spareMessage = new Gson().fromJson(message, SpareMessage.class);
                if (spareMessage != null && spareMessage.getAttributes() != null && spareMessage.getChildren() != null && spareMessage.getChildren().size() > 0
                        && spareMessage.getChildren().get(0) != null) {
                    Message message1 = new Message();
                    message1.setFrom(spareMessage.getAttributes().getFrom());
                    message1.setTo(spareMessage.getAttributes().getTo());
                    message1.setType(Message.Type.fromString(spareMessage.getAttributes().getType()));
                    message1.setStanzaId(spareMessage.getAttributes().getId());
                    for (SpareMessage.ChildrenBean data : spareMessage.getChildren()) {
                        if (!TextUtils.isEmpty(data.getcData())) {
                            message1.setBody(data.getcData().replaceAll("&quot;", "\""));
                            break;
                        }
                    }
                    XmppReceiptImpl.getInstance().sendBatchReceipt(message1.getStanzaId());
                    if (message1 != null && message1.getType() != null && message1.getType() == Message.Type.groupchat) {
                        processIncomingGroupChatMessage(message1);
                    } else if (message1 != null && message1.getType() != null && message1.getType() == Message.Type.chat) {
                        processIncomingChatMessage(message1);
                    }
                    saveLog(message1);
                }
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onClosed() {
        Log.i("SpareConnectionHelper", "备用连接已关闭....");
    }

    private static class SmackImplHolder {
        private static final XmppChatImpl INSTANCE = new XmppChatImpl();
    }

    public XmppChatImpl() {
    }

    public static final XmppChatImpl getInstance() {
        return SmackImplHolder.INSTANCE;
    }

    @Override
    public void processMessage(Message message) {
        try {
            XmppReceiptImpl.getInstance().sendBatchReceipt(message.getStanzaId());
            processIncomingGroupChatMessage(message);
            saveLog(message);
        } catch (Exception e) {
        }
    }

    private void saveLog(Message message) {
        if (BuildConfig.DEBUG) {
            Log.d("消息监听", "收+time-" + System.currentTimeMillis() + "**" + message + "---" + message.getBody());
            ReceiptMsgDao.getInstance().insertMsg(message);
        }

    }

    public void processIncomingGroupChatMessage(Message message) {
        if (message == null || TextUtils.isEmpty(message.getFrom()) || TextUtils.isEmpty(message.getTo())) {
            return;
        }
        String from = message.getFrom().toString();
        String to = message.getTo().toString();
        if (!XmppStringUtil.isJID(from) || !XmppStringUtil.isJID(to)) {
            return;
        }
        if (!to.contains(MyApplication.getLoginUserId())) {
            return;
        }
        String content = message.getBody();
        if (TextUtils.isEmpty(content)) {
            return;
        }
        ChatMessage chatMessage = new ChatMessage(content);
        if (chatMessage.getType() == Constants.TYPE_BACKSTAGE_DELETE) {// 收到协议为203消息
            String fromId = XmppStringUtil.getRoomJID(from);
            String roomJid = XmppStringUtil.getRoomJIDPrefix(fromId);
            ImHelper.backMessageGroupDelete(deleteMessage(chatMessage), roomJid);
            return;
        }

        DelayInformation delayInformation = (DelayInformation) message.getExtension("delay", "urn:xmpp:delay");

        //解析了没有使用，我就给注销掉 01.15
        /* if (StringUtils.strEquals(message.getStanzaId(), "") || message.getStanzaId() == null) {
         *//**
         * 接收到的packetId可能会为空，为了补上这个错误，我们发送消息的时候在body多发了一个messageId,用于容错
         * 所以我们这里如果查到packetId为空的话，给他补上messageId
         * 添加id位置：
         * @see {@link  ChatMessage#toJsonString(boolean)}
         * @see {@link  com.iimm.miliao.bean.message.NewFriendMessage#toJsonString(boolean)}
         * *//*
            try {
                JSONObject jsonObject = JSONObject.parseObject(message.getBody());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
        if (delayInformation != null) {// 这是历史记录
            Date date = delayInformation.getStamp();
            if (date != null) {
                ImHelper.saveGroupMessage(content, from, message.getStanzaId(), true);
                return;
            }
        }

        try {
            if (Constants.SUPPORT_GROUP_NICKNAME && chatMessage != null && !TextUtils.isEmpty(MyApplication.getLoginUserId()) && !TextUtils.isEmpty(chatMessage.getToUserId()) && !TextUtils.isEmpty(chatMessage.getFromUserId()) && !TextUtils.isEmpty(chatMessage.getFromUserName())) {
                if (chatMessage.getType() == Constants.TYPE_TEXT ||
                        chatMessage.getType() == Constants.TYPE_IMAGE ||
                        chatMessage.getType() == Constants.TYPE_VOICE ||
                        chatMessage.getType() == Constants.TYPE_LOCATION ||
                        chatMessage.getType() == Constants.TYPE_GIF ||
                        chatMessage.getType() == Constants.TYPE_VIDEO ||
                        chatMessage.getType() == Constants.TYPE_FILE ||
                        chatMessage.getType() == Constants.TYPE_CARD) {
                    ListenerManager.getInstance().notifyNickNameChanged(chatMessage.getToUserId(), chatMessage.getFromUserId(), chatMessage.getFromUserName());
                    ChatMessageDao.getInstance().updateNickName(MyApplication.getLoginUserId(), chatMessage.getToUserId(), chatMessage.getFromUserId(), chatMessage.getFromUserName());

                }
            }
        } catch (Exception e) {

        }

        ImHelper.saveGroupMessage(content, from, message.getStanzaId(), false);


    }

    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        try {
            XmppReceiptImpl.getInstance().sendBatchReceipt(message.getStanzaId());
            processIncomingChatMessage(message);
            saveLog(message);
        } catch (Exception e) {
        }
    }

    public void processIncomingChatMessage(Message message) {

        // body外
        String from = XmppStringUtils.parseBareJid(message.getFrom().toString());
        String resource = message.getFrom().toString().substring(message.getFrom().toString().indexOf("/") + 1, message.getFrom().length());// 发送方的resource ex:ios 、pc...
        String packetID = message.getStanzaId();
        // body内(即我们发送的ChatMessage)
        String body = message.getBody();
        ChatMessage chatMessage = new ChatMessage(body);
        String fromUserId = chatMessage.getFromUserId();
        String toUserId = chatMessage.getToUserId();

        if (message != null && !TextUtils.isEmpty(message.getTo().toString()) && !message.getTo().toString().contains(MyApplication.getLoginUserId())) {
            return;
        }

        if (TextUtils.isEmpty(chatMessage.getPacketId())) {
            chatMessage.setPacketId(packetID);
        }
        chatMessage.setFromId(message.getFrom().toString());
        chatMessage.setToId(message.getTo().toString());
        if (mMsgIDMap.containsKey(chatMessage.getPacketId())) {
            return;
        }
        mMsgIDMap.put(chatMessage.getPacketId(), chatMessage.getPacketId());
        int type = chatMessage.getType();
        if (type == 0) { // 消息过滤
            return;
        }
        int isEncrypt = chatMessage.getIsEncrypt();
        if (isEncrypt == 1) {
            ImHelper.decryptDES(chatMessage); // 解密
        }

        if (Constants.SUPPORT_BIDIRECTIONAL_WITHDRAW && type == Constants.EVENTBUS_GROUP_TWO_WAY_WIITHDRAWAL) {
            FriendDao.getInstance().resetFriendMessage(MyApplication.getLoginUserId(), chatMessage.getObjectId());
            ChatMessageDao.getInstance().deleteMessageTable(MyApplication.getLoginUserId(), chatMessage.getObjectId());
            MsgBroadcast.broadcastMsgMUCUpdate(MyApplication.getInstance());// 清空聊天界面
            MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
            return;
        }
        if (type == Constants.EVENTBUS_DISABLE_IP_DEVICE) {
            EventBus.getDefault().post(new EventBusMsg(Constants.EVENTBUS_DISABLE_IP_DEVICE));
            return;
        }

        if (chatMessage.getType() == Constants.TYPE_SEND_ONLINE_STATUS) {// 收到协议为200消息
            ImHelper.moreLogin(message, resource, chatMessage);
            return;
        } else if (chatMessage.getType() == Constants.TYPE_CURRENT_USER_INFO_UPDATE) {
            //重新获取用户信息，更新用户信息，如果是密码，则重新登陆
            EventBusMsg eventBusMsg = new EventBusMsg();
            eventBusMsg.setMessageType(Constants.EVENTBUS_MSG_CURRENT_USER_INFO_UPDATE);
            EventBus.getDefault().post(eventBusMsg);
            return;
        } else if (chatMessage.getType() == Constants.TYPE_FRIEND_INFO_UPDATE) {
            ImHelper.syncFriendInfoFromOtherMachine(chatMessage);
            return;
        } else if (chatMessage.getType() == Constants.TYPE_FRIEND_ADD_DELETE) {
            ImHelper.syncMyFriendFromOtherMachine(chatMessage);
            return;
        }

        boolean isGroupLiveNewFriendType = false;
        if (chatMessage.getType() >= Constants.TYPE_MUCFILE_ADD
                && chatMessage.getType() <= Constants.TYPE_FACE_GROUP_NOTIFY) {// 这些消息类型的fromUserId都有可能等于toUserId
            isGroupLiveNewFriendType = true;
        }
        // 收到 我的设备 消息
        if (fromUserId.equals(toUserId) && !isGroupLiveNewFriendType) {
            if (!resource.equals(MyApplication.MULTI_RESOURCE)
                    && message.getTo().toString().substring(message.getTo().toString().indexOf("/") + 1,
                    message.getTo().length()).equals(MyApplication.MULTI_RESOURCE)) {
                // from resource 不等于自己
                // to   resource 等于自己 方可进入该方法
                if (chatMessage.getType() == Constants.TYPE_READ) {
                    String packetId = chatMessage.getContent();
                    ChatMessageDao.getInstance().updateMessageRead(MyApplication.getLoginUserId(), resource, packetId, true);
                    boolean isReadChange = ChatMessageDao.getInstance().updateReadMessage(MyApplication.getLoginUserId(), resource, packetId);
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("packetId", packetId);
                    bundle.putBoolean("isReadChange", isReadChange);
                    intent.setAction(OtherBroadcast.IsRead);
                    intent.putExtras(bundle);
                    MyApplication.getInstance().sendBroadcast(intent);
                    return;
                }

                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), resource, chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), resource, chatMessage, false);
                }
            }
            return;
        }

        boolean isForwarding = false;// 该条消息是否需要转发
        boolean isNeedChangeMsgTableSave = false;// 该条消息是否需要换表存储
        // 注：非多点登录，不会走进该判断
        if (from.contains(MyApplication.getLoginUserId())) {
            // 收到转发消息，重置定时器
            MachineDao.getInstance().updateMachineOnLineStatus(resource, true);
            // 1.消息创建方的转发消息
            // 2.消息接收方的转发消息
            if (fromUserId.equals(MyApplication.getLoginUserId())) {
                isNeedChangeMsgTableSave = true;
                LogUtils.e(TAG, "消息创建方的转发消息,将isNeedChangeMsgTableSave置为true");
                chatMessage.setMySend(true);
                chatMessage.setUpload(true);
                chatMessage.setUploadSchedule(100);
                chatMessage.setMessageState(Constants.MESSAGE_SEND_SUCCESS);
                if (ChatMessageDao.getInstance().hasSameMessage(MyApplication.getLoginUserId(), toUserId, packetID)) {
                    LogUtils.e(TAG, "table exist this msg，return");
                    return;
                }

            } else {
                LogUtils.e(TAG, "消息接收方的转发消息");
                if (ChatMessageDao.getInstance().hasSameMessage(MyApplication.getLoginUserId(), fromUserId, packetID)) {
                    LogUtils.e(TAG, "table exist this msg，return");
                    return;
                }
            }
            LogUtils.e(TAG, "table not exist this msg，carry on");
        } else {// 收到对方发过来的消息
            isForwarding = true;
            Friend friend = FriendDao.getInstance().getFriend(MyApplication.getLoginUserId(), chatMessage.getFromUserId());
            if (friend != null && friend.getOfflineNoPushMsg() == 0) {
                NotificationUtils.notificationMessage(chatMessage, false);// 调用本地通知
            }
        }
        // 音视频相关消息100-124
        if (type >= Constants.TYPE_IS_CONNECT_VOICE && type <= Constants.TYPE_IS_BUSY) {
            ImHelper.chatAudioVideo(chatMessage);
            return;
        }
        // 朋友圈相关消息 301-304
        if (type >= Constants.DIANZAN && type <= Constants.ATMESEE) {
            ImHelper.chatDiscover(body, chatMessage);
            return;
        }
        // 新朋友相关消息 500-515
        if (type >= Constants.TYPE_SAYHELLO && type <= Constants.TYPE_BACK_DELETE) {
            ImHelper.chatFriend(body, chatMessage);
            return;
        }

        // 群文件上传、下载、删除 401-403
        if (type >= Constants.TYPE_MUCFILE_ADD && type <= Constants.TYPE_MUCFILE_DOWN) {
            Friend friend = FriendDao.getInstance().getFriend(MyApplication.getLoginUserId(), chatMessage.getObjectId());
            ImHelper.chatGroupTipForMe(body, chatMessage, friend);
            return;
        }
        /*
        服务端已修改了发送群控制消息的逻辑(不再遍历群成员之后以单聊的方式发送，而是直接发送到群组内(但是部分协议还是会以单聊的方式发送给个人,ex:邀请群成员...)，
        但服务端代码还未上传(怕影响老版本用户)，所以我们在XMuChatMessageListener内的处理暂时还未用上，且该类中的逻辑不能删除，还需要添加判断本地是否有存在该条消息
        防止服务端代码上传后 单、群聊监听都收到同一条消息重复处理
         */
        if ((type >= Constants.TYPE_CHANGE_NICK_NAME && type <= Constants.NEW_MEMBER)
                || type == Constants.TYPE_SEND_MANAGER
                || type == Constants.TYPE_UPDATE_ROLE) {
            Friend friend = FriendDao.getInstance().getFriend(MyApplication.getLoginUserId(), chatMessage.getObjectId());
            if (ChatMessageDao.getInstance().hasSameMessage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage.getPacketId())) {
                // 本地已经保存了这条消息，不处理，因为有些协议群组内也发了，单聊也发了
                LogUtils.e(TAG, "Return 6");
                return;
            }
            if (friend != null || type == Constants.NEW_MEMBER) {
                if (chatMessage.getFromUserId().equals(MyApplication.getLoginUserId())) {
                    ImHelper.chatGroupTipFromMe(body, chatMessage, friend);
                } else {
                    ImHelper.chatGroupTipForMe(body, chatMessage, friend);
                }
            }
            return;
        }
        // 群组控制消息[2] 915-925
        if (type >= Constants.TYPE_CHANGE_SHOW_READ && type <= Constants.TYPE_GROUP_TRANSFER) {
            boolean isJumpOver = false;
            if (type == Constants.TYPE_GROUP_VERIFY) {// 群验证跳过判断，因为自己发送的群验证消息object为一个json
                isJumpOver = true;
            }
            if (!isJumpOver && ChatMessageDao.getInstance().hasSameMessage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage.getPacketId())) {
                // 本地已经保存了这条消息，不处理，因为有些协议群组内也发了，单聊也发了
                LogUtils.e(TAG, "Return 7");
                return;
            }
            JSONObject jsonObject = JSONObject.parseObject(body);
            String toUserName = jsonObject.getString("toUserName");
            ImHelper.chatGroupTip2(type, chatMessage, toUserName);
            return;
        }
        // 后台操作发送过来的xmpp
        if (type == Constants.TYPE_DISABLE_GROUP) {
            if (chatMessage.getContent().equals("-1")) {// 锁定
                FriendDao.getInstance().updateFriendGroupStatus(MyApplication.getLoginUserId(), chatMessage.getObjectId(), 3);// 更新本地群组状态
            } else if (chatMessage.getContent().equals("1")) {// 解锁
                FriendDao.getInstance().updateFriendGroupStatus(MyApplication.getLoginUserId(), chatMessage.getObjectId(), 0);// 更新本地群组状态
            }
            MyApplication.getInstance().sendBroadcast(new Intent(MsgBroadcast.ACTION_DISABLE_GROUP_BY_SERVICE));
            return;
        }
        // 面对面建群有人加入、退出
        if (type == Constants.TYPE_FACE_GROUP_NOTIFY) {
            MsgBroadcast.broadcastFaceGroupNotify(MyApplication.getContext(), "notify_list");
            return;
        }

        // 表示这是已读回执类型的消息
        if (chatMessage.getType() == Constants.TYPE_READ) {
            if (MyApplication.IS_SUPPORT_MULTI_LOGIN && isForwarding) {
                ImHelper.sendChatMessage(MyApplication.getLoginUserId(), chatMessage);
            }
            String packetId = chatMessage.getContent();

            if (chatMessage.getFromUserId().equals(MyApplication.getLoginUserId())) {// 其他端发送过来的已读
                ChatMessage msgById = ChatMessageDao.getInstance().findMsgById(MyApplication.getLoginUserId(), chatMessage.getToUserId(), packetId);
                if (msgById != null && msgById.getIsReadDel()) {// 在其他端已读了该条阅后即焚消息，本端也需要删除
                    if (ChatMessageDao.getInstance().deleteSingleChatMessage(MyApplication.getLoginUserId(), chatMessage.getToUserId(), packetId)) {
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString("MULTI_LOGIN_READ_DELETE_PACKET", packetId);
                        intent.setAction(OtherBroadcast.MULTI_LOGIN_READ_DELETE);
                        intent.putExtras(bundle);
                        MyApplication.getInstance().sendBroadcast(intent);
                    }
                }
            } else {
                ChatMessageDao.getInstance().updateMessageRead(MyApplication.getLoginUserId(), fromUserId, packetId, true);// 更新状态为已读
                boolean isReadChange = ChatMessageDao.getInstance().updateReadMessage(MyApplication.getLoginUserId(), fromUserId, packetId);
                // 发送广播通知聊天页面，将未读的消息修改为已读
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("packetId", packetId);
                bundle.putBoolean("isReadChange", isReadChange);
                intent.setAction(OtherBroadcast.IsRead);
                intent.putExtras(bundle);
                MyApplication.getInstance().sendBroadcast(intent);
            }
            return;
        }

        if (type == Constants.TYPE_INPUT) {
            Intent intent = new Intent();
            intent.putExtra("fromId", chatMessage.getFromUserId());
            intent.setAction(OtherBroadcast.TYPE_INPUT);
            MyApplication.getInstance().sendBroadcast(intent);
            return;
        }

        // 某个用户发过来的撤回消息
        if (type == Constants.TYPE_BACK) {
            if (MyApplication.IS_SUPPORT_MULTI_LOGIN && isForwarding) {
                ImHelper.sendChatMessage(MyApplication.getLoginUserId(), chatMessage);
            }
            ImHelper.backMessage(chatMessage);
            return;
        }
        if (chatMessage.getType() == Constants.TYPE_BACKSTAGE_DELETE) {// 收到协议为203消息
            ImHelper.backMessageDelete(deleteMessage(chatMessage));
            return;
        }

        // 某个成员领取了红包
        if (type == Constants.TYPE_83) {
            if (MyApplication.IS_SUPPORT_MULTI_LOGIN && isForwarding) {
                ImHelper.sendChatMessage(MyApplication.getLoginUserId(), chatMessage);
            }

            String fromName;
            String toName;
            if (fromUserId.equals(MyApplication.getLoginUserId())) {
                fromName = MyApplication.getContext().getString(R.string.you);
                toName = MyApplication.getContext().getString(R.string.self);
            } else {
                fromName = chatMessage.getFromUserName();
                toName = MyApplication.getContext().getString(R.string.you);
            }

            String hasBennReceived = "";
            if (chatMessage.getFileSize() == 1) {// 红包是否领完
                try {
                    String sRedSendTime = chatMessage.getFilePath();
                    long redSendTime = Long.parseLong(sRedSendTime);
                    long betweenTime = chatMessage.getTimeSend() - redSendTime;
                    String sBetweenTime;
                    if (betweenTime < TimeUnit.MINUTES.toSeconds(1)) {
                        sBetweenTime = betweenTime + MyApplication.getContext().getString(R.string.second);
                    } else if (betweenTime < TimeUnit.HOURS.toSeconds(1)) {
                        sBetweenTime = TimeUnit.SECONDS.toMinutes(betweenTime) + MyApplication.getContext().getString(R.string.minute);
                    } else {
                        sBetweenTime = TimeUnit.SECONDS.toHours(betweenTime) + MyApplication.getContext().getString(R.string.hour);
                    }
                    hasBennReceived = MyApplication.getContext().getString(R.string.red_packet_has_received_place_holder, sBetweenTime);
                } catch (Exception e) {
                    hasBennReceived = MyApplication.getContext().getString(R.string.red_packet_has_received);
                }
            }
            String str = MyApplication.getContext().getString(R.string.tip_receive_red_packet_place_holder, fromName, toName) + hasBennReceived;

            // 针对红包领取的提示消息 需要做点击事件处理，将红包的type与id存入其他字段内
            chatMessage.setFileSize(Constants.TYPE_83);
            chatMessage.setFilePath(chatMessage.getContent());
            chatMessage.setType(Constants.TYPE_TIP);
            chatMessage.setContent(str);
            if (!TextUtils.isEmpty(chatMessage.getObjectId())) {// 群组红包领取 通知到群组
                fromUserId = chatMessage.getObjectId();
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), fromUserId, chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), fromUserId, chatMessage, true);
                }
            } else {// 单聊红包领取
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), fromUserId, chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), fromUserId, chatMessage, false);
                }
            }
            return;
        }
        // 某个成员领取了云红包
        if (type == Constants.TYPE_CLOUD_RECEIVE_RED) {
            if (MyApplication.IS_SUPPORT_MULTI_LOGIN && isForwarding) {
                ImHelper.sendChatMessage(MyApplication.getLoginUserId(), chatMessage);
            }

            String fromName;
            String toName;
            if (fromUserId.equals(MyApplication.getLoginUserId())) {
                fromName = MyApplication.getContext().getString(R.string.you);
                toName = MyApplication.getContext().getString(R.string.self);
            } else {
                fromName = chatMessage.getFromUserName();
                toName = MyApplication.getContext().getString(R.string.you);
            }

            String hasBennReceived = "";
            if (chatMessage.getFileSize() == 1) {// 红包是否领完
                try {
                    String sRedSendTime = chatMessage.getFilePath();
                    long redSendTime = Long.parseLong(sRedSendTime);
                    long betweenTime = chatMessage.getTimeSend() - redSendTime;
                    String sBetweenTime;
                    if (betweenTime < TimeUnit.MINUTES.toSeconds(1)) {
                        sBetweenTime = betweenTime + MyApplication.getContext().getString(R.string.second);
                    } else if (betweenTime < TimeUnit.HOURS.toSeconds(1)) {
                        sBetweenTime = TimeUnit.SECONDS.toMinutes(betweenTime) + MyApplication.getContext().getString(R.string.minute);
                    } else {
                        sBetweenTime = TimeUnit.SECONDS.toHours(betweenTime) + MyApplication.getContext().getString(R.string.hour);
                    }
                    hasBennReceived = MyApplication.getContext().getString(R.string.cloud_red_packet_has_received_place_holder, sBetweenTime);
                } catch (Exception e) {
                    hasBennReceived = MyApplication.getContext().getString(R.string.cloud_red_packet_has_received);
                }
            }
            String str = MyApplication.getContext().getString(R.string.cloud_tip_receive_red_packet_place_holder, fromName, toName) + hasBennReceived;

            // 针对红包领取的提示消息 需要做点击事件处理，将红包的type与id存入其他字段内
            chatMessage.setFileSize(Constants.TYPE_CLOUD_RECEIVE_RED);
            chatMessage.setFilePath(chatMessage.getContent());
            chatMessage.setType(Constants.TYPE_TIP);
            chatMessage.setContent(str);
            if (!TextUtils.isEmpty(chatMessage.getObjectId())) {// 群组红包领取 通知到群组
                fromUserId = chatMessage.getObjectId();
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), fromUserId, chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), fromUserId, chatMessage, true);
                }
            } else {// 单聊红包领取
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), fromUserId, chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), fromUserId, chatMessage, false);
                }
            }
            return;
        }
        // 红包退回通知，
        if (type == Constants.TYPE_RED_BACK) {
            if (MyApplication.IS_SUPPORT_MULTI_LOGIN && isForwarding) {
                ImHelper.sendChatMessage(MyApplication.getLoginUserId(), chatMessage);
            }
            String str = MyApplication.getContext().getString(R.string.tip_red_back);
            chatMessage.setType(Constants.TYPE_TIP);
            chatMessage.setContent(str);
            if (!TextUtils.isEmpty(chatMessage.getObjectId())) {// 群组红包退回 通知到群组
                fromUserId = chatMessage.getObjectId();
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), fromUserId, chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), fromUserId, chatMessage, true);
                }
            } else {// 单聊红包退回
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), fromUserId, chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), fromUserId, chatMessage, false);
                }
            }
            return;
        }
        // 云红包退回通知，
        if (type == Constants.TYPE_CLOUD_BACK_RED) {
            if (MyApplication.IS_SUPPORT_MULTI_LOGIN && isForwarding) {
                ImHelper.sendChatMessage(MyApplication.getLoginUserId(), chatMessage);
            }
            String str = MyApplication.getContext().getString(R.string.cloud_tip_red_back);
            chatMessage.setType(Constants.TYPE_TIP);
            chatMessage.setContent(str);
            if (!TextUtils.isEmpty(chatMessage.getObjectId())) {// 群组红包退回 通知到群组
                fromUserId = chatMessage.getObjectId();
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), fromUserId, chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), fromUserId, chatMessage, true);
                }
            } else {// 单聊红包退回
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), fromUserId, chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), fromUserId, chatMessage, false);
                }
            }
            return;
        }
        if (type == Constants.TYPE_TRANSFER_RECEIVE) {
            if (MyApplication.IS_SUPPORT_MULTI_LOGIN && isForwarding) {
                ImHelper.sendChatMessage(MyApplication.getLoginUserId(), chatMessage);
            }
            String str;
            if (type == Constants.TYPE_TRANSFER_RECEIVE) {
                str = MyApplication.getContext().getString(R.string.transfer_received);
            } else {
                str = MyApplication.getContext().getString(R.string.transfer_backed);
            }
            chatMessage.setType(Constants.TYPE_TIP);
            chatMessage.setContent(str);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), fromUserId, chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), fromUserId, chatMessage, false);
            }
            return;
        }
        //微转账
        if (type == Constants.TYPE_CLOUD_TRANSFER_RECEIVE) {
            if (MyApplication.IS_SUPPORT_MULTI_LOGIN && isForwarding) {
                ImHelper.sendChatMessage(MyApplication.getLoginUserId(), chatMessage);
            }
            String str;
            if (type == Constants.TYPE_CLOUD_TRANSFER_RECEIVE) {
                str = MyApplication.getContext().getString(R.string.transfer_received_micro);
            } else {
                str = MyApplication.getContext().getString(R.string.micro_transfer_backed);
            }
            chatMessage.setType(Constants.TYPE_TIP);
            chatMessage.setContent(str);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), fromUserId, chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), fromUserId, chatMessage, false);
            }
            return;
        }
        // 转账、收付款消息
        if (type >= Constants.TYPE_TRANSFER_BACK && type <= Constants.TYPE_RECEIPT_GET) {
            if (MyApplication.IS_SUPPORT_MULTI_LOGIN && isForwarding) {
                ImHelper.sendChatMessage(MyApplication.getLoginUserId(), chatMessage);
            }
            if (type == Constants.TYPE_PAYMENT_OUT) {// 通知到付款界面
                CodePay codePay = JSON.parseObject(chatMessage.getContent(), CodePay.class);
                EventBus.getDefault().post(new EventPaymentSuccess(codePay.getToUserName()));
            } else if (type == Constants.TYPE_RECEIPT_GET) {// 通知到收款界面
                CodePay codePay = JSON.parseObject(chatMessage.getContent(), CodePay.class);
                EventBus.getDefault().post(new EventReceiptSuccess(codePay.getToUserName()));
            }
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), fromUserId, chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), fromUserId, chatMessage, false);
            }
            return;
        }


        if (type == Constants.TYPE_SCREENSHOT) {
            chatMessage.setType(Constants.TYPE_TIP);
            chatMessage.setContent(MyApplication.getInstance().getResources().getString(R.string.tip_remote_screenshot));
            // 处理成tip后不return，正常流程处理，
        } else if (type == Constants.TYPE_SYNC_CLEAN_CHAT_HISTORY) {
            Intent intent = new Intent();
            if (isNeedChangeMsgTableSave) {
                intent.putExtra(AppConstant.EXTRA_USER_ID, chatMessage.getToUserId());
            } else {
                intent.putExtra(AppConstant.EXTRA_USER_ID, chatMessage.getFromUserId());
            }
            intent.setAction(OtherBroadcast.SYNC_CLEAN_CHAT_HISTORY);
            MyApplication.getInstance().sendBroadcast(intent);
            return;
        }
        // 存储消息
//        if (chatMessage.isExpired()) {// 该条消息为过期消息(基本可以判断为离线消息)，不进行存库通知
//            LogUtils.e(TAG, "该条消息为过期消息(基本可以判断为离线消息)，不进行存库通知");
//            return;
//        }
        // 戳一戳
        if (type == Constants.TYPE_SHAKE) {
            Vibrator vibrator = (Vibrator) MyApplication.getContext().getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {100, 400, 100, 400};
            vibrator.vibrate(pattern, -1);
        }
        //银行
        if (type == Constants.TYPE_RECHARGE_GET) {
            EventBus.getDefault().post(new EventBankRechargeInfo(chatMessage));
        }
        Friend friend = FriendDao.getInstance().getFriend(MyApplication.getLoginUserId(), chatMessage.getFromUserId());
        if (friend != null) {
            if (friend.getStatus() != -1) {
                ImHelper.saveCurrentMessage(chatMessage, isForwarding, isNeedChangeMsgTableSave);
                if (friend.getOfflineNoPushMsg() == 0) {// 未开启消息免打扰 可通知
                    if (!chatMessage.getFromUserId().equals(MyApplication.IsRingId)
                            && isForwarding) {// 收到该消息时不处于与发送方的聊天界面 && 非转发消息
                        LogUtils.e(TAG, "铃声通知");
                        NoticeVoicePlayer.getInstance().start();
                    }
                } else {
                    LogUtils.e(TAG, "已针对该好友开启了消息免打扰，不通知");
                }
            }
        } else {
            LogUtils.e(TAG, "陌生人发过来的消息");
            FriendDao.getInstance().createNewFriend(chatMessage);
            ImHelper.saveCurrentMessage(chatMessage, isForwarding, isNeedChangeMsgTableSave);
        }
    }

    @Override
    public void newOutgoingMessage(EntityBareJid to, Message message, Chat chat) {
        LogUtils.i(TAG, "newOutgoingMessage: " + message.getBody());
    }

    @Override
    public void stateChanged(Chat chat, ChatState state, Message message) {
    }

    public ChatMessage deleteMessage(ChatMessage chatMessage) {
        MassageDelete message1 = new Gson().fromJson(chatMessage.getContent(), MassageDelete.class);
        ChatMessage deleteMsg = new ChatMessage(message1.getBody());
        return deleteMsg;
       /* List<String> message = new ArrayList<>();
        if (chatMessage == null) {
            return;
        }
        message.clear();
        AsyncUtils.doAsync(this, mucChatActivityAsyncContext -> {
            String msg = chatMessage.getContent();
            MassageDelete message1 = new Gson().fromJson(msg, MassageDelete.class);
            if (!TextUtils.isEmpty(message1.getBody())) {
                ChatMessage deleteMsg = new ChatMessage(message1.getBody());
                String strings = deleteMsg.getPacketId();
                ChatMessageDao.getInstance().deleteMessage(MyApplication.getLoginUserId(), deleteMsg.getToUserId(), strings);
            }
        });*/
    }
}
