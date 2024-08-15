package com.iimm.miliao.audio_x;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.iimm.miliao.util.Constants;

import java.lang.ref.WeakReference;

import static android.content.Context.POWER_SERVICE;
import static android.content.Context.SENSOR_SERVICE;
import static com.iimm.miliao.AppConfig.DEBUG;
import static com.iimm.miliao.audio_x.VoiceManager.STATE_PLAY;

/**
 * Created by xuan on 2017/9/13.
 * 聊天语音播放器
 * 播放功能 停止功能 从某个时间开始播放
 */

public class VoicePlayer implements SensorEventListener, LifecycleObserver {

    private final String TAG = "VoicePlayer";
    private volatile static VoicePlayer instance;
    VoiceAnimView mOldView;

    private WeakReference<AppCompatActivity> mActivity;
    private VoiceManager.VoicePlayListener mListener;
    private PowerManager.WakeLock wakeLock;
    private SensorManager sensorManager;
    private Sensor sensor;
    private AudioManager audioManager;
    private PowerManager mPowerManager;
    private boolean isRegistered = false;

    private VoicePlayer() {
        VoiceManager.instance().addVoicePlayListener(new VoiceManager.VoicePlayListener() {
            @Override
            public void onFinishPlay(String path) {
                if (mOldView != null) {
                    mOldView.stop();
                }
                if (mListener != null) {
                    mListener.onFinishPlay(path);
                }
            }

            @Override
            public void onStopPlay(String path) {
                if (mListener != null) {
                    mListener.onStopPlay(path);
                }
            }

            @Override
            public void onErrorPlay() {
                if (mOldView != null) {
                    mOldView.stop();
                }
            }
        });
    }

    public static VoicePlayer instance() {
        if (instance == null) {
            synchronized (VoicePlayer.class) {
                if (instance == null) {
                    instance = new VoicePlayer();
                }
            }
        }
        return instance;
    }

    private AppCompatActivity getActivity() {
        if (mActivity != null) {
            return mActivity.get();
        }
        return null;
    }

    /**
     * 播放语音方法
     */
    public void playVoice(VoiceAnimView view, Context context) {
        if (Constants.SUPPORT_VOICE_SWICH) {
            AppCompatActivity activity = (AppCompatActivity) context;
            mActivity = new WeakReference<>(activity);
            if (getActivity() != null) {
                //可以监听生命周期
                if (getActivity() != null) {
                    getActivity().getLifecycle().addObserver(this);
                }
                if (audioManager == null) {
                    audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
                }
                if (mPowerManager == null) {
                    mPowerManager = (PowerManager) getActivity().getSystemService(POWER_SERVICE);
                }
                registerProximitySensorListener();
            }
        }
        if (VoiceManager.instance().getState() == STATE_PLAY) { // 正在播放的时候
            if (mOldView != null) {
                if (mOldView == view) {
                    mOldView.stop();
                } else {
                    mOldView.stop();
                    mOldView = view;
                    view.start();
                }
            }
        } else {
            mOldView = view;
            view.start();
        }
    }

    public void changeVoice(VoiceAnimView view) {
        if (view != mOldView) {
            mOldView.stopAnim();
        }
        mOldView = view;
    }

    public void playSeek(int mesc, VoiceAnimView view) {
        if (VoiceManager.instance().getState() == STATE_PLAY) { // 正在播放的时候
            if (mOldView != null) {
                if (mOldView == view) { // 自己的 正在拖
                    VoiceManager.instance().seek(mesc * 1000);
                } else { // 正在拖别人的
                    mOldView.stop();
                    mOldView = view;
                    view.start();
                    VoiceManager.instance().seek(mesc * 1000);
                }
            }
        } else {
            mOldView = view;
            view.start();
            VoiceManager.instance().seek(mesc * 1000);
        }
    }

    public void stop() {
        if (VoiceManager.instance().getState() == STATE_PLAY) {
            if (mOldView != null) {
                mOldView.stop();
            } else {
                VoiceManager.instance().stop();
            }
        }
    }

    public String getVoiceMsgId() {// 获取到正在播放的msgId
        String msgId;
        if (mOldView != null) {
            msgId = mOldView.getVoiceMsgId();
        } else {
            msgId = "";
        }
        return msgId;
    }

    public void addVoicePlayListener(VoiceManager.VoicePlayListener listener) {
        mListener = listener;
    }


    /**
     * 注册距离感应器监听器，监测用户是否靠近手机听筒
     */
    private void registerProximitySensorListener() {
        if (getActivity() == null) {
            return;
        }
        if (sensorManager == null) {
            sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        }
        if (sensorManager == null) {
            return;
        }
        if (sensor == null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
        if (!isRegistered) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            isRegistered = true;
        }
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
            Log.i(TAG, "onSensorChanged: " + VoiceManager.instance().getState() + " event.values[0]: " + event.values[0] + "====" + sensor.getMaximumRange());
        }
        if (VoiceManager.instance().getState() == STATE_PLAY) {
            // 如果音频正在播放
            float distance = event.values[0];
            if (distance >= sensor.getMaximumRange()) {
                // 用户远离听筒，音频外放，亮屏
                changeToSpeaker();
                if (DEBUG) {
                    Log.i(TAG, "onSensorChanged: 外放");
                }
            } else {
                VoiceManager.instance().seek(0);
                if (mOldView != null) {
                    mOldView.stop();
                }
                // 用户贴近听筒，切换音频到听筒输出，并且熄屏防误触
                changeToReceiver();
                if (DEBUG) {
                    Log.i(TAG, "onSensorChanged: 听筒");
                }
                audioManager.setSpeakerphoneOn(false);
            }
        } else {
            // 音频播放完了
            changeToSpeaker();
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
        if (mOldView != null) {
            mOldView.start();
        }
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

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            sensorManager = null;
        }
        if (wakeLock != null) {
            wakeLock = null;
        }
        if (mActivity != null) {
            mActivity = null;
        }
        isRegistered = false;
        Log.i(TAG, "onDestroy");
    }

}
