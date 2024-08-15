package com.iimm.miliao.view.mucChatHolder;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.util.Constants;

class LiveChatViewHolder extends AChatHolderInterface {

    TextView tvName;
    TextView tvContent;

    public LiveChatViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public int itemLayoutId(boolean isMysend) {
        return R.layout.chat_item_live_system;
    }

    @Override
    public void initView(View view) {
        tvName = view.findViewById(R.id.tv_name);
        tvContent = view.findViewById(R.id.tv_content);
    }

    @Override
    @SuppressLint("SetTextI18n")
    public void fillData(ChatMessage message) {
        String fromUserName = message.getFromUserName();
        if (TextUtils.equals(message.getFromUserId(), Constants.ID_SYSTEM_NOTIFICATION)) {
            fromUserName = mContext.getString(R.string.system_notification_user_name);
        }
        tvName.setText(fromUserName + ":");
        tvContent.setText(message.getContent());
    }

    @Override
    protected void onRootClick(View v) {

    }

    @Override
    public boolean isLongClick() {
        return false;
    }

    @Override
    public boolean isOnClick() {
        return false;
    }

    @Override
    public boolean enableNormal() {
        return false;
    }
}
