package com.iimm.miliao.call;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.VideoFile;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.VideoFileDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.AppUtils;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.HttpUtil;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToolUtils;
import com.iimm.miliao.util.permission.PermissionDialog;
import com.iimm.miliao.view.TipDialog;
import com.iimm.miliao.xmpp.util.ImHelper;

import org.jitsi.meet.sdk.JitsiMeetView;
import org.jitsi.meet.sdk.JitsiMeetViewListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

import static com.iimm.miliao.AppConfig.DEBUG;

/**
 * 2018-2-27 录屏，保存至本地视频
 */
public class Jitsi_connecting_second extends BaseActivity implements SensorEventListener {
    // 屏幕录制
    private static final int RECORD_REQUEST_CODE = 0x01;
    // 计时，给悬浮窗调用
    public static String time = null;
    private String mLocalHostJitsi = "https://meet.jit.si/";// 官网地址
    private String mLocalHost = "https://meet.ttechworld.com/";  // 本地地址,现改为变量
    // 通话类型(单人语音、单人视频、群组语音、群组视频)
    private int mCallType;
    private String fromUserId;
    private String toUserId;
    private long startTime = System.currentTimeMillis();// 通话开始时间
    private long stopTime; // 通话结束时间
    private FrameLayout mFrameLayout;
    private JitsiMeetView mJitsiMeetView;
    // 悬浮窗按钮
    private ImageView mFloatingView;
    // 录屏
    private LinearLayout mRecordLL;
    private ImageView mRecordIv;
    private TextView mRecordTv;
    // 当用户手动锁屏时，结束当前通话
    private ScreenListener mScreenListener;
    // 标记当前手机版本是否为android 5.0,且为对方挂断
    private boolean isApi21HangUp;
    // private MediaProjection mediaProjection;
    private RecordService recordService;
    private SensorManager sensorManager;
    private Sensor sensor;
    private AudioManager audioManager;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock wakeLock;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            RecordService.RecordBinder binder = (RecordService.RecordBinder) service;
            recordService = binder.getRecordService();
            recordService.setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("mm:ss");
    CountDownTimer mCountDownTimer = new CountDownTimer(18000000, 1000) {// 开始计时，用于显示在悬浮窗上，且每隔一秒发送一个广播更新悬浮窗
        @Override
        public void onTick(long millisUntilFinished) {
            time = formatTime();
            Jitsi_connecting_second.this.sendBroadcast(new Intent(CallConstants.REFRESH_FLOATING));
        }

        @Override
        public void onFinish() {// 12小时进入Finish

        }
    };
    private boolean isOldVersion = true;// 是否为老版本，如果一次 "通话中" 消息都没有收到，就判断对方使用的为老版本，自己也停止ping且不做检测
    private boolean isEndCallOpposite;// 对方是否结束了通话
    private int mPingReceiveFailCount;// 未收到对方发送 "通话中" 消息的次数
    // 每隔3秒给对方发送一条 "通话中" 消息
    CountDownTimer mCallingCountDownTimer = new CountDownTimer(3000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {// 计时结束
            if (!HttpUtil.isGprsOrWifiConnected(Jitsi_connecting_second.this)) {
                TipDialog tipDialog = new TipDialog(Jitsi_connecting_second.this);
                tipDialog.setmConfirmOnClickListener(getString(R.string.check_network), () -> {
                    finish();
                });
                tipDialog.show();
                return;
            }
            if (mCallType == 1 || mCallType == 2) {// 单人音视频通话
                if (isEndCallOpposite) {// 未收到对方发送的 "通话中" 消息
                    // 考虑到弱网情况，当Count等于3时才真正认为对方已经结束了通话，否则继续发送 "通话中" 消息且count+1
                    if (mPingReceiveFailCount == 10) {
                        if (isOldVersion) {
                            return;
                        }
                        Log.e(TAG, "true-->" + TimeUtils.time_current_time());
                        if (!isDestroyed()) {
                            stopTime = System.currentTimeMillis();
                            overCall((int) (stopTime - startTime) / 1000);
                            Toast.makeText(Jitsi_connecting_second.this, getString(R.string.tip_opposite_offline_auto__end_call), Toast.LENGTH_SHORT).show();
                            finish();
/*
                            TipDialog tipDialog = new TipDialog(Jitsi_connecting_second.this);
                            tipDialog.setmConfirmOnClickListener(getString(R.string.tip_opposite_offline_end_call), () -> {
                                stopTime = System.currentTimeMillis();
                                overCall((int) (stopTime - startTime) / 1000);
                                finish();
                            });
                            tipDialog.show();
*/
                        }
                    } else {
                        mPingReceiveFailCount++;
                        Log.e(TAG, "true-->" + mPingReceiveFailCount + "，" + TimeUtils.time_current_time());
                        sendCallingMessage();
                    }
                } else {
                    Log.e(TAG, "false-->" + TimeUtils.time_current_time());
                    sendCallingMessage();
                }
            }
        }
    };

    public static void start(Context ctx, String room, boolean isVideo) {
        Intent intent = new Intent(ctx, Jitsi_connecting_second.class);
        if (isVideo) {
            intent.putExtra("type", 2);
        } else {
            intent.putExtra("type", 1);
        }
        intent.putExtra("fromuserid", room);
        intent.putExtra("touserid", room);
        ctx.startActivity(intent);
    }

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

        setContentView(R.layout.jitsiconnecting);
        initData();
        initView();
        initEvent();
        EventBus.getDefault().register(this);
        JitsiMeetView.onHostResume(this);
    }

    @Override
    public void onCoreReady() {
        super.onCoreReady();
        sendCallingMessage();// 对方可能一进入就已经挂掉了，我们就会误判对方未老版本，所以一进入就发送一条 "通话中" 消息给对方
    }

    private void initData() {
        mCallType = getIntent().getIntExtra("type", 0);
        fromUserId = getIntent().getStringExtra("fromuserid");
        toUserId = getIntent().getStringExtra("touserid");

        JitsistateMachine.isInCalling = true;
        JitsistateMachine.callingOpposite = toUserId;

        if (mCallType == 1 || mCallType == 2) {// 集群
            mLocalHost = getIntent().getStringExtra("meetUrl");
            if (TextUtils.isEmpty(mLocalHost)) {
                mLocalHost = coreManager.getConfig().JitsiServer;
            }
        } else {
            mLocalHost = coreManager.getConfig().JitsiServer;
        }

        if (TextUtils.isEmpty(mLocalHost)) {
            DialogHelper.tip(mContext, getString(R.string.tip_meet_server_empty));
            finish();
        }

        // mCallingCountDownTimer.start();
    }

    /**
     * startWithAudioMuted:是否禁用语音
     * startWithVideoMuted:是否禁用录像
     */
    private void initView() {
        mFrameLayout = (FrameLayout) findViewById(R.id.jitsi_view);
        mJitsiMeetView = new JitsiMeetView(this);
        mFrameLayout.addView(mJitsiMeetView);

        mFloatingView = (ImageView) findViewById(R.id.open_floating);

        mRecordLL = (LinearLayout) findViewById(R.id.record_ll);
        mRecordIv = (ImageView) findViewById(R.id.record_iv);
        mRecordTv = (TextView) findViewById(R.id.record_tv);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {// 5.0以下录屏需要root，不考虑
            Intent intent = new Intent(this, RecordService.class);
            bindService(intent, connection, BIND_AUTO_CREATE);
            mRecordLL.setVisibility(View.VISIBLE);
        }
        // TODO 暂时关闭录屏功能
        mRecordLL.setVisibility(View.GONE);

        // 配置房间参数
        Bundle urlObject = new Bundle();
        Bundle config = new Bundle();
        if (mCallType == 1 || mCallType == 3) {
            config.putBoolean("startWithAudioMuted", false);
            config.putBoolean("startWithVideoMuted", true);
        } else if (mCallType == 2 || mCallType == 4) {
            config.putBoolean("startWithAudioMuted", false);
            config.putBoolean("startWithVideoMuted", true);
        }
        urlObject.putBundle("config", config);
        if (mCallType == 3) {// 群组语音添加标识，防止和群组视频进入同一房间地址
            urlObject.putString("url", mLocalHost + "/audio" + fromUserId);
        } else {
            urlObject.putString("url", mLocalHost + fromUserId);
        }
        mJitsiMeetView.setAvatarURL(AvatarHelper.getAvatarUrl(coreManager.getSelf().getUserId(), false));
        // 开始加载
        mJitsiMeetView.loadURLObject(urlObject);
    }

    private void initEvent() {
        ImageView iv = findViewById(R.id.ysq_iv);
        Friend friend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), fromUserId);
        if (friend != null && friend.getRoomFlag() != 0) {
            iv.setVisibility(View.VISIBLE);
            // 群组会议，可邀请其他群成员
            iv.setOnClickListener(v -> {
                Intent intent = new Intent(Jitsi_connecting_second.this, JitsiInviteActivity.class);
                intent.putExtra(Constants.IS_AUDIO_CONFERENCE, mCallType == 3);
                intent.putExtra("voicejid", fromUserId);
                startActivity(intent);
            });
        }

        mJitsiMeetView.setListener(new JitsiMeetViewListener() {

            @Override
            public void onLoadConfigError(Map<String, Object> map) { // 加载配置时错误
                Log.e("jitsi", "1");
            }

            @Override
            public void onConferenceFailed(Map<String, Object> map) {// 会议失败
                Log.e(TAG, "2");
                finish();
            }

            @Override
            public void onConferenceWillJoin(Map<String, Object> map) {
                Log.e("jitsi", "即将加入会议");
            }

            @Override
            public void onConferenceJoined(Map<String, Object> map) {
                Log.e(TAG, "已加入会议，显示悬浮窗按钮，开始计时");
                // 如果将runOnUiThread放在onConferenceWillJoin内，底部会闪现一条白边，偶尔白边还不会消失
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mFloatingView.setVisibility(View.VISIBLE);
                    }
                });
                // 会议开始，记录开始时间
                startTime = System.currentTimeMillis();
                // 开始计时
                mCountDownTimer.start();
                if (Constants.SUPPORT_VOICE_CALLS_SWICH) {
                    audioManager = (AudioManager)Jitsi_connecting_second. this.getSystemService(Context.AUDIO_SERVICE);
                    mPowerManager = (PowerManager) Jitsi_connecting_second.this.getSystemService(POWER_SERVICE);
                    registerProximitySensorListener();
                }
            }

            @Override
            public void onConferenceWillLeave(Map<String, Object> map) {
                Log.e(TAG, "5");
                // jitsi挂断可能需要一两秒的时间，
                DialogHelper.showMessageProgressDialog(Jitsi_connecting_second.this, getString(R.string.tip_handing_up));
                // 即将离开会议
                if (!isApi21HangUp) {
                    stopTime = System.currentTimeMillis();
                    overCall((int) (stopTime - startTime) / 1000);
                }
            }

            @Override
            public void onConferenceLeft(Map<String, Object> map) {
                Log.e(TAG, "6");
                DialogHelper.dismissProgressDialog();
                Jitsi_connecting_second.this.sendBroadcast(new Intent(CallConstants.CLOSE_FLOATING));
                finish();
            }
        });

        mScreenListener = new ScreenListener(this);
        mScreenListener.begin(new ScreenListener.ScreenStateListener() {
            @Override
            public void onScreenOn() {
            }

            @Override
            public void onScreenOff() {// 屏幕已锁定
                stopTime = System.currentTimeMillis();
                overCall((int) (stopTime - startTime) / 1000);
                finish();
            }

            @Override
            public void onUserPresent() {
            }
        });

        mFloatingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppUtils.checkAlertWindowsPermission(Jitsi_connecting_second.this)) { // 已开启悬浮窗权限
                    // nonRoot = false→ 仅当activity为task根（即首个activity例如启动activity之类的）时才生效
                    // nonRoot = true → 忽略上面的限制
                    // 这个方法不会改变task中的activity中的顺序，效果基本等同于home键
                    moveTaskToBack(true);
                    // 开启悬浮窗
                    Intent intent = new Intent(getApplicationContext(), JitsiFloatService.class);
                    startService(intent);
                } else { // 未开启悬浮窗权限
                    hideBottomUIMenu();
                    PermissionDialog.show(Jitsi_connecting_second.this, null, getString(R.string.av_no_float), null, null);
                }
            }
        });

        mRecordLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
/*
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (recordService.isRunning()) {
                        if (recordService.stopRecord()) {
                            mRecordIv.setImageResource(R.drawable.recording);
                            mRecordTv.setText(getString(R.string.screen_record));
                            saveScreenRecordFile();// 将录制的视频保存至本地
                        }
                    } else {
                        // 申请屏幕录制
                        MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
                        if (projectionManager != null) {
                            Intent captureIntent = projectionManager.createScreenCaptureIntent();
                            startActivityForResult(captureIntent, RECORD_REQUEST_CODE);
                        }
                    }
                }
*/
            }
        });
    }

    /**
     * 注册距离感应器监听器，监测用户是否靠近手机听筒
     */
    private void registerProximitySensorListener() {

        if (sensorManager == null) {
            sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        }
        if (sensorManager == null) {
            return;
        }
        if (sensor == null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
/*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
                if (projectionManager != null) {
                    mediaProjection = projectionManager.getMediaProjection(resultCode, data);
                    recordService.setMediaProject(mediaProjection);
                    // 开始录制
                    recordService.startRecord();

                    mRecordIv.setImageResource(R.drawable.stoped);
                    mRecordTv.setText(getString(R.string.stop));
                }
            }
*/
        }
    }

    public void sendCallingMessage() {
        isEndCallOpposite = true;

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(Constants.TYPE_IN_CALLING);

        chatMessage.setFromUserId(coreManager.getSelf().getUserId());
        chatMessage.setFromUserName(coreManager.getSelf().getNickName());
        chatMessage.setToUserId(toUserId);
        chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
        chatMessage.setPacketId(ToolUtils.getUUID());
        ImHelper.sendChatMessage(toUserId, chatMessage);

        mCallingCountDownTimer.start();// 重新开始计时
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageCallingEvent message) {
        if (message.chatMessage.getType() == Constants.TYPE_IN_CALLING) {
            if (message.chatMessage.getFromUserId().equals(toUserId)) {
                isOldVersion = false;
                // 收到 "通话中" 的消息，且该消息为当前通话对象发送过来的
                Log.e(TAG, "MessageCallingEvent-->" + TimeUtils.time_current_time());
                mPingReceiveFailCount = 0;// 将count置为0
                isEndCallOpposite = false;
            }
        }
    }

    // 对方挂断
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageHangUpPhone message) {
        if (message.chatMessage.getFromUserId().equals(fromUserId)
                || message.chatMessage.getFromUserId().equals(toUserId)) {// 挂断方为当前通话对象 否则不处理
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                isApi21HangUp = true;
                TipDialog tip = new TipDialog(Jitsi_connecting_second.this);
                tip.setmConfirmOnClickListener(getString(R.string.av_hand_hang), new TipDialog.ConfirmOnClickListener() {
                    @Override
                    public void confirm() {
                        hideBottomUIMenu();
                    }
                });
                tip.show();
                return;
            }

            // 关闭悬浮窗
            sendBroadcast(new Intent(CallConstants.CLOSE_FLOATING));
            finish();
        }
    }

    /*******************************************
     * Method
     ******************************************/
    // 发送挂断的XMPP消息
    private void overCall(int time) {
        if (mCallType == 1) {
            EventBus.getDefault().post(new MessageEventCancelOrHangUp(104, toUserId,
                    InternationalizationHelper.getString("JXSip_Canceled") + InternationalizationHelper.getString("JX_VoiceChat"),
                    time));
        } else if (mCallType == 2) {
            EventBus.getDefault().post(new MessageEventCancelOrHangUp(114, toUserId,
                    InternationalizationHelper.getString("JXSip_Canceled") + InternationalizationHelper.getString("JX_VideoChat"),
                    time));
        }
    }

    private String formatTime() {
        Date date = new Date(new Date().getTime() - startTime);
        return mSimpleDateFormat.format(date);
    }

    // 隐藏虚拟按键
    private void hideBottomUIMenu() {
        View v = this.getWindow().getDecorView();
        v.setSystemUiVisibility(View.GONE);
    }

    /*******************************************
     * 录屏，保存至本地视频
     ******************************************/
    public void saveScreenRecordFile() {
        // 录屏文件路径
        String imNewestScreenRecord = PreferenceUtils.getString(getApplicationContext(), "IMScreenRecord");
        File file = new File(imNewestScreenRecord);
        if (file.exists() && file.getName().trim().toLowerCase().endsWith(".mp4")) {
            VideoFile videoFile = new VideoFile();
            videoFile.setCreateTime(TimeUtils.f_long_2_str(getScreenRecordFileCreateTime(file.getName())));
            videoFile.setFileLength(getScreenRecordFileTimeLen(file.getPath()));
            videoFile.setFileSize(file.length());
            videoFile.setFilePath(file.getPath());
            videoFile.setOwnerId(coreManager.getSelf().getUserId());
            VideoFileDao.getInstance().addVideoFile(videoFile);
        }
    }

    private long getScreenRecordFileCreateTime(String srf) {
        int dot = srf.lastIndexOf('.');
        return Long.parseLong(srf.substring(0, dot));
    }

    private long getScreenRecordFileTimeLen(String srf) {
        long duration;
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(srf);
            player.prepare();
            duration = player.getDuration() / 1000;
        } catch (Exception e) {
            duration = 10;
            e.printStackTrace();
        }
        player.release();
        return duration;
    }

    /*******************************************
     * 生命周期
     ******************************************/
    @Override
    public void onBackPressed() {
        if (!JitsiMeetView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        JitsiMeetView.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (JitsistateMachine.isFloating) {
            sendBroadcast(new Intent(CallConstants.CLOSE_FLOATING));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // 释放摄像头，
        JitsiMeetView.onHostPause(this);
        JitsistateMachine.reset();

        mJitsiMeetView.dispose();
        mJitsiMeetView = null;
        JitsiMeetView.onHostDestroy(this);
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            sensorManager = null;
        }
        if (wakeLock != null) {
            wakeLock = null;
        }
        EventBus.getDefault().unregister(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (connection != null) {
                // 1.用户开启录屏之后未结束录屏就直接结束通话了，此时需要释放部分资源，否则下次录屏会引发崩溃
                // 2.对方结束通话
                if (recordService.isRunning()) {
                    recordService.stopRecord();
                    saveScreenRecordFile();
                }
                unbindService(connection);
            }
        }

        if (mScreenListener != null) {
            mScreenListener.unregisterListener();
        }
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        Log.e(TAG, "onDestory");
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (audioManager == null) {
            return;
        }
        if (isHeadphonesPlugged()) {
            // 如果耳机已插入，设置距离传感器失效
            return;
        }
        if (DEBUG) {
            Log.i(TAG, "onSensorChanged: event.values[0]: " + event.values[0] + "====" + sensor.getMaximumRange());
        }
        // 如果音频正在播放
        float distance = event.values[0];
        if (distance >= sensor.getMaximumRange()) {
            // 用户远离听筒，音频外放，亮屏
            changeToSpeaker();
            if (DEBUG) {
                Log.i(TAG, "onSensorChanged: 外放");
            }
        } else {
            // 用户贴近听筒，切换音频到听筒输出，并且熄屏防误触
            changeToReceiver();
            if (DEBUG) {
                Log.i(TAG, "onSensorChanged: 听筒");
            }
            audioManager.setSpeakerphoneOn(false);
        }
    }
    /**
     * 切换到外放
     */
    public void changeToSpeaker() {
        setScreenOn();
        if (audioManager == null) {
            return;
        }
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(true);
    }

    /**
     * 切换到听筒
     */
    public void changeToReceiver() {
        setScreenOff();
        if (audioManager == null) {
            return;
        }
        audioManager.setSpeakerphoneOn(false);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }


    private void setScreenOff() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.i(TAG, "setScreenOff: 熄灭屏幕");
            if (wakeLock == null) {
                wakeLock = mPowerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,TAG);
            }
            wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
        }
    }


    private void setScreenOn() {
        if (wakeLock != null) {
            wakeLock.setReferenceCounted(false);
            wakeLock.release();
            wakeLock = null;
        }
    }
    private boolean isHeadphonesPlugged() {
        if (audioManager == null) {
            return false;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            AudioDeviceInfo[] audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_ALL);
            for (AudioDeviceInfo deviceInfo : audioDevices) {
                if (deviceInfo.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                        || deviceInfo.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                    return true;
                }
            }
            return false;
        } else {
            return audioManager.isWiredHeadsetOn();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
