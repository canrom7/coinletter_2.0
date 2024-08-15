package com.iimm.miliao.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.iimm.miliao.AppConstant;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.audio.NoticeVoicePlayer;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.call.JitsistateMachine;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.ui.MainActivity;
import com.iimm.miliao.ui.message.ChatActivity;
import com.iimm.miliao.ui.message.MucChatActivity;
import com.iimm.miliao.xmpp.util.ImHelper;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * @Author: wangqx
 * @Date: 2019/8/2
 * @Description:描述
 */
public class NotificationUtils {
    public static String TAG = "NotificationUtils";
    /**
     * 本地 发送 通知 至 通知栏
     */
    public static int notifyId = 666;

    /*
    发送本地通知
     */
    public static void notificationMessage(ChatMessage chatMessage, boolean isGroupChat) {
        boolean isAppForeground = AppUtils.isAppForeground(MyApplication.getInstance());
        Log.e(TAG, "notificationMessage() called with: chatMessage = [" + chatMessage.getContent() + "], isGroupChat = [" + isGroupChat + "], isAppForeground = [" + isAppForeground + "]");

        boolean isReturned = true;
        if (JitsistateMachine.isInCalling && (chatMessage.getType() == Constants.TYPE_IS_CONNECT_VOICE
                || chatMessage.getType() == Constants.TYPE_IS_CONNECT_VIDEO
                || chatMessage.getType() == Constants.TYPE_IS_MU_CONNECT_VOICE
                || chatMessage.getType() == Constants.TYPE_IS_MU_CONNECT_VIDEO)) {
            // 正在通话中并且收到音视频邀请消息 强制通知
            isReturned = false;
        }
        if (isAppForeground && isReturned) {// 在前台 不通知
            return;
        }
        String title;
        String content;
        String loginUserId = MyApplication.getLoginUserId();
        boolean isSpecialMsg = false;// 特殊消息 跳转至主界面 而非聊天界面
        int messageType = chatMessage.getType();
        switch (messageType) {
            case Constants.TYPE_SAYHELLO:// 打招呼
                isSpecialMsg = true;
                break;
            case Constants.TYPE_PASS:    // 同意加好友
                isSpecialMsg = true;
                break;
            case Constants.TYPE_FRIEND:  // 直接成为好友
                isSpecialMsg = true;
                break;

            case Constants.DIANZAN:// 朋友圈点赞
                isSpecialMsg = true;
                break;
            case Constants.PINGLUN:    // 朋友圈评论
                isSpecialMsg = true;
                break;
            case Constants.ATMESEE:  // 朋友圈提醒我看
                isSpecialMsg = true;
                break;
            default:// 其他消息类型不通知
                break;
        }
        content = ImHelper.getShowContent(chatMessage);
        if (TextUtils.isEmpty(content)) {
            return;
        }
        NotificationManager mNotificationManager = (NotificationManager) MyApplication.getInstance()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    MyApplication.getInstance().getPackageName(),
                    getString(R.string.message_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            // 关闭通知铃声，我们有自己播放，
            channel.setSound(null, null);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder = new NotificationCompat.Builder(MyApplication.getInstance(), channel.getId());
        } else {
            //noinspection deprecation
            mBuilder = new NotificationCompat.Builder(MyApplication.getInstance());
        }
        String id;
        PendingIntent pendingIntent;
        if (isSpecialMsg) {
            title = chatMessage.getFromUserName();
            content = chatMessage.getFromUserName() + content;
            pendingIntent = pendingIntentForSpecial();
        } else {
            if (isGroupChat) {
                id = chatMessage.getToUserId();
                content = chatMessage.getFromUserName() + "：" + content;// 群组消息通知需要带上消息发送方的名字
            } else {
                id = chatMessage.getFromUserId();
            }
            Friend friend = FriendDao.getInstance().getFriend(loginUserId, id);
            if (friend != null) {
                title = TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName();
            } else {
                title = chatMessage.getFromUserName();
            }
            if (isGroupChat) {
                pendingIntent = pendingIntentForMuc(friend);
            } else {
                pendingIntent = pendingIntentForSingle(friend);
            }
        }
        if (pendingIntent == null) {
            return;
        }
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setContentTitle(title) // 通知标题
                .setContentText(content)  // 通知内容
                .setTicker(getString(R.string.tip_new_message))
                .setWhen(System.currentTimeMillis()) // 通知时间
                .setPriority(Notification.PRIORITY_HIGH) // 通知优先级
                .setAutoCancel(true)// 当用户单击面板就可以让通知自动取消
                .setOngoing(false)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setSmallIcon(R.mipmap.icon); // 通知icon
        Notification n = mBuilder.build();
        int numMessage = FriendDao.getInstance().getMsgUnReadNumTotal(loginUserId);
        // 先通知后保存的数据库，所以数据库里读出来的未读消息数要加1，
        ShortcutBadger.applyNotification(MyApplication.getInstance(), n, numMessage);
        mNotificationManager.notify(chatMessage.getFromUserId(), notifyId, n);
        if (isSpecialMsg) {// 特殊消息响铃通知
            NoticeVoicePlayer.getInstance().start();
        }
    }

    public static String getString(@StringRes int resId) {
        return MyApplication.getInstance().getResources().getString(resId);
    }

    /**
     * <跳到单人聊天界面>
     */
    public static PendingIntent pendingIntentForSingle(Friend friend) {
        Intent intent;
        if (friend != null) {
            intent = new Intent(MyApplication.getInstance(), ChatActivity.class);
            intent.putExtra(AppConstant.EXTRA_FRIEND, friend);
        } else {
            intent = new Intent(MyApplication.getInstance(), MainActivity.class);
        }
        intent.putExtra(Constants.IS_NOTIFICATION_BAR_COMING, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(MyApplication.getInstance(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    /**
     * <跳到群组聊天界面>
     */
    public static PendingIntent pendingIntentForMuc(Friend friend) {
        Intent intent;
        if (friend != null) {
            intent = new Intent(MyApplication.getInstance(), MucChatActivity.class);
            intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
            intent.putExtra(AppConstant.EXTRA_NICK_NAME, friend.getNickName());
        } else {
            intent = new Intent(MyApplication.getInstance(), MainActivity.class);
        }
        intent.putExtra(Constants.IS_NOTIFICATION_BAR_COMING, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(MyApplication.getInstance(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    /**
     * <跳到主界面>
     */
    public static PendingIntent pendingIntentForSpecial() {
        Intent intent = new Intent(MyApplication.getInstance(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(MyApplication.getInstance(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }
}
