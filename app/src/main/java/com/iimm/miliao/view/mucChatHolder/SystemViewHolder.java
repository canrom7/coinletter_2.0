package com.iimm.miliao.view.mucChatHolder;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.StringUtils;

// 系统消息的holder
class SystemViewHolder extends AChatHolderInterface {

    TextView mTvContent;
    private String time;

    public SystemViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public int itemLayoutId(boolean isMysend) {
        return R.layout.chat_item_system;
    }

    @Override
    public void initView(View view) {
        mTvContent = view.findViewById(R.id.chat_content_tv);
        mRootView = mTvContent;
    }

    @Override
    public void fillData(ChatMessage message) {
        SpannableString content;
        if (message.getFileSize() == Constants.TYPE_83) {
            // 红包被领取的提示
            content = StringUtils.matcherSearchTitle(Color.parseColor("#EB9F4F"), message.getContent(), getString(R.string.chat_red));
        } else if (message.getFileSize() == Constants.TYPE_CLOUD_RECEIVE_RED) {
            // 红包被领取的提示
            content = StringUtils.matcherSearchTitle(Color.parseColor("#EB9F4F"), message.getContent(), getString(R.string.cloud_chat_red));
        } else if (Constants.TYPE_GROUP_SIGN == message.getType()) {
            if (message.getContent().equals("1")) {
                String sure = message.isDownload() ? getString(R.string.has_confirm) : getString(R.string.to_confirm);
                content = StringUtils.matcherSearchTitle(Color.parseColor("#6699FF"), "群签到功能已开启", sure);
            } else {
                String sure = message.isDownload() ? getString(R.string.has_confirm) : getString(R.string.to_confirm);
                content = StringUtils.matcherSearchTitle(Color.parseColor("#6699FF"), "群签到功能已关闭", sure);
            }
        } else {
            //  验证该提示是否为邀请好友入群的验证提示，是的话高亮显示KeyWord 并针对Click事件进行处理
            // Todo  应该效仿红包被领取的提示，将原消息type与关键信息存在其他字段内，这样结构会更加清晰且不会出错
            String sure = message.isDownload() ? getString(R.string.has_confirm) : getString(R.string.to_confirm);
            content = StringUtils.matcherSearchTitle(Color.parseColor("#6699FF"), message.getContent(), sure);
        }
        setText(content);
        mTvContent.setOnClickListener(this);
    }

    @Override
    protected void onRootClick(View v) {

    }

    @Override
    public void showTime(String time) {
        this.time = time;
        setText(mTvContent.getText());
    }

    private void setText(CharSequence content) {
        if (!TextUtils.isEmpty(time)) {
            // 需要支持SpannableString， 不能直接使用StringBuilder之类，
            Editable editable = Editable.Factory.getInstance().newEditable(content);
            editable.append("(").append(time).append(")");
            mTvContent.setText(editable);
        } else {
            mTvContent.setText(content);
        }
    }

    @Override
    public boolean isLongClick() {
        return false;
    }

    @Override
    public boolean isOnClick() {
        return true;
    }

    @Override
    public boolean enableNormal() {
        return false;
    }
}
