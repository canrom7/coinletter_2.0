package com.iimm.miliao.xmpp;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.iimm.miliao.MyApplication;
import com.iimm.miliao.bean.EventLoginStatus;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.bean.message.NewFriendMessage;
import com.iimm.miliao.bean.message.XmppMessage;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.db.dao.login.MachineDao;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.log.LogUtils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.parts.Resourcepart;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import de.greenrobot.event.EventBus;
import fm.jiecao.jcvideoplayer_lib.MessageEvent;

/**
 * @Author: wangqx
 * @Date: 2019/7/31
 * @Description:描述
 */
public class XmppReceiptImpl implements ReceiptReceivedListener, StanzaListener {
    private static String TAG = "XmppReceiptImpl";
    public static final int MESSAGE_DELAY = 20 * 1000; // 等待消息回执， 超时时间
    private static final int RECEIPT_NO = 0x1; // 没有收到回执
    private static final int RECEIPT_YES = 0x2;// 收到回执
    public static Set<String> batchReceiptMessageQueue = new ConcurrentSkipListSet<>();
    public static boolean receiptThreadStop = false;
    public static boolean batchReceiptEnable = true;
    /**
     * 处理消息回执
     */
    public static Map<String, ReceiptObj> mReceiptMsgMap = new ConcurrentHashMap<>();

    private static class XmppReceiptImplHolder {
        private static final XmppReceiptImpl INSTANCE = new XmppReceiptImpl();
    }

    private XmppReceiptImpl() {
    }

    public static final XmppReceiptImpl getInstance() {
        return XmppReceiptImplHolder.INSTANCE;
    }

    @Override
    public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
        processReceiptStanza(packet);
    }

    @Override
    public void onReceiptReceived(Jid fromJid, Jid toJid, String receiptId, Stanza receipt) {
        if (TextUtils.isEmpty(receiptId)) {
            return;
        }
        ReceiptObj obj = mReceiptMsgMap.get(receiptId); // 接收到message先从map中去除对应键值的消息
        if (obj == null || obj.msg == null || obj.toUserId == null) {// 没有取出表示没有该消息
            return;
        }
        LogUtils.i(TAG, "onReceiptReceived: receiptId:" + receiptId);
        if (obj.msg instanceof ChatMessage) {
            ((ChatMessage) obj.msg).stopTimer();
        } else if (obj.msg instanceof NewFriendMessage) {
            ((NewFriendMessage) obj.msg).stopTimer();
        }
        mReceiptMsgMap.remove(receiptId);
        String send_200_packageId = PreferenceUtils.getString(MyApplication.getInstance(), "send_200_packageId", "");
        if (!TextUtils.isEmpty(receiptId) && send_200_packageId.equals(receiptId)) {
            //收到200 的回执
            Resourcepart resourceOrEmpty = fromJid.getResourceOrEmpty();
            if (!TextUtils.isEmpty(resourceOrEmpty)) {
                EventBus.getDefault().post(new EventLoginStatus(resourceOrEmpty.toString(), true));
                MachineDao.getInstance().updateMachineOnLineStatus(resourceOrEmpty.toString(), true);
                PreferenceUtils.putString(MyApplication.getInstance(), "send_200_packageId", "");
            }
        }
        if (obj.Read == 1) { // 已读消息 Type==26
            LogUtils.e(TAG, "已读消息发送成功: " + obj.Read_msg_pid + " to " + obj.toUserId + "修改本地");
            if (MyApplication.getLoginUserId().equals(obj.toUserId)) {
                for (String s : MyApplication.machine) {
                    ChatMessageDao.getInstance().updateMessageRead(MyApplication.getLoginUserId(), s, obj.Read_msg_pid, true);  // 传入的 packetId是被回执的消息的packetId
                }
            } else {
                ChatMessageDao.getInstance().updateMessageRead(MyApplication.getLoginUserId(), obj.toUserId, obj.Read_msg_pid, true); // 传入的 packetId是被回执的消息的packetId
            }
        } else {
            // LogUtils.e(TAG, "普通消息发送成功: " + packetId);
            LogUtils.e(TAG, "普通消息发送成功: " + receiptId);
            if (obj.sendType == SendType.NORMAL) {
                if (obj.msg.getType() != Constants.TYPE_CURRENT_USER_INFO_UPDATE
                        && obj.msg.getType() != Constants.TYPE_FRIEND_INFO_UPDATE
                        && obj.msg.getType() != Constants.TYPE_FRIEND_LABEL_INFO_UPDATE
                        && obj.msg.getType() != Constants.TYPE_FRIEND_OTHER_INFO_UPDATE
                        && obj.msg.getType() != Constants.TYPE_FRIEND_READDEL_UPDATE
                        && obj.msg.getType() != Constants.TYPE_FRIEND_ADD_DELETE
                ) {
                    if (obj.msg instanceof ChatMessage) {
                        ListenerManager.getInstance().notifyMessageSendStateChange(MyApplication.getLoginUserId(), obj.toUserId, ((ChatMessage) obj.msg).getPacketId(), Constants.MESSAGE_SEND_SUCCESS);
                    }
                    // 收到消息回执，通知消息群发页面
                    EventBus.getDefault().post(new MessageEvent(obj.toUserId));
                }
            } else {
                ListenerManager.getInstance().notifyNewFriendSendStateChange(obj.toUserId, ((NewFriendMessage) obj.msg), Constants.MESSAGE_SEND_SUCCESS);
            }
        }
        if (MyApplication.IS_SUPPORT_MULTI_LOGIN) {
            // 1.因为现在都是服务端代发回执，且回执的from不带resource，所以基本都会走到Exception内
            // 2.type==200的检测消息除了服务端发送回执之后，客户端收到之后也会发送回执，且客户端发送的from的带有resource
            // 3.所以这里基本是处理type==200消息的回执的地方
            /*
            try {
                String from = fromJid.toString().substring(0, fromJid.toString().indexOf("/"));
                String to = toJid.toString().substring(0, toJid.toString().indexOf("/"));
                if (from.equals(to)) {
                    String resource = fromJid.toString().substring(fromJid.toString().indexOf("/") + 1, fromJid.length());
                    //todo 不知道有什么作用，且会报错，暂时注释掉
                    //MachineDao.getInstance().updateMachineOnLineStatus(resource, true);
                }
            } catch (Exception e) {
                LogUtils.e(TAG, "updateMachineOnLineStatus Failed");
            }*/
        }
    }

    public void processReceiptStanza(Stanza packet) {
        // Message
        if (packet instanceof Message) {
            Message message = (Message) packet;
            if (message.getType() == Message.Type.chat) {
            } else if (message.getType() == Message.Type.groupchat) {
            } else if (message.getType() == Message.Type.error) {
            } else {
            }
        }
        if (TextUtils.isEmpty(packet.getStanzaId())) {
            return;
        }
        ReceiptObj mReceiptObj = mReceiptMsgMap.get(packet.getStanzaId());
        if (mReceiptObj != null) {
            LogUtils.e(TAG, "消息已送至服务器");
            if (mReceiptObj.Read == 1) {// 已读消息 Type==26
                if (MyApplication.getLoginUserId().equals(mReceiptObj.toUserId)) {
                    for (String s : MyApplication.machine) {
                        ChatMessageDao.getInstance().updateMessageRead(MyApplication.getLoginUserId(), s, mReceiptObj.Read_msg_pid, true);
                    }
                } else {
                    ChatMessageDao.getInstance().updateMessageRead(MyApplication.getLoginUserId(), mReceiptObj.toUserId, mReceiptObj.Read_msg_pid, true);
                }
            } else {// 普通消息 && 新朋友消息
                if (mReceiptObj.sendType == SendType.NORMAL) {
                    ListenerManager.getInstance().notifyMessageSendStateChange(MyApplication.getLoginUserId(), mReceiptObj.toUserId, packet.getStanzaId(),
                            Constants.MESSAGE_SEND_SUCCESS);

                    EventBus.getDefault().post(new MessageEvent(mReceiptObj.toUserId));
                } else {
                    ListenerManager.getInstance().notifyNewFriendSendStateChange(mReceiptObj.toUserId, ((NewFriendMessage) mReceiptObj.msg),
                            Constants.MESSAGE_SEND_SUCCESS);
                }
            }
            mReceiptMsgMap.remove(packet.getStanzaId());
        }
    }


    /**
     * 发送的消息类型，用于类型判断和消息回执的分发
     */
    public enum SendType {
        NORMAL, PUSH_NEW_FRIEND
    }

    public static class ReceiptObj {
        public String toUserId;// 普通消息和新朋友消息公用
        public XmppMessage msg;// 用于普通消息和新朋友消息公用
        public SendType sendType;// 用于分发普通消息和新朋友消息的回执
        public int Read;// 用于标记此消息是否为已读回执消息
        public String Read_msg_pid;// 被回执消息的packetId
    }

    public void sendBatchReceipt(@NonNull String messageId) {
        if (XmppReceiptImpl.batchReceiptEnable) {
            XmppReceiptImpl.batchReceiptMessageQueue.add(messageId);
        } else {
            Log.w(TAG, "IQ回执没有启用就收到了消息, " + messageId);
        }
    }

    public void releaseBatchReceipt() {
        XmppReceiptImpl.receiptThreadStop = true;
        EnableIQRequestHandler.sendBatchReceiptThread = null;
        batchReceiptMessageQueue.clear();
    }

}
