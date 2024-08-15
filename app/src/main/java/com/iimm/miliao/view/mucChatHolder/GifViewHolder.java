package com.iimm.miliao.view.mucChatHolder;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import pl.droidsonroids.gif.GifImageView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.util.ImageUtils;


class GifViewHolder extends AChatHolderInterface {

    GifImageView mGifView;
    private static final int IMAGE_MIN_SIZE = 70;
    private static final int IMAGE_MAX_SIZE = 105;
    private int width, height;

    public GifViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_gif : R.layout.chat_to_item_gif;
    }

    @Override
    public void initView(View view) {
        mGifView = view.findViewById(R.id.chat_gif_view);
        mRootView = mGifView;
    }

    @Override
    public void fillData(ChatMessage message) {
        changeImageLayaoutSize(message, IMAGE_MIN_SIZE, IMAGE_MAX_SIZE);
        String gifName = message.getContent();
        if (!TextUtils.isEmpty(gifName)) {
            loadGifImageFromServer(gifName);
        } else {
            Glide.with(mContext).load(R.drawable.fez).into(mGifView);
        }

        if (enableFire() && message.getIsReadDel()) {

            ReadDelManager.getInstants().addReadMsg(message, this);
        }
    }

    public void loadGifImageFromServer(String url) {

        ImageUtils.loadGifImageWithUrl(mContext, url, width, height, mGifView);


    }

    public void changeImageLayaoutSize(ChatMessage message, int mindp, int maxdp) {
        ViewGroup.LayoutParams mLayoutParams = mGifView.getLayoutParams();
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
                float height = width * image_height / image_width;
                mLayoutParams.width = dp2px(width);
                mLayoutParams.height = dp2px(height);
                this.width = mLayoutParams.width;
                this.height = mLayoutParams.height;
            }
        } else {

            mLayoutParams.width = dp2px(maxdp);
            mLayoutParams.height = dp2px(maxdp);
            this.width = mLayoutParams.width;
            this.height = mLayoutParams.height;
        }
        mGifView.setLayoutParams(mLayoutParams);
    }

    @Override
    protected void onRootClick(View v) {

    }

    @Override
    public boolean enableSendRead() {
        return true;
    }

    @Override
    public boolean enableFire() {
        return true;
    }
}
