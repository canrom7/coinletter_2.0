package com.iimm.miliao.audio_x;

import static com.iimm.miliao.audio_x.VoiceManager.STATE_PLAY;

/**
 * Created by xuan on 2017/9/13.
 * 聊天语音播放器
 * 播放功能 停止功能 从某个时间开始播放
 */

public class VoicePlayerCollection {

    private volatile static VoicePlayerCollection instance;
    VoiceAnimViewCollection mOldView;
    private VoiceManager.VoicePlayListener mListener;

    private VoicePlayerCollection() {
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

    public static VoicePlayerCollection instance() {
        if (instance == null) {
            synchronized (VoicePlayerCollection.class) {
                if (instance == null) {
                    instance = new VoicePlayerCollection();
                }
            }
        }
        return instance;
    }

    /**
     * 播放语音方法
     */
    public void playVoice(VoiceAnimViewCollection view) {

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

    public void changeVoice(VoiceAnimViewCollection view) {
        if (view != mOldView) {
            mOldView.stopAnim();
        }
        mOldView = view;
    }

    public void playSeek(int mesc, VoiceAnimViewCollection view) {
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
}
