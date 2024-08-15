package com.iimm.miliao.view.mucChatHolder;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.iimm.miliao.AppConstant;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.downloader.DownloadListener;
import com.iimm.miliao.downloader.DownloadProgressListener;
import com.iimm.miliao.downloader.Downloader;
import com.iimm.miliao.downloader.FailReason;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.UploadEngine;
import com.iimm.miliao.util.FileUtil;
import com.iimm.miliao.util.HttpUtil;
import com.iimm.miliao.video.ChatVideoPreviewActivity;
import com.iimm.miliao.view.SelectionFrame;
import com.iimm.miliao.view.XuanProgressPar;

public class VideoViewHolder extends AChatHolderInterface implements DownloadListener, DownloadProgressListener {

    // JVCideoPlayerStandardforchat mVideo;
    private static String TAG = "VideoViewHolder";
    ImageView mVideo;
    ImageView ivStart;
    XuanProgressPar progressPar;
    TextView tvInvalid;
    ImageView ivUploadCancel;
    int currentPro = 0;

    public VideoViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_video : R.layout.chat_to_item_video;
    }

    @Override
    public void initView(View view) {
        mVideo = view.findViewById(R.id.chat_jcvideo);
        ivStart = view.findViewById(R.id.iv_start);
        progressPar = view.findViewById(R.id.img_progress);
        tvInvalid = view.findViewById(R.id.tv_invalid);
        ivUploadCancel = view.findViewById(R.id.chat_upload_cancel_iv);
        mRootView = view.findViewById(R.id.chat_warp_view);
    }

    @Override
    public void fillData(ChatMessage message) {
        tvInvalid.setVisibility(View.GONE);

        String filePath = message.getFilePath();
        boolean isExist = FileUtil.isExist(filePath);

        if (!isExist) {
            AvatarHelper.getInstance().asyncDisplayOnlineVideoThumb(message.getContent(), mVideo);
            if (HttpUtil.isConnectedWifi(mContext)) {// Wifi下 才自动下载视频
                Downloader.getInstance().addDownload(message.getContent(), mSendingBar, this, this);
            }
        } else {
            AvatarHelper.getInstance().displayVideoThumb(filePath, mVideo);
            ivStart.setImageResource(fm.jiecao.jcvideoplayer_lib.R.drawable.jc_click_play_selector);
        }

        if (isMysend) { // 判断是否上传
            // 没有上传或者 进度小于100
            boolean show = !message.isUpload() && message.getUploadSchedule() < 100;
            changeVisible(progressPar, show);
            changeVisible(ivStart, !show);

            if (show) { //没有上传完成
                if (ivUploadCancel != null) {
                    ivUploadCancel.setVisibility(View.VISIBLE);
                }
            } else { //上传完成
                if (ivUploadCancel != null) {
                    ivUploadCancel.setVisibility(View.GONE);
                }
                if (message.getIsReadDel()) {
                    //是阅后即焚消息   ，倒计时此消息

                    ReadDelManager.getInstants().addReadMsg(message, this);
                }


            }
        }

        progressPar.update(message.getUploadSchedule());
        mSendingBar.setVisibility(View.GONE);

        if (ivUploadCancel != null) {
            ivUploadCancel.setOnClickListener(v -> {
                SelectionFrame selectionFrame = new SelectionFrame(mContext);
                selectionFrame.setSomething(getString(R.string.cancel_upload), getString(R.string.sure_cancel_upload), new SelectionFrame.OnSelectionFrameClickListener() {
                    @Override
                    public void cancelClick() {

                    }

                    @Override
                    public void confirmClick() {
                        // 用户可能在弹窗弹起后停留很久，所以点击确认的时候还需要判断一下
                        if (!mdata.isUpload()) {
                            UploadEngine.cancel(mdata.getPacketId());
                        }
                    }
                });
                selectionFrame.show();
            });
        }
    }

    @Override
    protected void onRootClick(View v) {
        if (tvInvalid.getVisibility() == View.VISIBLE) {
            return;
        }

        String filePath = mdata.getFilePath();
        Intent intent = new Intent(mContext, ChatVideoPreviewActivity.class);
        if (!FileUtil.isExist(filePath)) {
            filePath = mdata.getContent();
            // 本地不存在，传网络路径进去播放，下载。。。
            Downloader.getInstance().addDownload(filePath, mSendingBar, this, this);
        }
        intent.putExtra(AppConstant.EXTRA_VIDEO_FILE_PATH, filePath);
        if (mdata.getIsReadDel()) {
            intent.putExtra("DEL_PACKEDID", mdata.getPacketId());
        }

        ivUnRead.setVisibility(View.GONE);
        setRead();
        mContext.startActivity(intent);
        //进入观看一次了  如果是阅后即焚消息 就开始倒计时
        if (mdata.getIsReadDel() && !mdata.isMySend()) {
            ReadDelManager.getInstants().addReadMsg(mdata, this);
        }
    }

    @Override
    public void onStarted(String uri, View view) {
        changeVisible(progressPar, true);
        changeVisible(ivStart, false);
    }

    @Override
    public void onFailed(String uri, FailReason failReason, View view) {
        changeVisible(progressPar, false);
        ivStart.setImageResource(fm.jiecao.jcvideoplayer_lib.R.drawable.jc_click_error_selector);
        tvInvalid.setVisibility(View.VISIBLE);
        ivStart.setVisibility(View.VISIBLE);
    }

    @Override
    public void onComplete(String uri, String filePath, View view) {
        mdata.setFilePath(filePath);
        changeVisible(progressPar, false);
        changeVisible(ivStart, true);
        ivStart.setImageResource(fm.jiecao.jcvideoplayer_lib.R.drawable.jc_click_play_selector);

        // 更新数据库
        ChatMessageDao.getInstance().updateMessageDownloadState(mLoginUserId, mToUserId, mdata.get_id(), true, filePath);
        AvatarHelper.getInstance().displayVideoThumb(filePath, mVideo);
    }

    @Override
    public void onCancelled(String uri, View view) {
        changeVisible(progressPar, false);
        changeVisible(ivStart, true);
    }

    @Override
    public void onProgressUpdate(String imageUri, View view, int current, int total) {
        int pro = (int) (current / (float) total * 100);
        if (currentPro != pro) {
            currentPro = pro;
            progressPar.update(pro);
        }
    }

    @Override
    public boolean enableUnRead() {
        return true;
    }

    @Override
    public boolean enableFire() {
        return true;
    }

}
