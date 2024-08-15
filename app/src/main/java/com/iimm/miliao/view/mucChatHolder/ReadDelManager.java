package com.iimm.miliao.view.mucChatHolder;

import android.content.Context;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;

import com.iimm.miliao.MyApplication;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.PreferenceUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.greenrobot.event.EventBus;

import static com.iimm.miliao.util.Constants.MESSAGE_SEND_SUCCESS;

public class ReadDelManager {
    private static final String TAG = "ReadDelManager";
    private Map<String, ReadDelInfo> mChatMessages;  //当前倒计时的MAP
    private Map<String, String> mStrings; //所有的key 集合  为保证加如一次
    private static ReadDelManager readDelManager;
    private static CountDownTimer mCountDownTimer;
    private ReadDelListener listener;


    public static ReadDelManager getInstants() {
        synchronized (ReadDelManager.class) {
            if (readDelManager == null) {
                readDelManager = new ReadDelManager();
            }
            if (mCountDownTimer == null) {
                mCountDownTimer = CreateCountDownTimer();
            }
            return readDelManager;
        }
    }

    private static CountDownTimer CreateCountDownTimer() {
        return new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //   Log.i(TAG, "阅后即焚计时器：onTick-----" + millisUntilFinished);
                if (ReadDelManager.getInstants().mChatMessages != null && ReadDelManager.getInstants().mChatMessages.size() > 0) {
                    Set<String> keySet = ReadDelManager.getInstants().mChatMessages.keySet();
                    Iterator<String> iterator = keySet.iterator();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        if (!TextUtils.isEmpty(key)) {
                            ReadDelInfo info = ReadDelManager.getInstants().mChatMessages.get(key);
                            ChatMessage message = info.getChatMessage();
                            AChatHolderInterface holderInterface = info.getAChatHolderInterface();
                            if (message.isMySend()) {
                                //这条阅后即焚是我发送的
                                if (message.getReadTime() <= 0) {
                                    //时间用完了   要删除这条消息
                                    Log.i(TAG, "时间用完了");
                                    EventBus.getDefault().post(new MessageEventClickFire("delete", message));
                                    if (ReadDelManager.getInstants().listener != null) {
                                        ReadDelManager.getInstants().listener.downOver(info);
                                    }
                                    ReadDelManager.getInstants().mChatMessages.remove(message.getPacketId());
                                    if (holderInterface != null) {
                                        holderInterface.mIvFire.setProgress(0);
                                    }
                                    Log.i(TAG, "删除消息成功");
                                } else {
                                    if (message.getMessageState() != MESSAGE_SEND_SUCCESS) {
                                        return;
                                    }
                                    double newReadTime = message.getReadTime() - 1000.0;  //剩余的毫秒数
                                    if (holderInterface != null) {
                                        long total = analyzeTheTimeAfterBurning(message.getIsReadDelByInt());
                                        int progress = (int) ((newReadTime / total) * 12 * 30);
                                        holderInterface.mIvFire.setProgress(progress);
                                        Log.i(TAG, "当前进度倒数的view 的进度：" + progress);
                                    }
                                    message.setReadTime((long) newReadTime);
                                    ChatMessageDao.getInstance().updateMessageReadTime(CoreManager.requireSelf(MyApplication.getInstance()).getUserId(), message.getToUserId(), message.getPacketId(), message.getReadTime(), message.getDeleteTime());
                                    ChatMessage chatMessage = ChatMessageDao.getInstance().findMsgById(CoreManager.requireSelf(MyApplication.getInstance()).getUserId(), message.getToUserId(), message.getPacketId());
                                    if (chatMessage != null) {
                                        Log.i(TAG, "packageId:" + message.getContent() +
                                                "---更新的消息的删除时间：" + chatMessage.getDeleteTime()
                                                + "-----ReadTime" + chatMessage.getReadTime());
                                    }
                                    Log.i(TAG, "packageId:" + message.getContent() + "---重新计算新的阅读时间：" + newReadTime);
                                }
                            } else {
                                //不是我发送的
                                if (message.getReadTime() <= 0) {
                                    //时间用完了   要删除这条消息
                                    Log.i(TAG, "时间用完了");
                                    EventBus.getDefault().post(new MessageEventClickFire("delete", message));
                                    if (ReadDelManager.getInstants().listener != null) {
                                        ReadDelManager.getInstants().listener.downOver(info);
                                    }
                                    ReadDelManager.getInstants().mChatMessages.remove(message.getPacketId());
                                    if (holderInterface != null) {
                                        holderInterface.mIvFire.setProgress(0);
                                    }
                                    Log.i(TAG, "删除消息成功");
                                } else {
                                    switch (message.getType()) {
                                        case Constants.TYPE_VOICE: //这个是音频消息:
                                            if (!message.isSendRead()) { //如果我没有发送过已读  说明我 还没有读这条消息 ，就略过这条消息不倒计时
                                                continue;
                                            }
                                            break;
                                    }
                                    double newReadTime = message.getReadTime() - 1000.0;  //剩余的毫秒数
                                    if (holderInterface != null) {
                                        long total = analyzeTheTimeAfterBurning(message.getIsReadDelByInt());
                                        int progress = (int) ((newReadTime / total) * 12 * 30);
                                        holderInterface.mIvFire.setProgress(progress);
                                        Log.i(TAG, "当前进度倒数的view 的进度：" + progress);
                                    }

                                    message.setReadTime((long) newReadTime);
                                    ChatMessageDao.getInstance().updateMessageReadTime(CoreManager.requireSelf(MyApplication.getInstance()).getUserId(), message.getFromUserId(), message.getPacketId(), message.getReadTime(), message.getDeleteTime());
                                    ChatMessage chatMessage = ChatMessageDao.getInstance().findMsgById(CoreManager.requireSelf(MyApplication.getInstance()).getUserId(), message.getFromUserId(), message.getPacketId());
                                    if (chatMessage != null) {
                                        Log.i(TAG, "packageId:" + message.getContent() +
                                                "---更新的消息的删除时间：" + chatMessage.getDeleteTime()
                                                + "-----ReadTime" + chatMessage.getReadTime());
                                    }
                                    Log.i(TAG, "packageId:" + message.getContent() + "---重新计算新的阅读时间：" + newReadTime);
                                }
                            }
                        }
                    }
                }

            }

            @Override
            public void onFinish() {
                Log.i(TAG, "阅后即焚计时器：onFinish");
            }
        }.start();
    }


    public synchronized void addReadMsg(ChatMessage message, AChatHolderInterface holder) {
        if (mChatMessages == null) {
            mChatMessages = new ConcurrentHashMap<>();
        }
        if (mStrings == null) {
            mStrings = new ConcurrentHashMap<>();
        }
        String key = message.getPacketId();
        if (mStrings.containsKey(key)) {  //全体集合中都有 说明不是第一次添加 （处理多线程问题 同步问题）
            if (mChatMessages.get(key) != null) {
                mChatMessages.get(key).setAChatHolderInterface(holder);
                return;
            }
            return;
        } else {
            mStrings.put(key, "k");
        }
        ReadDelInfo readDelInfo = new ReadDelInfo(message, holder);
        if (message.getReadTime() == 0 && message.isMySend()) {
            long readTime = analyzeTheTimeAfterBurning(message.getIsReadDelByInt());
            message.setReadTime(readTime);  //能阅读的时间
            if (message.getDeleteTime() == 0 || message.getDeleteTime() == -1) {
                message.setDeleteTime((System.currentTimeMillis() + readTime) / 1000); //将要删除的时间
            }
        } else if (message.getReadTime() == 0 && !message.isMySend()) {
            long readTime = analyzeTheTimeAfterBurning(message.getIsReadDelByInt());
            message.setReadTime(readTime);  //能阅读的时间
            if (message.getDeleteTime() == 0 || message.getDeleteTime() == -1) {
                message.setDeleteTime((System.currentTimeMillis() + readTime) / 1000); //将要删除的时间
            }
        }
        mChatMessages.put(key, readDelInfo);
        Log.i(TAG, "添加消息成功");
    }


    public void release() {
        if (readDelManager != null) {
            readDelManager = null;
        }
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        if (mChatMessages != null) {
            //每次释放的时候 存储一下  没有到删除到数据的 消息
            Set<String> keySet = mChatMessages.keySet();
            Iterator<String> iterator = keySet.iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                if (!TextUtils.isEmpty(key)) {
                    ReadDelInfo readDelInfo = mChatMessages.get(key);
                    ChatMessage message = readDelInfo.getChatMessage();
                    if (message.isMySend()) {
                        ChatMessageDao.getInstance().updateMessageReadTime(CoreManager.requireSelf(MyApplication.getInstance()).getUserId(), message.getToUserId(), message.getPacketId(), message.getReadTime(), message.getDeleteTime());
                    } else {
                        ChatMessageDao.getInstance().updateMessageReadTime(CoreManager.requireSelf(MyApplication.getInstance()).getUserId(), message.getFromUserId(), message.getPacketId(), message.getReadTime(), message.getDeleteTime());
                    }
                }
            }
            mChatMessages.clear();
            mChatMessages = null;
        }
        if (mStrings != null) {
            mStrings.clear();
            mStrings = null;
        }
    }


    /**
     * 检测别人发送的阅后即焚 的时间 是否 和自己设置的不一样
     *
     * @param message
     * @param loginUserId
     * @param friend
     * @param isReadDelByMe 我的阅后即焚时间
     * @return 返回-1 一样或者没有开启阅后即焚  返回其他数表示阅后即焚时间
     */
    public int checkReadDelTimeForMe(Context context, ChatMessage message, String loginUserId, Friend friend, int isReadDelByMe) {
        if (message.getIsReadDel()) {
            int isReadDelByFriend = message.getIsReadDelByInt();
            if (isReadDelByMe != isReadDelByFriend) {
                PreferenceUtils.putInt(context, Constants.MESSAGE_READ_FIRE + friend.getUserId() + loginUserId, isReadDelByFriend);
                Log.i(TAG, "更改了 阅后即焚的时间");
                return isReadDelByFriend;
            } else {
                return -1;
            }
        } else {
            int isReadDelByFriend = message.getIsReadDelByInt();
            if (isReadDelByFriend != isReadDelByMe) {
                PreferenceUtils.putInt(context, Constants.MESSAGE_READ_FIRE + friend.getUserId() + loginUserId, isReadDelByFriend);
                Log.i(TAG, "更改了 阅后即焚的时间");
                return 0;
            } else {
                PreferenceUtils.putInt(context, Constants.MESSAGE_READ_FIRE + friend.getUserId() + loginUserId, isReadDelByFriend);
                return -1;
            }

        }

    }

    public class ReadDelInfo {
        private ChatMessage mChatMessage;
        private AChatHolderInterface mAChatHolderInterface;

        public ReadDelInfo(ChatMessage chatMessage, AChatHolderInterface AChatHolderInterface) {
            mChatMessage = chatMessage;
            mAChatHolderInterface = AChatHolderInterface;
        }

        public ChatMessage getChatMessage() {
            return mChatMessage;
        }

        public void setChatMessage(ChatMessage chatMessage) {
            mChatMessage = chatMessage;
        }

        public AChatHolderInterface getAChatHolderInterface() {
            return mAChatHolderInterface;
        }

        public void setAChatHolderInterface(AChatHolderInterface AChatHolderInterface) {
            mAChatHolderInterface = AChatHolderInterface;
        }
    }

    public static long analyzeTheTimeAfterBurning(int status) {
        switch (status) {
            case 1:
                return 5 * 1000;
            case 2:
                return 10 * 1000;
            case 3:
                return 30 * 1000;
            case 4:
                return 60 * 1000;
            case 5:
                return 5 * 60 * 1000;
            case 6:
                return 30 * 60 * 1000;
            case 7:
                return 60 * 60 * 1000;
            case 8:
                return 6 * 60 * 60 * 1000;
            case 9:
                return 12 * 60 * 60 * 1000;
            case 10:
                return 24 * 60 * 60 * 1000;
            case 11:
                return 7 * 24 * 60 * 60 * 1000;
            default:
                return 0;
        }
    }


    public void setListener(ReadDelListener listener) {
        this.listener = listener;
    }


    public interface ReadDelListener {

        void downOver(ReadDelInfo info);
    }


}
