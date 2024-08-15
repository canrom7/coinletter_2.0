package com.iimm.miliao.view.mucChatHolder;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.ui.message.single.PersonSettingActivity;
import com.iimm.miliao.view.NoDoubleClickListener;

import org.json.JSONException;
import org.json.JSONObject;

public class ReadDelTimeHintHolder extends AChatHolderInterface {
    private TextView mTvSetHintTime;
    private TextView mTvClickChange;

    public ReadDelTimeHintHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    protected int itemLayoutId(boolean isMysend) {
        return R.layout.chat_read_del_time_hint;
    }

    @Override
    protected void initView(View view) {
        mTvSetHintTime = view.findViewById(R.id.tv_set_hint_time);
        mTvClickChange = view.findViewById(R.id.tv_click_change);
    }

    @Override
    protected void fillData(ChatMessage message) {
        try {
            JSONObject jsonObject = new JSONObject(message.getContent());
            String readDelTime = jsonObject.getString("changeReadDelTime");
            boolean isChangeByMe = jsonObject.getBoolean("isChangeByMe");
            if (TextUtils.isEmpty(readDelTime)) {
                readDelTime = "0";
            }
            if (isChangeByMe) {
                if (readDelTime.equals("0")) {
                    mTvSetHintTime.setText(R.string.you_shut_down_and_burned_after_reading);
                } else {
                    mTvSetHintTime.setText(mContext.getResources().getString(R.string.hint_read_del_time, PersonSettingActivity.handlerMsg(Integer.parseInt(readDelTime))));
                }
            } else {
                if (readDelTime.equals("0")) {
                    mTvSetHintTime.setText(R.string.he_shut_down_and_burned_after_reading);
                } else {
                    mTvSetHintTime.setText(mContext.getResources().getString(R.string.he_hint_read_del_time, PersonSettingActivity.handlerMsg(Integer.parseInt(readDelTime))));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            mTvSetHintTime.setText(mContext.getResources().getString(R.string.hint_read_del_time, PersonSettingActivity.handlerMsg(0)));
        }
        mTvClickChange.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                mHolderListener.onItemClick(mTvClickChange, ReadDelTimeHintHolder.this, message);
            }
        });
    }

    @Override
    protected void onRootClick(View v) {

    }

    @Override
    public boolean enableNormal() {
        return false;
    }
}
