package com.iimm.miliao.xmpp.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iimm.miliao.AppConfig;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.BuildConfig;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.adapter.MessageContactEvent;
import com.iimm.miliao.adapter.MessageEventHongdian;
import com.iimm.miliao.audio.NoticeVoicePlayer;
import com.iimm.miliao.bean.AttentionUser;
import com.iimm.miliao.bean.CodePay;
import com.iimm.miliao.bean.Contact;
import com.iimm.miliao.bean.EventBusMsg;
import com.iimm.miliao.bean.EventCreateGroupFriend;
import com.iimm.miliao.bean.EventLoginStatus;
import com.iimm.miliao.bean.EventNewNotice;
import com.iimm.miliao.bean.EventXMPPJoinGroupFailed;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.MassageDelete;
import com.iimm.miliao.bean.MsgRoamTask;
import com.iimm.miliao.bean.MyZan;
import com.iimm.miliao.bean.RoomMember;
import com.iimm.miliao.bean.SpareMessage;
import com.iimm.miliao.bean.User;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.bean.message.LastChatHistoryList;
import com.iimm.miliao.bean.message.MucRoom;
import com.iimm.miliao.bean.message.NewFriendMessage;
import com.iimm.miliao.bean.message.SendMsg;
import com.iimm.miliao.bean.message.XmppMessage;
import com.iimm.miliao.broadcast.CardcastUiUpdateUtil;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.broadcast.MucgroupUpdateUtil;
import com.iimm.miliao.broadcast.OtherBroadcast;
import com.iimm.miliao.call.CallConstants;
import com.iimm.miliao.call.JitsistateMachine;
import com.iimm.miliao.call.MessageCallingEvent;
import com.iimm.miliao.call.MessageEventMeetingInvited;
import com.iimm.miliao.call.MessageEventSipEVent;
import com.iimm.miliao.call.MessageHangUpPhone;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.db.dao.ContactDao;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.MsgRoamTaskDao;
import com.iimm.miliao.db.dao.MyZanDao;
import com.iimm.miliao.db.dao.NewFriendDao;
import com.iimm.miliao.db.dao.OnCompleteListener2;
import com.iimm.miliao.db.dao.RoomMemberDao;
import com.iimm.miliao.db.dao.SendMsgDao;
import com.iimm.miliao.db.dao.login.MachineDao;
import com.iimm.miliao.db.dao.login.TimerListener;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.FriendHelper;
import com.iimm.miliao.helper.UploadEngine;
import com.iimm.miliao.pay.EventBankRechargeInfo;
import com.iimm.miliao.pay.EventPaymentSuccess;
import com.iimm.miliao.pay.EventReceiptSuccess;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.ui.circle.MessageEventNotifyDynamic;
import com.iimm.miliao.ui.live.LiveConstants;
import com.iimm.miliao.ui.message.ChatActivity;
import com.iimm.miliao.ui.message.multi.GroupStrongReminderActivity;
import com.iimm.miliao.ui.message.multi.TransferKingEvent;
import com.iimm.miliao.ui.message.single.PersonSettingActivity;
import com.iimm.miliao.ui.mucfile.XfileUtils;
import com.iimm.miliao.util.AppExecutors;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.DES;
import com.iimm.miliao.util.HttpUtil;
import com.iimm.miliao.util.Md5Util;
import com.iimm.miliao.util.NotificationUtils;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.ThreadJoinGroupUtil;
import com.iimm.miliao.util.ThreadManager;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ToolUtils;
import com.iimm.miliao.util.log.LogUtils;
import com.iimm.miliao.xmpp.ListenerManager;
import com.iimm.miliao.xmpp.XmppChatImpl;
import com.iimm.miliao.xmpp.XmppConnectionImpl;
import com.iimm.miliao.xmpp.XmppHelpService;
import com.iimm.miliao.xmpp.XmppReceiptImpl;
import com.iimm.miliao.xmpp.spare.SpareConnectionHelper;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jivesoftware.smackx.muc.MucEnterConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatException;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;
import org.json.JSONException;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.FullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Domainpart;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;
import org.jxmpp.util.XmppStringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @Author: wangqx
 * @Date: 2019/8/2
 * @Description:描述
 */
public class ImHelper {
    private static String TAG = "ImHelper";
    private static Map<String, String> mMsgIDMap = new ConcurrentHashMap<>();
    private static Map<String, MultiUserChat> mMucChatMap = new ConcurrentHashMap<>();

    public static void xmppConnect(Context context) {
        try {
            if (!TextUtils.isEmpty(MyApplication.getXmppAccount())
                    && !TextUtils.isEmpty(MyApplication.getXmppPassword())) {
                Intent xmppConnectIntent = new Intent(context, XmppHelpService.class);
                xmppConnectIntent.putExtra(Constants.TYPE_XMPP_ACTION, Constants.TYPE_XMPP_CONNECT);
                xmppConnectIntent.putExtra(Constants.XMPP_ACCOUNT, MyApplication.getXmppAccount());
                xmppConnectIntent.putExtra(Constants.XMPP_PASSWORD, MyApplication.getXmppPassword());
                xmppConnectIntent.putExtra(Constants.XMPP_RESOURCE, MyApplication.MULTI_RESOURCE);
                context.startService(xmppConnectIntent);
            } else {
                EventBusMsg eventBusMsg = new EventBusMsg();
                eventBusMsg.setMessageType(Constants.EVENTBUS_MSG_AUTH_NOT);
                EventBus.getDefault().post(eventBusMsg);
                MyApplication.applicationHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToast("账号或密码为空!");
                    }
                });
            }
        } catch (Exception e) {
        }
    }

    public static void xmppLogin(Context context, String username, String password) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            return;
        }
        String resource;
        if (MyApplication.IS_SUPPORT_MULTI_LOGIN) {
            resource = MyApplication.MULTI_RESOURCE;
        } else {
            resource = MyApplication.MULTI_RESOURCE;
        }
        Intent xmppLoginIntent = new Intent(context, XmppHelpService.class);
        xmppLoginIntent.putExtra(Constants.TYPE_XMPP_ACTION, Constants.TYPE_XMPP_LOGIN);
        xmppLoginIntent.putExtra(Constants.XMPP_ACCOUNT, username);
        xmppLoginIntent.putExtra(Constants.XMPP_PASSWORD, password);
        xmppLoginIntent.putExtra(Constants.XMPP_RESOURCE, resource);
        context.startService(xmppLoginIntent);
        if (Constants.SUPPORT_SECOND_CHANNEL) {
            getHistoryIM();
        }
    }

    public static void xmppLogin(Context context, String username, String password, String resource) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(resource)) {
            return;
        }
        Intent xmppLoginIntent = new Intent(context, XmppHelpService.class);
        xmppLoginIntent.putExtra(Constants.TYPE_XMPP_ACTION, Constants.TYPE_XMPP_LOGIN);
        xmppLoginIntent.putExtra(Constants.XMPP_ACCOUNT, username);
        xmppLoginIntent.putExtra(Constants.XMPP_PASSWORD, password);
        xmppLoginIntent.putExtra(Constants.XMPP_RESOURCE, resource);
        context.startService(xmppLoginIntent);
    }

    /**
     * 检测认证状态，未认证，则认证
     */
    public static boolean checkXmppAuthenticated() {
        try {
            LogUtils.i(TAG, "checkXmppAuthenticated 检测连接");
            XMPPTCPConnection xmpptcpConnection = XmppConnectionImpl.getInstance().getXMPPConnection();
            if (xmpptcpConnection != null) {
                if (xmpptcpConnection.isAuthenticated()) {
                    if (!TextUtils.isEmpty(MyApplication.getXmppAccount())) {
                        if (xmpptcpConnection.getUser().getLocalpart().toString().equals(MyApplication.getXmppAccount())) {
                            LogUtils.i(TAG, "checkXmppAuthenticated: 无需重新登录");
                            return true;
                        } else {
                            LogUtils.i(TAG, "checkXmppAuthenticated 连接 178");
                            xmpptcpConnection.disconnect();
                            ImHelper.xmppConnect(MyApplication.getInstance());
                            return false;
                        }
                    } else {
                        LogUtils.i(TAG, "checkXmppAuthenticated 连接 183");
                        xmpptcpConnection.disconnect();
                        ImHelper.xmppConnect(MyApplication.getInstance());
                        return false;
                    }
                } else if (xmpptcpConnection.isConnected()) {
                    if (!TextUtils.isEmpty(MyApplication.getXmppAccount())
                            && !TextUtils.isEmpty(MyApplication.getXmppPassword())) {
                        LogUtils.i(TAG, "checkXmppAuthenticated 连接 192");
                        ImHelper.xmppLogin(MyApplication.getInstance(), MyApplication.getXmppAccount(), MyApplication.getXmppPassword());
                        return false;
                    }
                } else {
                    LogUtils.i(TAG, "checkXmppAuthenticated 连接 197");
                    ImHelper.xmppConnect(MyApplication.getInstance());
                    return false;
                }
            } else {
                LogUtils.i(TAG, "checkXmppAuthenticated 连接 202");
                ImHelper.xmppConnect(MyApplication.getInstance());
                return false;
            }
            LogUtils.i(TAG, "checkXmppAuthenticated 连接 206");
            ImHelper.xmppConnect(MyApplication.getInstance());
            return false;
        } catch (Exception e) {
            LogUtils.e(TAG, "checkXmppAuthenticated:" + e.getMessage());
        }
        return false;
    }

    public static boolean isConnected() {
        XMPPTCPConnection xmpptcpConnection = XmppConnectionImpl.getInstance().getXMPPConnection();
        if (xmpptcpConnection != null
                && xmpptcpConnection.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isConnected(XMPPTCPConnection xmpptcpConnection) {
        if (xmpptcpConnection != null
                && xmpptcpConnection.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isAuthenticated() {
        XMPPTCPConnection xmpptcpConnection = XmppConnectionImpl.getInstance().getXMPPConnection();
        if (xmpptcpConnection != null
                && xmpptcpConnection.isConnected()
                && xmpptcpConnection.isAuthenticated()
                && !TextUtils.isEmpty(MyApplication.getXmppAccount())
                && xmpptcpConnection.getUser().getLocalpart().toString().equals(MyApplication.getXmppAccount())) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isAuthenticated(XMPPTCPConnection xmpptcpConnection) {
        if (xmpptcpConnection != null
                && xmpptcpConnection.isConnected()
                && xmpptcpConnection.isAuthenticated()
                && !TextUtils.isEmpty(MyApplication.getXmppAccount())
                && xmpptcpConnection.getUser().getLocalpart().toString().equals(MyApplication.getXmppAccount())) {
            return true;
        } else {
            return false;
        }
    }

    public static String getShowContent(ChatMessage chatMessage) {
        int messageType = chatMessage.getType();
        String content = "";
        switch (messageType) {
            case Constants.TYPE_REPLAY:
            case Constants.TYPE_TEXT:
                if (chatMessage.getIsReadDel()) {
                    content = getString(R.string.tip_click_to_read);
                } else {
                    content = chatMessage.getContent();
                }
                break;
            case Constants.TYPE_VOICE:
                content = getString(R.string.msg_voice);
                break;
            case Constants.TYPE_GIF:
                content = getString(R.string.msg_animation);
                break;
            case Constants.TYPE_IMAGE:
                content = getString(R.string.msg_picture);
                break;
            case Constants.TYPE_VIDEO:
                content = getString(R.string.msg_video);
                break;
            case Constants.TYPE_RED:
                content = getString(R.string.msg_red_packet);
                break;
            case Constants.TYPE_READ_EXCLUSIVE:
                content = getString(R.string.msg_red_packet_exclusive);
                break;
            case Constants.TYPE_CLOUD_RED:
                content = getString(R.string.msg_red_cloud_packet);
                break;
            case Constants.TYPE_LOCATION:
                content = getString(R.string.msg_location);
                break;
            case Constants.TYPE_CARD:
                content = getString(R.string.msg_card);
                break;
            case Constants.TYPE_FILE:
                content = getString(R.string.msg_file);
                break;
            case Constants.TYPE_TIP:
            case Constants.TYPE_GROUP_SIGN:
                content = getString(R.string.msg_system);
                break;
            case Constants.TYPE_IMAGE_TEXT:
            case Constants.TYPE_IMAGE_TEXT_MANY:
                content = getString(R.string.msg_image_text);
                break;
            case Constants.TYPE_LINK:
            case Constants.TYPE_SHARE_LINK:
                content = getString(R.string.msg_link);
                break;
            case Constants.TYPE_SHAKE:
                content = getString(R.string.msg_shake);
                break;
            case Constants.TYPE_CHAT_HISTORY:
                content = getString(R.string.msg_chat_history);
                break;
            case Constants.TYPE_TRANSFER:
                content = getString(R.string.tip_transfer_money);
                break;
            case Constants.TYPE_CLOUD_TRANSFER:
                content = getString(R.string.micro_tip_transfer_money);
                break;
            case Constants.TYPE_TRANSFER_RECEIVE:
                content = getString(R.string.tip_transfer_money) + getString(R.string.transfer_friend_sure_save);
                break;
            case Constants.TYPE_CLOUD_TRANSFER_RECEIVE:
                content = getString(R.string.micro_tip_transfer_money) + getString(R.string.transfer_friend_sure_save);
                break;
            case Constants.TYPE_TRANSFER_BACK:
                content = getString(R.string.transfer_back);
                break;
            case Constants.TYPE_CLOUD_TRANSFER_RETURN:
                content = getString(R.string.micro_transfer_back);
                break;
            case Constants.TYPE_IS_CONNECT_VOICE:
                content = getString(R.string.suffix_invite_you_voice);
                break;
            case Constants.TYPE_IS_CONNECT_VIDEO:
                content = getString(R.string.suffix_invite_you_video);
                break;
            case Constants.TYPE_IS_MU_CONNECT_VOICE:
                content = getString(R.string.suffix_invite_you_voice_meeting);
                break;
            case Constants.TYPE_IS_MU_CONNECT_VIDEO:
                content = getString(R.string.suffix_invite_you_video_meeting);
                break;

            case Constants.TYPE_SAYHELLO:// 打招呼
                content = getString(R.string.apply_to_add_me_as_a_friend);
                break;
            case Constants.TYPE_PASS:    // 同意加好友
                content = getString(R.string.agree_with_my_plus_friend_request);
                break;
            case Constants.TYPE_FRIEND:  // 直接成为好友
                content = getString(R.string.added_me_as_a_friend);
                break;

            case Constants.DIANZAN:// 朋友圈点赞
                content = getString(R.string.notification_praise_me_life_circle);
                break;
            case Constants.PINGLUN:    // 朋友圈评论
                content = getString(R.string.notification_comment_me_life_circle);
                break;
            case Constants.ATMESEE:  // 朋友圈提醒我看
                content = getString(R.string.notification_at_me_life_circle);
                break;
            case Constants.TYPE_NEW_NOTICE:
                try {
                    org.json.JSONObject jsonObject = new org.json.JSONObject(content);
                    String noticeContent = jsonObject.getString("text");
                    content = getString(R.string.notice) + ":" + noticeContent;
                } catch (JSONException e) {
                    e.printStackTrace();
                    content = getString(R.string.msg_hint_notice);
                }
                break;
            case Constants.TYPE_CUSTOM_CHANGE_READ_DEL_TIME:  //您设置了消息几秒后消失  阅后即焚  自定义的view:
                try {
                    org.json.JSONObject jsonObject = new org.json.JSONObject(content);
                    String changeReadDelTime = jsonObject.getString("changeReadDelTime");
                    boolean isChangeByMe = jsonObject.getBoolean("isChangeByMe");
                    if (TextUtils.isEmpty(changeReadDelTime)) {
                        changeReadDelTime = "0";
                    }
                    if (isChangeByMe) {
                        if (changeReadDelTime.equals("0")) {
                            content = getString(R.string.you_shut_down_and_burned_after_reading);
                        } else {
                            content = String.format(getString(R.string.hint_read_del_time), PersonSettingActivity.handlerMsg(Integer.parseInt(changeReadDelTime)));
                        }
                    } else {
                        if (changeReadDelTime.equals("0")) {
                            content = getString(R.string.he_shut_down_and_burned_after_reading);
                        } else {
                            content = String.format(getString(R.string.he_hint_read_del_time), PersonSettingActivity.handlerMsg(Integer.parseInt(changeReadDelTime)));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    content = getString(R.string.open_read_del_time);
                }
                break;
            case Constants.TYPE_RECHARGE_GET:
                content = getString(R.string.payment_notice);
                break;
            case Constants.TYPE_RECHARGE_H5_GET:
                content = getString(R.string.payment_notice);
                break;
        }

        return content;
    }

    public static String createMucRoom(String roomName) {
        if (!ImHelper.checkXmppAuthenticated()) {
            return null;
        }
        XMPPTCPConnection xmpptcpConnection = XmppConnectionImpl.getInstance().getXMPPConnection();
        try {
            /**
             * randomUUID
             */
            String roomId = ToolUtils.getUUID();
            String roomJid = roomId + getMucChatServiceName(xmpptcpConnection);
            // 创建聊天室
            EntityBareJid mEntityBareJid = JidCreate.entityBareFrom(roomJid);
            MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(xmpptcpConnection);
            MultiUserChat multiUserChat = multiUserChatManager.getMultiUserChat(mEntityBareJid);
            Resourcepart resourcepart = Resourcepart.fromOrThrowUnchecked(MyApplication.getLoginUserId());
            multiUserChat.create(resourcepart);
            multiUserChat.addMessageListener(XmppChatImpl.getInstance());// 添加消息监听
            // 获得聊天室的配置表单
            Form form = multiUserChat.getConfigurationForm();
            // 根据原始表单创建一个要提交的新表单。
            Form submitForm = form.createAnswerForm();
            // 向要提交的表单添加默认答复
            List<FormField> fields = form.getFields();
            for (int i = 0; i < fields.size(); i++) {
                FormField field = (FormField) fields.get(i);
/*
                if (!FormField.TYPE_HIDDEN.equals(field.getType()) && field.getVariable() != null) {
                    // 设置默认值作为答复
                    submitForm.setDefaultAnswer(field.getVariable());
                }
*/
                submitForm.setDefaultAnswer(field.getVariable());
            }
            // 设置聊天室的新拥有者
            // List owners = new ArrayList();
            // owners.add("liaonaibo2\\40slook.cc");
            // owners.add("liaonaibo1\\40slook.cc");
            // submitForm.setAnswer("muc#roomconfig_roomowners", owners);
            // 设置聊天室的名字
            submitForm.setAnswer("muc#roomconfig_roomname", roomName);
            // 登录房间对话
            submitForm.setAnswer("muc#roomconfig_enablelogging", true);
            // 设置聊天室是持久聊天室，即将要被保存下来
            submitForm.setAnswer("muc#roomconfig_persistentroom", true);
            // 设置聊天室描述
            // if (!TextUtils.isEmpty(roomDesc)) {
            // submitForm.setAnswer("muc#roomconfig_roomdesc", roomDesc);
            // }
            // 允许修改主题
            // submitForm.setAnswer("muc#roomconfig_changesubject", true);
            // 允许占有者邀请其他人
            // submitForm.setAnswer("muc#roomconfig_allowinvites", true);
            // 最大人数
            // List<String> maxusers = new ArrayList<String>();
            // maxusers.add("50");
            // submitForm.setAnswer("muc#roomconfig_maxusers", maxusers);
            // 公开的，允许被搜索到
            // submitForm.setAnswer("muc#roomconfig_publicroom", true);
            // 是否主持腾出空间(加了这个默认游客进去不能发言)
            // submitForm.setAnswer("muc#roomconfig_moderatedroom", true);
            // 房间仅对成员开放
            // submitForm.setAnswer("muc#roomconfig_membersonly", true);
            // 不需要密码
            // submitForm.setAnswer("muc#roomconfig_passwordprotectedroom",false);
            // 房间密码
            // submitForm.setAnswer("muc#roomconfig_roomsecret", "111");
            // 允许主持 能够发现真实 JID
            // List<String> whois = new ArrayList<String>();
            // whois.add("anyone");
            // submitForm.setAnswer("muc#roomconfig_whois", whois);

            // 管理员
            // <field var='muc#roomconfig_roomadmins'>
            // <value>wiccarocks@shakespeare.lit</value>
            // <value>hecate@shakespeare.lit</value>
            // </field>

            // 仅允许注册的昵称登录
            // submitForm.setAnswer("x-muc#roomconfig_reservednick", true);
            // 允许使用者修改昵称
            // submitForm.setAnswer("x-muc#roomconfig_canchangenick", false);
            // 允许用户注册房间
            // submitForm.setAnswer("x-muc#roomconfig_registration", false);

            // 发送已完成的表单（有默认值）到服务器来配置聊天室
            multiUserChat.sendConfigurationForm(submitForm);
            // muc.changeSubject(roomSubject);
            // mMucChatMap.put(roomJid, muc);
            mMucChatMap.put(roomJid, multiUserChat);
            return roomId;
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param roomJidLocal
     * @param lastSeconds
     */
    public synchronized static boolean joinMucChat(final String roomJidLocal, long lastSeconds) {
        if (!ImHelper.checkXmppAuthenticated()) {
            return false;
        }
        XMPPTCPConnection xmpptcpConnection = XmppConnectionImpl.getInstance().getXMPPConnection();
        String roomJid = roomJidLocal + getMucChatServiceName(xmpptcpConnection);
        // 创建聊天室
        try {
            EntityBareJid mEntityBareJid = JidCreate.entityBareFrom(roomJid);
            MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(xmpptcpConnection);
            MultiUserChat multiUserChat = multiUserChatManager.getMultiUserChat(mEntityBareJid);
            if (multiUserChat.isJoined()) {
                Log.i(TAG, "已加入，无需重新加入");
                mMucChatMap.put(roomJid, multiUserChat);
                multiUserChat.addMessageListener(XmppChatImpl.getInstance());
                return true;
            }
            Resourcepart resourcepart = Resourcepart.fromOrThrowUnchecked(MyApplication.getLoginUserId());
            Friend friend = FriendDao.getInstance().getFriend(MyApplication.getLoginUserId(), roomJidLocal);
            if (friend != null) {
                if (friend.getGroupStatus() != 0) {
                    // 我已被踢出该群 || 该群已解散 || 该群已被后台锁定，不加入该群
                    LogUtils.e(TAG, " 我已被踢出该群 || 该群已解散 || 该群已被后台锁定，Return");
                    return false;
                }
            }

            boolean isShieldGroupMsg = PreferenceUtils.getBoolean(MyApplication.getInstance(),
                    Constants.SHIELD_GROUP_MSG + roomJid + MyApplication.getLoginUserId(), false);
            if (isShieldGroupMsg) {// 屏蔽了该群组消息
                lastSeconds = 0;
            }
            // http://download.igniterealtime.org/smack/docs/latest/javadoc/org/jivesoftware/smackx/muc/MucEnterConfiguration.Builder.html
            MucEnterConfiguration.Builder mucChatEnterConfigurationBuilder = multiUserChat.getEnterConfigurationBuilder(resourcepart);
            mucChatEnterConfigurationBuilder.requestHistorySince((int) lastSeconds);
            MucEnterConfiguration mucEnterConfiguration = mucChatEnterConfigurationBuilder.build();

            if (!multiUserChat.isJoined()) {
                multiUserChat.join(mucEnterConfiguration);
            } else {
                LogUtils.e(TAG, "已加入，无需重新加入");
            }
            mMucChatMap.put(roomJid, multiUserChat);
            multiUserChat.addMessageListener(XmppChatImpl.getInstance());
            LogUtils.e(TAG, "加入成功");
            return true;
        } catch (XMPPException e) {// 如果加入前是将对方挤下线的，那么对方可能还在这个房间内，会导致加入房间失败(概率偏小)
            LogUtils.e(TAG, "加入失败");
            /**
             * if an error occurs joining the room. In particular, a 401 error can occur if no password was provided and one is required; or a 403 error can occur if the user is banned;
             * or a 404 error can occur if the room does not exist or is locked;
             * or a 407 error can occur if user is not on the member list; or a 409 error can occur if someone is already in the group chat with the same nickname.
             *
             * 如果没有提供密码并且需要一个密码，则会发生401错误。 或者如果用户被禁止，则可能发生403错误;
             * 或者如果房间不存在或被锁定，则可能发生404错误; 或者如果用户不在成员列表中，则可能发生407错误;
             * 或者如果某人已经在使用相同昵称的组聊天中，则可能会发生409错误。
             *
             * e.getMessage() : feature-not-implemented Changing nickname is not supported yet.// 功能未实现更改昵称尚不支持。
             */
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MultiUserChatException.NotAMucServiceException e) {
            e.printStackTrace();
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void exitMucChat(String toUserId) {
        if (!ImHelper.checkXmppAuthenticated()) {
            return;
        }
        XMPPTCPConnection xmpptcpConnection = XmppConnectionImpl.getInstance().getXMPPConnection();
        String roomJid = toUserId + getMucChatServiceName(xmpptcpConnection);
        try {
            EntityBareJid mEntityBareJid = JidCreate.entityBareFrom(roomJid);
            MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(xmpptcpConnection);
            MultiUserChat multiUserChat = multiUserChatManager.getMultiUserChat(mEntityBareJid);
            if (multiUserChat.isJoined()) {
                multiUserChat.leave();
                multiUserChat.removeMessageListener(XmppChatImpl.getInstance());
            }
            mMucChatMap.remove(roomJid);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
    }

    // 现在加入了群组分页漫游，群组的离线消息不能立即获取，必须要等到'tigase/getLastChatList'接口调用完毕后在加入群组，获取离线消息记录
    public static void joinExistGroup() {
        // 先获取全局的离线-->上线 这个时间段的时间
        int lastSeconds;
        long offlineTime = PreferenceUtils.getLong(MyApplication.getInstance(), Constants.OFFLINE_TIME + MyApplication.getLoginUserId(), 0);
        if (offlineTime == 0) {
            lastSeconds = 0;
        } else {
            lastSeconds = (int) (TimeUtils.time_current_time() - offlineTime);
        }
        List<Friend> friends = FriendDao.getInstance().getAllRooms(MyApplication.getLoginUserId());// 获取本地所有群组
        if (friends != null && friends.size() > 0) {
            ThreadJoinGroupUtil threadJoinGroupUtil = new ThreadJoinGroupUtil(friends, lastSeconds);
            threadJoinGroupUtil.start();
        }
    }

    /*
    多端登录
     */
    // 加载设备表
    public static void loadMachineList() {
        MachineDao.getInstance().loadMachineList(new TimerListener() {
            @Override
            public void onFinish(String machineName) {
                LogUtils.e(TAG, machineName + "计时完成，开始检测" + machineName + "的在线状态 ");
                if (MachineDao.getInstance().getMachineSendReceiptStatus(machineName)) {
                    sendOnLineMessage();
                    // 发送检测消息后，将是否发送回执的状态更新为false
                    MachineDao.getInstance().updateMachineSendReceiptStatus(machineName, false);
                } else {// 当前machine对于我上次发给他的转发 || 检测消息，并未给回执给我，所以我们判断他离线了，将他的状态置为false
                    LogUtils.e(TAG, "发送回执的状态为false，判断" + machineName + "为离线 ");
                    MachineDao.getInstance().updateMachineOnLineStatus(machineName, false);
                }
            }
        });
        MyApplication.IS_SEND_MSG_EVERYONE = true;
        sendOnLineMessage();
    }


    // 发送上线、检测消息
    public static void sendOnLineMessage() {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(Constants.TYPE_SEND_ONLINE_STATUS);
        chatMessage.setFromUserId(MyApplication.getLoginUserId());
        chatMessage.setFromUserName(CoreManager.getSelf(MyApplication.getInstance()).getNickName());
        chatMessage.setToUserId(MyApplication.getLoginUserId());
        chatMessage.setContent("1");// 0 离线 1 在线
        chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
        String uuid = ToolUtils.getUUID();
        chatMessage.setPacketId(uuid);
        PreferenceUtils.putString(MyApplication.getInstance(), "send_200_packageId", uuid);
        sendChatMessage(MyApplication.getLoginUserId(), chatMessage);
    }

    // 发送下线消息
    public void sendOffLineMessage() {
        MyApplication.IS_SEND_MSG_EVERYONE = true;
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(Constants.TYPE_SEND_ONLINE_STATUS);

        chatMessage.setFromUserId(MyApplication.getLoginUserId());
        chatMessage.setFromUserName(CoreManager.getSelf(MyApplication.getInstance()).getNickName());
        chatMessage.setToUserId(MyApplication.getLoginUserId());
        chatMessage.setContent("0");// 0 离线 1在线
        chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
        chatMessage.setPacketId(ToolUtils.getUUID());
        sendChatMessage(MyApplication.getLoginUserId(), chatMessage);
    }

    // 发送忙线消息
    public static void sendBusyMessage(String toUserId) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(Constants.TYPE_IS_BUSY);

        chatMessage.setFromUserId(MyApplication.getLoginUserId());
        chatMessage.setFromUserName(CoreManager.getSelf(MyApplication.getInstance()).getNickName());
        chatMessage.setToUserId(toUserId);

        chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
        chatMessage.setPacketId(ToolUtils.getUUID());
        sendChatMessage(toUserId, chatMessage);
    }

    /**
     * 发送新的朋友消息
     */
    public static void sendNewFriendMessage(String toUserId, NewFriendMessage message) {
        if (!checkXmppAuthenticated()) {
            //ListenerManager.getInstance().notifyNewFriendSendStateChange(toUserId, message, Constants.MESSAGE_SEND_FAILED);
            ToastUtil.showToast("请稍后重试！");
        } else {
            addWillSendMessage(toUserId, message, XmppReceiptImpl.SendType.PUSH_NEW_FRIEND, message.getContent());
            sendNewFriendXmppMessage(toUserId, message);
        }
    }


    /********************
     *  其他操作
     *********************/
    /*
    XMPP认证后需要做的操作
    */
    public static void authenticatedOperating() {
        LogUtils.e(TAG, "认证之后需要调用的操作" + TimeUtils.getCurrentTime());
        if (MyApplication.IS_SUPPORT_MULTI_LOGIN) {
            LogUtils.e(TAG, "我已上线，发送Type 200 协议");
            loadMachineList();
        }
        AppExecutors.getInstance().networkIO().execute(new Runnable() {
            @Override
            public void run() {
                List<Friend> nearlyFriendMsg = FriendDao.getInstance().getNearlyFriendMsg(MyApplication.getLoginUserId());
                for (int i = 0; i < nearlyFriendMsg.size(); i++) {
                    if (nearlyFriendMsg.get(i).getRoomFlag() == 0) {// 单聊可删除
                        ChatMessageDao.getInstance().deleteOutTimeChatMessage(MyApplication.getLoginUserId(), nearlyFriendMsg.get(i).getUserId());
                    } else {// 群聊修改字段
                        //ChatMessageDao.getInstance().updateExpiredStatus(MyApplication.getLoginUserId(), nearlyFriendMsg.get(i).getUserId());
                    }
                }
                // 从服务端获取与其它好友 || 群组内最后一条聊天消息列表(单聊：我在其他端的产生的聊天记录 群聊：离线消息大于100条时，之前的数据)
                ImHelper.getLastChatHistory();
            }
        });
    }

    public static void getLastChatHistory() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(MyApplication.getInstance()).accessToken);
        long syncTimeLen;
        if (Constants.OFFLINE_TIME_IS_FROM_SERVICE) {// 离线时间为服务端获取 取出消息漫游时长
            Constants.OFFLINE_TIME_IS_FROM_SERVICE = false;

            syncTimeLen = 0;
        } else {// syncTime为上一次本地保存的离线时间
            syncTimeLen = PreferenceUtils.getLong(MyApplication.getInstance(), Constants.OFFLINE_TIME + MyApplication.getLoginUserId(), 0);
        }
        params.put("startTime", String.valueOf(syncTimeLen));

        HttpUtils.get().url(CoreManager.requireConfig(MyApplication.getInstance()).GET_LAST_CHAT_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<LastChatHistoryList>(LastChatHistoryList.class) {
                    @Override
                    public void onResponse(ArrayResult<LastChatHistoryList> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            AppExecutors.getInstance().networkIO().execute(new Runnable() {
                                @Override
                                public void run() {
                                    final List<LastChatHistoryList> data = result.getData();
                                    List<String> userIds = new ArrayList<>();
                                    for (int i = 0; i < data.size(); i++) {
                                        LastChatHistoryList mLastChatHistoryList = data.get(i);
                                        if (mLastChatHistoryList.getIsRoom() == 1) {// 群组消息
                                            // 取出该群组最后一条消息
                                            ChatMessage mLocalLastMessage = ChatMessageDao.getInstance().getLastChatMessage(MyApplication.getLoginUserId(), mLastChatHistoryList.getJid());
                                            if (mLocalLastMessage == null
                                                    || mLocalLastMessage.getPacketId().equals(mLastChatHistoryList.getMessageId())) {
                                                // 最后一条消息为空(代表本地为空)
                                                // || 最后一条消息的msgId==服务端记录的该群组最后一条消息msgId(代表离线期间无消息产生 理论上服务端这个情况服务端是不会返回的) 不需要生成任务
                                                // getThisGroupFriend(mLastChatHistoryList.getJid());

                                            } else {
                                                // 生成一条群组漫游任务，存入任务表
                                                MsgRoamTask mMsgRoamTask = new MsgRoamTask();
                                                mMsgRoamTask.setTaskId(System.currentTimeMillis());
                                                mMsgRoamTask.setOwnerId(MyApplication.getLoginUserId());
                                                mMsgRoamTask.setUserId(mLastChatHistoryList.getJid());
                                                mMsgRoamTask.setStartTime(mLocalLastMessage.getTimeSend());
                                                mMsgRoamTask.setStartMsgId(mLocalLastMessage.getPacketId());
                                                MsgRoamTaskDao.getInstance().createMsgRoamTask(mMsgRoamTask);
                                            }
                                        }
                                        // 更新朋友表部分字段，用于显示
                                        String str = "";
                                        if (mLastChatHistoryList.getIsEncrypt() == 1) {// 需要解密
                                            if (!TextUtils.isEmpty(mLastChatHistoryList.getContent())) {
                                                String content = mLastChatHistoryList.getContent().replaceAll("\n", "");
                                                String decryptKey = Md5Util.toMD5(AppConfig.apiKey + mLastChatHistoryList.getTimeSend() + mLastChatHistoryList.getMessageId());
                                                try {
                                                    str = DES.decryptDES(content, decryptKey);
                                                } catch (Exception e) {
                                                    str = mLastChatHistoryList.getContent();
                                                    e.printStackTrace();
                                                }
                                            }
                                        } else {
                                            str = mLastChatHistoryList.getContent();
                                        }
                                        String userId = FriendDao.getInstance().updateApartDownloadTime(mLastChatHistoryList.getUserId(), mLastChatHistoryList.getJid(),
                                                str, mLastChatHistoryList.getType(), mLastChatHistoryList.getTimeSend(),
                                                mLastChatHistoryList.getIsRoom(), mLastChatHistoryList.getFrom(), mLastChatHistoryList.getFromUserName(),
                                                mLastChatHistoryList.getToUserName());
                                        if (!TextUtils.isEmpty(userId)) {
                                            userIds.add(userId);
                                        }
                                    }
                                    notifyMsgListUi(new boolean[]{true});
                                    // 以上任务生成之后，在通知XMPP加入群组 获取群组离线消息
                                    joinExistGroup();
                                    if (userIds.size() > 0) {
                                        boolean[] resultB = new boolean[userIds.size()];
                                        for (int i = 0; i < userIds.size(); i++) {
                                            String userId = userIds.get(i);
                                            Map<String, String> params = new HashMap<>();
                                            params.put("access_token", CoreManager.requireSelfStatus(MyApplication.getInstance()).accessToken);
                                            params.put("userId", userId);
                                            int finalI = i;
                                            HttpUtils.get().url(CoreManager.requireConfig(MyApplication.getInstance()).USER_GET_URL)
                                                    .params(params)
                                                    .build()
                                                    .execute(new BaseCallback<User>(User.class) {
                                                        @Override
                                                        public void onResponse(ObjectResult<User> result) {
                                                            resultB[finalI] = true;
                                                            if (result.getResultCode() == Result.CODE_SUCCESS && result.getData() != null) {
                                                                User data = result.getData();
                                                                FriendHelper.updateFriendRelationship(MyApplication.getLoginUserId(), data);
                                                            }
                                                            notifyMsgListUi(resultB);
                                                        }

                                                        @Override
                                                        public void onError(Call call, Exception e) {
                                                            resultB[finalI] = true;
                                                            notifyMsgListUi(resultB);
                                                        }
                                                    });
                                        }
                                    }
                                }
                            });
                        } else {// 数据异常，也需要调用XMPP加入群组
                            joinExistGroup();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        // 同上
                        joinExistGroup();
                    }
                });
    }

    private static void notifyMsgListUi(boolean[] resultB) {
        if (checkResultB(resultB)) {
            MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getContext());
        }
    }

    private static boolean checkResultB(boolean[] resultB) {
        for (int i = 0; i < resultB.length; i++) {
            if (resultB[i]) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    private static void getThisGroupFriend(String roomId) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(MyApplication.getInstance()).accessToken);
        params.put("roomId", roomId);
        HttpUtils.get().url(CoreManager.requireConfig(MyApplication.getInstance()).ROOM_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<MucRoom>(MucRoom.class) {

                    @Override
                    public void onResponse(ObjectResult<MucRoom> result) {
                        Log.i(TAG, "getThisGroupFriend,onResponse");
                        try {
                            if (result.getResultCode() == 1 && result.getData() != null) {
                                final MucRoom mucRoom = result.getData();
                                EventBus.getDefault().post(new EventCreateGroupFriend(mucRoom));
                                Log.i(TAG, "getThisGroupFriend,EventCreateGroupFriend");

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });

    }

    public static void backMessage(ChatMessage chatMessage) {
        // 本地数据库处理
        String packetId = chatMessage.getContent();
        if (TextUtils.isEmpty(packetId)) {
            return;
        }
        if (chatMessage.getFromUserId().equals(MyApplication.getLoginUserId())) {// 其他端撤回
            ChatMessageDao.getInstance().updateMessageBack(MyApplication.getLoginUserId(), chatMessage.getToUserId(), packetId, MyApplication.getInstance().getString(R.string.you));
        } else {
            ChatMessageDao.getInstance().updateMessageBack(MyApplication.getLoginUserId(), chatMessage.getFromUserId(), packetId, chatMessage.getFromUserName());
        }

        /** 更新聊天界面 */
        Intent intent = new Intent();
        intent.putExtra("packetId", packetId);
        intent.setAction(com.iimm.miliao.broadcast.OtherBroadcast.MSG_BACK);
        MyApplication.getInstance().sendBroadcast(intent);

        // 更新UI界面
        if (chatMessage.getFromUserId().equals(MyApplication.getLoginUserId())) {
            ChatMessage chat = ChatMessageDao.getInstance().getLastChatMessage(MyApplication.getLoginUserId(), chatMessage.getToUserId());
            if (chat != null && !TextUtils.isEmpty(chat.getPacketId()) && chat.getPacketId().equals(packetId)) {
                // 要撤回的消息正是朋友表的最后一条消息
                FriendDao.getInstance().updateFriendContent(MyApplication.getLoginUserId(), chatMessage.getToUserId(),
                        MyApplication.getInstance().getString(R.string.you) + " " + InternationalizationHelper.getString("JX_OtherWithdraw"), Constants.TYPE_TEXT, chat.getTimeSend());
                MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
            }
        } else {
            ChatMessage chat = ChatMessageDao.getInstance().getLastChatMessage(MyApplication.getLoginUserId(), chatMessage.getFromUserId());
            if (chat != null && !TextUtils.isEmpty(chat.getPacketId()) && chat.getPacketId().equals(packetId)) {
                // 要撤回的消息正是朋友表的最后一条消息
                FriendDao.getInstance().updateFriendContent(MyApplication.getLoginUserId(), chatMessage.getFromUserId(),
                        chatMessage.getFromUserName() + " " + InternationalizationHelper.getString("JX_OtherWithdraw"), Constants.TYPE_TEXT, chat.getTimeSend());
                MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
            }
        }
    }

    public static void backMessageDelete(ChatMessage chatMessage) {
        // 本地数据库处理
        String packetId = chatMessage.getPacketId();
        if (TextUtils.isEmpty(packetId)) {
            return;
        }
        if (chatMessage.getFromUserId().equals(MyApplication.getLoginUserId())) {// 其他端撤回
            ChatMessageDao.getInstance().deleteMessage(MyApplication.getLoginUserId(), chatMessage.getToUserId(), packetId);
        } else {
            ChatMessageDao.getInstance().deleteMessage(MyApplication.getLoginUserId(), chatMessage.getFromUserId(), packetId);
        }

        /** 更新聊天界面 */
        Intent intent = new Intent();
        intent.putExtra("packetId", packetId);
        intent.setAction(com.iimm.miliao.broadcast.OtherBroadcast.MSG_BACK);
        MyApplication.getInstance().sendBroadcast(intent);

        // 更新UI界面
        if (chatMessage.getFromUserId().equals(MyApplication.getLoginUserId())) {
            ChatMessage chat = ChatMessageDao.getInstance().getLastChatMessage(MyApplication.getLoginUserId(), chatMessage.getToUserId());
            if (chat != null && !TextUtils.isEmpty(chat.getPacketId()) && chat.getPacketId().equals(packetId)) {
                // 要撤回的消息正是朋友表的最后一条消息
                FriendDao.getInstance().updateFriendContent(MyApplication.getLoginUserId(), chatMessage.getToUserId(),
                        "", Constants.TYPE_TEXT, chat.getTimeSend());
                MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
            }
        } else {
            ChatMessage chat = ChatMessageDao.getInstance().getLastChatMessage(MyApplication.getLoginUserId(), chatMessage.getFromUserId());
            if (chat != null && !TextUtils.isEmpty(chat.getPacketId()) && chat.getPacketId().equals(packetId)) {
                // 要撤回的消息正是朋友表的最后一条消息
                FriendDao.getInstance().updateFriendContent(MyApplication.getLoginUserId(), chatMessage.getFromUserId(),
                        "", Constants.TYPE_TEXT, chat.getTimeSend());
                MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
            }
        }
    }

    public static void backMessageGroupDelete(ChatMessage chatMessage, String roomid) {
        // 本地数据库处理
        String packetId = chatMessage.getPacketId();
        if (TextUtils.isEmpty(packetId)) {
            return;
        }
        if (chatMessage.getFromUserId().equals(MyApplication.getLoginUserId())) {// 其他端撤回
            ChatMessageDao.getInstance().deleteMessage(MyApplication.getLoginUserId(), roomid, packetId);
        } else {
            ChatMessageDao.getInstance().deleteMessage(MyApplication.getLoginUserId(), roomid, packetId);
        }

        /** 更新聊天界面 */
        Intent intent = new Intent();
        intent.putExtra("packetId", packetId);
        intent.setAction(com.iimm.miliao.broadcast.OtherBroadcast.MSG_BACK);
        MyApplication.getInstance().sendBroadcast(intent);

        // 更新UI界面
        ChatMessage chat = ChatMessageDao.getInstance().getLastChatMessage(MyApplication.getLoginUserId(), roomid);
        if (chat != null) {
            if (chat.getPacketId().equals(packetId)) {
                // 要撤回的消息正是朋友表的最后一条消息
                if (chatMessage.getFromUserId().equals(MyApplication.getLoginUserId())) {// 自己发的不用处理
                    FriendDao.getInstance().updateFriendContent(MyApplication.getLoginUserId(), roomid,
                            "", Constants.TYPE_TEXT, chatMessage.getTimeSend());
                } else {
                    FriendDao.getInstance().updateFriendContent(MyApplication.getLoginUserId(), roomid,
                            "", Constants.TYPE_TEXT, chatMessage.getTimeSend());
                }
                MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
            }
        }
    }


    public static String getString(@StringRes int resId) {
        return MyApplication.getInstance().getResources().getString(resId);
    }

    public static void saveCurrentMessage(ChatMessage chatMessage, boolean isForwarding, boolean isNeedChangeMsgTableSave) {
        if (isNeedChangeMsgTableSave) {
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), chatMessage.getToUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), chatMessage.getFromUserId(), chatMessage, false);
            }
        } else {
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), chatMessage.getFromUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), chatMessage.getFromUserId(), chatMessage, false);
            }
        }

        if (MyApplication.IS_SUPPORT_MULTI_LOGIN && isForwarding) {
            LogUtils.e(TAG, "消息存入数据库后，将消息转发出去");
            sendChatMessage(MyApplication.getLoginUserId(), chatMessage);
        }
    }

    public static void chatGroupTipFromMe(String body, ChatMessage chatMessage, Friend friend) {
        JSONObject jsonObject = JSONObject.parseObject(body);
        String toUserId = jsonObject.getString("toUserId");
        String toUserName = jsonObject.getString("toUserName");

        // 我针对其他人的操作，只需要为toUserName重新赋值
        String xT = getName(friend, toUserId);
        if (!TextUtils.isEmpty(xT)) {
            toUserName = xT;
        }

        chatMessage.setGroup(false);
        switch (chatMessage.getType()) {
            case Constants.TYPE_CHANGE_NICK_NAME:
                // 我修改了群内昵称
                String content = chatMessage.getContent();
                if (!TextUtils.isEmpty(content)) {
                    friend.setRoomMyNickName(content);
                    FriendDao.getInstance().updateRoomMyNickName(friend.getUserId(), content);
                    ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), MyApplication.getLoginUserId(), content);
                    ChatMessageDao.getInstance().updateNickName(MyApplication.getLoginUserId(), friend.getUserId(), MyApplication.getLoginUserId(), content);

                    chatMessage.setContent(chatMessage.getFromUserName() + " " + InternationalizationHelper.getString("JXMessageObject_UpdateNickName") + "‘" + content + "’");
                    chatMessage.setType(Constants.TYPE_TIP);
                    if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage)) {
                        ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage, true);
                    }
                }
                break;
            case Constants.TYPE_CHANGE_ROOM_NAME:
                // 更新朋友表
                String groupName = chatMessage.getContent();
                FriendDao.getInstance().updateMucFriendRoomName(friend.getUserId(), groupName);
                ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), "ROOMNAMECHANGE", groupName);

                chatMessage.setContent(chatMessage.getFromUserName() + " " + InternationalizationHelper.getString("JXMessageObject_UpdateRoomName") + groupName);
                chatMessage.setType(Constants.TYPE_TIP);
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage, true);
                }
                break;
            case Constants.TYPE_DELETE_ROOM:
                // 我解散了该群组
                exitMucChat(chatMessage.getObjectId());
                FriendDao.getInstance().deleteFriend(MyApplication.getLoginUserId(), chatMessage.getObjectId());
                // 消息表中删除
                ChatMessageDao.getInstance().deleteMessageTable(MyApplication.getLoginUserId(), chatMessage.getObjectId());
                RoomMemberDao.getInstance().deleteRoomMemberTable(chatMessage.getObjectId());
                // 通知界面更新
                MsgBroadcast.broadcastMsgNumReset(MyApplication.getInstance());
                MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
                MucgroupUpdateUtil.broadcastUpdateUi(MyApplication.getInstance());
                break;
            case Constants.TYPE_DELETE_MEMBER:
                if (toUserId.equals(MyApplication.getLoginUserId())) {
                    // 我退出了该群组
                    exitMucChat(chatMessage.getObjectId());
                    FriendDao.getInstance().deleteFriend(MyApplication.getLoginUserId(), chatMessage.getObjectId());
                    // 消息表中删除
                    ChatMessageDao.getInstance().deleteMessageTable(MyApplication.getLoginUserId(), chatMessage.getObjectId());
                    RoomMemberDao.getInstance().deleteRoomMemberTable(chatMessage.getObjectId());
                    // 通知界面更新
                    MsgBroadcast.broadcastMsgNumReset(MyApplication.getInstance());
                    MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
                    MucgroupUpdateUtil.broadcastUpdateUi(MyApplication.getInstance());
                } else {
                    // toUserId被我踢出群组
                    chatMessage.setContent(toUserName + " " + InternationalizationHelper.getString("KICKED_OUT_GROUP"));
                    chatMessage.setType(Constants.TYPE_TIP);
                    // chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                    if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage)) {
                        ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage, true);
                    }
                }
                break;
            case Constants.TYPE_NEW_NOTICE:
                // 我发布了公告
                try {
                    MucRoom.Notice notice = JSONObject.parseObject(chatMessage.getContent(), MucRoom.Notice.class);
                    EventBus.getDefault().post(new EventNewNotice(notice, chatMessage));
                    chatMessage.setContent(chatMessage.getFromUserName() + " " + InternationalizationHelper.getString("JXMessageObject_AddNewAdv") + notice.getText());
                    chatMessage.setType(Constants.TYPE_TIP);
                    if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage)) {
                        ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage, true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case Constants.TYPE_GAG:
                // 我对群组内其他成员禁言了
                long time = Long.parseLong(chatMessage.getContent());
                // 为防止其他用户接收不及时，给3s的误差
                if (time > TimeUtils.time_current_time() + 3) {
                    String formatTime = XfileUtils.fromatTime((time * 1000), "MM-dd HH:mm");
                    // message.setContent("用户：" + toUserName + " 已被禁言到" + formatTime);
                    chatMessage.setContent(chatMessage.getFromUserName() + " " + InternationalizationHelper.getString("JXMessageObject_Yes") + toUserName +
                            InternationalizationHelper.getString("JXMessageObject_SetGagWithTime") + formatTime);
                } else {
                    // message.setContent("用户：" + toUserName + " 已被取消禁言");
                    /*chatMessage.setContent(chatMessage.getFromUserName() + " " + getString("JXMessageObject_Yes") + toUserName +
                            getString("JXMessageObject_CancelGag"));*/
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.be_cancel_ban_place_holder, toUserName, chatMessage.getFromUserName()));
                }

                chatMessage.setType(Constants.TYPE_TIP);
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage, true);
                }
                break;
            case Constants.NEW_MEMBER:
                String desc = chatMessage.getFromUserName() + " " + InternationalizationHelper.getString("JXMessageObject_GroupChat");
                if (toUserId.equals(MyApplication.getLoginUserId())) {
                    /**
                     * 以下四种情况会进入该判断内
                     * 1.我在本端创建了该群组
                     * 2.我在本端加入该群组
                     * 3.我在其他端创建了该群组
                     * 4.我在其他端加入了该群组
                     * 5.面对面建群，调加入接口服务端代发了907
                     *
                     * 注：以上四种情况服务端都会通过smack推送Type==907的消息过来
                     */
                    Friend mFriend = FriendDao.getInstance().getFriend(MyApplication.getLoginUserId(), chatMessage.getObjectId());
                    if (mFriend != null && mFriend.getGroupStatus() == 1) {// 本地存在该群组，且被踢出了该群组 先将该群组删除在创建(如调用updateGroupStatus直接修改该群组状态，可以会有问题，保险起见还是创建吧)
                        FriendDao.getInstance().deleteFriend(MyApplication.getLoginUserId(), friend.getUserId());
                        ChatMessageDao.getInstance().deleteMessageTable(MyApplication.getLoginUserId(), friend.getUserId());
                        mFriend = null;
                    }

                    // 将当前群组部分属性存入共享参数内
                    String roomId = "";
                    try {
                        roomId = jsonObject.getString("fileName");
                        String other = jsonObject.getString("other");
                        JSONObject jsonObject2 = JSONObject.parseObject(other);
                        int showRead = jsonObject2.getInteger("showRead");
                        int allowSecretlyChat = jsonObject2.getInteger("allowSendCard");
                        MyApplication.getInstance().saveGroupPartStatus(chatMessage.getObjectId(), showRead, allowSecretlyChat,
                                1, 1, 0);
                    } catch (Exception e) {
                        LogUtils.e(TAG, "解析时抛出异常");
                    }

                    /**
                     * 本来只需要判断mFriend是否为空即可，但在第一、二种情况下，在调用接口之后(创建、加入接口)本地也在创建该群组，而当前收到Type==907后
                     * 又会去创建群组，造成群组重复的问题，所以需要坐下兼容
                     */
                    if (mFriend == null
                            && !chatMessage.getObjectId().equals(MyApplication.mRoomKeyLastCreate)) {
                        Friend mCreateFriend = new Friend();
                        mCreateFriend.setOwnerId(MyApplication.getLoginUserId());
                        mCreateFriend.setUserId(chatMessage.getObjectId());
                        mCreateFriend.setNickName(chatMessage.getContent());
                        mCreateFriend.setDescription("");
                        mCreateFriend.setRoomId(roomId);
                        mCreateFriend.setContent(desc);
                        mCreateFriend.setTimeSend(chatMessage.getTimeSend());
                        mCreateFriend.setRoomFlag(1);
                        mCreateFriend.setStatus(Constants.STATUS_FRIEND);
                        mCreateFriend.setGroupStatus(0);
                        FriendDao.getInstance().createOrUpdateFriend(mCreateFriend);
                        EventBusMsg eventBusMsg = new EventBusMsg();
                        eventBusMsg.setMessageType(Constants.EVENTBUS_ROOM_MEMBER_DOWNLOAD_FOR_AVATAR);
                        eventBusMsg.setObject(roomId);
                        EventBus.getDefault().post(eventBusMsg);
                        // 调用smack加入群组的方法
                        joinMucChat(chatMessage.getObjectId(), 0);
                        MsgBroadcast.broadcastFaceGroupNotify(MyApplication.getInstance(), "join_room");
                    }
                } else {
                    // toUserId被我邀请进入群组
                    desc = chatMessage.getFromUserName() + " " + InternationalizationHelper.getString("JXMessageObject_InterFriend") + toUserName;

                    String roomId = jsonObject.getString("fileName");
                    operatingRoomMemberDao(0, roomId, chatMessage.getToUserId(), toUserName);
                }

                chatMessage.setType(Constants.TYPE_TIP);
                chatMessage.setContent(desc);
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage, true);
                    MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(MyApplication.getInstance());
                }
                break;
            case Constants.TYPE_SEND_MANAGER:
                // 我对该群组成员设置/取消管理员
                String messageType = chatMessage.getContent();
                if (messageType.equals("1")) {
                    chatMessage.setContent(chatMessage.getFromUserName() + " " + InternationalizationHelper.getString("JXSettingVC_Set") + toUserName + " " + InternationalizationHelper.getString("JXMessage_admin"));
                } else {
                    chatMessage.setContent(chatMessage.getFromUserName() + " " + InternationalizationHelper.getString("JXSip_Canceled") + toUserName + " " + InternationalizationHelper.getString("JXMessage_admin"));
                }
                chatMessage.setType(Constants.TYPE_TIP);
                chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage, true);
                }
                break;
            case Constants.TYPE_UPDATE_ROLE:
                // 我对该群组成员设置/取消管理员
                int tipContent = -1;
                switch (chatMessage.getContent()) {
                    case "1": // 1:设置隐身人
                        tipContent = R.string.tip_set_invisible_place_holder;
                        break;
                    case "-1": // -1:取消隐身人
                        tipContent = R.string.tip_cancel_invisible_place_holder;
                        break;
                    case "2": // 2：设置监控人
                        tipContent = R.string.tip_set_guardian_place_holder;
                        break;
                    case "0": // 0：取消监控人
                        tipContent = R.string.tip_cancel_guardian_place_holder;
                        break;
                    default:
                        Reporter.unreachable();
                        return;
                }
                chatMessage.setContent(MyApplication.getInstance().getString(tipContent, chatMessage.getFromUserName(), toUserName));
                chatMessage.setType(Constants.TYPE_TIP);
                chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage, true);
                }
                break;

        }
    }

    /**
     * 因为多点登录问题，我在其他端做的群组操作，本端也收到了，需要做一样的处理
     * 所以在做处理前需要判断该群组操作是否为自己操作的(fromUserId==当前账号id)
     * 如果为自己操作了走到了另外一个方法，所以本方法内的对自己的操作代码可以注释掉
     * 现在先不管了，反正不会走到对自己的判断内，无影响
     */
    public static void chatGroupTipForMe(String body, ChatMessage chatMessage, Friend friend) {
        int type = chatMessage.getType();
        String fromUserId = chatMessage.getFromUserId();
        String fromUserName = chatMessage.getFromUserName();
        JSONObject jsonObject = JSONObject.parseObject(body);
        String toUserId = jsonObject.getString("toUserId");
        String toUserName = jsonObject.getString("toUserName");

        if (!TextUtils.isEmpty(toUserId)) {
            if (toUserId.equals(MyApplication.getLoginUserId())) {// 其他人针对我的操作，只需要为fromUserName赋值
                String xF = getName(friend, fromUserId);
                if (!TextUtils.isEmpty(xF)) {
                    fromUserName = xF;
                }
            } else {// 其他人针对其他人的操作，fromUserName与toUserName都需要赋值
                String xF = getName(friend, fromUserId);
                if (!TextUtils.isEmpty(xF)) {
                    fromUserName = xF;
                }
                String xT = getName(friend, toUserId);
                if (!TextUtils.isEmpty(xT)) {
                    toUserName = xT;
                }
            }
        }

        chatMessage.setGroup(false);
        if (type == Constants.TYPE_CHANGE_NICK_NAME) {
            // 修改群内昵称
            String content = chatMessage.getContent();
            if (!TextUtils.isEmpty(toUserId) && toUserId.equals(MyApplication.getLoginUserId())) {
                // 我修改了昵称
                if (!TextUtils.isEmpty(content)) {
                    friend.setRoomMyNickName(content);
                    FriendDao.getInstance().updateRoomMyNickName(friend.getUserId(), content);
                    ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), toUserId, content);
                    ChatMessageDao.getInstance().updateNickName(MyApplication.getLoginUserId(), friend.getUserId(), toUserId, content);
                }
            } else {
                // 其他人修改了昵称，通知下就可以了
                chatMessage.setContent(fromUserName + " " + InternationalizationHelper.getString("JXMessageObject_UpdateNickName") + "‘" + content + "’");
                chatMessage.setType(Constants.TYPE_TIP);
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage, true);
                }
                ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), toUserId, content);
                ChatMessageDao.getInstance().updateNickName(MyApplication.getLoginUserId(), friend.getUserId(), toUserId, content);
            }
        } else if (type == Constants.TYPE_CHANGE_ROOM_NAME) {
            // 修改房间名
            // 更新朋友表
            String content = chatMessage.getContent();
            FriendDao.getInstance().updateMucFriendRoomName(friend.getUserId(), content);
            ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), "ROOMNAMECHANGE", content);

            chatMessage.setContent(toUserName + " " + InternationalizationHelper.getString("JXMessageObject_UpdateRoomName") + content);
            chatMessage.setType(Constants.TYPE_TIP);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage, true);
            }
        } else if (type == Constants.TYPE_DELETE_ROOM) {
            // 群主解散该群
            if (fromUserId.equals(toUserId)) {
                // 我为群主
                FriendDao.getInstance().deleteFriend(MyApplication.getLoginUserId(), chatMessage.getObjectId());
                // 消息表中删除
                ChatMessageDao.getInstance().deleteMessageTable(MyApplication.getLoginUserId(), chatMessage.getObjectId());
                RoomMemberDao.getInstance().deleteRoomMemberTable(chatMessage.getObjectId());
                // 通知界面更新
                MsgBroadcast.broadcastMsgNumReset(MyApplication.getInstance());
                MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
                MucgroupUpdateUtil.broadcastUpdateUi(MyApplication.getInstance());
            } else {
                exitMucChat(chatMessage.getObjectId());
                // 2 标志该群已被解散  更新朋友表
                FriendDao.getInstance().updateFriendGroupStatus(MyApplication.getLoginUserId(), friend.getUserId(), 2);
                chatMessage.setType(Constants.TYPE_TIP);
                chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_disbanded));
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage, true);
                }
            }
            ListenerManager.getInstance().notifyDeleteMucRoom(chatMessage.getObjectId());
        } else if (type == Constants.TYPE_DELETE_MEMBER) {
            // 群组 退出 || 踢人
            chatMessage.setType(Constants.TYPE_TIP);
            // 退出 || 被踢出群组
            if (toUserId.equals(MyApplication.getLoginUserId())) { // 该操作为针对我的
                if (fromUserId.equals(toUserId)) {
                    // 自己退出了群组
                    exitMucChat(friend.getUserId());
                    // 删除这个房间
                    FriendDao.getInstance().deleteFriend(MyApplication.getLoginUserId(), friend.getUserId());
                    RoomMemberDao.getInstance().deleteRoomMemberTable(chatMessage.getObjectId());
                    // 消息表中删除
                    ChatMessageDao.getInstance().deleteMessageTable(MyApplication.getLoginUserId(), friend.getUserId());
                    // 通知界面更新
                    MsgBroadcast.broadcastMsgNumReset(MyApplication.getInstance());
                    MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
                    MucgroupUpdateUtil.broadcastUpdateUi(MyApplication.getInstance());
                } else {
                    // 被xx踢出了群组
                    exitMucChat(friend.getUserId());
                    // / 1 标志被踢出该群组， 更新朋友表
                    FriendDao.getInstance().updateFriendGroupStatus(MyApplication.getLoginUserId(), friend.getUserId(), 1);
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_been_kick_place_holder, fromUserName));

                    ListenerManager.getInstance().notifyMyBeDelete(friend.getUserId());// 通知群组聊天界面
                }
            } else {
                // 其他人退出 || 被踢出
                if (fromUserId.equals(toUserId)) {
                    // message.setContent(toUserName + "退出了群组");
                    chatMessage.setContent(toUserName + " " + InternationalizationHelper.getString("QUIT_GROUP"));
                } else {
                    // message.setContent(toUserName + "被踢出群组");
                    chatMessage.setContent(toUserName + " " + InternationalizationHelper.getString("KICKED_OUT_GROUP"));
                }
                // 更新RoomMemberDao、更新群聊界面
                operatingRoomMemberDao(1, friend.getRoomId(), toUserId, null);
                MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(MyApplication.getContext());
            }
            int role = RoomMemberDao.getInstance().getMyRole(friend.getRoomId());
            if (role == Constants.ROLE_MANAGER || role == Constants.ROLE_OWNER) {
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage, true);
                }
            }
        } else if (type == Constants.TYPE_NEW_NOTICE) {
            // 发布公告
            try {
                MucRoom.Notice notice = JSONObject.parseObject(chatMessage.getContent(), MucRoom.Notice.class);
                EventBus.getDefault().post(new EventNewNotice(notice, chatMessage));
                chatMessage.setContent(fromUserName + " " + InternationalizationHelper.getString("JXMessageObject_AddNewAdv") + notice.getText());
                chatMessage.setType(Constants.TYPE_TIP);
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage, true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (type == Constants.TYPE_GAG) {// 群组与直播间禁言
            long time = Long.parseLong(chatMessage.getContent());
            if (toUserId != null && toUserId.equals(MyApplication.getLoginUserId())) {
                // 被禁言了|| 取消禁言 更新RoomTalkTime字段
                FriendDao.getInstance().updateRoomMyTalkTime(MyApplication.getLoginUserId(), friend.getUserId(), time);
                ListenerManager.getInstance().notifyMyVoiceBanned(friend.getUserId(), time);
            }
            // 为防止其他用户接收不及时，给3s的误差
            if (time > TimeUtils.time_current_time() + 3) {
                String formatTime = XfileUtils.fromatTime((time * 1000), "MM-dd HH:mm");
                // message.setContent("用户：" + toUserName + " 已被禁言到" + formatTime);
                chatMessage.setContent(fromUserName + " " + InternationalizationHelper.getString("JXMessageObject_Yes") + toUserName +
                        InternationalizationHelper.getString("JXMessageObject_SetGagWithTime") + formatTime);
            } else {
                // message.setContent("用户：" + toUserName + " 已被取消禁言");
                /*chatMessage.setContent(chatMessage.getFromUserName() + " " + getString("JXMessageObject_Yes") + toUserName +
                        getString("JXMessageObject_CancelGag"));*/
                chatMessage.setContent(toUserName + MyApplication.getInstance().getString(R.string.tip_been_cancel_ban_place_holder, fromUserName));
            }

            chatMessage.setType(Constants.TYPE_TIP);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage, true);
            }
        } else if (type == Constants.NEW_MEMBER) {
            String desc = "";
            if (chatMessage.getFromUserId().equals(toUserId)) {
                // 主动加入
                desc = fromUserName + " " + InternationalizationHelper.getString("JXMessageObject_GroupChat");
            } else {
                // 被邀请加入
                desc = fromUserName + " " + InternationalizationHelper.getString("JXMessageObject_InterFriend") + toUserName;

                String roomId = jsonObject.getString("fileName");
                if (!toUserId.equals(MyApplication.getLoginUserId())) {// 被邀请人为自己时不能更新RoomMemberDao，如更新了，在群聊界面判断出该表有人而不会在去调用接口获取该群真实的人数了
                    operatingRoomMemberDao(0, roomId, chatMessage.getToUserId(), toUserName);
                }
            }

            if (toUserId.equals(MyApplication.getLoginUserId())) {
                // 其他人邀请我加入该群组 才会进入该方法
                if (friend != null && friend.getGroupStatus() == 1) {// 本地存在该群组，且被踢出了该群组 先将该群组删除在创建(如调用updateGroupStatus直接修改该群组状态，可以会有问题，保险起见还是创建吧)
                    FriendDao.getInstance().deleteFriend(MyApplication.getLoginUserId(), friend.getUserId());
                    ChatMessageDao.getInstance().deleteMessageTable(MyApplication.getLoginUserId(), friend.getUserId());
                }

                String roomId = "";
                // 将当前群组部分属性存入共享参数内
                try {
                    // 群已读、公开、群验证、群成员列表可见、允许普通成员私聊
                    roomId = jsonObject.getString("fileName");
                    String other = jsonObject.getString("other");
                    JSONObject jsonObject2 = JSONObject.parseObject(other);
                    int showRead = jsonObject2.getInteger("showRead");
                    int allowSecretlyChat = jsonObject2.getInteger("allowSendCard");
                    MyApplication.getInstance().saveGroupPartStatus(chatMessage.getObjectId(), showRead, allowSecretlyChat,
                            1, 1, 0);
                } catch (Exception e) {
                    LogUtils.e(TAG, "解析时抛出异常");
                }

                Friend mCreateFriend = new Friend();
                mCreateFriend.setOwnerId(MyApplication.getLoginUserId());
                mCreateFriend.setUserId(chatMessage.getObjectId());
                mCreateFriend.setNickName(chatMessage.getContent());
                mCreateFriend.setDescription("");
                mCreateFriend.setRoomId(roomId);
                mCreateFriend.setContent(desc);
                mCreateFriend.setTimeSend(chatMessage.getTimeSend());
                mCreateFriend.setRoomFlag(1);
                mCreateFriend.setStatus(Constants.STATUS_FRIEND);
                mCreateFriend.setGroupStatus(0);
                FriendDao.getInstance().createOrUpdateFriend(mCreateFriend);
                // 调用smack加入群组的方法
                // 被邀请加入群组，lastSeconds == 当前时间 - 被邀请时的时间
                //20191209添加，防止加群失败
                long lastSeconds = TimeUtils.time_current_time() - chatMessage.getTimeSend();
                joinMucChat(chatMessage.getObjectId(), lastSeconds);
            }

            // 更新数据库
            chatMessage.setType(Constants.TYPE_TIP);
            chatMessage.setContent(desc);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage, true);
                MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(MyApplication.getContext());
            }
        } else if (type == Constants.TYPE_SEND_MANAGER) {
            String content = chatMessage.getContent();
            int role;
            if (content.equals("1")) {
                role = 2;
                chatMessage.setContent(fromUserName + " " + InternationalizationHelper.getString("JXSettingVC_Set") + toUserName + " " + InternationalizationHelper.getString("JXMessage_admin"));
            } else {
                role = 3;
                chatMessage.setContent(fromUserName + " " + InternationalizationHelper.getString("JXSip_Canceled") + toUserName + " " + InternationalizationHelper.getString("JXMessage_admin"));
            }

            RoomMemberDao.getInstance().updateRoomMemberRole(friend.getRoomId(), toUserId, role);

            chatMessage.setType(Constants.TYPE_TIP);
            // chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage, true);
                Intent intent = new Intent();
                intent.putExtra("roomId", friend.getUserId());
                intent.putExtra("toUserId", chatMessage.getToUserId());
                intent.putExtra("isSet", content.equals("1"));
                intent.setAction(com.iimm.miliao.broadcast.OtherBroadcast.REFRESH_MANAGER);
                MyApplication.getInstance().sendBroadcast(intent);
            }
        } else if (type == Constants.TYPE_UPDATE_ROLE) {
            int tipContent = -1;
            int role = Constants.ROLE_MEMBER;
            switch (chatMessage.getContent()) {
                case "1": // 1:设置隐身人
                    tipContent = R.string.tip_set_invisible_place_holder;
                    role = Constants.ROLE_INVISIBLE;
                    break;
                case "-1": // -1:取消隐身人
                    tipContent = R.string.tip_cancel_invisible_place_holder;
                    break;
                case "2": // 2：设置监控人
                    tipContent = R.string.tip_set_guardian_place_holder;
                    role = Constants.ROLE_GUARDIAN;
                    break;
                case "0": // 0：取消监控人
                    tipContent = R.string.tip_cancel_guardian_place_holder;
                    break;
                default:
                    Reporter.unreachable();
                    return;
            }
            chatMessage.setContent(MyApplication.getInstance().getString(tipContent, chatMessage.getFromUserName(), toUserName));

            RoomMemberDao.getInstance().updateRoomMemberRole(friend.getRoomId(), toUserId, role);

            chatMessage.setType(Constants.TYPE_TIP);
            // chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage, true);
                MsgBroadcast.broadcastMsgRoleChanged(MyApplication.getInstance());
            }
        }

        // 某个用户删除了群文件 或者 上传了群文件
        if (type == Constants.TYPE_MUCFILE_DEL || type == Constants.TYPE_MUCFILE_ADD) {
            String roomid = chatMessage.getObjectId();
            String str;
            if (type == Constants.TYPE_MUCFILE_DEL) {
                // str = chatMessage.getFromUserName() + " 删除了群文件 " + chatMessage.getFilePath();
                str = fromUserName + " " + InternationalizationHelper.getString("JXMessage_fileDelete") + ":" + chatMessage.getFilePath();
            } else {
                // str = chatMessage.getFromUserName() + " 上传了群文件 " + chatMessage.getFilePath();
                str = fromUserName + " " + InternationalizationHelper.getString("JXMessage_fileUpload") + ":" + chatMessage.getFilePath();
            }
            // 更新朋友表最后一条消息
            FriendDao.getInstance().updateFriendContent(MyApplication.getLoginUserId(), roomid, str, type, TimeUtils.time_current_time());
            FriendDao.getInstance().markUserMessageUnRead(MyApplication.getLoginUserId(), roomid); // 加一个小红点
            // 更新聊天记录表最后一条消息
            chatMessage.setContent(str);
            chatMessage.setType(Constants.TYPE_TIP);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), roomid, chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), roomid, chatMessage, true);
            }
            // 更新消息界面
            MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
            return;
        }
    }


    public static void chatGroupTip2(int type, ChatMessage chatMessage, String toUserName) {
        chatMessage.setType(Constants.TYPE_TIP);
        if (type == Constants.TYPE_GROUP_VERIFY) {
            // 916协议分为两种
            // 第一种为服务端发送，触发条件为群主在群组信息内 开/关 进群验证按钮，群组内每个人都能收到
            // 第二种为邀请、申请加入该群组，由邀请人或加入方发送给群主的消息，只有群主可以收到
            if (!TextUtils.isEmpty(chatMessage.getContent()) &&
                    (chatMessage.getContent().equals("0") || chatMessage.getContent().equals("1"))) {// 第一种
                if (chatMessage.getContent().equals("1")) {
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_group_enable_verify));
                } else {
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_group_disable_verify));
                }
                // chatMessage.setPacketId(ToolUtils.getUUID());
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage, true);
                }
            } else {//  群聊邀请确认消息 我收到该条消息 说明我就是该群的群主 待我审核
                try {
                    org.json.JSONObject json = new org.json.JSONObject(chatMessage.getObjectId());
                    String isInvite = json.getString("isInvite");
                    if (TextUtils.isEmpty(isInvite)) {
                        isInvite = "0";
                    }
                    if (isInvite.equals("0")) {
                        String id = json.getString("userIds");
                        String[] ids = id.split(",");
                        chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_invite_need_verify_place_holder, chatMessage.getFromUserName(), ids.length));
                    } else {
                        chatMessage.setContent(chatMessage.getFromUserName() + MyApplication.getInstance().getString(R.string.tip_need_verify_place_holder));
                    }
                    String roomJid = json.getString("roomJid");
                    if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), roomJid, chatMessage)) {
                        ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), roomJid, chatMessage, true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (type == Constants.TYPE_CHANGE_SHOW_READ) {
                PreferenceUtils.putBoolean(MyApplication.getInstance(),
                        Constants.IS_SHOW_READ + chatMessage.getObjectId(), chatMessage.getContent().equals("1"));
                if (chatMessage.getContent().equals("1")) {
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_enable_read));
                } else {
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_disable_read));
                }
            } else if (type == Constants.TYPE_GROUP_LOOK) {
                if (chatMessage.getContent().equals("1")) {
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_private));
                } else {
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_public));
                }
            } else if (type == Constants.TYPE_GROUP_SHOW_MEMBER) {
                if (chatMessage.getContent().equals("1")) {
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_enable_member));
                } else {
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_disable_member));
                }
            } else if (type == Constants.TYPE_GROUP_SEND_CARD) {
                PreferenceUtils.putBoolean(MyApplication.getInstance(),
                        Constants.IS_SEND_CARD + chatMessage.getObjectId(), chatMessage.getContent().equals("1"));
                if (chatMessage.getContent().equals("1")) {
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_enable_chat_privately));
                } else {
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_disable_chat_privately));
                }
            } else if (type == Constants.TYPE_GROUP_ALL_SHAT_UP) {
                long time = Long.parseLong(chatMessage.getContent());
                FriendDao.getInstance().updateRoomTalkTime(MyApplication.getLoginUserId(), chatMessage.getObjectId(), time);
                if (!chatMessage.getContent().equals("0")) {
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_now_ban_all));
                } else {
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_now_disable_ban_all));
                }
                MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(MyApplication.getContext());
            } else if (type == Constants.TYPE_GROUP_ALLOW_NORMAL_INVITE) {
                if (!chatMessage.getContent().equals("0")) {
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_enable_invite));
                } else {
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_disable_invite));
                }
            } else if (type == Constants.TYPE_GROUP_ALLOW_NORMAL_UPLOAD) {
                if (!chatMessage.getContent().equals("0")) {
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_enable_upload));
                } else {
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_disable_upload));
                }
            } else if (type == Constants.TYPE_GROUP_ALLOW_NORMAL_CONFERENCE) {
                PreferenceUtils.putBoolean(MyApplication.getInstance(),
                        Constants.IS_ALLOW_NORMAL_CONFERENCE + chatMessage.getObjectId(), !chatMessage.getContent().equals("0"));
                if (!chatMessage.getContent().equals("0")) {
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_enable_meeting));
                } else {
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_disable_meeting));
                }
            } else if (type == Constants.TYPE_GROUP_ALLOW_NORMAL_SEND_COURSE) {
                PreferenceUtils.putBoolean(MyApplication.getInstance(),
                        Constants.IS_ALLOW_NORMAL_SEND_COURSE + chatMessage.getObjectId(), !chatMessage.getContent().equals("0"));
                if (!chatMessage.getContent().equals("0")) {
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_enable_cource));
                } else {
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_disable_cource));
                }
            } else if (type == Constants.TYPE_GROUP_TRANSFER) {
                chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_new_group_owner_place_holder, toUserName));
                Friend friend = FriendDao.getInstance().getFriend(MyApplication.getLoginUserId(), chatMessage.getObjectId());
                if (friend != null) {
                    FriendDao.getInstance().updateRoomCreateUserId(MyApplication.getLoginUserId(),
                            chatMessage.getObjectId(), chatMessage.getToUserId());
                    RoomMemberDao.getInstance().updateRoomMemberRole(friend.getRoomId(), 0);
                    RoomMemberDao.getInstance().updateRoomMemberRole(friend.getRoomId(), chatMessage.getToUserId(), 1);
                    EventBus.getDefault().post(new TransferKingEvent(friend.getUserId(), chatMessage.getToUserId(), toUserName));
                }

            }
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage, true);
            }
        }
    }

    /**
     * 朋友相关消息逻辑处理
     */
    public static void chatFriend(String body, ChatMessage chatMessage) {
        /**
         * 改变
         * 1.因为 新的朋友从消息页面移至通讯录，不需要显示最后一条消息，updateLastChatMessage(...,Constants.ID_NEW_FRIEND_MESSAGE,...)可以注释掉
         * 2.因为多点登录，ex:我在web端删除了某个好友，android端接收到了，需要另外做处理
         */
        LogUtils.e(TAG, MyApplication.getLoginUserId() + "，" + chatMessage.getFromUserId() + "，" + chatMessage.getToUserId());
        LogUtils.e(TAG, chatMessage.getType() + "，" + chatMessage.getPacketId());

        if (chatMessage.getFromUserId().equals(MyApplication.getLoginUserId())) {// 我在其他端做的操作，在android也接收到了
            chatFriendFromMe(body, chatMessage);
        } else {
            chatFriendForMe(body, chatMessage);
        }
    }

    /**
     * 自己在其他端做的好友操作，发送过来的新朋友消息，需要另做处理
     * 处理逻辑与发送该条Type消息时所做的操作一样，可将代码复制过来
     */
    public static void chatFriendFromMe(String body, ChatMessage chatMessage) {
        String toUserId = chatMessage.getToUserId();
        JSONObject jsonObject = JSONObject.parseObject(body);
        String toUserName = jsonObject.getString("toUserName");
        if (TextUtils.isEmpty(toUserName)) {
            toUserName = "NULL";
        }
        switch (chatMessage.getType()) {
            case Constants.TYPE_SAYHELLO:
                // 我与对方打招呼
                NewFriendMessage message = NewFriendMessage.createLocalMessage(CoreManager.requireSelf(MyApplication.getInstance()),
                        Constants.TYPE_SAYHELLO, InternationalizationHelper.getString("HEY-HELLO"), toUserId, toUserName);
                NewFriendDao.getInstance().createOrUpdateNewFriend(message);
                NewFriendDao.getInstance().changeNewFriendState(toUserId, Constants.STATUS_10);//朋友状态
                ListenerManager.getInstance().notifyNewFriend(MyApplication.getLoginUserId(), message, true);

                // 发送打招呼的消息
                ChatMessage sayMessage = new ChatMessage();
                sayMessage.setFromUserId(MyApplication.getLoginUserId());
                sayMessage.setFromUserName(CoreManager.requireSelf(MyApplication.getInstance()).getNickName());
                sayMessage.setContent(InternationalizationHelper.getString("HEY-HELLO"));
                sayMessage.setType(Constants.TYPE_TEXT); //文本类型
                sayMessage.setMySend(true);
                sayMessage.setMessageState(Constants.MESSAGE_SEND_SUCCESS);
                sayMessage.setPacketId(ToolUtils.getUUID());
                sayMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
                ChatMessageDao.getInstance().saveNewSingleChatMessage(message.getOwnerId(), message.getUserId(), sayMessage);
                break;
            case Constants.TYPE_PASS:
                // 我同意了对方的加好友请求
                NewFriendMessage passMessage = NewFriendMessage.createLocalMessage(CoreManager.requireSelf(MyApplication.getInstance()),
                        Constants.TYPE_PASS, null, toUserId, toUserName);

                NewFriendDao.getInstance().ascensionNewFriend(passMessage, Constants.STATUS_FRIEND);
                FriendHelper.addFriendExtraOperation(MyApplication.getLoginUserId(), toUserId);
                FriendDao.getInstance().updateFriendContent(MyApplication.getLoginUserId(), toUserId,
                        InternationalizationHelper.getString("JXMessageObject_BeFriendAndChat"), Constants.TYPE_TEXT, TimeUtils.time_current_time());
                NewFriendDao.getInstance().changeNewFriendState(toUserId, Constants.STATUS_12);
                ListenerManager.getInstance().notifyNewFriend(MyApplication.getLoginUserId(), passMessage, true);
                break;
            case Constants.TYPE_FEEDBACK:
                // 我发送给对方的回话
                NewFriendMessage feedBackMessage = NewFriendDao.getInstance().getNewFriendById(MyApplication.getLoginUserId(), toUserId);
                if (feedBackMessage == null) {
                    feedBackMessage = NewFriendMessage.createLocalMessage(CoreManager.requireSelf(MyApplication.getInstance()),
                            Constants.TYPE_FEEDBACK, chatMessage.getContent(), toUserId, toUserName);
                    NewFriendDao.getInstance().createOrUpdateNewFriend(feedBackMessage);
                }
                if (feedBackMessage.getState() == Constants.STATUS_11 || feedBackMessage.getState() == Constants.STATUS_15) {
                    NewFriendDao.getInstance().changeNewFriendState(feedBackMessage.getUserId(), Constants.STATUS_15);
                } else {
                    NewFriendDao.getInstance().changeNewFriendState(feedBackMessage.getUserId(), Constants.STATUS_14);
                }
                NewFriendDao.getInstance().updateNewFriendContent(feedBackMessage.getUserId(), chatMessage.getContent());

                ChatMessage chatFeedMessage = new ChatMessage();// 本地也保存一份
                chatFeedMessage.setType(Constants.TYPE_TEXT); // 文本类型
                chatFeedMessage.setFromUserId(MyApplication.getLoginUserId());
                chatFeedMessage.setFromUserName(CoreManager.requireSelf(MyApplication.getInstance()).getNickName());
                chatFeedMessage.setContent(chatMessage.getContent());
                chatFeedMessage.setMySend(true);
                chatFeedMessage.setMessageState(Constants.MESSAGE_SEND_SUCCESS);
                chatFeedMessage.setPacketId(ToolUtils.getUUID());
                chatFeedMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
                ChatMessageDao.getInstance().saveNewSingleAnswerMessage(MyApplication.getLoginUserId(), toUserId, chatFeedMessage);
                ListenerManager.getInstance().notifyNewFriend(MyApplication.getLoginUserId(), feedBackMessage, true);
                break;
            case Constants.TYPE_FRIEND:
                // 对方未开启验证，我直接将对方添加为好友
                NewFriendMessage friendMessage = NewFriendMessage.createLocalMessage(CoreManager.requireSelf(MyApplication.getInstance()),
                        Constants.TYPE_FRIEND, null, toUserId, toUserName);
                NewFriendDao.getInstance().ascensionNewFriend(friendMessage, Constants.STATUS_FRIEND);
                FriendHelper.addFriendExtraOperation(MyApplication.getLoginUserId(), toUserId);
                NewFriendDao.getInstance().changeNewFriendState(toUserId, Constants.STATUS_22);
                FriendDao.getInstance().updateFriendContent(MyApplication.getLoginUserId(), toUserId,
                        InternationalizationHelper.getString("JXMessageObject_BeFriendAndChat"), Constants.TYPE_TEXT, TimeUtils.time_current_time());
                ListenerManager.getInstance().notifyNewFriend(MyApplication.getLoginUserId(), friendMessage, true);
                break;
            case Constants.TYPE_BLACK:
                // 我将对方拉黑
                NewFriendMessage blackMessage = NewFriendMessage.createLocalMessage(CoreManager.requireSelf(MyApplication.getInstance()),
                        Constants.TYPE_BLACK, null, toUserId, toUserName);
                FriendDao.getInstance().updateFriendStatus(MyApplication.getLoginUserId(), toUserId, Constants.STATUS_BLACKLIST);
                FriendHelper.addBlacklistExtraOperation(blackMessage.getOwnerId(), blackMessage.getUserId());
                NewFriendDao.getInstance().createOrUpdateNewFriend(blackMessage);
                NewFriendDao.getInstance().changeNewFriendState(toUserId, Constants.STATUS_18);
                ListenerManager.getInstance().notifyNewFriend(MyApplication.getLoginUserId(), blackMessage, true);
                break;
            case Constants.TYPE_REFUSED:
                // 我将对方移除黑名单
                NewFriendMessage removeMessage = NewFriendMessage.createLocalMessage(CoreManager.requireSelf(MyApplication.getInstance()),
                        Constants.TYPE_REFUSED, null, toUserId, toUserName);
                NewFriendDao.getInstance().ascensionNewFriend(removeMessage, Constants.STATUS_FRIEND);
                FriendHelper.beAddFriendExtraOperation(removeMessage.getOwnerId(), removeMessage.getUserId());
                NewFriendDao.getInstance().createOrUpdateNewFriend(removeMessage);
                NewFriendDao.getInstance().changeNewFriendState(toUserId, Constants.STATUS_24);
                ListenerManager.getInstance().notifyNewFriend(MyApplication.getLoginUserId(), removeMessage, true);
                break;
            case Constants.TYPE_DELALL:
                // 我删除了对方
                NewFriendMessage deleteMessage = NewFriendMessage.createLocalMessage(CoreManager.requireSelf(MyApplication.getInstance()),
                        Constants.TYPE_DELALL, null, chatMessage.getToUserId(), toUserName);
                FriendHelper.removeAttentionOrFriend(MyApplication.getLoginUserId(), chatMessage.getToUserId());
                NewFriendDao.getInstance().createOrUpdateNewFriend(deleteMessage);
                NewFriendDao.getInstance().changeNewFriendState(chatMessage.getToUserId(), Constants.STATUS_16);
                ListenerManager.getInstance().notifyNewFriend(MyApplication.getLoginUserId(), deleteMessage, true);
                break;
        }
        // 更新通讯录页面
        CardcastUiUpdateUtil.broadcastUpdateUi(MyApplication.getInstance());
    }

    /**
     * 对方发送过来的新朋友消息，处理逻辑不变
     */
    public static void chatFriendForMe(String body, ChatMessage chatMessage) {
        //  收到对方的新朋友消息也需要转发
        if (MyApplication.IS_SUPPORT_MULTI_LOGIN) {
            sendChatMessage(MyApplication.getLoginUserId(), chatMessage);
        }
        // json:fromUserId fromUserName type  content timeSend
        NewFriendMessage mNewMessage = new NewFriendMessage(body);
        mNewMessage.setOwnerId(MyApplication.getLoginUserId());
        mNewMessage.setUserId(chatMessage.getFromUserId());
        mNewMessage.setRead(false);
        mNewMessage.setMySend(false);
        mNewMessage.setPacketId(chatMessage.getPacketId());
        String content = "";
        switch (chatMessage.getType()) {
            case Constants.TYPE_SAYHELLO:
                // 对方发过来的打招呼消息
                NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);
                NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Constants.STATUS_11);
                // FriendDao.getInstance().updateLastChatMessage(MyApplication.getLoginUserId(), Constants.ID_NEW_FRIEND_MESSAGE, chatMessage);

                ChatMessage sayHelloMessage = new ChatMessage();
                sayHelloMessage.setType(Constants.TYPE_TEXT); //文本类型
                sayHelloMessage.setFromUserId(chatMessage.getFromUserId());
                sayHelloMessage.setFromUserName(chatMessage.getFromUserName());
                sayHelloMessage.setContent(InternationalizationHelper.getString("HEY-HELLO"));
                sayHelloMessage.setMySend(false);
                sayHelloMessage.setMessageState(Constants.MESSAGE_SEND_SUCCESS);
                sayHelloMessage.setPacketId(chatMessage.getPacketId());
                sayHelloMessage.setDoubleTimeSend(chatMessage.getTimeSend());
                ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), chatMessage.getFromUserId(), sayHelloMessage);
                ListenerManager.getInstance().notifyNewFriend(MyApplication.getLoginUserId(), mNewMessage, true);
                break;
            case Constants.TYPE_PASS:
                // 对方同意加我为好友
                NewFriendDao.getInstance().ascensionNewFriend(mNewMessage, Constants.STATUS_FRIEND);
                FriendHelper.beAddFriendExtraOperation(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                /*content = getString("JXFriendObject_PassGo");
                FriendDao.getInstance().updateLastChatMessage(MyApplication.getLoginUserId(), Constants.ID_NEW_FRIEND_MESSAGE, content);*/
                NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Constants.STATUS_13);//添加了xxx
                content = InternationalizationHelper.getString("JXMessageObject_BeFriendAndChat");
                FriendDao.getInstance().updateLastChatMessage(MyApplication.getLoginUserId(), mNewMessage.getUserId(), content);
                ListenerManager.getInstance().notifyNewFriend(MyApplication.getLoginUserId(), mNewMessage, true);
                break;
            case Constants.TYPE_FEEDBACK: {
                // 对方的回话
                NewFriendMessage feedBackMessage = NewFriendDao.getInstance().getNewFriendById(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);
                if (feedBackMessage.getState() == Constants.STATUS_11 || feedBackMessage.getState() == Constants.STATUS_15) {
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Constants.STATUS_15);
                } else {
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Constants.STATUS_14);
                }
                NewFriendDao.getInstance().updateNewFriendContent(mNewMessage.getUserId(), chatMessage.getContent());

                ChatMessage message = new ChatMessage();
                message.setType(Constants.TYPE_TEXT);// 文本类型
                message.setFromUserId(mNewMessage.getUserId());
                message.setFromUserName(mNewMessage.getNickName());
                message.setContent(mNewMessage.getContent());
                message.setMySend(false);
                message.setMessageState(Constants.MESSAGE_SEND_SUCCESS);
                message.setPacketId(chatMessage.getPacketId());
                message.setDoubleTimeSend(TimeUtils.time_current_time_double());
                ChatMessageDao.getInstance().saveNewSingleAnswerMessage(MyApplication.getLoginUserId(), mNewMessage.getUserId(), message);
                ListenerManager.getInstance().notifyNewFriend(MyApplication.getLoginUserId(), mNewMessage, true);
                break;
            }
            case Constants.TYPE_FRIEND:
                // 我未开启好友验证，对方直接添加我为好友
                NewFriendDao.getInstance().ascensionNewFriend(mNewMessage, Constants.STATUS_FRIEND);
                FriendHelper.beAddFriendExtraOperation(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Constants.STATUS_21);//添加了xxx
                /*content = mNewMessage.getNickName() + " 添加我为好友";
                FriendDao.getInstance().updateLastChatMessage(MyApplication.getLoginUserId(), Constants.ID_NEW_FRIEND_MESSAGE, content);*/
                content = InternationalizationHelper.getString("JXMessageObject_BeFriendAndChat");
                FriendDao.getInstance().updateLastChatMessage(MyApplication.getLoginUserId(), mNewMessage.getUserId(), content);
                ListenerManager.getInstance().notifyNewFriend(MyApplication.getLoginUserId(), mNewMessage, true);
                break;
            case Constants.TYPE_BLACK:
                // 对方将我拉黑
                NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);// 本地可能没有该NewFriend，需要先创建在修改其status
                NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Constants.STATUS_19);
                FriendHelper.beDeleteAllNewFriend(mNewMessage.getOwnerId(), mNewMessage.getUserId());
               /* content = mNewMessage.getNickName() + " " + getString("JXFriendObject_PulledBlack");
                FriendDao.getInstance().updateLastChatMessage(MyApplication.getLoginUserId(), Constants.ID_NEW_FRIEND_MESSAGE, content);*/
                ListenerManager.getInstance().notifyNewFriend(MyApplication.getLoginUserId(), mNewMessage, true);
                // 关闭聊天界面
                ChatActivity.callFinish(MyApplication.getInstance(), MyApplication.getInstance().getString(R.string.be_pulled_black), mNewMessage.getUserId());
                break;
            case Constants.TYPE_REFUSED:
                // 对方将我移出了黑名单
                NewFriendDao.getInstance().ascensionNewFriend(mNewMessage, Constants.STATUS_FRIEND);
                FriendHelper.beAddFriendExtraOperation(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Constants.STATUS_24);//添加了xxx
                /*content = mNewMessage.getNickName() + " 已取消黑名单";
                FriendDao.getInstance().updateLastChatMessage(MyApplication.getLoginUserId(), Constants.ID_NEW_FRIEND_MESSAGE, content);*/
                content = InternationalizationHelper.getString("JXMessageObject_BeFriendAndChat");
                FriendDao.getInstance().updateLastChatMessage(MyApplication.getLoginUserId(), mNewMessage.getUserId(), content);
                ListenerManager.getInstance().notifyNewFriend(MyApplication.getLoginUserId(), mNewMessage, true);
                break;
            case Constants.TYPE_DELALL:
                // 对方删除了我
                NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);// 本地可能没有该NewFriend，需要先创建在修改其status
                FriendHelper.beDeleteAllNewFriend(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Constants.STATUS_17);
                /*content = mNewMessage.getNickName() + " 删除了我";
                FriendDao.getInstance().updateLastChatMessage(MyApplication.getLoginUserId(), Constants.ID_NEW_FRIEND_MESSAGE, content);*/
                ListenerManager.getInstance().notifyNewFriend(MyApplication.getLoginUserId(), mNewMessage, true);
                // 关闭聊天界面
                ChatActivity.callFinish(MyApplication.getInstance(), InternationalizationHelper.getString("JXAlert_DeleteFirend"), mNewMessage.getUserId());
                break;
            case Constants.TYPE_CONTACT_BE_FRIEND:
                // 对方通过 手机联系人 添加我 直接成为好友
                NewFriendDao.getInstance().ascensionNewFriend(mNewMessage, Constants.STATUS_FRIEND);
                FriendHelper.beAddFriendExtraOperation(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Constants.STATUS_25);// 通过手机联系人添加
                content = InternationalizationHelper.getString("JXMessageObject_BeFriendAndChat");
                FriendDao.getInstance().updateLastChatMessage(MyApplication.getLoginUserId(), mNewMessage.getUserId(), content);
                ListenerManager.getInstance().notifyNewFriend(MyApplication.getLoginUserId(), mNewMessage, true);
                break;
            case Constants.TYPE_NEW_CONTACT_REGISTER: {
                // 我之前上传给服务端的联系人表内有人注册了，更新 手机联系人
                JSONObject jsonObject = JSONObject.parseObject(chatMessage.getContent());
                Contact contact = new Contact();
                contact.setTelephone(jsonObject.getString("telephone"));
                contact.setToTelephone(jsonObject.getString("toTelephone"));
                String toUserId = jsonObject.getString("toUserId");
                contact.setToUserId(toUserId);
                contact.setToUserName(jsonObject.getString("toUserName"));
                contact.setUserId(jsonObject.getString("userId"));
                if (ContactDao.getInstance().createContact(contact)) {// 本地创建成功 更新未读数量
                    EventBus.getDefault().post(new MessageContactEvent(toUserId));
                }
                break;
            }
            case Constants.TYPE_REMOVE_ACCOUNT: {
                // 用户被后台删除，用于客户端更新本地数据 ，from是系统管理员 ObjectId是被删除人的userId，
                String removedAccountId = chatMessage.getObjectId();
                Friend toUser = FriendDao.getInstance().getFriend(MyApplication.getLoginUserId(), removedAccountId);
                if (toUser != null) {
                    mNewMessage.setUserId(removedAccountId);
                    mNewMessage.setNickName(toUser.getNickName());
                    NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);// 本地可能没有该NewFriend，需要先创建在修改其status
                    FriendHelper.friendAccountRemoved(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Constants.STATUS_26);
                    NewFriendDao.getInstance().updateNewFriendContent(mNewMessage.getUserId(), chatMessage.getContent());
                    ListenerManager.getInstance().notifyNewFriend(MyApplication.getLoginUserId(), mNewMessage, true);
                    // 关闭聊天界面
                    ChatActivity.callFinish(MyApplication.getInstance(), chatMessage.getContent(), removedAccountId);
                }
                break;
            }
            case Constants.TYPE_BACK_DELETE: {
                // 后台删除了我的一个好友关系，
                JSONObject json = JSON.parseObject(chatMessage.getObjectId());
                String fromUserId = json.getString("fromUserId");
                String fromUserName = json.getString("fromUserName");
                String toUserId = json.getString("toUserId");
                String toUserName = json.getString("toUserName");
                if (TextUtils.equals(fromUserId, MyApplication.getLoginUserId())) {
                    // 我删除别人，
                    mNewMessage.setUserId(toUserId);
                    mNewMessage.setNickName(toUserName);
                    NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);// 本地可能没有该NewFriend，需要先创建在修改其status
                    FriendHelper.beDeleteAllNewFriend(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Constants.STATUS_16);
                    ListenerManager.getInstance().notifyNewFriend(MyApplication.getLoginUserId(), mNewMessage, true);
                } else {
                    // 别人删除我，
                    mNewMessage.setUserId(fromUserId);
                    mNewMessage.setNickName(fromUserName);
                    NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);// 本地可能没有该NewFriend，需要先创建在修改其status
                    FriendHelper.beDeleteAllNewFriend(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Constants.STATUS_17);
                    ListenerManager.getInstance().notifyNewFriend(MyApplication.getLoginUserId(), mNewMessage, true);
                }
                // 关闭聊天界面
                ChatActivity.callFinish(MyApplication.getInstance(), InternationalizationHelper.getString("JXAlert_DeleteFirend"), mNewMessage.getUserId());
                break;
            }
            case Constants.TYPE_BACK_BLACK: {
                // 后台拉黑了我的好友或者拉黑了我本身，
                JSONObject json = JSON.parseObject(chatMessage.getObjectId());
                String fromUserId = json.getString("fromUserId");
                String fromUserName = json.getString("fromUserName");
                String toUserId = json.getString("toUserId");
                if (TextUtils.equals(fromUserId, MyApplication.getLoginUserId())) {
                    // 我拉黑别人，
                    mNewMessage.setUserId(toUserId);
                    Friend toUser = FriendDao.getInstance().getFriend(MyApplication.getLoginUserId(), toUserId);
                    if (toUser == null) {
                        Reporter.post("后台拉黑了个不存在的好友，" + toUserId);
                        return;
                    }
                    mNewMessage.setNickName(toUser.getNickName());
                    FriendDao.getInstance().updateFriendStatus(MyApplication.getLoginUserId(), toUserId, Constants.STATUS_BLACKLIST);
                    FriendHelper.addBlacklistExtraOperation(MyApplication.getLoginUserId(), toUserId);

                    ChatMessage addBlackChatMessage = new ChatMessage();
                    addBlackChatMessage.setContent(InternationalizationHelper.getString("JXFriendObject_AddedBlackList") + " " + toUser.getShowName());
                    addBlackChatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
                    FriendDao.getInstance().updateLastChatMessage(MyApplication.getLoginUserId(), Constants.ID_NEW_FRIEND_MESSAGE, addBlackChatMessage);

                    NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Constants.STATUS_18);
                    ListenerManager.getInstance().notifyNewFriend(MyApplication.getLoginUserId(), mNewMessage, true);
                    // 关闭聊天界面
                    ChatActivity.callFinish(MyApplication.getInstance(), chatMessage.getContent(), toUserId);
                } else {
                    // 我被拉黑，
                    mNewMessage.setUserId(fromUserId);
                    mNewMessage.setNickName(fromUserName);
                    NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);// 本地可能没有该NewFriend，需要先创建在修改其status
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Constants.STATUS_19);
                    FriendHelper.beDeleteAllNewFriend(mNewMessage.getOwnerId(), mNewMessage.getUserId());
               /* content = mNewMessage.getNickName() + " " + getString("JXFriendObject_PulledBlack");
                FriendDao.getInstance().updateLastChatMessage(MyApplication.getLoginUserId(), Constants.ID_NEW_FRIEND_MESSAGE, content);*/
                    ListenerManager.getInstance().notifyNewFriend(MyApplication.getLoginUserId(), mNewMessage, true);
                    // 关闭聊天界面
                    ChatActivity.callFinish(MyApplication.getInstance(), MyApplication.getInstance().getString(R.string.be_pulled_black), mNewMessage.getUserId());
                }
                break;
            }
            case Constants.TYPE_BACK_REFUSED: {
                // 后台取消拉黑了我的好友或者取消拉黑了我本身，
                JSONObject json = JSON.parseObject(chatMessage.getObjectId());
                String fromUserId = json.getString("fromUserId");
                String fromUserName = json.getString("fromUserName");
                String toUserId = json.getString("toUserId");
                if (TextUtils.equals(fromUserId, MyApplication.getLoginUserId())) {
                    // 取消拉黑了我的黑名单，
                    mNewMessage.setUserId(toUserId);
                    Friend toUser = FriendDao.getInstance().getFriend(MyApplication.getLoginUserId(), toUserId);
                    if (toUser == null) {
                        Reporter.post("后台取消拉黑了个不存在的好友，" + toUserId);
                    } else {
                        mNewMessage.setNickName(toUser.getNickName());
                    }
                    NewFriendDao.getInstance().ascensionNewFriend(mNewMessage, Constants.STATUS_FRIEND);
                    FriendHelper.beAddFriendExtraOperation(MyApplication.getLoginUserId(), toUserId);

                    User self = CoreManager.requireSelf(MyApplication.getInstance());
                    ChatMessage removeChatMessage = new ChatMessage();
                    removeChatMessage.setContent(self.getNickName() + InternationalizationHelper.getString("REMOVE"));
                    removeChatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
                    FriendDao.getInstance().updateLastChatMessage(MyApplication.getLoginUserId(), Constants.ID_NEW_FRIEND_MESSAGE, removeChatMessage);
                    NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);
                    NewFriendDao.getInstance().changeNewFriendState(toUserId, Constants.STATUS_24);
                    ListenerManager.getInstance().notifyNewFriend(MyApplication.getLoginUserId(), mNewMessage, true);
                } else {
                    // 我被取消拉黑，
                    mNewMessage.setUserId(fromUserId);
                    mNewMessage.setNickName(fromUserName);
                    NewFriendDao.getInstance().ascensionNewFriend(mNewMessage, Constants.STATUS_FRIEND);
                    FriendHelper.beAddFriendExtraOperation(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Constants.STATUS_24);//添加了xxx
                    content = InternationalizationHelper.getString("JXMessageObject_BeFriendAndChat");
                    FriendDao.getInstance().updateLastChatMessage(MyApplication.getLoginUserId(), mNewMessage.getUserId(), content);
                    ListenerManager.getInstance().notifyNewFriend(MyApplication.getLoginUserId(), mNewMessage, true);
                }
                break;
            }
            case Constants.TYPE_NEWSEE:// 对方单向关注了我
            case Constants.TYPE_DELSEE:// 对方取消了对我的单向关注
                // 单向关注 功能已去掉
                break;
            case Constants.TYPE_RECOMMEND:
                // 新推荐好友 好像无此功能
                break;
            default:
                break;
        }
        // 更新通讯录页面
        CardcastUiUpdateUtil.broadcastUpdateUi(MyApplication.getInstance());
    }

    /**
     * 朋友圈相关消息逻辑处理
     */
    public static void chatDiscover(String body, ChatMessage chatMessage) {
        JSONObject jObject = JSON.parseObject(body);
        String to = ToolUtils.getStringValueFromJSONObject(jObject, "to");
        if (TextUtils.isEmpty(to)) {
            to = "";
        }
        String fromUserId = chatMessage.getFromUserId();
        String toUserId = chatMessage.getToUserId();
        Friend fromFriend = FriendDao.getInstance().getMyFriendAndSystemFriends(MyApplication.getLoginUserId(), fromUserId);
        if (fromFriend == null && !fromUserId.equals(MyApplication.getLoginUserId())) {
            //点赞的人不是自己好友 就不处理，只有是自己好友才处理
            return;
        }
        if (!TextUtils.isEmpty(toUserId) && toUserId.equals("0")) {
            //自己评论的自己
        } else {
            Friend toFriend = FriendDao.getInstance().getMyFriendAndSystemFriends(MyApplication.getLoginUserId(), toUserId);
            if (toFriend == null && !toUserId.equals(MyApplication.getLoginUserId())) {
                //不是给自己的不处理
                return;
            }
        }
        if (MyZanDao.getInstance().hasSameZan(chatMessage.getPacketId())) {
            LogUtils.e(TAG, "本地已存在该条赞或评论消息");
            return;
        }
        MyZan zan = new MyZan();
        zan.setFromUserId(chatMessage.getFromUserId());
        zan.setFromUsername(chatMessage.getFromUserName());
        zan.setSendtime(String.valueOf(chatMessage.getTimeSend()));
        zan.setLoginUserId(MyApplication.getLoginUserId());
        zan.setZanbooleanyidu(0);
        zan.setSystemid(chatMessage.getPacketId());
        /**
         * object组成: id,type,content
         *
         * id
         * type:1 文本 2 图片 3 语音 4 视频
         * content:文本内容
         */
        String[] data = chatMessage.getObjectId().split(",");
        zan.setCricleuserid(data[0]);
        zan.setType(Integer.parseInt(data[1]));
        if (Integer.parseInt(data[1]) == 1) {// 文本类型
            zan.setContent(data[2]);
        } else {// 其他类型
            zan.setContenturl(data[2]);
        }

        if (chatMessage.getType() == Constants.DIANZAN) {// 赞
            zan.setHuifu("101");
            if (MyZanDao.getInstance().addZan(zan)) {
                int size = MyZanDao.getInstance().getZanSize(MyApplication.getLoginUserId());
                EventBus.getDefault().post(new MessageEventHongdian(size));
                EventBus.getDefault().post(new MessageEventNotifyDynamic(size));
            } else {
                // 针对该条说说fromUserId已经点赞过一次了，就不重复提醒了，需要Return掉，继续往下走会有提示音
                return;
            }
        } else if (chatMessage.getType() == Constants.PINGLUN) {// 评论
            if (chatMessage.getContent() != null) {
                zan.setHuifu(chatMessage.getContent());
            }
            JSONObject jsonObject = JSONObject.parseObject(body);
            String toUserName = jsonObject.getString("toUserName");
            if (!TextUtils.isEmpty(toUserName)) {
                zan.setTousername(toUserName);
            }
            MyZanDao.getInstance().addZan(zan);
            int size = MyZanDao.getInstance().getZanSize(MyApplication.getLoginUserId());
            EventBus.getDefault().post(new MessageEventHongdian(size));
            EventBus.getDefault().post(new MessageEventNotifyDynamic(size));
        } else if (chatMessage.getType() == Constants.ATMESEE) {// 提醒我看
            zan.setHuifu("102");
            MyZanDao.getInstance().addZan(zan);
            int size = MyZanDao.getInstance().getZanSize(MyApplication.getLoginUserId());
            EventBus.getDefault().post(new MessageEventHongdian(size));
            EventBus.getDefault().post(new MessageEventNotifyDynamic(size));
        }

        // 朋友圈消息也要提示，
        NoticeVoicePlayer.getInstance().start();
    }

    /**
     * 音视频相关消息逻辑处理
     */
    public static void chatAudioVideo(ChatMessage chatMessage) {
        int type = chatMessage.getType();
        LogUtils.e(TAG, type + "");
        String fromUserId = chatMessage.getFromUserId();
        if (fromUserId.equals(MyApplication.getLoginUserId())) {
            switch (chatMessage.getType()) {
                case Constants.TYPE_IS_CONNECT_VOICE:
                    // 其他端发起语音通话请求，转发给本端，不处理
                    break;
                case Constants.TYPE_CONNECT_VOICE:
                    // 其他端已接听语音通话，本端需要结束当前来电显示界面
                    EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
                    break;
                case Constants.TYPE_NO_CONNECT_VOICE:
                    // 其他端拒接 || 无响应 语音通话，本端需要结束当前来电显示界面
                    EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
                    break;
                case Constants.TYPE_END_CONNECT_VOICE:
                    // 其他端结束了语音通话，不处理
                    break;
                case Constants.TYPE_IS_CONNECT_VIDEO:
                    // 其他端发起视频通话请求，转发给本端， 不处理
                    break;
                case Constants.TYPE_CONNECT_VIDEO:
                    // 其他端已接听视频通话，本端需要结束当前来电显示界面
                    EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
                    break;
                case Constants.TYPE_NO_CONNECT_VIDEO:
                    // 其他端拒接 || 无响应 视频通话，本端需要结束当前来电显示界面
                    EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
                    break;
                case Constants.TYPE_END_CONNECT_VIDEO:
                    // 其他端结束了视频通话，不处理
                    break;
                case Constants.TYPE_IS_MU_CONNECT_VOICE:
                    // 其他端发起语音会议请求，不处理
                    break;
                case Constants.TYPE_IS_MU_CONNECT_VIDEO:
                    // 其他端发起视频会议请求，不处理
                    break;

                case Constants.TYPE_IN_CALLING:
                    // 其他端发送的通话中消息，不处理
                    break;
                case Constants.TYPE_IS_BUSY:
                    // 其他端发送的忙线消息，不处理
                    break;
            }
        } else {
            if (chatMessage.getType() == Constants.TYPE_IN_CALLING
                    || chatMessage.getType() == Constants.TYPE_IS_BUSY) {
                if (chatMessage.getType() == Constants.TYPE_IS_BUSY) {// 延迟两秒发送该通知，防止自己拨号页面还未拉起就收到了
                    LogUtils.e(TAG, "收到" + chatMessage.getFromUserName() + "的busy消息");
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LogUtils.e(TAG, "发送busy通知给" + chatMessage.getFromUserName());
                            EventBus.getDefault().post(new MessageCallingEvent(chatMessage));
                        }
                    }, 2000);
                } else {
                    EventBus.getDefault().post(new MessageCallingEvent(chatMessage));
                }
                return;
            }

            // Todo 直接在来电邀请界面做处理好了
/*
            // 当前正在通话中且该条消息的发送方与通话对象不一致 通知发送方忙线中...
            if (JitsistateMachine.isInCalling
                    && !TextUtils.isEmpty(JitsistateMachine.callingOpposite)
                    && !JitsistateMachine.callingOpposite.equals(chatMessage.getFromUserId())) {
                LogUtils.e(TAG, "发送busy消息给" + chatMessage.getFromUserName());
                MyApplication.getInstance().sendBusyMessage(chatMessage.getFromUserId());
                return;
            }
*/

            /*
            单聊 语音通话
             */
            if (chatMessage.getType() == Constants.TYPE_IS_CONNECT_VOICE) {
                // 对方来电
                if (JitsistateMachine.isInCalling) {
                    sendBusyMessage(chatMessage.getFromUserId());
                    return;
                }
                LogUtils.e(TAG, TimeUtils.time_current_time() - chatMessage.getTimeSend() + "");
                if (TimeUtils.time_current_time() - chatMessage.getTimeSend() <= 30) {// 当前时间与对方发送邀请的时间间隔在30s以内
                    EventBus.getDefault().post(new MessageEventSipEVent(100, fromUserId, chatMessage));
                } else {
                    LogUtils.e(TAG, "离线消息");
                }
            } else if (chatMessage.getType() == Constants.TYPE_CONNECT_VOICE) {
                // 对方接听语音通话，发送102
                EventBus.getDefault().post(new MessageEventSipEVent(102, null, chatMessage));
            } else if (chatMessage.getType() == Constants.TYPE_NO_CONNECT_VOICE) {
                // 对方拒接 || 无响应
                EventBus.getDefault().post(new MessageEventSipEVent(103, null, chatMessage));
                String content = "";
                chatMessage.setMySend(false);
                if (chatMessage.getTimeLen() == 0) {
                    content = InternationalizationHelper.getString("JXSip_Canceled") + InternationalizationHelper.getString("JX_VoiceChat");
                } else {
                    content = InternationalizationHelper.getString("JXSip_noanswer");
                }
                ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), fromUserId, chatMessage);
                ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), fromUserId, chatMessage, false);
                FriendDao.getInstance().updateFriendContent(MyApplication.getLoginUserId(), fromUserId, content, Constants.TYPE_NO_CONNECT_VOICE, chatMessage.getTimeSend());
                MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
                    }
                }, 1000);// 延迟一秒在发送挂断消息，防止当我们离线时，对方发起通话之后又取消了通话，我们30秒内上线，在来点界面拉起时该Event也发送出去了
            } else if (chatMessage.getType() == Constants.TYPE_END_CONNECT_VOICE) {
                // 通话后，对方挂断
                chatMessage.setMySend(false);
                ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), fromUserId, chatMessage);
                ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), fromUserId, chatMessage, false);
                FriendDao.getInstance().updateFriendContent(MyApplication.getLoginUserId(), fromUserId,
                        InternationalizationHelper.getString("JXSip_finished") + InternationalizationHelper.getString("JX_VoiceChat") + ","
                                + InternationalizationHelper.getString("JXSip_timeLenth") + ":" + chatMessage.getTimeLen()
                                + InternationalizationHelper.getString("JX_second"), Constants.TYPE_END_CONNECT_VOICE, chatMessage.getTimeSend());
                MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
                // 通知通话界面挂断
                EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
            }

             /*
            单聊  视频通话
             */
            if (type == Constants.TYPE_IS_CONNECT_VIDEO) {
                if (JitsistateMachine.isInCalling) {
                    sendBusyMessage(chatMessage.getFromUserId());
                    return;
                }
                LogUtils.e(TAG, TimeUtils.time_current_time() - chatMessage.getTimeSend() + "");
                if (TimeUtils.time_current_time() - chatMessage.getTimeSend() <= 30) {// 当前时间与对方发送邀请的时间间隔在30s以内
                    EventBus.getDefault().post(new MessageEventSipEVent(110, fromUserId, chatMessage));
                } else {
                    LogUtils.e(TAG, "离线消息");
                }
            } else if (type == Constants.TYPE_CONNECT_VIDEO) {
                EventBus.getDefault().post(new MessageEventSipEVent(112, null, chatMessage));
            } else if (type == Constants.TYPE_NO_CONNECT_VIDEO) {
                EventBus.getDefault().post(new MessageEventSipEVent(113, null, chatMessage));
                chatMessage.setMySend(false);
                String content = "";
                if (chatMessage.getTimeLen() == 0) {
                    content = InternationalizationHelper.getString("JXSip_Canceled") + InternationalizationHelper.getString("JX_VideoChat");
                } else {
                    content = InternationalizationHelper.getString("JXSip_noanswer");
                }
                ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), fromUserId, chatMessage);
                FriendDao.getInstance().updateFriendContent(MyApplication.getLoginUserId(), fromUserId, content, Constants.TYPE_NO_CONNECT_VIDEO, chatMessage.getTimeSend());
                ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), fromUserId, chatMessage, false);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
                    }
                }, 1000);// 延迟一秒在发送挂断消息，防止当我们离线时，对方发起通话之后又取消了通话，我们30秒内上线，在来点界面拉起时该Event也发送出去了
            } else if (type == Constants.TYPE_END_CONNECT_VIDEO) {
                chatMessage.setMySend(false);
                ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), fromUserId, chatMessage);
                ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), fromUserId, chatMessage, false);
                FriendDao.getInstance().updateFriendContent(MyApplication.getLoginUserId(), fromUserId, InternationalizationHelper.getString("JX_VideoChat") + "," +
                        InternationalizationHelper.getString("JXSip_timeLenth") + ":" + chatMessage.getTimeLen() + InternationalizationHelper.getString("JX_second"), Constants.TYPE_END_CONNECT_VIDEO, chatMessage.getTimeSend());
                EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
            }

            /**
             群组 音视频会议邀请
             */
            if (type == Constants.TYPE_IS_MU_CONNECT_VOICE) {
                LogUtils.e(TAG, TimeUtils.time_current_time() - chatMessage.getTimeSend() + "");
                if (TimeUtils.time_current_time() - chatMessage.getTimeSend() <= 30) {// 当前时间与对方发送邀请的时间间隔在30s以内
                    EventBus.getDefault().post(new MessageEventMeetingInvited(CallConstants.Audio_Meet, chatMessage));
                } else {
                    LogUtils.e(TAG, "离线消息");
                }
                // 音视频会议消息不保存
/*
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), fromUserId, chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), fromUserId, chatMessage, false);
                    FriendDao.getInstance().updateFriendContent(MyApplication.getLoginUserId(), fromUserId, chatMessage.getContent(), Constants.TYPE_IS_MU_CONNECT_VOICE, chatMessage.getTimeSend());
                }
*/
            } else if (type == Constants.TYPE_IS_MU_CONNECT_VIDEO) {
                LogUtils.e(TAG, TimeUtils.time_current_time() - chatMessage.getTimeSend() + "");
                if (TimeUtils.time_current_time() - chatMessage.getTimeSend() <= 30) {// 当前时间与对方发送邀请的时间间隔在30s以内
                    EventBus.getDefault().post(new MessageEventMeetingInvited(CallConstants.Video_Meet, chatMessage));
                } else {
                    LogUtils.e(TAG, "离线消息");
                }
                // 音视频会议消息不保存
/*
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), fromUserId, chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), fromUserId, chatMessage, false);
                    FriendDao.getInstance().updateFriendContent(MyApplication.getLoginUserId(), fromUserId, chatMessage.getContent(), Constants.TYPE_IS_MU_CONNECT_Video, chatMessage.getTimeSend());
                }
*/
            }
            // 将消息转发出去
            if (MyApplication.IS_SUPPORT_MULTI_LOGIN) {
                sendChatMessage(MyApplication.getLoginUserId(), chatMessage);
            }
        }
    }


    // 多点登录
    public static void moreLogin(Message message, String resource, ChatMessage chatMessage) {
        if (!checkXmppAuthenticated()) {
            return;
        }
        XMPPTCPConnection xmpptcpConnection = XmppConnectionImpl.getInstance().getXMPPConnection();
        boolean onLine;
        if (chatMessage.getContent().equals("0")) {
            // 下线消息
            onLine = false;
        } else {
            // 上线消息g
            onLine = true;
        }
        EventBus.getDefault().post(new EventLoginStatus(resource, onLine));
        MachineDao.getInstance().updateMachineOnLineStatus(resource, onLine);
        // 收到 type== 200 的消息，主动给对方发送回执
        Message ack = DeliveryReceiptManager.receiptMessageFor(message);
        if (ack == null) {
            LogUtils.e(TAG, "ack == null ");
            return;
        }
        try {
            xmpptcpConnection.sendStanza(ack);
            saveSendMsg(ack);
            LogUtils.e(TAG, "sendStanza success");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, "sendStanza Exception");
        }
    }

    /**
     * 发送新朋友消息
     */
    public static void sendNewFriendXmppMessage(String toUserId, final NewFriendMessage newFriendMessage) {
        LogUtils.e("SendNewFriendMessage：", "toUserId:" + toUserId);
        if (!checkXmppAuthenticated()) {
            return;
        }
        XMPPTCPConnection xmpptcpConnection = XmppConnectionImpl.getInstance().getXMPPConnection();
        try {
            Message msg = new Message();
            msg.setType(Message.Type.chat);
            msg.setBody(newFriendMessage.toJsonString());// 新朋友推送消息
            msg.setStanzaId(newFriendMessage.getPacketId());
            EntityBareJid entityBareJid = getEntityBarJid(toUserId);
            if (entityBareJid == null) {
                return;
            }
            msg.setTo(entityBareJid);
            if (MyApplication.IS_OPEN_RECEIPT) {
                DeliveryReceiptRequest.addTo(msg);
            }
            try {
                xmpptcpConnection.sendStanza(msg);// 发送消息
                saveSendMsg(msg);
                //ListenerManager.getInstance().notifyNewFriendSendStateChange(toUserId, newFriendMessage, Constants.MESSAGE_SEND_ING);
            } catch (InterruptedException e) {
                ListenerManager.getInstance().notifyNewFriendSendStateChange(toUserId, newFriendMessage, Constants.MESSAGE_SEND_FAILED);
                e.printStackTrace();
            }

            // 转发给自己
            if (MyApplication.IS_SUPPORT_MULTI_LOGIN) {// 多点登录下需要转发
                sendForwardMessage(msg);
            }

        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            ListenerManager.getInstance().notifyNewFriendSendStateChange(toUserId, newFriendMessage, Constants.MESSAGE_SEND_FAILED);
        }
    }

    // 发送上、下线，检测(type==200)，转发消息
    public static void sendForwardMessage(Message msg) {
        if (MyApplication.IS_SEND_MSG_EVERYONE) {
            LogUtils.e(TAG, "sendMessageToEvery");
            /*
            第一次发送type==200的消息，因为本地其他端的状态都为离线，
            因此不能调用sendMessageToSome去发消息，直接发一条200的消息出去，
            无条件请求回执
             */
            if (!MyApplication.IS_OPEN_RECEIPT) {// 为true的话上面已经请求过回执了，不在重复请求
                DeliveryReceiptRequest.addTo(msg);
            }
            sendMessageToEvery(msg);
        } else {
            sendMessageToOtherMachine(msg);
        }
    }

    public static void sendMessageToEvery(Message msg) {
        if (!checkXmppAuthenticated()) {
            return;
        }
        XMPPTCPConnection xmpptcpConnection = XmppConnectionImpl.getInstance().getXMPPConnection();
        EntityBareJid entityBareJid = getEntityBarJid(MyApplication.getLoginUserId());
        if (entityBareJid == null) {
            return;
        }
        msg.setTo(entityBareJid);
        try {
            xmpptcpConnection.sendStanza(msg);
            saveTransferSendMsg(msg, "所有人");
        } catch (Exception e) {
            e.printStackTrace();
        }
        MyApplication.IS_SEND_MSG_EVERYONE = false;
    }

    /**
     * 发给我的其他设备
     *
     * @param msg
     */
    public static void sendMessageToOtherMachine(Message msg) {
        if (!checkXmppAuthenticated()) {
            return;
        }
        XMPPTCPConnection xmpptcpConnection = XmppConnectionImpl.getInstance().getXMPPConnection();
        for (String machineResource : MyApplication.machine) {
            if (MachineDao.getInstance().getMachineOnLineStatus(machineResource)) {
                LogUtils.e(TAG, "转发给" + machineResource + "设备");
                try {
                    Message message = new Message();// 需要重新创建一个Msg，如果引用之前的Msg对象，当第一个Msg或前面的Msg还未发送出去时，可能会出问题
                    message.setType(Message.Type.chat);
                    message.setBody(msg.getBody());
                    message.setStanzaId(msg.getStanzaId());
                    Jid jid = getFullJid(MyApplication.getLoginUserId(), machineResource);
                    if (jid == null) {
                        return;
                    }
                    message.setTo(jid);
                    xmpptcpConnection.sendStanza(message);
                    saveTransferSendMsg(message, machineResource);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static FullJid getFullJid(String localPart, String resource) {
        if (!checkXmppAuthenticated()) {
            return null;
        }
        XMPPTCPConnection xmpptcpConnection = XmppConnectionImpl.getInstance().getXMPPConnection();
        try {
            Localpart localpart = Localpart.from(localPart);
            Domainpart domainpart = xmpptcpConnection.getXMPPServiceDomain().getDomain();
            Resourcepart resourcepart = Resourcepart.from(resource);
            FullJid jid = JidCreate.fullFrom(localpart, domainpart, resourcepart);
            return jid;
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static FullJid getFullJid(String jid) {
        try {
            FullJid fullJid = JidCreate.fullFrom(jid);
            return fullJid;
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static EntityBareJid getEntityBarJid(String localPart) {
        if (!checkXmppAuthenticated()) {
            return null;
        }
        XMPPTCPConnection xmpptcpConnection = XmppConnectionImpl.getInstance().getXMPPConnection();
        try {
            Localpart localpart = Localpart.from(localPart);
            Domainpart domainpart = xmpptcpConnection.getXMPPServiceDomain().getDomain();
            EntityBareJid jid = JidCreate.entityBareFrom(localpart, domainpart);
            return jid;
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 发送聊天的消息
     *
     * @param toUserId 要发送给的用户
     * @param oMessage 已经存到本地数据库的一条即将发送的消息
     */
    public static void sendChatXmppMessage(final String toUserId, final ChatMessage oMessage) {
        if (!checkXmppAuthenticated()) {
            return;
        }
        XMPPTCPConnection xmpptcpConnection = XmppConnectionImpl.getInstance().getXMPPConnection();
        // 加密可能影响到消息对象复用，所以拷贝一份，
        ChatMessage chatMessage = oMessage.clone(false);
        /*
         * 先将自己定义的消息类型转(ChatMessage)换成 smack第三方定义的Message类型
         * 然后通过smack的Chat对象来发送一个msg
         */
        try {
            if (!MyApplication.getLoginUserId().equals(toUserId)) {
                if (chatMessage.getIsEncrypt() == 1) {
                    try {
                        // 因为多点登录转发也是这里，需要
                        // String x = DES.encryptDES(chatMessage.getContent(), Constants.ENCRYPT_DES);
                        // 更换加密key
                        String encryptKey = Md5Util.toMD5(AppConfig.apiKey + chatMessage.getTimeSend() + chatMessage.getPacketId());
                        String x = DES.encryptDES(chatMessage.getContent(), encryptKey);
                        chatMessage.setContent(x);
                    } catch (Exception e) {
                        // 加密失败，将该字段置为不加密，以防接收方收到后去解密
                        chatMessage.setIsEncrypt(0);
                    }
                }
            } else {
                // 给自己的消息不加密，
                chatMessage.setIsEncrypt(0);
            }
            Message msg = new Message();
            msg.setType(Message.Type.chat);
            msg.setBody(chatMessage.toJsonString());
            msg.setStanzaId(chatMessage.getPacketId());
            EntityBareJid entityBareJid = getEntityBarJid(toUserId);
            if (entityBareJid == null) {
                return;
            }
            msg.setTo(entityBareJid);
            if (MyApplication.IS_OPEN_RECEIPT) {// 在发送消息之前发送回执请求
                DeliveryReceiptRequest.addTo(msg);
            }
            // 发送消息给其他人(一条resource不拼接的消息)
            if (!MyApplication.getLoginUserId().equals(toUserId)) {// 发送转发消息 || 检测消息 || 给我的设备发消息，会直接往下走
                try {
                    LogUtils.e(TAG, "发送消息给其他人");
                    xmpptcpConnection.sendStanza(msg);
                    saveSendMsg(msg);
                    // 调用消息发送状态监听，将消息发送状态改为发送中...
//                    ListenerManager.getInstance().notifyMessageSendStateChange(
//                            MyApplication.getLoginUserId(), toUserId, chatMessage.getPacketId(),
//                            Constants.MESSAGE_SEND_ING);
                } catch (InterruptedException e) {
                    // 调用消息发送状态监听，将消息发送状态改为发送中...
                    ListenerManager.getInstance().notifyMessageSendStateChange(
                            MyApplication.getLoginUserId(), toUserId, chatMessage.getPacketId(),
                            Constants.MESSAGE_SEND_FAILED);
                    e.printStackTrace();
                }
            }
            // 给我的设备发消息，不转发
            if (Constants.SEND_TO_OTHER_DEVIE) {
                if (!TextUtils.isEmpty(chatMessage.getFromUserId()) && !TextUtils.isEmpty(chatMessage.getToUserId())
                        && chatMessage.getFromUserId().equals(chatMessage.getToUserId())
                        && chatMessage.getType() != Constants.TYPE_SEND_ONLINE_STATUS) {
                    try {
                        if (MyApplication.IsRingId.equals("Empty")) {
                            xmpptcpConnection.sendStanza(msg);// 理论上不太可能
                            saveSendMsg(msg);
                        } else {
                            LogUtils.e(TAG, toUserId + "--&&--" + MyApplication.IsRingId);
                            FullJid fullJid = getFullJid(MyApplication.getLoginUserId(), MyApplication.IsRingId);
                            if (fullJid == null) {
                                return;
                            }
                            msg.setTo(fullJid);
                            xmpptcpConnection.sendStanza(msg);
                            saveSendMsg(msg);
                            LogUtils.e(TAG, "消息发送成功");
                        }
                    } catch (InterruptedException e) {
                        ListenerManager.getInstance().notifyMessageSendStateChange(
                                MyApplication.getLoginUserId(), toUserId, chatMessage.getPacketId(),
                                Constants.MESSAGE_SEND_FAILED);
                    }
                    return;
                }
                if (MyApplication.IS_SUPPORT_MULTI_LOGIN) {// 发送转发消息 || 检测消息
                    LogUtils.e(TAG, "发送转发消息 || 检测消息");
                    sendForwardMessage(msg);
                }
            }
        } catch (SmackException.NotConnectedException e) {
            // 发送异常，调用消息发送状态监听，将消息发送状态改为发送失败
            e.printStackTrace();
        }
    }

    public static String getMucChatServiceName(XMPPConnection connection) {
        return "@muc." + connection.getXMPPServiceDomain();
    }

    /**
     * 发送失败的消息 重新发送
     */
    public static void reSendFailSendChatMessage(ChatMessage message, Friend friend, CoreManager coreManager, UploadEngine.ImFileUploadResponse mUploadResponse) {
        if (!ImHelper.checkXmppAuthenticated()) {
            return;
        }
        if (message.getType() == Constants.TYPE_VOICE || message.getType() == Constants.TYPE_IMAGE
                || message.getType() == Constants.TYPE_VIDEO || message.getType() == Constants.TYPE_FILE
                || message.getType() == Constants.TYPE_LOCATION) {
            if (!message.isUpload()) {
                // 将需要上传的消息状态置为发送中，防止在上传的时候退出当前界面，回来后[还未上传成功]读取数据库又变为了感叹号
                ChatMessageDao.getInstance().updateMessageSendState(MyApplication.getLoginUserId(), friend.getUserId(),
                        message.get_id(), Constants.MESSAGE_SEND_ING);
                UploadEngine.uploadImFile(MyApplication.getInstance(), coreManager, coreManager.getSelfStatus().accessToken, coreManager.getSelf().getUserId(), friend.getUserId(), message, mUploadResponse);
            } else {
                reSendChatMessage(friend.getUserId(), message, true);
            }
        } else {
            reSendChatMessage(friend.getUserId(), message, true);
        }
    }

    /**
     * 发送单聊聊天消息
     */
    public static void sendChatMessage(String toUserId, ChatMessage chatMessage) {
        if (TextUtils.isEmpty(chatMessage.getFromUserName())) {
            chatMessage.setFromUserName(CoreManager.getSelf(MyApplication.getInstance()).getNickName());
        }
        if (TextUtils.isEmpty(chatMessage.getFromUserId())) {
            chatMessage.setFromUserName(MyApplication.getLoginUserId());
        }
        if (chatMessage.getDoubleTimeSend() == 0) {
            chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
        }
        if (TextUtils.isEmpty(chatMessage.getPacketId())) {
            chatMessage.setPacketId(ToolUtils.getUUID());
        }
        if (!HttpUtil.isGprsOrWifiConnected(MyApplication.getContext())) {
            // 现在!isAuthenticated()不能直接标记发送失败，还需要判断网络是否连接
            ListenerManager.getInstance().notifyMessageSendStateChange(MyApplication.getLoginUserId(), toUserId, chatMessage.getPacketId(),
                    Constants.MESSAGE_SEND_FAILED);// 保存自己发送的消息 先给一个默认值
        } else {
            /**
             * 先添加一个等待接收回执的消息
             * 然后再发送这条消息
             */
            addWillSendMessage(toUserId, chatMessage, XmppReceiptImpl.SendType.NORMAL, chatMessage.getContent());
            sendChatXmppMessage(toUserId, chatMessage);
        }
    }


    /**
     * 修改我的个人信息，同步
     */
    public static void syncMyInfoToOtherMachine() {
        for (String machineResource : MyApplication.machine) {
            if (MachineDao.getInstance().getMachineOnLineStatus(machineResource)) {
                LogUtils.e(TAG, "转发给" + machineResource + "设备");
                try {
                    Jid jid = getFullJid(MyApplication.getLoginUserId(), machineResource);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setType(Constants.TYPE_CURRENT_USER_INFO_UPDATE);
                    chatMessage.setFromUserId(MyApplication.getLoginUserId());
                    chatMessage.setFromUserName(CoreManager.getSelf(MyApplication.getInstance()).getNickName());
                    chatMessage.setToUserId(jid.toString());
                    chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
                    chatMessage.setPacketId(ToolUtils.getUUID());
                    sendMachineMessage(jid.toString(), chatMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 同意好友申请，同步
     */
    public static void syncMyFriendToOtherMachine() {
        for (String machineResource : MyApplication.machine) {
            if (MachineDao.getInstance().getMachineOnLineStatus(machineResource)) {
                LogUtils.e(TAG, "转发给" + machineResource + "设备");
                try {
                    Jid jid = getFullJid(MyApplication.getLoginUserId(), machineResource);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setType(Constants.TYPE_FRIEND_ADD_DELETE);
                    chatMessage.setFromUserId(MyApplication.getLoginUserId());
                    chatMessage.setFromUserName(CoreManager.getSelf(MyApplication.getInstance()).getNickName());
                    chatMessage.setToUserId(jid.toString());
                    chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
                    chatMessage.setPacketId(ToolUtils.getUUID());
                    sendMachineMessage(jid.toString(), chatMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 修改好友备注、描述信息，同步
     */
    public static void syncFriendInfoToOtherMachine(String content, String friendId) {
        if (TextUtils.isEmpty(friendId)) {
            return;
        }
        for (String machineResource : MyApplication.machine) {
            if (MachineDao.getInstance().getMachineOnLineStatus(machineResource)) {
                LogUtils.e(TAG, "转发给" + machineResource + "设备");
                try {
                    Jid jid = getFullJid(MyApplication.getLoginUserId(), machineResource);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setType(Constants.TYPE_FRIEND_INFO_UPDATE);
                    chatMessage.setFromUserId(MyApplication.getLoginUserId());
                    chatMessage.setFromUserName(CoreManager.getSelf(MyApplication.getInstance()).getNickName());
                    chatMessage.setToUserId(jid.toString());
                    chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
                    chatMessage.setPacketId(ToolUtils.getUUID());
                    chatMessage.setObjectId(friendId);
                    if (!TextUtils.isEmpty(content)) {
                        chatMessage.setContent(content);
                    }
                    sendMachineMessage(jid.toString(), chatMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 修改好友 阅后即焚，同步
     */
    public static void syncFriendMsgDeadDelToOtherMachine(String content, String friendId) {
        if (TextUtils.isEmpty(friendId)) {
            return;
        }
        for (String machineResource : MyApplication.machine) {
            if (MachineDao.getInstance().getMachineOnLineStatus(machineResource)) {
                LogUtils.e(TAG, "转发给" + machineResource + "设备");
                try {
                    Jid jid = getFullJid(MyApplication.getLoginUserId(), machineResource);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setType(Constants.TYPE_FRIEND_READDEL_UPDATE);
                    chatMessage.setFromUserId(MyApplication.getLoginUserId());
                    chatMessage.setFromUserName(CoreManager.getSelf(MyApplication.getInstance()).getNickName());
                    chatMessage.setToUserId(jid.toString());
                    chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
                    chatMessage.setPacketId(ToolUtils.getUUID());
                    chatMessage.setObjectId(friendId);
                    if (!TextUtils.isEmpty(content)) {
                        chatMessage.setContent(content);
                    }
                    sendMachineMessage(jid.toString(), chatMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 修改好友标签信息，同步
     */
    public static void syncFriendLabelInfoToOtherMachine(String friendId) {
        if (TextUtils.isEmpty(friendId)) {
            return;
        }
        for (String machineResource : MyApplication.machine) {
            if (MachineDao.getInstance().getMachineOnLineStatus(machineResource)) {
                LogUtils.e(TAG, "转发给" + machineResource + "设备");
                try {
                    Jid jid = getFullJid(MyApplication.getLoginUserId(), machineResource);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setType(Constants.TYPE_FRIEND_LABEL_INFO_UPDATE);
                    chatMessage.setFromUserId(MyApplication.getLoginUserId());
                    chatMessage.setFromUserName(CoreManager.getSelf(MyApplication.getInstance()).getNickName());
                    chatMessage.setToUserId(jid.toString());
                    chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
                    chatMessage.setPacketId(ToolUtils.getUUID());
                    chatMessage.setObjectId(friendId);
                    sendMachineMessage(jid.toString(), chatMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 修改好友其他信息 如 消息免打扰、消息过期时间、双向撤回等，同步
     */
    public static void syncFriendOtherInfoToOtherMachine(String friendId) {
        if (TextUtils.isEmpty(friendId)) {
            return;
        }
        for (String machineResource : MyApplication.machine) {
            if (MachineDao.getInstance().getMachineOnLineStatus(machineResource)) {
                LogUtils.e(TAG, "转发给" + machineResource + "设备");
                try {
                    Jid jid = getFullJid(MyApplication.getLoginUserId(), machineResource);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setType(Constants.TYPE_FRIEND_OTHER_INFO_UPDATE);
                    chatMessage.setFromUserId(MyApplication.getLoginUserId());
                    chatMessage.setFromUserName(CoreManager.getSelf(MyApplication.getInstance()).getNickName());
                    chatMessage.setToUserId(jid.toString());
                    chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
                    chatMessage.setPacketId(ToolUtils.getUUID());
                    chatMessage.setObjectId(friendId);
                    sendMachineMessage(jid.toString(), chatMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 同步其他端的 修改好友备注、描述信息
     */
    public static void syncFriendInfoFromOtherMachine(ChatMessage chatMessage) {
        if (chatMessage == null || TextUtils.isEmpty(chatMessage.getObjectId())) {
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getSelfStatus(MyApplication.getInstance()).accessToken);
        params.put("userId", chatMessage.getObjectId());
        HttpUtils.get().url(MyApplication.mCoreManager.getConfig().USER_GET_URL)
                .params(params)
                .build()
                .execute(new BaseCallback<User>(User.class) {
                    @Override
                    public void onResponse(ObjectResult<User> result) {
                        if (result.getResultCode() == 1) {
                            User user = result.getData();
                            if (user != null && user.getFriends() != null) {
                                AttentionUser attentionUser = user.getFriends();
                                FriendDao.getInstance().updateRemarkNameAndDescribe(
                                        MyApplication.getLoginUserId(), chatMessage.getObjectId(), attentionUser.getRemarkName(), attentionUser.getDescribe());
                                MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
                                CardcastUiUpdateUtil.broadcastUpdateUi(MyApplication.getInstance());
                                Intent intent = new Intent(com.iimm.miliao.broadcast.OtherBroadcast.NAME_CHANGE);
                                intent.putExtra("remarkName", attentionUser.getRemarkName());
                                intent.putExtra("describe", attentionUser.getDescribe());
                                MyApplication.getInstance().sendBroadcast(intent);
                                EventBusMsg eventBusMsg = new EventBusMsg();
                                eventBusMsg.setObject(chatMessage.getObjectId());
                                eventBusMsg.setMessageType(Constants.EVENTBUS_MSG_FRIEND_INFO_UPDATE_UI);
                                EventBus.getDefault().post(eventBusMsg);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });

    }

    /**
     * 同步其他端的 好友列表
     */
    public static void syncMyFriendFromOtherMachine(ChatMessage chatMessage) {
        if (chatMessage == null) {
            return;
        }
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getSelfStatus(MyApplication.getInstance()).accessToken);

        HttpUtils.get().url(MyApplication.mCoreManager.getConfig().FRIENDS_ATTENTION_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<AttentionUser>(AttentionUser.class) {
                    @Override
                    public void onResponse(ArrayResult<AttentionUser> result) {
                        if (result.getResultCode() == 1) {
                            AppExecutors.getInstance().networkIO().execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        FriendDao.getInstance().addAttentionUsers(MyApplication.mCoreManager.getSelf().getUserId(), result.getData(),
                                                new OnCompleteListener2() {

                                                    @Override
                                                    public void onLoading(int progressRate, int sum) {

                                                    }

                                                    @Override
                                                    public void onCompleted() {

                                                    }
                                                });
                                    } catch (Exception e) {
                                    }
                                }
                            });
                        } else {
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });

    }

    /**
     * 发送需要同步的消息
     */
    public static void sendMachineMessage(String toUserJid, ChatMessage chatMessage) {
        if (TextUtils.isEmpty(chatMessage.getFromUserName())) {
            chatMessage.setFromUserName(CoreManager.getSelf(MyApplication.getInstance()).getNickName());
        }
        if (TextUtils.isEmpty(chatMessage.getFromUserId())) {
            chatMessage.setFromUserName(MyApplication.getLoginUserId());
        }
        if (chatMessage.getDoubleTimeSend() == 0) {
            chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
        }
        if (TextUtils.isEmpty(chatMessage.getPacketId())) {
            chatMessage.setPacketId(ToolUtils.getUUID());
        }
        if (!checkXmppAuthenticated() && !HttpUtil.isGprsOrWifiConnected(MyApplication.getContext())) {

        } else {
            /**
             * 先添加一个等待接收回执的消息
             * 然后再发送这条消息
             */
            addWillSendMessage(toUserJid, chatMessage, XmppReceiptImpl.SendType.NORMAL, chatMessage.getContent());
            sendMachineXmppMessage(toUserJid, chatMessage);
        }
    }

    /**
     * 发送聊天的消息
     *
     * @param toUserJid 要发送给自己的其他设备
     * @param oMessage  已经存到本地数据库的一条即将发送的消息
     */
    public static void sendMachineXmppMessage(final String toUserJid, final ChatMessage oMessage) {
        if (!checkXmppAuthenticated()) {
            return;
        }
        XMPPTCPConnection xmpptcpConnection = XmppConnectionImpl.getInstance().getXMPPConnection();
        // 加密可能影响到消息对象复用，所以拷贝一份，
        ChatMessage chatMessage = oMessage.clone(false);
        /*
         * 先将自己定义的消息类型转(ChatMessage)换成 smack第三方定义的Message类型
         * 然后通过smack的Chat对象来发送一个msg
         */
        try {
            Message msg = new Message();
            msg.setType(Message.Type.chat);
            msg.setBody(chatMessage.toJsonString());
            msg.setStanzaId(chatMessage.getPacketId());
            FullJid fullJid = getFullJid(toUserJid);
            if (fullJid == null) {
                return;
            }
            msg.setTo(fullJid);
            if (MyApplication.IS_OPEN_RECEIPT) {// 在发送消息之前发送回执请求
                DeliveryReceiptRequest.addTo(msg);
            }
            try {
                xmpptcpConnection.sendStanza(msg);
                saveSendMsg(msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (SmackException.NotConnectedException e) {
            // 发送异常，调用消息发送状态监听，将消息发送状态改为发送失败
            e.printStackTrace();
        }
    }

    private static void saveSendMsg(Message msg) {
        if (BuildConfig.DEBUG) {
            Log.d("消息监听", "发" + msg + "---" + msg.getBody());
            SendMsg sendMsg = new SendMsg();
            sendMsg.setMsgContent(msg.toString());
            sendMsg.setWhetherToForward(false);
            sendMsg.setWhetherToForwardDevice(null);
            SendMsgDao.getInstance().insertMsg(sendMsg);
        }

    }

    private static void saveTransferSendMsg(Message msg, String toUser) {
        if (BuildConfig.DEBUG) {
            Log.d("消息监听", "转发" + msg + "---" + msg.getBody());
            SendMsg sendMsg = new SendMsg();
            sendMsg.setMsgContent(msg.toString());
            sendMsg.setWhetherToForward(true);
            sendMsg.setWhetherToForwardDevice(toUser);
            SendMsgDao.getInstance().insertMsg(sendMsg);
        }
    }


    public static void reSendChatMessage(String toUserId, ChatMessage chatMessage) {
        reSendChatMessage(toUserId, chatMessage, false);
    }

    /**
     * @param toUserId
     * @param chatMessage
     * @param needReceipt 失败重发的消息，需要加回执。
     */
    public static void reSendChatMessage(String toUserId, ChatMessage chatMessage, boolean needReceipt) {
        if (TextUtils.isEmpty(chatMessage.getPacketId())) {
            chatMessage.setPacketId(ToolUtils.getUUID());
        }
        if (!checkXmppAuthenticated() && !HttpUtil.isGprsOrWifiConnected(MyApplication.getContext())) {
            // 现在!isAuthenticated()不能直接标记发送失败，还需要判断网络是否连接
            ListenerManager.getInstance().notifyMessageSendStateChange(MyApplication.getLoginUserId(), toUserId, chatMessage.getPacketId(),
                    Constants.MESSAGE_SEND_FAILED);// 保存自己发送的消息 先给一个默认值
        } else {
            if (needReceipt) {
                addWillSendMessage(toUserId, chatMessage, XmppReceiptImpl.SendType.NORMAL, chatMessage.getContent());
            }
            sendChatXmppMessage(toUserId, chatMessage);
        }
    }

    /* 添加一个即将发送的消息 */
    public static void addWillSendMessage(final String toUserId, final XmppMessage xmppMessage, XmppReceiptImpl.SendType sendType, String content) {
        //LogUtils.i(TAG, "addWillSendMessage: "+LogUtils.getStackTraceString(new Exception()));
        // 将之前可能存在的回执缓存清除掉
        if (XmppReceiptImpl.mReceiptMsgMap.get(xmppMessage.getPacketId()) != null) {
            XmppReceiptImpl.ReceiptObj oldObj = XmppReceiptImpl.mReceiptMsgMap.get(xmppMessage.getPacketId());
            if (oldObj.msg instanceof ChatMessage) {
                ((ChatMessage) oldObj.msg).stopTimer();
            } else if (oldObj.msg instanceof NewFriendMessage) {
                ((NewFriendMessage) oldObj.msg).stopTimer();
            }
            XmppReceiptImpl.mReceiptMsgMap.remove(xmppMessage.getPacketId());
        }
        int type = xmppMessage.getType(); // 消息类型
        // 将这个回执对象缓存起来，这样在接到回执的时候容易定位到是发给谁的哪条消息
        XmppReceiptImpl.ReceiptObj obj = new XmppReceiptImpl.ReceiptObj();
        obj.toUserId = toUserId;
        obj.msg = xmppMessage;
        obj.sendType = sendType;
        obj.Read = (type == Constants.TYPE_READ) ? 1 : 0; // 判断类型
        obj.Read_msg_pid = content;
        XmppReceiptImpl.mReceiptMsgMap.put(xmppMessage.getPacketId(), obj);// 记录一条新发送出去的消息(还没有接收到回执)
        if (xmppMessage instanceof ChatMessage) {
            ChatMessage chatMessage = (ChatMessage) xmppMessage;
            chatMessage.startTimer(chatMessage.getReSendCount());
            if (BuildConfig.LOG_DEBUG) {
                LogUtils.e(TAG, "产生一条消息，等待回执..." + "Constants.getPacketId()--->" + xmppMessage.getPacketId()
                        + " ，chatMessage.getPacketId()--->" + chatMessage.getPacketId() + " ，type--->" + chatMessage.getType()
                        + " ，content--->" + chatMessage.getContent());
            }
        } else if (xmppMessage instanceof NewFriendMessage) {
            NewFriendMessage newFriendMessage = (NewFriendMessage) xmppMessage;
            newFriendMessage.startTimer();
            if (BuildConfig.LOG_DEBUG) {
                LogUtils.e(TAG, "产生一条消息，等待回执..." + "Constants.getPacketId()--->" + xmppMessage.getPacketId()
                        + " ，chatMessage.getPacketId()--->" + newFriendMessage.getPacketId() + " ，type--->" + newFriendMessage.getType()
                        + " ，content--->" + newFriendMessage.getContent());
            }
        }

    }

    /**
     * 发送失败的消息 重新发送
     *
     * @param roomJidLocal
     * @param message
     * @param friend
     * @param coreManager
     * @param mUploadResponse
     */
    public static void reSendFailSendMucChatMessage(String roomJidLocal, ChatMessage message, Friend friend, CoreManager coreManager, UploadEngine.ImFileUploadResponse mUploadResponse) {
        if (!ImHelper.checkXmppAuthenticated()) {
            return;
        }
        if (message.getType() == Constants.TYPE_VOICE || message.getType() == Constants.TYPE_IMAGE
                || message.getType() == Constants.TYPE_VIDEO || message.getType() == Constants.TYPE_FILE
                || message.getType() == Constants.TYPE_LOCATION) {
            if (!message.isUpload()) {
                // 将需要上传的消息状态置为发送中，防止在上传的时候退出当前界面，回来后[还未上传成功]读取数据库又变为了感叹号
                ChatMessageDao.getInstance().updateMessageSendState(MyApplication.getLoginUserId(), friend.getUserId(),
                        message.get_id(), Constants.MESSAGE_SEND_ING);
                UploadEngine.uploadImFile(MyApplication.getInstance(), coreManager, coreManager.getSelfStatus().accessToken, coreManager.getSelf().getUserId(), roomJidLocal, message, mUploadResponse);
            } else {
                reSendMucChatMessage(roomJidLocal, message, true);
            }
        } else {
            reSendMucChatMessage(roomJidLocal, message, true);
        }
    }

    /**
     * 重发群组消息
     *
     * @param toUserId
     * @param chatMessage
     */
    public static void reSendMucChatMessage(String toUserId, ChatMessage chatMessage) {
        reSendMucChatMessage(toUserId, chatMessage, false);
    }

    /**
     * @param toUserId
     * @param chatMessage
     * @param needReceipt 是否需要回执
     */
    public static void reSendMucChatMessage(String toUserId, ChatMessage chatMessage, boolean needReceipt) {
        if (chatMessage.getDoubleTimeSend() == 0) {
            chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
        }
        if (TextUtils.isEmpty(chatMessage.getPacketId())) {
            chatMessage.setPacketId(ToolUtils.getUUID());
        }
        if (!checkXmppAuthenticated() && !HttpUtil.isGprsOrWifiConnected(MyApplication.getContext())) {
            // 现在!isAuthenticated()不能直接标记发送失败，还需要判断网络是否连接
            ListenerManager.getInstance().notifyMessageSendStateChange(MyApplication.getLoginUserId(), toUserId, chatMessage.getPacketId(),
                    Constants.MESSAGE_SEND_FAILED);// 保存自己发送的消息 先给一个默认值
        } else {
            /**
             * 先添加一个等待接收回执的消息
             * 然后再发送这条消息
             */
            if (needReceipt) {
                addWillSendMessage(toUserId, chatMessage, XmppReceiptImpl.SendType.NORMAL, chatMessage.getContent());
            }
            sendMucChatXmppMessage(toUserId, chatMessage);
        }
    }

    public static void sendMucChatMessage(String toUserId, ChatMessage chatMessage) {
        if (chatMessage.getDoubleTimeSend() == 0) {
            chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
        }
        if (TextUtils.isEmpty(chatMessage.getPacketId())) {
            chatMessage.setPacketId(ToolUtils.getUUID());
        }
        if (!HttpUtil.isGprsOrWifiConnected(MyApplication.getContext())) {
            // 现在!isAuthenticated()不能直接标记发送失败，还需要判断网络是否连接
            ListenerManager.getInstance().notifyMessageSendStateChange(MyApplication.getLoginUserId(), toUserId, chatMessage.getPacketId(),
                    Constants.MESSAGE_SEND_FAILED);// 保存自己发送的消息 先给一个默认值
        } else {
            /**
             * 先添加一个等待接收回执的消息
             * 然后再发送这条消息
             */
            addWillSendMessage(toUserId, chatMessage, XmppReceiptImpl.SendType.NORMAL, chatMessage.getContent());
            sendMucChatXmppMessage(toUserId, chatMessage);
        }
    }

    /**
     * @param roomJidLocal 要发送消息的房间Id
     * @param oMessage     已经存到本地数据库的一条即将发送的消息
     */
    public static void sendMucChatXmppMessage(final String roomJidLocal, final ChatMessage oMessage) {
        if (!ImHelper.checkXmppAuthenticated()) {
            return;
        }
        if (oMessage.getDoubleTimeSend() == 0) {
            oMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
        }
        if (TextUtils.isEmpty(oMessage.getPacketId())) {
            oMessage.setPacketId(ToolUtils.getUUID());
        }
        XMPPTCPConnection xmpptcpConnection = XmppConnectionImpl.getInstance().getXMPPConnection();
        String roomJid = roomJidLocal + getMucChatServiceName(xmpptcpConnection);
        ChatMessage chatMessage = oMessage.clone(false);
        try {
            EntityBareJid entityBareJid = JidCreate.entityBareFrom(roomJid);
            MultiUserChat multiUserChat = MultiUserChatManager.getInstanceFor(xmpptcpConnection).getMultiUserChat(entityBareJid);
            if (multiUserChat == null || !multiUserChat.isJoined()) {
                if (multiUserChat != null) {
                    LogUtils.e(TAG, "是否加入了该群组:" + multiUserChat.isJoined());
                } else {
                    LogUtils.e(TAG, "该群组的MultiUserChat对象为空");
                }
                //没加入群组尝试加入
                // TODO: 2019/7/9 0009
                EventBus.getDefault().post(new EventXMPPJoinGroupFailed(roomJidLocal));// 通知聊天界面xmpp加入群组失败
//                ListenerManager.getInstance().notifyMessageSendStateChange(MyApplication.getLoginUserId(), roomJidLocal, chatMessage.getPacketId(),
//                        Constants.MESSAGE_SEND_ING);
                tryToJoin(roomJidLocal, result -> {
                    if (result) {
                        sendMucChatXmppMessage(roomJidLocal, oMessage);
                    } else {
                        EventBus.getDefault().post(new EventXMPPJoinGroupFailed(roomJidLocal));// 通知聊天界面xmpp加入群组失败
                        ListenerManager.getInstance().notifyMessageSendStateChange(MyApplication.getLoginUserId(), roomJidLocal, chatMessage.getPacketId(),
                                Constants.MESSAGE_SEND_FAILED);
                    }
                });
                return;
            }
            if (chatMessage.getIsEncrypt() == 1) {
                try {
                    // String x = DES.encryptDES(chatMessage.getContent(), Constants.ENCRYPT_DES);
                    // 更换加密key
                    String encryptKey = Md5Util.toMD5(AppConfig.apiKey + chatMessage.getTimeSend() + chatMessage.getPacketId());
                    String x = DES.encryptDES(chatMessage.getContent(), encryptKey);
                    chatMessage.setContent(x);
                } catch (Exception e) {
                    // 加密失败，将该字段置为不加密，以防接收方收到后去解密
                    chatMessage.setIsEncrypt(0);
                }
            }
            Message msg = new Message();
            msg.setType(Message.Type.groupchat);
            msg.setBody(chatMessage.toJsonString());
            msg.setStanzaId(chatMessage.getPacketId());
            msg.setTo(entityBareJid);
            // int sendStatus = Constants.MESSAGE_SEND_FAILED;
            if (MyApplication.IS_OPEN_RECEIPT) {// 添加回执请求
                DeliveryReceiptRequest.addTo(msg);
            }
            // 发送消息
            multiUserChat.sendMessage(msg);
            saveSendMsg(msg);
            //ListenerManager.getInstance().notifyMessageSendStateChange(MyApplication.getLoginUserId(), roomJidLocal, chatMessage.getPacketId(), Constants.MESSAGE_SEND_ING);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
    }

    /**
     * 尝试加入到群组
     *
     * @param roomJidLocal
     * @param resultListener
     */
    private static void tryToJoin(String roomJidLocal, ResultListener resultListener) {
        ThreadManager.getPool().execute(new Runnable() {
            @Override
            public void run() {
                boolean result = ImHelper.joinMucChat(roomJidLocal, 0);
                resultListener.callBack(result);
            }
        });
    }

    public interface ResultListener {
        void callBack(boolean result);
    }

    public static void resetXmppMap() {
        resetMucChatMap();
        resetReceiptMap();
    }

    public static void resetMucChatMap() {
        mMucChatMap.clear();
    }

    public static void resetReceiptMap() {
        //第二种方式:entrySet的Iterator遍历方式
        Iterator<Map.Entry<String, XmppReceiptImpl.ReceiptObj>> iterator2 = XmppReceiptImpl.mReceiptMsgMap.entrySet().iterator();
        Map.Entry<String, XmppReceiptImpl.ReceiptObj> entry;
        while (iterator2.hasNext()) {
            entry = iterator2.next();
            if (entry.getValue().msg instanceof ChatMessage) {
                ((ChatMessage) entry.getValue().msg).onTimeOut();
                ((ChatMessage) entry.getValue().msg).stopTimer();
            } else if (entry.getValue().msg instanceof NewFriendMessage) {
                ((NewFriendMessage) entry.getValue().msg).onTimeOut();
                ((NewFriendMessage) entry.getValue().msg).stopTimer();
            }
        }
        XmppReceiptImpl.mReceiptMsgMap.clear();
    }

    public static void chatGroup(String body, ChatMessage chatMessage, Friend friend) {
        int type = chatMessage.getType();
        String fromUserId = chatMessage.getFromUserId();
        String fromUserName = chatMessage.getFromUserName();
        String toUserId = chatMessage.getToUserId();
        JSONObject jsonObject = JSONObject.parseObject(body);
        String toUserName = jsonObject.getString("toUserName");

        if (!TextUtils.isEmpty(toUserId)) {
            if (toUserId.equals(MyApplication.getLoginUserId())) {// 针对我的操作，只需要为fromUserName赋值
                String xF = getName(friend, fromUserId);
                if (!TextUtils.isEmpty(xF)) {
                    fromUserName = xF;
                }
            } else {// 针对其他人的操作，fromUserName与toUserName都需要赋值
                String xF = getName(friend, fromUserId);
                if (!TextUtils.isEmpty(xF)) {
                    fromUserName = xF;
                }
                String xT = getName(friend, toUserId);
                if (!TextUtils.isEmpty(xT)) {
                    toUserName = xT;
                }
            }
        }
        chatMessage.setGroup(true);
        chatMessage.setType(Constants.TYPE_TIP);

        /*
        群文件
         */
        if (type == Constants.TYPE_MUCFILE_DEL || type == Constants.TYPE_MUCFILE_ADD) {
            String str;
            if (type == Constants.TYPE_MUCFILE_DEL) {
                // str = chatMessage.getFromUserName() + " 删除了群文件 " + chatMessage.getFilePath();
                str = fromUserName + " " + InternationalizationHelper.getString("JXMessage_fileDelete") + ":" + chatMessage.getFilePath();
            } else {
                // str = chatMessage.getFromUserName() + " 上传了群文件 " + chatMessage.getFilePath();
                str = fromUserName + " " + InternationalizationHelper.getString("JXMessage_fileUpload") + ":" + chatMessage.getFilePath();
            }
            // 更新聊天记录表最后一条消息
            chatMessage.setContent(str);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage, true);
            }
            return;
        }

        /*
        群管理
         */
        if (type >= Constants.TYPE_CHANGE_SHOW_READ && type <= Constants.TYPE_GROUP_TRANSFER) {
            if (type == Constants.TYPE_GROUP_VERIFY) {
                // 916协议分为两种
                // 第一种为服务端发送，触发条件为群主在群组信息内 开/关 进群验证按钮，群组内每个人都能收到
                // 第二种为邀请、申请加入该群组，由邀请人或加入方发送给群主的消息，只有群主可以收到
                if (!TextUtils.isEmpty(chatMessage.getContent()) &&
                        (chatMessage.getContent().equals("0") || chatMessage.getContent().equals("1"))) {// 第一种
                    PreferenceUtils.putBoolean(MyApplication.getInstance(),
                            Constants.IS_NEED_OWNER_ALLOW_NORMAL_INVITE_FRIEND + chatMessage.getObjectId(), chatMessage.getContent().equals("1"));
                    if (chatMessage.getContent().equals("1")) {
                        chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_group_enable_verify));
                    } else {
                        chatMessage.setContent(getString(R.string.tip_group_disable_verify));
                    }
                    // chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                    if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage)) {
                        ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage, true);
                    }
                } else {//  群聊邀请确认消息 我收到该条消息 说明我就是该群的群主 待我审核
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(chatMessage.getObjectId());
                        String isInvite = json.getString("isInvite");
                        if (TextUtils.isEmpty(isInvite)) {
                            isInvite = "0";
                        }
                        if (isInvite.equals("0")) {
                            String id = json.getString("userIds");
                            String[] ids = id.split(",");
                            chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_invite_need_verify_place_holder, chatMessage.getFromUserName(), ids.length));
                        } else {
                            chatMessage.setContent(chatMessage.getFromUserName() + MyApplication.getInstance().getString(R.string.tip_need_verify_place_holder));
                        }
                        String roomJid = json.getString("roomJid");
                        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), roomJid, chatMessage)) {
                            ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), roomJid, chatMessage, true);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (type == Constants.TYPE_CHANGE_SHOW_READ) {
                    PreferenceUtils.putBoolean(MyApplication.getInstance(),
                            Constants.IS_SHOW_READ + chatMessage.getObjectId(), chatMessage.getContent().equals("1"));
                    if (chatMessage.getContent().equals("1")) {
                        chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_enable_read));
                    } else {
                        chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_disable_read));
                    }
                } else if (type == Constants.TYPE_GROUP_LOOK) {
                    if (chatMessage.getContent().equals("1")) {
                        chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_private));
                    } else {
                        chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_public));
                    }
                } else if (type == Constants.TYPE_GROUP_SHOW_MEMBER) {
                    if (chatMessage.getContent().equals("1")) {
                        chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_enable_member));
                    } else {
                        chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_disable_member));
                    }
                } else if (type == Constants.TYPE_GROUP_SEND_CARD) {
                    PreferenceUtils.putBoolean(MyApplication.getInstance(),
                            Constants.IS_SEND_CARD + chatMessage.getObjectId(), chatMessage.getContent().equals("1"));
                    if (chatMessage.getContent().equals("1")) {
                        chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_enable_chat_privately));
                    } else {
                        chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_disable_chat_privately));
                    }
                    MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(MyApplication.getContext());
                } else if (type == Constants.TYPE_GROUP_ALL_SHAT_UP) {
                    long time = Long.parseLong(chatMessage.getContent());
                    FriendDao.getInstance().updateRoomTalkTime(MyApplication.getLoginUserId(), chatMessage.getObjectId(), time);
                    if (!chatMessage.getContent().equals("0")) {
                        chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_now_ban_all));
                    } else {
                        chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_now_disable_ban_all));
                    }
                    MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(MyApplication.getContext());
                } else if (type == Constants.TYPE_GROUP_ALLOW_NORMAL_INVITE) {
                    if (!chatMessage.getContent().equals("0")) {
                        chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_enable_invite));
                        MsgBroadcast.broadcastMsgRoomUpdateInvite(MyApplication.getInstance(), 1);
                    } else {
                        chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_disable_invite));
                        MsgBroadcast.broadcastMsgRoomUpdateInvite(MyApplication.getInstance(), 0);
                    }
                } else if (type == Constants.TYPE_GROUP_ALLOW_NORMAL_UPLOAD) {
                    PreferenceUtils.putBoolean(MyApplication.getInstance(),
                            Constants.IS_ALLOW_NORMAL_SEND_UPLOAD + chatMessage.getObjectId(), !chatMessage.getContent().equals("0"));
                    if (!chatMessage.getContent().equals("0")) {
                        chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_enable_upload));
                    } else {
                        chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_disable_upload));
                    }
                } else if (type == Constants.TYPE_GROUP_ALLOW_NORMAL_CONFERENCE) {
                    PreferenceUtils.putBoolean(MyApplication.getInstance(),
                            Constants.IS_ALLOW_NORMAL_CONFERENCE + chatMessage.getObjectId(), !chatMessage.getContent().equals("0"));
                    if (!chatMessage.getContent().equals("0")) {
                        chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_enable_meeting));
                    } else {
                        chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_disable_meeting));
                    }
                } else if (type == Constants.TYPE_GROUP_ALLOW_NORMAL_SEND_COURSE) {
                    PreferenceUtils.putBoolean(MyApplication.getInstance(),
                            Constants.IS_ALLOW_NORMAL_SEND_COURSE + chatMessage.getObjectId(), !chatMessage.getContent().equals("0"));
                    if (!chatMessage.getContent().equals("0")) {
                        chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_enable_cource));
                    } else {
                        chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_owner_disable_cource));
                    }
                } else if (type == Constants.TYPE_GROUP_TRANSFER) {
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_new_group_owner_place_holder, toUserName));
                    if (friend != null) {
                        FriendDao.getInstance().updateRoomCreateUserId(MyApplication.getLoginUserId(),
                                chatMessage.getObjectId(), chatMessage.getToUserId());
                        RoomMemberDao.getInstance().updateRoomMemberRole(friend.getRoomId(), 0);
                        RoomMemberDao.getInstance().updateRoomMemberRole(friend.getRoomId(), chatMessage.getToUserId(), 1);
                        EventBus.getDefault().post(new TransferKingEvent(friend.getUserId(), chatMessage.getToUserId(), toUserName));
                    }
                }
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage, true);
                }
            }
            return;
        }

        /*
        群内其它设置
         */
        if (type == Constants.TYPE_CHANGE_NICK_NAME) { // 修改群内昵称
            String content = chatMessage.getContent();
            if (!TextUtils.isEmpty(toUserId) && toUserId.equals(MyApplication.getLoginUserId())) {// 我修改了昵称
                if (!TextUtils.isEmpty(content)) {
                    friend.setRoomMyNickName(content);
                    FriendDao.getInstance().updateRoomMyNickName(friend.getUserId(), content);
                    ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), toUserId, content);
                    ChatMessageDao.getInstance().updateNickName(MyApplication.getLoginUserId(), friend.getUserId(), toUserId, content);
                }
                // 自己改了昵称也要留一条消息，
                chatMessage.setContent(toUserName + " " + InternationalizationHelper.getString("JXMessageObject_UpdateNickName") + "‘" + content + "’");
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage, true);
                }
            } else {  // 其他人修改了昵称，通知下就可以了
                chatMessage.setContent(toUserName + " " + InternationalizationHelper.getString("JXMessageObject_UpdateNickName") + "‘" + content + "’");
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage, true);
                }
                ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), toUserId, content);
                ChatMessageDao.getInstance().updateNickName(MyApplication.getLoginUserId(), friend.getUserId(), toUserId, content);
            }
        } else if (type == Constants.TYPE_CHANGE_ROOM_NAME) {
            // 修改房间名、更新朋友表
            String content = chatMessage.getContent();
            FriendDao.getInstance().updateMucFriendRoomName(friend.getUserId(), content);
            ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), "ROOMNAMECHANGE", content);

            chatMessage.setContent(fromUserName + " " + InternationalizationHelper.getString("JXMessageObject_UpdateRoomName") + content);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage, true);
            }
        } else if (type == Constants.TYPE_DELETE_ROOM) {// 群主解散该群
            if (fromUserId.equals(toUserId)) {
                // 我为群主
                FriendDao.getInstance().deleteFriend(MyApplication.getLoginUserId(), chatMessage.getObjectId());
                // 消息表中删除
                ChatMessageDao.getInstance().deleteMessageTable(MyApplication.getLoginUserId(), chatMessage.getObjectId());
                RoomMemberDao.getInstance().deleteRoomMemberTable(chatMessage.getObjectId());
                // 通知界面更新
                MsgBroadcast.broadcastMsgNumReset(MyApplication.getInstance());
                MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
                MucgroupUpdateUtil.broadcastUpdateUi(MyApplication.getInstance());
            } else {
                exitMucChat(chatMessage.getObjectId());
                // 2 标志该群已被解散  更新朋友表
                FriendDao.getInstance().updateFriendGroupStatus(MyApplication.getLoginUserId(), friend.getUserId(), 2);
                chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_disbanded));
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage, true);
                }
            }
            ListenerManager.getInstance().notifyDeleteMucRoom(chatMessage.getObjectId());
        } else if (type == Constants.TYPE_DELETE_MEMBER) {
            // 群组 退出 || 踢人
            if (toUserId.equals(MyApplication.getLoginUserId())) { // 该操作为针对我的
                // Todo 针对自己消息的在XChatListener内已经处理了，为了防止加群后拉群组离线消息又拉到该条消息，针对自己的不处理
/*
                if (fromUserId.equals(toUserId)) {
                    // 自己退出了群组
                    MyApplication.getInstance().exitMucChat(friend.getUserId());
                    // 删除这个房间
                    FriendDao.getInstance().deleteFriend(MyApplication.getLoginUserId(), friend.getUserId());
                    RoomMemberDao.getInstance().deleteRoomMemberTable(chatMessage.getObjectId());
                    // 消息表中删除
                    ChatMessageDao.getInstance().deleteMessageTable(MyApplication.getLoginUserId(), friend.getUserId());
                    // 通知界面更新
                    MsgBroadcast.broadcastMsgNumReset(MyApplication.getInstance());
                    MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
                    MucgroupUpdateUtil.broadcastUpdateUi(MyApplication.getInstance());
                } else {
                    // 被xx踢出了群组
                    MyApplication.getInstance().exitMucChat(friend.getUserId());
                    // / 1 标志被踢出该群组， 更新朋友表
                    FriendDao.getInstance().updateFriendGroupStatus(MyApplication.getLoginUserId(), friend.getUserId(), 1);
                    chatMessage.setContent(MyApplication.getInstance().getString(R.string.tip_been_kick_place_holder, fromUserName));

                    ListenerManager.getInstance().notifyMyBeDelete(friend.getUserId());// 通知群组聊天界面
                }
*/
            } else {
                // 其他人退出 || 被踢出
                if (fromUserId.equals(toUserId)) {
                    // message.setContent(toUserName + "退出了群组");
                    chatMessage.setContent(toUserName + " " + InternationalizationHelper.getString("QUIT_GROUP"));
                } else {
                    // message.setContent(toUserName + "被踢出群组");
                    chatMessage.setContent(toUserName + " " + InternationalizationHelper.getString("KICKED_OUT_GROUP"));
                }
                // 更新RoomMemberDao、更新群聊界面
                operatingRoomMemberDao(1, friend.getRoomId(), toUserId, null);
                MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(MyApplication.getContext());
            }
            int role = RoomMemberDao.getInstance().getMyRole(friend.getRoomId());
            if (role == Constants.ROLE_MANAGER || role == Constants.ROLE_OWNER) {
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage, true);
                }
            }
        } else if (type == Constants.TYPE_NEW_NOTICE) { // 发布公告
            try {
                MucRoom.Notice notice = JSONObject.parseObject(chatMessage.getContent(), MucRoom.Notice.class);
                EventBus.getDefault().post(new EventNewNotice(notice, chatMessage));
                if (notice.getNoticeType() == 1) {
                    openStrongRemind(chatMessage, friend);
                }
                chatMessage.setContent(fromUserName + " " + InternationalizationHelper.getString("JXMessageObject_AddNewAdv") + notice.getText());
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage, true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (type == Constants.TYPE_GAG) {// 禁言
            long time = Long.parseLong(chatMessage.getContent());
            if (toUserId != null && toUserId.equals(MyApplication.getLoginUserId())) {
                // Todo 针对自己消息的在XChatListener内已经处理了，为了防止加群后拉群组离线消息又拉到该条消息，针对自己的不处理
/*
                // 被禁言了|| 取消禁言 更新RoomTalkTime字段
                FriendDao.getInstance().updateRoomTalkTime(MyApplication.getLoginUserId(), friend.getUserId(), (int) time);
                ListenerManager.getInstance().notifyMyVoiceBanned(friend.getUserId(), (int) time);
*/
            }

            // 为防止其他用户接收不及时，给3s的误差
            if (time > TimeUtils.time_current_time() + 3) {
                String formatTime = XfileUtils.fromatTime((time * 1000), "MM-dd HH:mm");
                // message.setContent("用户：" + toUserName + " 已被禁言到" + formatTime);
                chatMessage.setContent(fromUserName + " " + InternationalizationHelper.getString("JXMessageObject_Yes") + toUserName +
                        InternationalizationHelper.getString("JXMessageObject_SetGagWithTime") + formatTime);
            } else {
                // message.setContent("用户：" + toUserName + " 已被取消禁言");
                /*chatMessage.setContent(chatMessage.getFromUserName() + " " + getString("JXMessageObject_Yes") + toUserName +
                        getString("JXMessageObject_CancelGag"));*/
                chatMessage.setContent(toUserName + MyApplication.getInstance().getString(R.string.tip_been_cancel_ban_place_holder, fromUserName));
            }

            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage, true);
            }
        } else if (type == Constants.NEW_MEMBER) {
            String desc = "";
            if (chatMessage.getFromUserId().equals(toUserId)) {
                // 主动加入
                desc = fromUserName + " " + InternationalizationHelper.getString("JXMessageObject_GroupChat");
            } else {
                // 被邀请加入
                desc = fromUserName + " " + InternationalizationHelper.getString("JXMessageObject_InterFriend") + toUserName;

                String roomId = jsonObject.getString("fileName");
                if (!toUserId.equals(MyApplication.getLoginUserId())) {// 被邀请人为自己时不能更新RoomMemberDao，如更新了，在群聊界面判断出该表有人而不会在去调用接口获取该群真实的人数了
                    operatingRoomMemberDao(0, roomId, chatMessage.getToUserId(), toUserName);
                }
            }

            // Todo 针对自己消息的在XChatListener内已经处理了，为了防止加群后拉群组离线消息又拉到该条消息，针对自己的不处理
/*
            if (toUserId.equals(MyApplication.getLoginUserId())) {
                // 其他人邀请我加入该群组 才会进入该方法
                if (friend != null && friend.getGroupStatus() == 1) {// 本地存在该群组，且被踢出了该群组 先将该群组删除在创建(如调用updateGroupStatus直接修改该群组状态，可以会有问题，保险起见还是创建吧)
                    FriendDao.getInstance().deleteFriend(MyApplication.getLoginUserId(), friend.getUserId());
                    ChatMessageDao.getInstance().deleteMessageTable(MyApplication.getLoginUserId(), friend.getUserId());
                }

                String roomId = "";
                // 将当前群组部分属性存入共享参数内
                try {
                    // 群已读、公开、群验证、群成员列表可见、允许普通成员私聊
                    roomId = jsonObject.getString("fileName");
                    String other = jsonObject.getString("other");
                    JSONObject jsonObject2 = JSONObject.parseObject(other);
                    int showRead = jsonObject2.getInteger("showRead");
                    int allowSecretlyChat = jsonObject2.getInteger("allowSendCard");
                    MyApplication.getInstance().saveGroupPartStatus(chatMessage.getObjectId(), showRead, allowSecretlyChat,
                            1, 1, 0);
                } catch (Exception e) {
                    LogUtils.e("msg", "解析时抛出异常");
                }

                Friend mCreateFriend = new Friend();
                mCreateFriend.setOwnerId(MyApplication.getLoginUserId());
                mCreateFriend.setUserId(chatMessage.getObjectId());
                mCreateFriend.setNickName(chatMessage.getContent());
                mCreateFriend.setDescription("");
                mCreateFriend.setRoomId(roomId);
                mCreateFriend.setContent(desc);
                mCreateFriend.setDoubleTimeSend(chatMessage.getTimeSend());
                mCreateFriend.setRoomFlag(1);
                mCreateFriend.setStatus(Constants.STATUS_FRIEND);
                mCreateFriend.setGroupStatus(0);
                FriendDao.getInstance().createOrUpdateFriend(mCreateFriend);
                // 调用smack加入群组的方法
                // 被邀请加入群组，lastSeconds == 当前时间 - 被邀请时的时间
                MyApplication.getInstance().joinMucChat(chatMessage.getObjectId(), TimeUtils.time_current_time() - chatMessage.getTimeSend());
            }
*/

            // 更新数据库
            chatMessage.setContent(desc);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage, true);
                MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(MyApplication.getContext());
            }
        } else if (type == Constants.TYPE_SEND_MANAGER) {
            String content = chatMessage.getContent();
            int role;
            if (content.equals("1")) {
                role = 2;
                chatMessage.setContent(fromUserName + " " + InternationalizationHelper.getString("JXSettingVC_Set") + toUserName + " " + InternationalizationHelper.getString("JXMessage_admin"));
            } else {
                role = 3;
                chatMessage.setContent(fromUserName + " " + InternationalizationHelper.getString("JXSip_Canceled") + toUserName + " " + InternationalizationHelper.getString("JXMessage_admin"));
            }

            RoomMemberDao.getInstance().updateRoomMemberRole(friend.getRoomId(), toUserId, role);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), friend.getUserId(), chatMessage, true);
                Intent intent = new Intent();
                intent.putExtra("roomId", friend.getUserId());
                intent.putExtra("toUserId", chatMessage.getToUserId());
                intent.putExtra("isSet", content.equals("1"));
                intent.setAction(com.iimm.miliao.broadcast.OtherBroadcast.REFRESH_MANAGER);
                MyApplication.getInstance().sendBroadcast(intent);
            }
        }
    }

    /**
     * 打开强提醒界面
     *
     * @param chatMessage
     * @param friend
     */
    public static void openStrongRemind(ChatMessage chatMessage, Friend friend) {
        MucRoom.Notice notice = JSONObject.parseObject(chatMessage.getContent(), MucRoom.Notice.class);
        if (chatMessage.getFromUserId().equals(MyApplication.getLoginUserId())) {  //如果是自己发送的  不要进行强提醒
            return;
        }
        if (FriendDao.getInstance().getStrongRemindStatus(friend.getUserId())) {  //开启了  “关闭强提醒”   不提示
            return;
        }
        Intent intent = new Intent(MyApplication.getInstance(), GroupStrongReminderActivity.class);
        intent.putExtra("sendUserId", chatMessage.getFromUserId());  //发送人的ID
        intent.putExtra("sendUserName", chatMessage.getFromUserName()); //发送人的昵称
        intent.putExtra("sendUserHeadImg", AvatarHelper.getAvatarUrl(chatMessage.getFromUserId(), true)); //发送人的头像
        intent.putExtra("groupId", friend.getUserId());  //发送到id
        intent.putExtra("groupName", friend.getNickName());//发送到昵称
        intent.putExtra("content", notice.getText());//公告内容
        intent.putExtra("currentChatWithId", MyApplication.IsRingId);//在没有此界面之前 ，获取当前聊天的对象ID ,用于检测是否打开群聊天界面
        MyApplication.getInstance().startActivity(intent);
    }

    /**
     * 直播消息处理
     */
    public static void chatLive(String body, ChatMessage chatMessage) {
        JSONObject mJSONObject = JSONObject.parseObject(body);
        String toUserId = mJSONObject.getString("toUserId");
        String toUserName = mJSONObject.getString("toUserName");
        int type = chatMessage.getType();
        if (type == Constants.TYPE_SEND_DANMU) {// 弹幕
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("danmu", chatMessage.getContent());
            bundle.putString("fromUserId", chatMessage.getFromUserId());
            bundle.putString("fromUserName", chatMessage.getFromUserName());
            intent.putExtras(bundle);
            intent.setAction(LiveConstants.LIVE_DANMU_DRAWABLE);
            MyApplication.getInstance().sendBroadcast(intent);
        } else if (type == Constants.TYPE_SEND_GIFT) {// 礼物
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("gift", chatMessage.getContent());
            bundle.putString("fromUserId", chatMessage.getFromUserId());
            bundle.putString("fromUserName", chatMessage.getFromUserName());
            intent.putExtras(bundle);
            intent.setAction(LiveConstants.LIVE_SEND_GIFT);
            MyApplication.getInstance().sendBroadcast(intent);
        } else if (type == Constants.TYPE_SEND_HEART) {// 点赞
            Intent intent = new Intent();
            intent.setAction(LiveConstants.LIVE_SEND_LOVE_HEART);
            MyApplication.getInstance().sendBroadcast(intent);
        } else if (type == Constants.TYPE_LIVE_LOCKING) {// 锁定直播间
            Intent intent = new Intent();
            intent.setAction(LiveConstants.LIVE_SEND_LOCKED);
            MyApplication.getInstance().sendBroadcast(intent);

            chatMessage.setType(Constants.TYPE_TIP);
            chatMessage.setContent(toUserName + " 直播间已经被锁定");
            ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage, true);
        } else if (type == Constants.TYPE_SEND_ENTER_LIVE_ROOM) {// 加入直播间
            Intent intent = new Intent();
            intent.setAction(LiveConstants.LIVE_MEMBER_ADD);
            MyApplication.getInstance().sendBroadcast(intent);

            chatMessage.setType(Constants.TYPE_TIP);
            chatMessage.setContent(toUserName + " 进入了直播间");
            ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage, true);
        } else if (chatMessage.getType() == Constants.TYPE_LIVE_EXIT_ROOM) {// 退出、被提出直播间
            Intent intent = new Intent();
            intent.putExtra("fromUserId", chatMessage.getFromUserId());
            intent.putExtra("toUserId", toUserId);
            intent.setAction(LiveConstants.LIVE_MEMBER_DELETE);
            MyApplication.getInstance().sendBroadcast(intent);
            if (TextUtils.equals(toUserId, MyApplication.getLoginUserId())) {
                // 只有发给我的结束直播消息才处理，
                chatMessage.setType(Constants.TYPE_TIP);
                if (chatMessage.getFromUserId().equals(toUserId)) {// 退出直播间
                    chatMessage.setContent(toUserName + " " + InternationalizationHelper.getString("EXITED_LIVE_ROOM"));
                } else {// 被踢出直播间
                    chatMessage.setContent(toUserName + " " + InternationalizationHelper.getString("JX_LiveVC_kickLive"));
                }
                ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage, true);
            }
        } else if (type == Constants.TYPE_LIVE_SHAT_UP) {// 禁言
            long time = Long.parseLong(chatMessage.getContent());
            // 发送广播到直播间,禁言/取消禁言该人
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("state", chatMessage.getContent());
            bundle.putString("fromUserId", chatMessage.getFromUserId());
            bundle.putString("toUserId", toUserId);
            intent.putExtras(bundle);
            intent.setAction(LiveConstants.LIVE_SEND_SHUT_UP);
            MyApplication.getInstance().sendBroadcast(intent);

            chatMessage.setType(Constants.TYPE_TIP);
            if (time == 0L) {
                // message.setContent("用户：" + toUserName + " 已被取消禁言");
                chatMessage.setContent(chatMessage.getFromUserName() + " " + InternationalizationHelper.getString("JXMessageObject_Yes") + toUserName +
                        InternationalizationHelper.getString("JXMessageObject_CancelGag"));
            } else {
                // message.setContent(toUserName + " 已被禁言");
                chatMessage.setContent(toUserName + " " + InternationalizationHelper.getString("HAS_BEEN_BANNED"));
            }
            ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage, true);
        } else if (chatMessage.getType() == Constants.TYPE_LIVE_SET_MANAGER) { // 设置/取消管理员
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("toUserId", toUserId);
            intent.putExtras(bundle);
            intent.setAction(LiveConstants.LIVE_SEND_MANAGER);
            MyApplication.getInstance().sendBroadcast(intent);

            String content = chatMessage.getContent();
            chatMessage.setType(Constants.TYPE_TIP);
            if (content.equals("1")) {
                chatMessage.setContent(chatMessage.getFromUserName() + " " + InternationalizationHelper.getString("JXSettingVC_Set") + toUserName + " " + InternationalizationHelper.getString("JXMessage_admin"));
            } else {
                chatMessage.setContent(chatMessage.getFromUserName() + " " + InternationalizationHelper.getString("JXSip_Canceled") + toUserName + " " + InternationalizationHelper.getString("JXMessage_admin"));
            }
            ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage, true);
        }
    }

    public static String getName(Friend friend, String userId) {
        if (friend == null) {
            return null;
        }
        RoomMember mRoomMember = RoomMemberDao.getInstance().getSingleRoomMember(friend.getRoomId(), MyApplication.getLoginUserId());
        if (mRoomMember != null && mRoomMember.getRole() == 1) {// 我为群主 Name显示为群内备注
            RoomMember member = RoomMemberDao.getInstance().getSingleRoomMember(friend.getRoomId(), userId);
            if (member != null && !TextUtils.equals(member.getUserName(), member.getCardName())) {
                // 当userName与cardName不一致时，我们认为群主有设置群内备注
                return member.getCardName();
            } else {
                Friend mFriend = FriendDao.getInstance().getFriend(MyApplication.getLoginUserId(), userId);
                if (mFriend != null && !TextUtils.isEmpty(mFriend.getRemarkName())) {
                    return mFriend.getRemarkName();
                }
            }
        } else {// 为好友 显示备注
            Friend mFriend = FriendDao.getInstance().getFriend(MyApplication.getLoginUserId(), userId);
            if (mFriend != null && !TextUtils.isEmpty(mFriend.getRemarkName())) {
                return mFriend.getRemarkName();
            }
        }
        return null;
    }

    // 更新群成员表
    public static void operatingRoomMemberDao(int type, String roomId, String userId, String userName) {
        if (type == 0) {
            RoomMember roomMember = new RoomMember();
            roomMember.setRoomId(roomId);
            roomMember.setUserId(userId);
            roomMember.setUserName(userName);
            roomMember.setCardName(userName);
            roomMember.setRole(3);
            roomMember.setCreateTime(0);
            RoomMemberDao.getInstance().saveSingleRoomMember(roomId, roomMember);
        } else {
            RoomMemberDao.getInstance().deleteRoomMember(roomId, userId);
        }
    }

    /**
     * 解密
     */
    public static void decryptDES(ChatMessage chatMessage) {
        if (TextUtils.isEmpty(chatMessage.getContent())) {
            return;
        }
        try {
            String decryptKey = Md5Util.toMD5(AppConfig.apiKey + chatMessage.getTimeSend() + chatMessage.getPacketId());
            String decryptContent = DES.decryptDES(chatMessage.getContent(), decryptKey);
            // 为chatMessage重新设值
            chatMessage.setContent(decryptContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存接收到的聊天信息(群聊)
     */
    public static void saveGroupMessage(String body, String from, String packetId, boolean isDelay) {
        String fromId = XmppStringUtil.getRoomJID(from);
        String roomJid = XmppStringUtil.getRoomJIDPrefix(fromId);

        ChatMessage chatMessage = new ChatMessage(body);
        if (BuildConfig.LOG_DEBUG) {
            if (chatMessage.getType() == Constants.TYPE_TEXT
                    || chatMessage.getType() == Constants.TYPE_IMAGE
                    || chatMessage.getType() == Constants.TYPE_VOICE
                    || chatMessage.getType() == Constants.TYPE_LOCATION
                    || chatMessage.getType() == Constants.TYPE_GIF
                    || chatMessage.getType() == Constants.TYPE_VIDEO
                    || chatMessage.getType() == Constants.TYPE_CARD
                    || chatMessage.getType() == Constants.TYPE_FILE
                    || chatMessage.getType() == Constants.TYPE_RED
                    || chatMessage.getType() == Constants.TYPE_READ_EXCLUSIVE
                    || chatMessage.getType() == Constants.TYPE_TRANSFER
                    || chatMessage.getType() == Constants.TYPE_CLOUD_TRANSFER
                    || chatMessage.getType() == Constants.TYPE_IMAGE_TEXT
                    || chatMessage.getType() == Constants.TYPE_IMAGE_TEXT_MANY
                    || chatMessage.getType() == Constants.TYPE_LINK
                    || chatMessage.getType() == Constants.TYPE_SHARE_LINK
                    || chatMessage.getType() == Constants.TYPE_SHAKE
                    || chatMessage.getType() == Constants.TYPE_CARD
                    || chatMessage.getType() == Constants.TYPE_CLOUD_RED) {
                LogUtils.logMessage(MyApplication.getContext(), body);
            }
        }
        if (TextUtils.equals(chatMessage.getFromUserId(), MyApplication.getLoginUserId())
                && chatMessage.getType() == Constants.TYPE_READ
                && TextUtils.isEmpty(chatMessage.getFromUserName())) {
            chatMessage.setFromUserName(CoreManager.requireSelf(MyApplication.getInstance()).getNickName());
        }

        if (!chatMessage.validate()) {
            return;
        }
        int isEncrypt = chatMessage.getIsEncrypt();
        if (isEncrypt == 1) {
            decryptDES(chatMessage);// 解密
        }
        int type = chatMessage.getType();

        chatMessage.setGroup(true);
        chatMessage.setMessageState(Constants.MESSAGE_SEND_SUCCESS);
        if (TextUtils.isEmpty(packetId)) {
            if (isDelay) {
                //LogUtils.e(TAG, "离线消息的packetId为空，漫游任务可能会受到影响，考虑要不要直接Return");
            }
            packetId = ToolUtils.getUUID();
        }
        chatMessage.setPacketId(packetId);

        // 生成漫游任务
        if (isDelay) {
//            if (chatMessage.isExpired()) {// 该条消息为过期消息，存入本地后直接Return ，不通知
//                //LogUtils.e(TAG, "// 该条消息为过期消息，存入本地后直接Return ，不通知");
//                chatMessage.setIsExpired(1);
//                ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), roomJid, chatMessage);
//                return;
//            }
            // 离线消息 判断当前群组的实际离线消息是否大于100条，如大于100条，为之前创建的任务的endTime字段赋值，反之则删除任务
            // 判断条件 离线消息内有一条消息的msgId等于当前任务的startMsgId 离线消息小于100条
            MsgRoamTask mLastMsgRoamTask = MsgRoamTaskDao.getInstance().getFriendLastMsgRoamTask(MyApplication.getLoginUserId(), roomJid); // 获取该群组最后一个任务
            if (mLastMsgRoamTask == null) {
            } else if (mLastMsgRoamTask.getEndTime() == 0) {// 为该任务的EndTime赋值 理论上只会赋值一次
                MsgRoamTaskDao.getInstance().updateMsgRoamTaskEndTime(MyApplication.getLoginUserId(), roomJid, mLastMsgRoamTask.getTaskId(), chatMessage.getTimeSend());
            } else if (packetId.equals(mLastMsgRoamTask.getStartMsgId())) {
                MsgRoamTaskDao.getInstance().deleteMsgRoamTask(MyApplication.getLoginUserId(), roomJid, mLastMsgRoamTask.getTaskId());
            }
        }

        boolean isShieldGroupMsg = PreferenceUtils.getBoolean(MyApplication.getInstance(), Constants.SHIELD_GROUP_MSG + roomJid + MyApplication.getLoginUserId(), false);
        if (isShieldGroupMsg) {// 已屏蔽
            return;
        }

        if (Constants.SUPPORT_BIDIRECTIONAL_WITHDRAW && type == Constants.EVENTBUS_GROUP_TWO_WAY_WIITHDRAWAL) {
            FriendDao.getInstance().resetFriendMessage(MyApplication.getLoginUserId(), roomJid);
            ChatMessageDao.getInstance().deleteMessageTable(MyApplication.getLoginUserId(), roomJid);
            MsgBroadcast.broadcastMsgMUCUpdate(MyApplication.getInstance());// 清空聊天界面
            MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
            return;
        }
        if (type == Constants.EVENTBUS_DISABLE_IP_DEVICE) {
            EventBus.getDefault().post(new EventBusMsg(Constants.EVENTBUS_DISABLE_IP_DEVICE));
            return;
        }

        if (type == Constants.TYPE_TEXT
                && !TextUtils.isEmpty(chatMessage.getObjectId())) {// 判断为@消息
            Friend friend = FriendDao.getInstance().getFriend(MyApplication.getLoginUserId(), roomJid);
            if (friend != null) {
                if (friend.getIsAtMe() == 0
                        && !TextUtils.equals(MyApplication.IsRingId, roomJid)) {// 本地无@通知 && 收到该条消息时不处于当前群组的聊天界面
                    if (chatMessage.getObjectId().equals(roomJid)) {// @全体成员
                        FriendDao.getInstance().updateAtMeStatus(roomJid, 2);
                    } else if (chatMessage.getObjectId().contains(MyApplication.getLoginUserId())) {// @我
                        FriendDao.getInstance().updateAtMeStatus(roomJid, 1);
                    }
                }
            }
        }

        // 群已读
        if (type == Constants.TYPE_READ) {
            if (Constants.SUPPORT_READ_PERSON_COUNT) {
                packetId = chatMessage.getContent();
                ChatMessage chat = ChatMessageDao.getInstance().findMsgById(MyApplication.getLoginUserId(), roomJid, packetId);
                if (chat != null) {
                    String fromUserId = chatMessage.getFromUserId();
                    boolean repeat = ChatMessageDao.getInstance().checkRepeatRead(MyApplication.getLoginUserId(), roomJid, fromUserId, packetId);
                    if (!repeat) {
                        int count = chat.getReadPersons();// 查看人数+1
                        chat.setReadPersons(count + 1);
                        // 覆盖最后时间
                        chat.setReadTime(chatMessage.getTimeSend());
                        // 更新消息数据
                        ChatMessageDao.getInstance().updateMessageRead(MyApplication.getLoginUserId(), roomJid, chat);
                        // 保存新消息
                        //ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), roomJid, chatMessage);
                        // 通知刷新
                        MsgBroadcast.broadcastMsgReadUpdate(MyApplication.getInstance(), packetId);
                    }
                }
            }
            return;
        }

        // 消息撤回
        if (type == Constants.TYPE_BACK) {
            // 本地数据库处理
            packetId = chatMessage.getContent();
            if (chatMessage.getFromUserId().equals(MyApplication.getLoginUserId())) {// 自己发的不用处理
                ChatMessageDao.getInstance().updateMessageBack(MyApplication.getLoginUserId(), roomJid, packetId, MyApplication.getInstance().getString(R.string.you));
            } else {
                ChatMessageDao.getInstance().updateMessageBack(MyApplication.getLoginUserId(), roomJid, packetId, chatMessage.getFromUserName());
            }

            Intent intent = new Intent();
            intent.putExtra("packetId", packetId);
            intent.setAction(com.iimm.miliao.broadcast.OtherBroadcast.MSG_BACK);
            MyApplication.getInstance().sendBroadcast(intent);

            // 更新UI界面
            ChatMessage chat = ChatMessageDao.getInstance().getLastChatMessage(MyApplication.getLoginUserId(), roomJid);
            if (chat != null) {
                if (chat.getPacketId().equals(packetId)) {
                    // 要撤回的消息正是朋友表的最后一条消息
                    if (chatMessage.getFromUserId().equals(MyApplication.getLoginUserId())) {// 自己发的不用处理
                        FriendDao.getInstance().updateFriendContent(MyApplication.getLoginUserId(), roomJid,
                                MyApplication.getInstance().getString(R.string.you) + " " + InternationalizationHelper.getString("JX_OtherWithdraw"), Constants.TYPE_TEXT, chatMessage.getTimeSend());
                    } else {
                        FriendDao.getInstance().updateFriendContent(MyApplication.getLoginUserId(), roomJid,
                                chatMessage.getFromUserName() + " " + InternationalizationHelper.getString("JX_OtherWithdraw"), Constants.TYPE_TEXT, chatMessage.getTimeSend());
                    }
                    MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
                }
            }
            return;
        }

        if ((type >= Constants.TYPE_MUCFILE_ADD && type <= Constants.TYPE_MUCFILE_DOWN)
                || (type >= Constants.TYPE_CHANGE_NICK_NAME && type <= Constants.NEW_MEMBER)
                || type == Constants.TYPE_SEND_MANAGER
                || (type >= Constants.TYPE_CHANGE_SHOW_READ && type <= Constants.TYPE_GROUP_TRANSFER)) {
            if (TextUtils.isEmpty(chatMessage.getObjectId())) {
                LogUtils.e(TAG, "Return 4");
                return;
            }
            if (ChatMessageDao.getInstance().hasSameMessage(MyApplication.getLoginUserId(), chatMessage.getObjectId(), chatMessage.getPacketId())) {// 本地已经保存了这条消息，不处理
                LogUtils.e(TAG, "Return 5");
                return;
            }
            Friend friend = FriendDao.getInstance().getFriend(MyApplication.getLoginUserId(), chatMessage.getObjectId());
            if (friend != null) {
                chatGroup(body, chatMessage, friend);
            }
            return;
        }

        // 直播消息处理
        if ((type >= Constants.TYPE_SEND_DANMU && type <= Constants.TYPE_SEND_ENTER_LIVE_ROOM)
                || (type >= Constants.TYPE_LIVE_LOCKING && type <= Constants.TYPE_LIVE_SET_MANAGER)) {
            chatLive(body, chatMessage);
            return;
        }

        if (chatMessage.getFromUserId().equals(MyApplication.getLoginUserId()) &&
                (chatMessage.getType() == Constants.TYPE_IMAGE || chatMessage.getType() == Constants.TYPE_VIDEO || chatMessage.getType() == Constants.TYPE_FILE)) {
            LogUtils.e(TAG, "多点登录，需要显示上传进度的消息");
            chatMessage.setUpload(true);
            chatMessage.setUploadSchedule(100);
        }
        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getLoginUserId(), roomJid, chatMessage)) {
            Friend friend = FriendDao.getInstance().getFriend(MyApplication.getLoginUserId(), roomJid);
            if (friend != null) {// friend == null 为直播间消息，直接跳过
                if (friend.getOfflineNoPushMsg() == 0) {
                    NotificationUtils.notificationMessage(chatMessage, true);// 消息已存入本地，调用本地通知
                    if (!roomJid.equals(MyApplication.IsRingId)
                            && !chatMessage.getFromUserId().equals(MyApplication.getLoginUserId())) {// 收到该消息时不处于与发送方的聊天界面 && 不是自己发送的消息
                        LogUtils.e("msg", "群组铃声通知");
                        NoticeVoicePlayer.getInstance().start();
                    }
                } else {
                    LogUtils.e("msg", "已针对该群组开启了消息免打扰，不通知");
                }
            }

            ListenerManager.getInstance().notifyNewMesssage(MyApplication.getLoginUserId(), roomJid, chatMessage, true);
        }

    }

    /**
     * 所有消息类型都用文字描述，
     * 比如[图片],
     * 部分类型可能返回空字符串，
     */
    @NonNull
    public static String getSimpleContent(int type, String content) {
        Context ctx = MyApplication.getInstance();
        switch (type) {
            case Constants.TYPE_TEXT:
                break;
            case Constants.TYPE_VOICE:
                content = ctx.getString(R.string.msg_voice);
                break;
            case Constants.TYPE_GIF:
                content = ctx.getString(R.string.msg_animation);
                break;
            case Constants.TYPE_IMAGE:
                content = ctx.getString(R.string.msg_picture);
                break;
            case Constants.TYPE_VIDEO:
                content = ctx.getString(R.string.msg_video);
                break;
            case Constants.TYPE_RED:
                content = ctx.getString(R.string.msg_red_packet);
                break;
            case Constants.TYPE_READ_EXCLUSIVE:
                content = ctx.getString(R.string.msg_red_packet_exclusive);
                break;
            case Constants.TYPE_CLOUD_RED:
                content = ctx.getString(R.string.msg_red_cloud_packet);
                break;
            case Constants.TYPE_LOCATION:
                content = ctx.getString(R.string.msg_location);
                break;
            case Constants.TYPE_CARD:
                content = ctx.getString(R.string.msg_card);
                break;
            case Constants.TYPE_FILE:
                content = ctx.getString(R.string.msg_file);
                break;
            case Constants.TYPE_TIP:
                break;
            case Constants.TYPE_IMAGE_TEXT:
            case Constants.TYPE_IMAGE_TEXT_MANY:
                content = ctx.getString(R.string.msg_image_text);
                break;
            case Constants.TYPE_LINK:
            case Constants.TYPE_SHARE_LINK:
                content = ctx.getString(R.string.msg_link);
                break;
            case Constants.TYPE_SHAKE:
                content = ctx.getString(R.string.msg_shake);
                break;
            case Constants.TYPE_CHAT_HISTORY:
                content = ctx.getString(R.string.msg_chat_history);
                break;
            case Constants.TYPE_TRANSFER:
                content = ctx.getString(R.string.tip_transfer_money);
                break;
            case Constants.TYPE_CLOUD_TRANSFER:
                content = ctx.getString(R.string.micro_tip_transfer_money);
                break;
            case Constants.TYPE_TRANSFER_RECEIVE:
                content = ctx.getString(R.string.tip_transfer_money) + ctx.getString(R.string.transfer_friend_sure_save);
                break;
            case Constants.TYPE_CLOUD_TRANSFER_RECEIVE:
                content = ctx.getString(R.string.micro_tip_transfer_money) + ctx.getString(R.string.transfer_friend_sure_save);
                break;
            case Constants.TYPE_TRANSFER_BACK:
                content = ctx.getString(R.string.transfer_back);
                break;
            case Constants.TYPE_CLOUD_TRANSFER_RETURN:
                content = ctx.getString(R.string.micro_transfer_back);
                break;
            case Constants.TYPE_IS_CONNECT_VOICE:
                content = ctx.getString(R.string.suffix_invite_you_voice);
                break;
            case Constants.TYPE_IS_CONNECT_VIDEO:
                content = ctx.getString(R.string.suffix_invite_you_video);
                break;

            case Constants.TYPE_SAYHELLO:// 打招呼
                content = ctx.getString(R.string.apply_to_add_me_as_a_friend);
                break;
            case Constants.TYPE_PASS:    // 同意加好友
                content = ctx.getString(R.string.agree_with_my_plus_friend_request);
                break;
            case Constants.TYPE_FRIEND:  // 直接成为好友
                content = ctx.getString(R.string.added_me_as_a_friend);
                break;

            case Constants.DIANZAN:// 朋友圈点赞
                content = ctx.getString(R.string.notification_praise_me_life_circle);
                break;
            case Constants.PINGLUN:    // 朋友圈评论
                content = ctx.getString(R.string.notification_comment_me_life_circle);
                break;
            case Constants.ATMESEE:  // 朋友圈提醒我看
                content = ctx.getString(R.string.notification_at_me_life_circle);
                break;
            case Constants.TYPE_NEW_NOTICE:
                try {
                    org.json.JSONObject jsonObject = new org.json.JSONObject(content);
                    String noticeContent = jsonObject.getString("text");
                    content = ctx.getString(R.string.notice) + ":" + noticeContent;
                } catch (JSONException e) {
                    e.printStackTrace();
                    content = ctx.getString(R.string.msg_hint_notice);
                }
                break;
            case Constants.TYPE_CUSTOM_CHANGE_READ_DEL_TIME:  //您设置了消息几秒后消失  阅后即焚  自定义的view:
                try {
                    org.json.JSONObject jsonObject = new org.json.JSONObject(content);
                    String changeReadDelTime = jsonObject.getString("changeReadDelTime");
                    boolean isChangeByMe = jsonObject.getBoolean("isChangeByMe");
                    if (TextUtils.isEmpty(changeReadDelTime)) {
                        changeReadDelTime = "0";
                    }
                    if (isChangeByMe) {
                        if (changeReadDelTime.equals("0")) {
                            content = getString(R.string.you_shut_down_and_burned_after_reading);
                        } else {
                            content = String.format(getString(R.string.hint_read_del_time), PersonSettingActivity.handlerMsg(Integer.parseInt(changeReadDelTime)));
                        }
                    } else {
                        if (changeReadDelTime.equals("0")) {
                            content = getString(R.string.he_shut_down_and_burned_after_reading);
                        } else {
                            content = String.format(getString(R.string.he_hint_read_del_time), PersonSettingActivity.handlerMsg(Integer.parseInt(changeReadDelTime)));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    content = getString(R.string.open_read_del_time);
                }
                break;
            case Constants.TYPE_RECHARGE_GET:
                content = getString(R.string.payment_notice);
                break;
            case Constants.TYPE_RECHARGE_H5_GET:
                content = getString(R.string.payment_notice);
                break;
        }
        return content;
    }

    /**
     * 所有消息类型都用文字描述，
     * 比如[图片],
     * 部分类型可能返回空字符串，
     */
    @NonNull
    public static String getSimpleContent(ChatMessage chatMessage) {
        String content = "";
        switch (chatMessage.getType()) {
            case Constants.TYPE_REPLAY:
            case Constants.TYPE_TEXT:
                if (chatMessage.getIsReadDel()) {
                    content = getString(R.string.tip_click_to_read);
                } else {
                    content = chatMessage.getContent();
                }
                break;
            default:
                content = ImHelper.getSimpleContent(chatMessage.getType(), chatMessage.getContent());
                break;
        }
        return content;
    }

    public static void saveImageSize(ChatMessage chatMessage, String filePath) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options); // 此时返回的bitmap为null
            chatMessage.setLocation_x(String.valueOf(options.outWidth));
            chatMessage.setLocation_y(String.valueOf(options.outHeight));
            // 保存下载到数据库
            ChatMessageDao.getInstance().updateMessageLocationXY(chatMessage, chatMessage.getToUserId());
        } catch (Exception e) {

        }
    }


    public static boolean canCreateGroup(CoreManager core) {
        if (core == null) {
            return false;
        }
        boolean ordinaryUserCannotCreateGroup = core.getConfig().ordinaryUserCannotCreateGroup
                && core.getSelf().isOrdinaryUser();
        if (ordinaryUserCannotCreateGroup) {
            int isCreateRoom = PreferenceUtils.getInt(MyApplication.getInstance(), MyApplication.getLoginUserId() + "_" + Constants.IS_CREATE_ROOM, 0);
            return isCreateRoom > 0;
        } else {
            return true;
        }
    }


    //**********************************************第二通道离线消息*********************************************************
    public static void getHistoryIM() {
        Map<String, String> params = new HashMap<>();
        params.put("token", CoreManager.requireSelfStatus(MyApplication.getInstance()).accessToken);
        params.put("device", "youjob");
        params.put("delete", "true");
        HttpUtils.get().url(SpareConnectionHelper.HISTORY_RECORD)
                .params(params)
                .build()
                .execute(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        try {
                            String body = response.body().string();
                            if (TextUtils.isEmpty(body)) {
                                return;
                            }
                            ArrayList<String> jsonObjects = new Gson().fromJson(body, new TypeToken<ArrayList<String>>() {
                            }.getType());
                            if (jsonObjects == null || jsonObjects.size() == 0) {
                                return;
                            }

                            List<SpareMessage> list = new ArrayList<>();
                            for (String jsonObject : jsonObjects) {
                                list.add(new Gson().fromJson(jsonObject, SpareMessage.class));
                            }

                            Log.i(TAG, "服务器数据包：" + body);
                            Log.i(TAG, "222服务器数据包：" + list.size());
                            for (SpareMessage spareMessage : list) {
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
                                        Log.i(TAG, "groupchat服务器数据包：" + message1.getBody());
                                    } else if (message1 != null && message1.getType() != null && message1.getType() == Message.Type.chat) {
                                        processIncomingChatMessage(message1);
                                        Log.i(TAG, "chat服务器数据包：" + message1);
                                    }
                                }
                            }

                        } catch (Exception e) {

                        }
                    }
                });
    }

    public static void processIncomingGroupChatMessage(Message message) {
        if (TextUtils.isEmpty(message.getFrom()) || TextUtils.isEmpty(message.getTo())) {
            return;
        }
        String from = message.getFrom().toString();
        String to = message.getTo().toString();
        if (!XmppStringUtil.isJID(from) || !XmppStringUtil.isJID(to)) {
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

            MassageDelete message1 = new Gson().fromJson(chatMessage.getContent(), MassageDelete.class);
            ChatMessage deleteMsg = new ChatMessage(message1.getBody());
            ImHelper.backMessageGroupDelete(deleteMsg, roomJid);
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
        ImHelper.saveGroupMessage(content, from, message.getStanzaId(), false);


    }

    public static void processIncomingChatMessage(Message message) {

        // body外
        String from = XmppStringUtils.parseBareJid(message.getFrom().toString());
        String resource = message.getFrom().toString().substring(message.getFrom().toString().indexOf("/") + 1, message.getFrom().length());// 发送方的resource ex:ios 、pc...
        String packetID = message.getStanzaId();
        // body内(即我们发送的ChatMessage)
        String body = message.getBody();
        ChatMessage chatMessage = new ChatMessage(body);
        String fromUserId = chatMessage.getFromUserId();
        String toUserId = chatMessage.getToUserId();

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
            MassageDelete message1 = new Gson().fromJson(chatMessage.getContent(), MassageDelete.class);
            ChatMessage deleteMsg = new ChatMessage(message1.getBody());
            ImHelper.backMessageDelete(deleteMsg);
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


}
