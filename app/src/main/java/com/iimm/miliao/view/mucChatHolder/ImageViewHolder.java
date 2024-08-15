package com.iimm.miliao.view.mucChatHolder;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.ui.message.ChatOverviewActivity;
import com.iimm.miliao.ui.tool.SingleImagePreviewActivity;
import com.iimm.miliao.util.AppUtils;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.view.XuanProgressPar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;


public class ImageViewHolder extends AChatHolderInterface {
    private String TAG = "ImageViewHolder";
    private static final int IMAGE_MIN_SIZE = 100;
    private static final int IMAGE_MAX_SIZE = 135;
//    private static final int IMAGE_MIN_SIZE = 70;
//    private static final int IMAGE_MAX_SIZE = 105;
    //    ChatImageView mImageView;
    GifImageView mGifView;
    XuanProgressPar progressPar;
//    private int width, height;

    public ImageViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_image : R.layout.chat_to_item_image;
    }

    @Override
    public void initView(View view) {
        mGifView = view.findViewById(R.id.chat_gif_view);
//        mImageView = view.findViewById(R.id.chat_image);
        progressPar = view.findViewById(R.id.img_progress);
        mRootView = view.findViewById(R.id.chat_warp_view);
    }

    @Override
    public void fillData(ChatMessage message) {
        mGifView.setImageResource(R.drawable.fez);
        changeImageLayaoutSize(message, IMAGE_MIN_SIZE, IMAGE_MAX_SIZE);
        Log.e("git", message.getContent() + "");
        Log.e("gitf", message.getFilePath() + "");
        String filePath = message.getFilePath();
        if (message.isMySend() || message.isDownload()) {//本地图片路径,如果不是原图，则这个路径是 压缩图的路径
            if (!TextUtils.isEmpty(filePath)) {
                File file = new File(filePath);
                if (file.exists()) {
                    if (filePath.endsWith(".gif")) { // 加载gif
                        fillImageGif(filePath);
                    } else {
                        fillImage(filePath);
                    }
                } else {
                    if (!TextUtils.isEmpty(message.getContent())) {
                        if (message.getContent().endsWith(".gif")) { // 加载gif
                            loadGifImageFromServer(message);
                        } else {
                            fillImage(message.getContent());
                        }
                    } else {
                        mGifView.setImageResource(R.drawable.fez);
                    }
                }
            } else {
                if (!TextUtils.isEmpty(message.getContent())) {
                    if (message.getContent().endsWith(".gif")) { // 加载gif
                        loadGifImageFromServer(message);
                    } else {
                        fillImage(message.getContent());
                    }
                } else {
                    mGifView.setImageResource(R.drawable.fez);
                }
            }
        } else {
            if (!TextUtils.isEmpty(message.getContent())) {
                if (message.getContent().endsWith(".gif")) { // 加载gif
                    loadGifImageFromServer(message);
                } else {
                    fillImage(message.getContent());
                }
            } else {
                mGifView.setImageResource(R.drawable.fez);
            }
        }
        // 判断是否为阅后即焚类型的图片，如果是 模糊显示该图片
        if (!isGroup) {
            mGifView.setAlpha(message.getIsReadDel() ? 0.1f : 1f);
        }

        // 上传进度条 我的消息才有进度条
        if (message.isUpload() || !isMysend || message.getUploadSchedule() >= 100) {
            progressPar.setVisibility(View.GONE);
        } else {
            progressPar.setVisibility(View.VISIBLE);
        }
        if (isMysend && message.getIsReadDel()) {
            //我发送的阅后即焚图片消息

            ReadDelManager.getInstants().addReadMsg(message, this);
        } //不是我发送的 查看后再加入 计时列表
        progressPar.update(message.getUploadSchedule());
    }

    public void loadGifImageFromServer(ChatMessage message) {
        Glide.with(mContext).load(message.getContent()).downloadOnly(new SimpleTarget<File>() {
            @Override
            public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                try {
                    GifDrawable gifFromStream = new GifDrawable(resource);
                    mGifView.setImageDrawable(gifFromStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
//        Glide.with(mContext).load(message.getContent()).priority(Priority.IMMEDIATE).dontAnimate().into(mGifView);
    }

    private void fillImage(String fileUrl) {
        if (AppUtils.isContextExisted(mContext)){
            Glide.with(mContext).load(fileUrl).dontAnimate().into(mGifView);
        }
    }

    private void fillImageGif(String filePath) {
        try {
            GifDrawable gifFromFile = new GifDrawable(new File(filePath));
            mGifView.setImageDrawable(gifFromFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        Glide.with(mContext).load(new File(filePath)).priority(Priority.IMMEDIATE).dontAnimate().into(mGifView);
    }

    public void changeImageLayaoutSize(ChatMessage message, int mindp, int maxdp) {
//        mGifView.setVisibility(View.VISIBLE);
//        mImageView.setVisibility(View.GONE);
//        if (!TextUtils.isEmpty(message.getFilePath())) {
//            File file = new File(message.getFilePath());
//            if (file.exists()) {
//                if (message.getFilePath().endsWith(".gif")) { // 加载gif
//                    mGifView.setVisibility(View.VISIBLE);
//                    mImageView.setVisibility(View.GONE);
//                } else {
//                    mGifView.setVisibility(View.GONE);
//                    mImageView.setVisibility(View.VISIBLE);
//                }
//            } else {
//                if (!TextUtils.isEmpty(message.getContent())) {
//                    if (message.getContent().endsWith(".gif")) { // 加载gif
//                        mGifView.setVisibility(View.VISIBLE);
//                        mImageView.setVisibility(View.GONE);
//                    } else {
//                        mGifView.setVisibility(View.GONE);
//                        mImageView.setVisibility(View.VISIBLE);
//                    }
//                } else {
//                    mGifView.setVisibility(View.GONE);
//                    mImageView.setVisibility(View.VISIBLE);
//                }
//            }
//        } else {
//            if (!TextUtils.isEmpty(message.getContent())) {
//                if (message.getContent().endsWith(".gif")) {
//                    mGifView.setVisibility(View.VISIBLE);
//                    mImageView.setVisibility(View.GONE);
//                } else {
//                    mGifView.setVisibility(View.GONE);
//                    mImageView.setVisibility(View.VISIBLE);
//                }
//            } else {
//                mGifView.setVisibility(View.GONE);
//                mImageView.setVisibility(View.VISIBLE);
//            }
//        }
//        ViewGroup.LayoutParams mLayoutParams = mImageView.getLayoutParams();
        ViewGroup.LayoutParams mGifLayoutParams = mGifView.getLayoutParams();
        if (!TextUtils.isEmpty(message.getLocation_x()) && !TextUtils.isEmpty(message.getLocation_y())) {
            float image_width = Float.parseFloat(message.getLocation_x());
            float image_height = Float.parseFloat(message.getLocation_y());
            if (image_width > 0 && image_height > 0) {
                // 基于宽度进行缩放,三挡:宽图 55/100,窄图100/55
                float width = 0;
                if (image_width < dp2px(mindp * 1.0f)) {
                    width = mindp;
                } else if (image_width > dp2px(maxdp * 1.0f)) {
                    width = maxdp;
                } else {
                    width = Px2Dp(mContext, image_width);
                }
                if(image_height<image_width){
                    width=width*2;
                    float height = width * image_height / image_width;
                    mGifLayoutParams.width = dp2px(width);
                    mGifLayoutParams.height = dp2px(height);
                }

                float height = width * image_height / image_width;
                mGifLayoutParams.width = dp2px(width);
                mGifLayoutParams.height = dp2px(height);
//                this.width = mGifLayoutParams.width;
//                this.height = mGifLayoutParams.height;
            } else {
                mGifLayoutParams.width = dp2px(maxdp);
                mGifLayoutParams.height = dp2px(maxdp);
//                this.width = mGifLayoutParams.width;
//                this.height = mGifLayoutParams.height;
            }
        } else {
            mGifLayoutParams.width = dp2px(maxdp);
            mGifLayoutParams.height = dp2px(maxdp);
//            this.width = mGifLayoutParams.width;
//            this.height = mGifLayoutParams.height;
        }
        mGifView.setLayoutParams(mGifLayoutParams);
    }

    @Override
    public void onRootClick(View v) {
        if (mdata.getIsReadDel()) { // 阅后即焚图片跳转至单张图片预览类
            if (!mdata.isMySend()) { //不是我发送的  并且是阅后即焚的图片
                ReadDelManager.getInstants().addReadMsg(mdata, this);
            }
            Intent intent = new Intent(mContext, SingleImagePreviewActivity.class);
            intent.putExtra(AppConstant.EXTRA_IMAGE_URI, mdata.getContent());
            intent.putExtra("image_path", mdata.getFilePath());
            intent.putExtra("isReadDel", mdata.getIsReadDel());
            if (!isGroup && !isMysend && mdata.getIsReadDel()) {
                intent.putExtra("DEL_PACKEDID", mdata.getPacketId());
            }
            mContext.startActivity(intent);
        } else {
            int imageChatMessageList_current_position = 0;
            List<ChatMessage> imageChatMessageList = new ArrayList<>();
            for (int i = 0; i < chatMessages.size(); i++) {
                if (chatMessages.get(i).getType() == Constants.TYPE_IMAGE
                        && !chatMessages.get(i).getIsReadDel()) {
                    if (chatMessages.get(i).getPacketId().equals(mdata.getPacketId())) {
                        imageChatMessageList_current_position = imageChatMessageList.size();
                    }
                    imageChatMessageList.add(chatMessages.get(i));
                }
            }
            try {
                Intent intent = new Intent(mContext, ChatOverviewActivity.class);
                intent.putExtra("imageChatMessageList", JSON.toJSONString(imageChatMessageList));
                intent.putExtra("imageChatMessageList_current_position", imageChatMessageList_current_position);
                mContext.startActivity(intent);
            } catch (Exception e) {

            }
        }
    }

    @Override
    public boolean enableSendRead() {
        return true;
    }

    // 启用阅后即焚
    @Override
    public boolean enableFire() {
        return true;
    }
}
