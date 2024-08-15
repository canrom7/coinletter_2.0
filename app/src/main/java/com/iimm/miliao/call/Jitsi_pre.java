package com.iimm.miliao.call;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.iimm.miliao.R;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.Constants;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

import static com.iimm.miliao.AppConfig.DEBUG;

/**
 * 单聊 拨号界面
 */
public class Jitsi_pre extends BaseActivity implements SensorEventListener {
    Timer timer = new Timer();
    private String mLoginUserId;
    private boolean isAudio;
    private String call_toUser;
    private String call_toName;
    private String meetUrl;
    private AssetFileDescriptor mAssetFileDescriptor;
    private MediaPlayer mediaPlayer;
    TimerTask timerTask = new TimerTask() {//  单聊 拨号界面 三十秒内 对方未接听  发送挂断消息 结束当前页面
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    abort();
                    if (isAudio) {
                        EventBus.getDefault().post(new MessageEventCancelOrHangUp(103, call_toUser,
                                InternationalizationHelper.getString("JXSip_Canceled") + InternationalizationHelper.getString("JX_VoiceChat"), 0));
                    } else {
                        EventBus.getDefault().post(new MessageEventCancelOrHangUp(113, call_toUser,
                                InternationalizationHelper.getString("JXSip_Canceled") + InternationalizationHelper.getString("JX_VideoChat"), 0));
                    }
                    JitsistateMachine.reset();
                    finish();
                }
            });
        }
    };
    private ImageView mCallAvatar;
    private TextView mCallName;
    private ImageButton mHangUp;
    private boolean isAllowBack = false;
    private PowerManager.WakeLock wakeLock;
    private SensorManager sensorManager;
    private Sensor sensor;
    private AudioManager audioManager;
    private PowerManager mPowerManager;

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
        setContentView(R.layout.view_call_incall_false);
        initData();
        initView();
        timer.schedule(timerTask, 30000, 30000);// 开启计时器
        EventBus.getDefault().register(this);
    }

    private void initData() {
        mLoginUserId = coreManager.getSelf().getUserId();

        isAudio = getIntent().getBooleanExtra("isvoice", false);
        call_toUser = getIntent().getStringExtra("touserid");
        call_toName = getIntent().getStringExtra("username");
        meetUrl = getIntent().getStringExtra("meetUrl");

        JitsistateMachine.isInCalling = true;
        JitsistateMachine.callingOpposite = call_toUser;

        bell();// 响铃

        if (Constants.SUPPORT_VOICE_CALLS_SWICH) {
            audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            mPowerManager = (PowerManager) this.getSystemService(POWER_SERVICE);
            registerProximitySensorListener();
        }
    }

    private void initView() {
        mCallAvatar = (ImageView) findViewById(R.id.call_avatar);
        mCallName = (TextView) findViewById(R.id.call_name);
        mHangUp = (ImageButton) findViewById(R.id.call_hang_up);
        TextView wait_tv = (TextView) findViewById(R.id.call_wait);
        TextView hang_up_tv = (TextView) findViewById(R.id.call_hang_up_tv);
        wait_tv.setText(InternationalizationHelper.getString("AskCallVC_Wait"));
        hang_up_tv.setText(InternationalizationHelper.getString("JXMeeting_Hangup"));
        AvatarHelper.getInstance().displayAvatar(call_toUser, mCallAvatar, true);
        mCallName.setText(call_toName);
        mHangUp.setOnClickListener(new View.OnClickListener() {// 主动挂断
            @Override
            public void onClick(View view) {
                abort();
                if (isAudio) {
                    EventBus.getDefault().post(new MessageEventCancelOrHangUp(103, call_toUser,
                            InternationalizationHelper.getString("JXSip_Canceled") + InternationalizationHelper.getString("JX_VoiceChat"), 0));
                } else {
                    EventBus.getDefault().post(new MessageEventCancelOrHangUp(113, call_toUser,
                            InternationalizationHelper.getString("JXSip_Canceled") + InternationalizationHelper.getString("JX_VideoChat"), 0));
                }
                JitsistateMachine.reset();
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageCallingEvent message) {
        if (message.chatMessage.getType() == Constants.TYPE_IS_BUSY) {// 对方忙线
            if (message.chatMessage.getFromUserId().equals(call_toUser)) {
                Toast.makeText(this, R.string.tip_opposite_busy_call, Toast.LENGTH_SHORT).show();
                abort();
                JitsistateMachine.reset();
                finish();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventSipPreview message) {// 对方接听
        abort();
        Intent intent = new Intent(this, Jitsi_connecting_second.class);
        if (message.number == 200) {
            intent.putExtra("type", 1);
        } else if (message.number == 201) {
            intent.putExtra("type", 2);
        }
        intent.putExtra("fromuserid", mLoginUserId);
        intent.putExtra("touserid", call_toUser);
        if (!TextUtils.isEmpty(meetUrl)) {
            intent.putExtra("meetUrl", meetUrl);
        }
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.zoomin, R.anim.zoomout);// 淡入淡出动画
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageHangUpPhone message) {// 对方拒接
        if (message.chatMessage.getFromUserId().equals(call_toUser)) {
            abort();
            JitsistateMachine.reset();
            finish();
        }
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
        }
        mediaPlayer.release();
    }

    @Override
    public void onBackPressed() {
        if (isAllowBack) {
            super.onBackPressed();
        }
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
     * 切换到耳机模式
     */
    public void changeToHeadset() {
        if (audioManager == null) {
            return;
        }
        audioManager.setSpeakerphoneOn(false);
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
                wakeLock = mPowerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, TAG);
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
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            sensorManager = null;
        }
        if (wakeLock != null) {
            wakeLock = null;
        }
        try {
            mAssetFileDescriptor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        EventBus.getDefault().unregister(this);
    }
}
