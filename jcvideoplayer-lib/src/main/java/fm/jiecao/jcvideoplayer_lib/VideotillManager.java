package fm.jiecao.jcvideoplayer_lib;

import android.util.Log;

/**
 * create by xuan 抖音视频管理器
 */
public class VideotillManager {
    private String TAG = "VideotillManager";
    private static VideotillManager mVideotillManager;
    private JCVideoViewbyXuan mVideo;

    private VideotillManager() {

    }

    public static VideotillManager instance() {
        if (mVideotillManager == null) {
            mVideotillManager = new VideotillManager();
        }
        return mVideotillManager;
    }

    public void addVideoPlay(JCVideoViewbyXuan video) {
        mVideo = video;
    }


    public void releaseVideo() {
        if (mVideo != null) {
            mVideo.reset();
            mVideo = null;
        }
    }

    public void play() {
        if (mVideo != null) {
            Log.e(TAG, "VideotillManager play: ");
            mVideo.play("");
        }
    }

    public void pause() {
        Log.e(TAG, "VideotillManager pause: ");
        if (mVideo != null && mVideo.isPlaying()) {
            mVideo.pause();
        }
    }


}
