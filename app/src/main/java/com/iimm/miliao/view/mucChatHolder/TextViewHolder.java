package com.iimm.miliao.view.mucChatHolder;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.HtmlUtils;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.StringUtils;
import com.iimm.miliao.util.link.HttpTextView;

public class TextViewHolder extends AChatHolderInterface {
    private static String TAG = "TextViewHolder";

    public HttpTextView mTvContent;
    public TextView tvFireTime;

    public TextViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_text : R.layout.chat_to_item_text;
    }

    @Override
    public void initView(View view) {
        mTvContent = view.findViewById(R.id.chat_text);
        mRootView = view.findViewById(R.id.chat_warp_view);
    }

    @Override
    public void fillData(ChatMessage message) {
        // 修改字体功能
        int size = PreferenceUtils.getInt(mContext, Constants.FONT_SIZE_TYPE, 0) + Constants.DEFAULT_TEXT_SIZE;
        mTvContent.setTextSize(size);
        mTvContent.setTextColor(ContextCompat.getColor(mContext, R.color.black));

        String content = StringUtils.replaceSpecialChar(message.getContent());
        CharSequence charSequence = HtmlUtils.transform200SpanString(content, true);
        if (message.getIsReadDel() && !isMysend) {// 阅后即焚
            if (!message.isGroup() && !message.isSendRead()) {
                mIvFire.setProgress(getReadDleProgress(message));
                ReadDelManager.getInstants().addReadMsg(message, this);
                mTvContent.setText(charSequence);
            } else {
                // 已经查看了，当适配器再次刷新的时候，不需要重新赋值
                mTvContent.setText(charSequence);
            }
        } else if (message.getIsReadDel() && isMysend) {
            ReadDelManager.getInstants().addReadMsg(message, this);
            mTvContent.setText(charSequence);
            mIvFire.setProgress(getReadDleProgress(message));
        } else {
            mTvContent.setText(charSequence);
        }

        mTvContent.setUrlText(mTvContent.getText());

        mTvContent.setOnClickListener(v -> mHolderListener.onItemClick(mRootView, TextViewHolder.this, mdata));
        mTvContent.setOnLongClickListener(v -> {
            mHolderListener.onItemLongClick(v, TextViewHolder.this, mdata);
            return true;
        });
    }

    @Override
    protected void onRootClick(View v) {

    }

    @Override
    public boolean enableFire() {
        return true;
    }

    @Override
    public boolean enableSendRead() {
        return true;
    }

}
