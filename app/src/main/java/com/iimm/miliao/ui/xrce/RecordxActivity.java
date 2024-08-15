package com.iimm.miliao.ui.xrce;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.gyf.immersionbar.ImmersionBar;
import com.joe.camera2recorddemo.View.CameraRecordView;
import com.iimm.miliao.R;
import com.iimm.miliao.audio_x.VoiceManager;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.trill.MarqueTextView;
import com.iimm.miliao.util.RecorderUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.video.FilterPreviewDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import VideoHandle.EpEditor;
import VideoHandle.EpVideo;
import VideoHandle.OnEditorListener;

public class RecordxActivity extends BaseActivity implements View.OnClickListener {

    private static final int STATE_INIT = 0;
    private static final int STATE_RECORDING = 1;
    private static final int STATE_PAUSE = 2;
    BroadcastReceiver closeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
    private Xrecprogressbar xbar;
    private CameraRecordView mRecordView;
    private List<EpVideo> videos;
    private int mRecorderState;
    private String mCurrBgmPath;
    private String mCurrBgmName;
    private String mCurrPath;
    private SelectMusicDialog mSelectDialog;
    private FilterPreviewDialog mDialog;
    private RecordButton mRecordBtn;
    private RelativeLayout rlMore;
    FilterPreviewDialog.OnUpdateFilterListener listener = new FilterPreviewDialog.OnUpdateFilterListener() {
        @Override
        public void select(int type) {
            mRecordView.switchFilter(type);
        }

        @Override
        public void dismiss() {
            rlMore.setVisibility(View.VISIBLE);
            mRecordBtn.setVisibility(View.VISIBLE);
        }
    };
    private FrameLayout waitPar;
    private MarqueTextView tvBgName;
    private LinearLayout llMusic;
    private boolean isStop = false;
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            waitPar.setVisibility(View.GONE);
            switch (msg.what) {
                case RecorderUtils.ACTIVATE_BTN: // 激活按钮
                    mRecordBtn.setEnabled(true);
                    break;
                case RecorderUtils.MUSIC_SUCCESS: // 音乐拼合成功
                    intentPreview(mCurrPath);
                    break;
                case RecorderUtils.MERGE_FAILURE: // 视频合并失败
                    showToast(getString(R.string.flatten_failure));
                    break;
            }
            return false;
        }
    });

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recx);
        ImmersionBar.with(this).fitsSystemWindows(true).init();
        xbar = findViewById(R.id.xpbar);
        mRecordView = findViewById(R.id.surfaceView);
        rlMore = findViewById(R.id.rl_more);
        mRecordBtn = findViewById(R.id.btn_rec);
        waitPar = findViewById(R.id.progress_ing);
        tvBgName = findViewById(R.id.tv_bgname);
        llMusic = findViewById(R.id.ll_select_music);

        findViewById(R.id.btn_rec).setOnClickListener(this);
        findViewById(R.id.ll_filter).setOnClickListener(this);
        findViewById(R.id.ll_swith).setOnClickListener(this);
        findViewById(R.id.iv_comp).setOnClickListener(this);
        findViewById(R.id.iv_del).setOnClickListener(this);
        findViewById(R.id.ll_back).setOnClickListener(this);
        findViewById(R.id.beauty).setOnClickListener(this);
        llMusic.setOnClickListener(this);
        waitPar.setOnClickListener(this);

        videos = new ArrayList<>();
        mDialog = new FilterPreviewDialog(this, listener);
        xbar.addOnComptListener(() -> {
            Log.e(TAG, "onCompte: ");
            stopRecord();
            mRecorderState = STATE_INIT;
            refreshControlUI();
            compteRecord();
        });

        mSelectDialog = new SelectMusicDialog(this, info -> {
            mCurrBgmPath = info.path;
            mCurrBgmName = info.getName();
            tvBgName.setText(info.getName() + "  " + info.getName() + "   " + info.getName());
            tvBgName.setTextColor(getResources().getColor(R.color.white));
        }, getToken(), getAppConfig().GET_MUSIC_LIST, getAppConfig().downloadUrl);

        broadcast();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isStop = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isStop) {
            VoiceManager.instance().pause();
        }
        stopRecord();
        mRecorderState = STATE_INIT;
        //        refreshControlUI();
    }

    @Override
    public void onBackPressed() {
        if (waitPar.getVisibility() == View.VISIBLE) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(closeReceiver);
    }

    private void broadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MsgBroadcast.ACTION_MSG_CLOSE_TRILL);
        registerReceiver(closeReceiver, intentFilter);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.ll_filter:
                mDialog.show();
                rlMore.setVisibility(View.GONE);
                mRecordBtn.setVisibility(View.GONE);
                break;
            case R.id.btn_rec:
                if (mRecorderState == STATE_INIT) {
                    if (!xbar.isNotOver()) {
                        showToast(getString(R.string.delete_some));
                        return;
                    }

                    if (!TextUtils.isEmpty(mCurrBgmPath)) {
                        VoiceManager.instance().play(mCurrBgmPath);
                        int msec = xbar.getCurrentPro() - 1800;
                        if (msec < 0) {
                            msec = 0;
                        }
                        VoiceManager.instance().seek(msec);
                    }

                    //开始录制视频
                    if (startRecord(RecorderUtils.getVideoFileByTime())) {
                        mRecorderState = STATE_RECORDING;
                        xbar.record();
                        refreshControlUI();
                    }

                } else if (mRecorderState == STATE_RECORDING) {
                    //停止视频录制
                    VoiceManager.instance().pause();
                    stopRecord();
                    mRecorderState = STATE_INIT;
                    refreshControlUI();
                }
                break;
            case R.id.ll_swith:
                mRecordView.switchCamera();
                break;
            case R.id.iv_comp:
                compteRecord();
                break;
            case R.id.iv_del:
                // 删除一段视频
                popDelVideo();
                break;
            case R.id.ll_back: // 退出录制
                VoiceManager.instance().pause();
                mRecorderState = STATE_INIT;
                refreshControlUI();
                stopRecord();
                exitRecord();
                break;
            case R.id.ll_select_music: // 退出录制
                mSelectDialog.show();
                break;
            case R.id.beauty:
                ToastUtil.showToast(mContext, "暂未开启");
                break;
        }
    }

    private void exitRecord() {
        for (int i = videos.size() - 1; i > -1; i--) {
            EpVideo video = videos.get(i);
            RecorderUtils.delVideoFile(video.getVideoPath());
        }
        finish();
    }

    /**
     * 开始录制
     *
     * @return
     */
    private boolean startRecord(String path) {
        try {
            Log.e(TAG, "开始录制：" + path);
            mRecordView.startRecord(path);
            videos.add(new EpVideo(path));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 结束录制
     *
     * @return
     */
    private boolean stopRecord() {
        try {
            mRecordView.stopRecord();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 删除上一段视频
     */
    private void popDelVideo() {
        if (videos.size() > 0) {
            EpVideo video = videos.get(videos.size() - 1);
            RecorderUtils.delVideoFile(video.getVideoPath());
            videos.remove(videos.size() - 1);
        }

        if (videos.size() == 0) {
            llMusic.setVisibility(View.VISIBLE);
        }

        xbar.popTask();

        Log.e(TAG, "popDelVideo: " + videos.size());
    }

    /**
     * 完成录制
     */
    public void compteRecord() {
        VoiceManager.instance().stop();
        int length = videos == null ? 0 : videos.size();
        if (length == 0) {
            showToast(getString(R.string.record_frist_video));
        } else if (length == 1) { // 直接去拼合音乐
            showWaitDialog();
            EpVideo video = videos.get(0);
            appendBgm(video.getVideoPath());
        } else {
            VoiceManager.instance().stop();
            showWaitDialog();
            // 先去拼合视频

            Log.e(TAG, "即将要拼合: " + videos.size() + "个视频");
            final String outFile = RecorderUtils.getVideoFileByTime();
            EpEditor.mergeByLc(this, videos, new EpEditor.OutputOption(outFile), new OnEditorListener() {
                @Override
                public void onSuccess() {
                    appendBgm(outFile);
                }

                @Override
                public void onFailure() {
                    Log.e(TAG, "合并失败");
                    handler.sendEmptyMessage(RecorderUtils.MERGE_FAILURE);
                }

                @Override
                public void onProgress(float progress) {
                    //这里获取处理进度
                    Log.e(TAG, "正在合并" + progress);
                }
            });
        }
    }

    private void appendBgm(final String filePath) {
        if (TextUtils.isEmpty(mCurrBgmPath)) {
            mCurrPath = filePath;
            handler.sendEmptyMessage(RecorderUtils.MUSIC_SUCCESS);
            return;
        }

        final String outfilePath = RecorderUtils.getVideoFileByTime();
        mCurrPath = outfilePath;

        // 合并音效
        EpEditor.music(filePath, mCurrBgmPath, outfilePath, 0f, 1f, new OnEditorListener() {
            @Override
            public void onSuccess() {
                handler.sendEmptyMessage(RecorderUtils.MUSIC_SUCCESS);
            }

            @Override
            public void onFailure() {
                mCurrPath = filePath;
                handler.sendEmptyMessage(RecorderUtils.MUSIC_SUCCESS);
            }

            @Override
            public void onProgress(float progress) {
                //这里获取处理进度
                Log.e(TAG, "music正在合并" + progress);
            }
        });
    }

    private void refreshControlUI() {
        if (mRecorderState == STATE_RECORDING) {
            //1s后才能按停止录制按钮
            mRecordBtn.setEnabled(false);
            handler.sendEmptyMessageDelayed(RecorderUtils.ACTIVATE_BTN, 1000);
            mRecordBtn.record();
            rlMore.setVisibility(View.GONE);
            llMusic.setVisibility(View.INVISIBLE);
            xbar.record();
        } else if (mRecorderState == STATE_INIT) {
            mRecordBtn.pause();
            xbar.pause();
            rlMore.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 跳转到视频预览界面
     */
    private void intentPreview(String filePath) {
        isStop = true;

        videos.clear();
        xbar.reset();
        llMusic.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, PreviewxActivity.class);
        intent.putExtra("file_path", filePath);
        if (!TextUtils.isEmpty(mCurrBgmName)) {
            intent.putExtra("music_name", mCurrBgmName);
        }

        startActivity(intent);
    }

    public void showWaitDialog() {
        waitPar.setVisibility(View.VISIBLE);
    }

    public void showToast(String content) {
        ToastUtil.showToast(this, content);
    }
}

/**
 * 录制监听器
 * <p>
 * 删除上一段视频
 * <p>
 * 开始录制
 *
 * @return 结束录制
 * @return 完成录制
 * <p>
 * 跳转到视频预览界面
 * <p>
 * 删除上一段视频
 * <p>
 * 开始录制
 * @return 结束录制
 * @return 完成录制
 * <p>
 * 跳转到视频预览界面
 * <p>
 * 删除上一段视频
 * <p>
 * 开始录制
 * @return 结束录制
 * @return 完成录制
 * <p>
 * 跳转到视频预览界面
 * <p>
 * 删除上一段视频
 * <p>
 * 开始录制
 * @return 结束录制
 * @return 完成录制
 * <p>
 * 跳转到视频预览界面
 * <p>
 * 删除上一段视频
 * <p>
 * 开始录制
 * @return 结束录制
 * @return 完成录制
 * <p>
 * 跳转到视频预览界面
 * <p>
 * 删除上一段视频
 * <p>
 * 开始录制
 * @return 结束录制
 * @return 完成录制
 * <p>
 * 跳转到视频预览界面
 * <p>
 * 删除上一段视频
 * <p>
 * 开始录制
 * @return 结束录制
 * @return 完成录制
 * <p>
 * 跳转到视频预览界面
 * <p>
 * 删除上一段视频
 * <p>
 * 开始录制
 * @return 结束录制
 * @return 完成录制
 * <p>
 * 跳转到视频预览界面
 * <p>
 * 删除上一段视频
 * <p>
 * 开始录制
 * @return 结束录制
 * @return 完成录制
 * <p>
 * 跳转到视频预览界面
 * <p>
 * 删除上一段视频
 * <p>
 * 开始录制
 * @return 结束录制
 * @return 完成录制
 * <p>
 * 跳转到视频预览界面
 * <p>
 * 删除上一段视频
 * <p>
 * 开始录制
 * @return 结束录制
 * @return 完成录制
 * <p>
 * 跳转到视频预览界面
 * <p>
 * 删除上一段视频
 * <p>
 * 开始录制
 * @return 结束录制
 * @return 完成录制
 * <p>
 * 跳转到视频预览界面
 * <p>
 * 删除上一段视频
 * <p>
 * 开始录制
 * @return 结束录制
 * @return 完成录制
 * <p>
 * 跳转到视频预览界面
 * <p>
 * 删除上一段视频
 * <p>
 * 开始录制
 * @return 结束录制
 * @return 完成录制
 * <p>
 * 跳转到视频预览界面
 * <p>
 * 删除上一段视频
 * <p>
 * 开始录制
 * @return 结束录制
 * @return 完成录制
 * <p>
 * 跳转到视频预览界面
 * <p>
 * 删除上一段视频
 * <p>
 * 开始录制
 * @return 结束录制
 * @return 完成录制
 * <p>
 * 跳转到视频预览界面
 * <p>
 * 删除上一段视频
 * <p>
 * 开始录制
 * @return 结束录制
 * @return 完成录制
 * <p>
 * 跳转到视频预览界面
 *//*

    private OnRecordListener mRecordListener = new OnRecordListener() {

        @Override
        public void onRecordStarted() {
            // 编码器已经进入录制状态，则快门按钮可用
        }

        @Override
        public void onRecordProgressChanged(final long duration) {
        }

        @Override
        public void onRecordFinish() {
            // 编码器已经完全释放，则快门按钮可用
            Log.e("FFmpeg_EpMedia", "完成录制：" + mCurrPath);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mRecordBtn.setEnabled(true);
                    mRecordBtn.pause();
                    xbar.pause();
                    llfilterBar.setVisibility(View.VISIBLE);
                    rlMore.setVisibility(View.VISIBLE);
                }
            });


        }
    };
    // -------------------------------------- 短视频合成监听器 ---------------------------------
    // 合成监听器
    private VideoCombiner.CombineListener mCombineListener = new VideoCombiner.CombineListener() {
        @Override
        public void onCombineStart() {

        }

        @Override
        public void onCombineProcessing(final int current, final int sum) {

        }

        @Override
        public void onCombineFinished(final boolean success) {

        }
    };

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recx);

        int widht = ScreenUtil.getScreenWidth(this);
        int height = ScreenUtil.getScreenHeight(this);

        mCameraParam = CameraParam.getInstance();
        mCameraParam.setAspectRatio(widht, height);

        xbar = findViewById(R.id.xpbar);
        mCameraSurfaceView = findViewById(R.id.view_surface);
        // 绑定需要渲染的SurfaceView
        PreviewRenderer.getInstance().setSurfaceView(mCameraSurfaceView);
        rlMore = findViewById(R.id.rl_more);
        mRecordBtn = findViewById(R.id.btn_rec);
        waitPar = findViewById(R.id.progress_ing);
        tvBgName = findViewById(R.id.tv_bgname);
        llMusic = findViewById(R.id.ll_select_music);
        llfilterBar = findViewById(R.id.ll_filter_bar);

        findViewById(R.id.btn_rec).setOnClickListener(this);
        findViewById(R.id.ll_filter).setOnClickListener(this);
        findViewById(R.id.ll_swith).setOnClickListener(this);
        findViewById(R.id.iv_comp).setOnClickListener(this);
        findViewById(R.id.iv_del).setOnClickListener(this);
        findViewById(R.id.ll_back).setOnClickListener(this);
        llMusic.setOnClickListener(this);
        waitPar.setOnClickListener(this);

        videos = new ArrayList<>();
        mDialog = new FilterSelectDialog(this, resourceData -> {
            if (!resourceData.name.equals("none")) {
                String folderPath = FilterHelper.getFilterDirectory(RecordxActivity.this) + File.separator + resourceData.unzipFolder;
                DynamicColor color = null;
                try {
                    color = ResourceJsonCodec.decodeFilterData(folderPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                PreviewRenderer.getInstance().changeDynamicFilter(color);
            } else {
                PreviewRenderer.getInstance().changeDynamicFilter(null);
            }
        });

        mDialog.setOnDismissListener(() -> {
            rlMore.setVisibility(View.VISIBLE);
            mRecordBtn.setVisibility(View.VISIBLE);
        });

        xbar.addOnComptListener(() -> {
            Log.e(TAG, "onCompte: ");
            mRecorderState = STATE_COMPT;
            stopRecord();
            //            refreshControlUI();

        });

        mSelectDialog = new SelectMusicDialog(this, info -> {
            mCurrBgmPath = info.path;
            mCurrBgmName = info.getName();
            mCurrBgmId = info.getId();
            tvBgName.setText(info.getName() + "  " + info.getName() + "   " + info.getName());
            tvBgName.setTextColor(getResources().getColor(R.color.white));
        }, getToken(), getAppConfig().GET_MUSIC_LIST, getAppConfig().downloadUrl);

        broadcast();
        initCamera();
    }

    private void initCamera() {
        // 初始化相机渲染引擎
        PreviewRenderer.getInstance()
                .setCameraCallback(mCameraCallback)
                .setFpsCallback(new OnFpsListener() {
                    @Override
                    public void onFpsCallback(float fps) {

                    }
                }).initRenderer(this);

        PreviewRecorder.getInstance()
                .setMilliSeconds(PreviewRecorder.CountDownType.ThreeMinute);
    }

    private void broadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MsgBroadcast.ACTION_MSG_CLOSE_TRILL);
        registerReceiver(closeReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(closeReceiver);
        PreviewRecorder.getInstance().removeAllSubVideo();
        PreviewRecorder.getInstance().deleteRecordDuration();
        PreviewRecorder.getInstance().destroyRecorder();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.ll_filter:
                mDialog.show();
                rlMore.setVisibility(View.GONE);
                mRecordBtn.setVisibility(View.GONE);
                break;
            case R.id.btn_rec:
                if (mRecorderState == STATE_INIT) {
                    if (!xbar.isNotOver()) {
                        showToast(getString(R.string.delete_some));
                        return;
                    }

                    if (!TextUtils.isEmpty(mCurrBgmPath)) {
                        VoiceManager.instance().play(mCurrBgmPath);
                        int msec = xbar.getCurrentPro() - 1800;
                        if (msec < 0) {
                            msec = 0;
                        }
                        VoiceManager.instance().seek(msec);
                    }

                    //开始录制视频
                    if (startRecord(RecorderUtils.getVideoFileByTime())) {
                        mRecorderState = STATE_RECORDING;
                        mRecordBtn.setEnabled(false);
                        xbar.record();
                        mRecordBtn.record();
                        refreshControlUI();
                    }

                } else if (mRecorderState == STATE_RECORDING) {
                    //停止视频录制

                    VoiceManager.instance().pause();
                    mRecorderState = STATE_INIT;
                    stopRecord();

                }
                break;
            case R.id.ll_swith:
                PreviewRenderer.getInstance().switchCamera();
                break;
            case R.id.iv_comp:
                compteRecord();
                break;
            case R.id.iv_del:
                // 删除一段视频
                popDelVideo();
                break;
            case R.id.ll_back: // 退出录制
                VoiceManager.instance().pause();
                mRecorderState = STATE_INIT;
                //                refreshControlUI();
                stopRecord();
                exitRecord();
                break;
            case R.id.ll_select_music: // 退出录制
                mSelectDialog.show();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (waitPar.getVisibility() == View.VISIBLE) {
            return;
        }
        super.onBackPressed();
    }

    private void exitRecord() {
        for (int i = videos.size() - 1; i > -1; i--) {
            EpVideo video = videos.get(i);
            RecorderUtils.delVideoFile(video.getVideoPath());
        }
        finish();
    }

    */
/**
 * 删除上一段视频
 *//*


    private void popDelVideo() {
        if (videos.size() > 0) {
            EpVideo video = videos.get(videos.size() - 1);
            RecorderUtils.delVideoFile(video.getVideoPath());
            videos.remove(videos.size() - 1);
        }

        PreviewRecorder.getInstance().removeAllSubVideo();
        PreviewRecorder.getInstance().deleteRecordDuration();

        if (videos.size() == 0) {
            llMusic.setVisibility(View.VISIBLE);
        }

        xbar.popTask();

        Log.e(TAG, "popDelVideo: " + videos.size());
    }

    */
/**
 * 开始录制
 *
 * @return
 *//*

    private boolean startRecord(String path) {

        mCameraParam.mGalleryType = GalleryType.VIDEO;
        // 是否允许录制音频
        boolean enableAudio = mCameraParam.audioPermitted && mCameraParam.recordAudio
                && mCameraParam.mGalleryType == GalleryType.VIDEO;

        // 计算输入纹理的大小
        int width = mCameraParam.previewWidth;
        int height = mCameraParam.previewHeight;
        if (mCameraParam.orientation == 90 || mCameraParam.orientation == 270) {
            width = mCameraParam.previewHeight;
            height = mCameraParam.previewWidth;
        }

        mCurrPath = path;

        PreviewRecorder.getInstance()
                .setRecordType(PreviewRecorder.RecordType.Video)
                .setOutputPath(path)
                .enableAudio(enableAudio)
                .setRecordSize(width, height)
                .setOnRecordListener(mRecordListener)
                .startRecord();

        Log.e("FFmpeg_EpMedia", "开始录制：" + path);
        videos.add(new EpVideo(path));

        return true;
    }

    */
/**
 * 结束录制
 *
 * @return
 *//*

    private boolean stopRecord() {
        mRecordBtn.setEnabled(false);
        PreviewRecorder.getInstance().stopRecord(true);
        return true;
    }

    */
/**
 * 完成录制
 *//*

    public void compteRecord() {
        VoiceManager.instance().stop();


        int length = videos == null ? 0 : videos.size();
        if (length == 0) {
            showToast(getString(R.string.record_frist_video));
        } else if (length == 1) { // 直接去拼合音乐
            showWaitDialog();
            EpVideo video = videos.get(0);
            appendBgm(video.getVideoPath());
        } else {
            VoiceManager.instance().stop();
            showWaitDialog();
            // 先去拼合视频

            Log.e(TAG, "即将要拼合: " + videos.size() + "个视频");
            final String outFile = RecorderUtils.getVideoFileByTime();
            EpEditor.mergeByLc(this, videos, new EpEditor.OutputOption(outFile), new OnEditorListener() {
                @Override
                public void onSuccess() {
                    appendBgm(outFile);
                }

                @Override
                public void onFailure() {
                    Log.e(TAG, "合并失败");
                    handler.sendEmptyMessage(RecorderUtils.MERGE_FAILURE);
                }

                @Override
                public void onProgress(float progress) {
                    //这里获取处理进度
                    Log.e(TAG, "正在合并" + progress);
                }
            });
        }
    }

    private void appendBgm(final String filePath) {

        if (TextUtils.isEmpty(mCurrBgmPath)) {
            mCurrPath = filePath;
            handler.sendEmptyMessage(RecorderUtils.MUSIC_SUCCESS);
            return;
        }

        final String outfilePath = RecorderUtils.getVideoFileByTime();
        mCurrPath = outfilePath;

        // 合并音效
        EpEditor.music(filePath, mCurrBgmPath, outfilePath, 0f, 1f, new OnEditorListener() {
            @Override
            public void onSuccess() {
                handler.sendEmptyMessage(RecorderUtils.MUSIC_SUCCESS);
            }

            @Override
            public void onFailure() {
                mCurrPath = filePath;
                handler.sendEmptyMessage(RecorderUtils.MUSIC_SUCCESS);
            }

            @Override
            public void onProgress(float progress) {
                //这里获取处理进度
                Log.e(TAG, "music正在合并" + progress);
            }
        });
    }

    private void refreshControlUI() {
        if (mRecorderState == STATE_RECORDING) {
            //1s后才能按停止录制按钮
            handler.sendEmptyMessageDelayed(RecorderUtils.ACTIVATE_BTN, 1000);
            rlMore.setVisibility(View.GONE);
            llfilterBar.setVisibility(View.GONE);
            llMusic.setVisibility(View.INVISIBLE);
        } else if (mRecorderState == STATE_INIT || mRecorderState == STATE_COMPT) {
            mRecordBtn.pause();
            xbar.pause();
            llfilterBar.setVisibility(View.VISIBLE);
            rlMore.setVisibility(View.VISIBLE);
        }
    }

    */
/**
 * 跳转到视频预览界面
 *//*

    private void intentPreview(String filePath) {
        isStop = true;

        PreviewRecorder.getInstance().removeAllSubVideo();
        PreviewRecorder.getInstance().destroyRecorder();
        videos.clear();
        xbar.reset();
        mRecorderState = STATE_INIT;
        llMusic.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, PreviewxActivity.class);
        intent.putExtra("file_path", filePath);
        if (!TextUtils.isEmpty(mCurrBgmName)) {
            intent.putExtra("music_name", mCurrBgmName);
            intent.putExtra("music_id", mCurrBgmId);
        }


        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isStop = false;
    }

    public void showWaitDialog() {
        waitPar.setVisibility(View.VISIBLE);
    }

    public void showToast(String content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isStop) {
            VoiceManager.instance().pause();
        }
        if (mRecorderState == STATE_RECORDING) {
            stopRecord();

            mRecorderState = STATE_INIT;
        }
    }


}
*/
