package com.iimm.miliao.call;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToolUtils;
import com.iimm.miliao.xmpp.ListenerManager;
import com.iimm.miliao.xmpp.util.ImHelper;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

import static com.iimm.miliao.R.id.call_answer;
import static com.iimm.miliao.R.id.call_avatar;
import static com.iimm.miliao.R.id.call_hang_up;
import static com.iimm.miliao.R.id.call_invite_type;
import static com.iimm.miliao.R.id.call_name;

/**
 * 来电显示
 */
public class JitsiIncomingcall extends BaseActivity implements View.OnClickListener {
    Timer timer = new Timer();
    private String mLoginUserId;
    private String mLoginUserName;
    private int mCallType;
    private String call_fromUser;
    private String call_toUser;
    private String call_Name;
    private String meetUrl;

    private AssetFileDescriptor mAssetFileDescriptor;
    private MediaPlayer mediaPlayer;
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    abort();
                    if (mCallType == 1 || mCallType == 2) {//  来电界面显示 三十秒内 不主动响应  发送挂断消息给对方
                        sendHangUpMessage();
                    }
                    JitsistateMachine.reset();
                    finish();
                }
            });
        }
    };
    private ImageView mInviteAvatar;
    private TextView mInviteName;
    private TextView mInviteInfo;
    private ImageButton mAnswer; // 接听
    private ImageButton mHangUp; // 挂断
    private boolean isAllowBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 自动解锁屏幕 | 锁屏也可显示 | Activity启动时点亮屏幕 | 保持屏幕常亮
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.view_call_trying);
        initData();
        initView();
        timer.schedule(timerTask, 30000, 30000);
        EventBus.getDefault().register(this);
    }

    private void initData() {
        mLoginUserId = coreManager.getSelf().getUserId();
        mLoginUserName = coreManager.getSelf().getNickName();

        mCallType = getIntent().getIntExtra(CallConstants.AUDIO_OR_VIDEO_OR_MEET, 0);
        call_fromUser = getIntent().getStringExtra("fromuserid");
        call_toUser = getIntent().getStringExtra("touserid");
        call_Name = getIntent().getStringExtra("name");
        meetUrl = getIntent().getStringExtra("meetUrl");

        JitsistateMachine.isInCalling = true;
        JitsistateMachine.callingOpposite = call_toUser;

        bell();
    }

    private void initView() {
        mInviteAvatar = (ImageView) findViewById(call_avatar);
        mInviteName = (TextView) findViewById(call_name);
        mInviteInfo = (TextView) findViewById(call_invite_type);
        mAnswer = (ImageButton) findViewById(call_answer);
        mHangUp = (ImageButton) findViewById(call_hang_up);
        AvatarHelper.getInstance().displayAvatar(call_toUser, mInviteAvatar, true);
        mInviteName.setText(call_Name);
        if (mCallType == 1) {
            mInviteInfo.setText(getString(R.string.suffix_invite_you_voice));
        } else if (mCallType == 2) {
            mInviteInfo.setText(getString(R.string.suffix_invite_you_video));
        } else if (mCallType == 3) {
            mInviteInfo.setText(getString(R.string.tip_invite_voice_meeting));
        } else if (mCallType == 4) {
            mInviteInfo.setText(getString(R.string.tip_invite_video_meeting));
        }
        mAnswer.setOnClickListener(this);
        mHangUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.call_answer:// 接听
                abort();
                if (ImHelper.checkXmppAuthenticated()) {
                    if (mCallType == 1 || mCallType == 2) {// 通话 接听 告诉 对方，会议不需要
                        sendAnswerMessage();
                    }
                    Intent intent = new Intent(this, Jitsi_connecting_second.class);
                    if (mCallType == 1) {
                        intent.putExtra("type", 1);
                    } else if (mCallType == 2) {
                        intent.putExtra("type", 2);
                    } else if (mCallType == 3) {
                        intent.putExtra("type", 3);
                    } else if (mCallType == 4) {
                        intent.putExtra("type", 4);
                    }
                    intent.putExtra("fromuserid", call_fromUser);
                    intent.putExtra("touserid", call_toUser);
                    if (!TextUtils.isEmpty(meetUrl)) {
                        intent.putExtra("meetUrl", meetUrl);
                    }
                    startActivity(intent);
                    finish();
                }
                break;
            case R.id.call_hang_up:// 拒绝
                abort();
                if (ImHelper.checkXmppAuthenticated()) {
                    if (mCallType == 1 || mCallType == 2) {// 通话 拒绝 告诉 对方，会议不需要
                        sendHangUpMessage();
                    }
                }
                JitsistateMachine.reset();
                finish();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageHangUpPhone message) {// 对方取消了 || 其他端 (接听 || 取消)了
        if (message.chatMessage.getFromUserId().equals(call_toUser)
                || message.chatMessage.getFromUserId().equals(mLoginUserId)) {
            abort();
            JitsistateMachine.reset();
            finish();
            /*if (isSipback) {// 当app处于关闭状态收到来电，通话结束后终止程序
                ActivityStack.getInstance().exit();
                android.os.Process.killProcess(android.os.Process.myPid());
            }*/
        }
    }

    private void sendAnswerMessage() {
        ChatMessage message = new ChatMessage();
        if (mCallType == 1) {
            message.setType(Constants.TYPE_CONNECT_VOICE);
        } else if (mCallType == 2) {
            message.setType(Constants.TYPE_CONNECT_VIDEO);
        }
        message.setContent("");
        message.setFromUserId(mLoginUserId);
        message.setToUserId(call_toUser);
        message.setPacketId(ToolUtils.getUUID());
        message.setFromUserName(mLoginUserName);
        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
        ImHelper.sendChatMessage(call_toUser, message);
    }

    private void sendHangUpMessage() {
        ChatMessage message = new ChatMessage();
        if (mCallType == 1) {
            message.setType(Constants.TYPE_NO_CONNECT_VOICE);
        } else if (mCallType == 2) {
            message.setType(Constants.TYPE_NO_CONNECT_VIDEO);
        }
        message.setMySend(true);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginUserName);
        message.setToUserId(call_toUser);
        message.setPacketId(ToolUtils.getUUID());
        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, call_toUser, message)) {
            ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, call_toUser, message, false);
        }

        ImHelper.sendChatMessage(call_toUser, message);
        MsgBroadcast.broadcastMsgUiUpdate(this);  // 更新消息界面
    }

    private void bell() {
        try {
            mAssetFileDescriptor = getAssets().openFd("dial.mp3");
            mediaPlayer = new MediaPlayer();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(mAssetFileDescriptor.getFileDescriptor(), mAssetFileDescriptor.getStartOffset(), mAssetFileDescriptor.getLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer arg0) {
                    mediaPlayer.start();
                    mediaPlayer.setLooping(true);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void abort() {
        if (timer != null) {
            timer.cancel();
        }
        try {
            mediaPlayer.stop();
        } catch (Exception e) {
            // 在华为手机上疯狂点击挂断按钮会出现崩溃的情况
            Reporter.post("停止铃声出异常，", e);
        }
        mediaPlayer.release();
    }

    @Override
    public void onBackPressed() {
        if (isAllowBack) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mAssetFileDescriptor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventBus.getDefault().unregister(this);
    }
}
