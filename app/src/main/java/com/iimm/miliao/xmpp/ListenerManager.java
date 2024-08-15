package com.iimm.miliao.xmpp;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.iimm.miliao.MyApplication;
import com.iimm.miliao.audio.NoticeVoicePlayer;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.bean.message.NewFriendMessage;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.NewFriendDao;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.xmpp.listener.ChatMessageListener;
import com.iimm.miliao.xmpp.listener.MucListener;
import com.iimm.miliao.xmpp.listener.NewFriendListener;

import java.util.ArrayList;
import java.util.List;


// * @描述: TODO
public class ListenerManager {
    private String TAG = "ListenerManager";
    private static ListenerManager instance;
    /* 回调监听 */
    private List<NewFriendListener> mNewFriendListeners = new ArrayList<NewFriendListener>();
    private List<ChatMessageListener> mChatMessageListeners = new ArrayList<ChatMessageListener>();
    private List<MucListener> mMucListeners = new ArrayList<MucListener>();
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private ListenerManager() {
    }

    public static ListenerManager getInstance() {
        if (instance == null) {
            instance = new ListenerManager();
        }
        return instance;
    }

    public void reset() {
        instance = null;
    }

    public void addNewFriendListener(NewFriendListener listener) {
        mNewFriendListeners.add(listener);
    }

    public void removeNewFriendListener(NewFriendListener listener) {
        mNewFriendListeners.remove(listener);
    }

    public void addChatMessageListener(ChatMessageListener messageListener) {
        mChatMessageListeners.add(messageListener);
    }

    public void removeChatMessageListener(ChatMessageListener messageListener) {
        mChatMessageListeners.remove(messageListener);
    }

    public void addMucListener(MucListener listener) {
        mMucListeners.add(listener);
    }

    public void removeMucListener(MucListener listener) {
        mMucListeners.remove(listener);
    }

    /**
     * 消息发送状态监听
     */
    public void notifyMessageSendStateChange(String mLoginUserId, String toUserId, final String msgId, final int messageState) {
        if (TextUtils.isEmpty(mLoginUserId) || TextUtils.isEmpty(toUserId)) {
            return;
        }
        if (mLoginUserId.equals(toUserId)) {
            for (String s : MyApplication.machine) {
                ChatMessageDao.getInstance().updateMessageState(mLoginUserId, s, msgId, messageState);
            }
        } else {
            ChatMessageDao.getInstance().updateMessageState(mLoginUserId, toUserId, msgId, messageState);
        }

        mHandler.post(new Runnable() {
            public void run() {
                for (ChatMessageListener listener : mChatMessageListeners) {
                    listener.onMessageSendStateChange(messageState, msgId);
                }
            }
        });
    }

    /**
     * 新朋友发送消息的状态变化
     */
    public void notifyNewFriendSendStateChange(final String toUserId, final NewFriendMessage message, final int messageState) {
        if (mNewFriendListeners.size() <= 0) {
            return;
        }
        mHandler.post(new Runnable() {
            public void run() {
                for (NewFriendListener listener : mNewFriendListeners) {
                    listener.onNewFriendSendStateChange(toUserId, message, messageState);
                }
            }
        });
    }

    /**
     * 新的朋友
     */
    public void notifyNewFriend(final String loginUserId, final NewFriendMessage message, final boolean isPreRead) {

        mHandler.post(new Runnable() {
            public void run() {
                boolean hasRead = false;// 是否已经被读了 (如果有类添加)

                for (NewFriendListener listener : mNewFriendListeners) {
                    if (listener.onNewFriend(message)) {
                        hasRead = true;
                    }
                }
                if (!hasRead && isPreRead) {
                    int i = NewFriendDao.getInstance().getNewFriendUnRead(message.getOwnerId(), message.getUserId());
                    if (i <= 0) {// 当该新的朋友存在一条未读消息时，不在更新
                        NewFriendDao.getInstance().markNewFriendUnRead(message.getOwnerId(), message.getUserId());
                        FriendDao.getInstance().markUserMessageUnRead(loginUserId, Constants.ID_NEW_FRIEND_MESSAGE);
                    }
                    MsgBroadcast.broadcastMsgNumUpdateNewFriend(MyApplication.getInstance());
                }
                MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
            }
        });
    }

    /**
     * 新消息来临
     */
    public void notifyNewMesssage(final String loginUserId, final String fromUserId
            , final ChatMessage message, final boolean isGroupMsg) {
        mHandler.post(new Runnable() {
            public void run() {
                if (message != null) {
                    boolean hasRead = false;
                    for (int i = mChatMessageListeners.size() - 1; i >= 0; i--) {
                        // 如果在某个for循环中对message进行了改变，那在他之后的所有循环，message都是被改变了的
                        ChatMessage tempMessage = message.clone(true);
                        tempMessage.setFromId(message.getFromId());
                        tempMessage.setToId(message.getToId());
                        tempMessage.setUpload(message.isUpload());
                        tempMessage.setUploadSchedule(message.getUploadSchedule());
                        tempMessage.setMessageState(message.getMessageState());// clone方法不会copy MessageState 需要重新赋值
                        if (hasRead) {
                            // 如果他是true，证明已经有类说明他是已读的了，所以就不用再赋值了
                            mChatMessageListeners.get(i).onNewMessage(fromUserId, tempMessage, isGroupMsg);
                        } else {
                            // 进行接口回调,为添加了该监听的类赋值
                            hasRead = mChatMessageListeners.get(i).onNewMessage(fromUserId, tempMessage, isGroupMsg);
                        }
                    }

                    String selfId = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();
                    if (isGroupMsg) {
                        Log.e("msg_fid", message.getFromUserId());
                        Log.e("msg_fid", selfId);

                        if (!hasRead
                                && !message.getFromUserId().equals(selfId)) {// 未读 || 其他端转发给我的 自己发送的消息(我的设备除外)
                            // 更新朋友表中该朋友的消息未读数量
                            boolean isRepeatFriend = FriendDao.getInstance().markUserMessageUnRead(loginUserId, fromUserId);
                            if (isRepeatFriend) {// 同一个群组不止一个，需要更新
                                MyApplication.getContext().sendBroadcast(new Intent(Constants.UPDATE_ROOM));
                            }
                            // 发送广播更新总未读消息数量
                            MsgBroadcast.broadcastMsgNumUpdate(MyApplication.getInstance(), true, 1);
                            NoticeVoicePlayer.getInstance().start();
                            Log.e("aaqa","NoticeVoicePlayer.getInstance().start()");
                        }
                    } else {
                        if (!hasRead
                                && !fromUserId.equals(selfId)) {
                            // 更新朋友表中该朋友的消息未读数量
                            FriendDao.getInstance().markUserMessageUnRead(loginUserId, fromUserId);
                            // 发送广播更新总未读消息数量
                            MsgBroadcast.broadcastMsgNumUpdate(MyApplication.getInstance(), true, 1);
                            NoticeVoicePlayer.getInstance().start();
                            Log.e("aaqa","NoticeVoicePlayer.getInstance().start()");
                        }
                    }
                    MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
                }
            }
        });
    }

    //////////////////////Muc Listener//////////////////////
    public void notifyDeleteMucRoom(final String toUserId) {
        if (mMucListeners.size() <= 0) {
            return;
        }
        mHandler.post(new Runnable() {
            public void run() {
                for (MucListener listener : mMucListeners) {
                    listener.onDeleteMucRoom(toUserId);
                }
            }
        });
    }

    public void notifyMyBeDelete(final String toUserId) {
        if (mMucListeners.size() <= 0) {
            return;
        }
        mHandler.post(new Runnable() {
            public void run() {
                for (MucListener listener : mMucListeners) {
                    listener.onMyBeDelete(toUserId);
                }
            }
        });
    }

    public void notifyNickNameChanged(final String toUserId, final String changedUserId, final String changedName) {
        if (mMucListeners.size() <= 0) {
            return;
        }
        mHandler.post(new Runnable() {
            public void run() {
                for (MucListener listener : mMucListeners) {
                    listener.onNickNameChange(toUserId, changedUserId, changedName);
                }
            }
        });
    }

    public void notifyGroupVoiceBanned(final String toUserId, final int time) {
        if (mMucListeners.size() <= 0) {
            return;
        }
        mHandler.post(new Runnable() {
            public void run() {
                for (MucListener listener : mMucListeners) {
                    listener.onGroupVoiceBanned(toUserId, time);
                }
            }
        });
    }

    public void notifyMyVoiceBanned(final String toUserId, final long time) {
        if (mMucListeners.size() <= 0) {
            return;
        }
        mHandler.post(new Runnable() {
            public void run() {
                for (MucListener listener : mMucListeners) {
                    listener.onMyVoiceBanned(toUserId, time);
                }
            }
        });
    }
}
