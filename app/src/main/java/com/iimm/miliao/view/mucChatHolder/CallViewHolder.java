package com.iimm.miliao.view.mucChatHolder;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.DateUtils;


class CallViewHolder extends AChatHolderInterface {

    ImageView ivTextImage;
    TextView mTvContent;

    public CallViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_call : R.layout.chat_to_item_call;
    }

    @Override
    public void initView(View view) {
        ivTextImage = view.findViewById(R.id.chat_text_img);
        mTvContent = view.findViewById(R.id.chat_text);
        mRootView = view.findViewById(R.id.chat_warp_view);
    }

    @Override
    public void fillData(ChatMessage message) {
        switch (message.getType()) {
            case Constants.TYPE_NO_CONNECT_VOICE: {
                String content;
                if (message.getTimeLen() == 0) {
                    content = InternationalizationHelper.getString("JXSip_Canceled") + InternationalizationHelper.getString("JX_VoiceChat");
                } else {
                    content = InternationalizationHelper.getString("JXSip_noanswer");
                }
                mTvContent.setText(content);
                if (message.isMySend()) {
                    ivTextImage.setImageResource(R.drawable.chat_from_voice_call_bg);
                } else {
                    ivTextImage.setImageResource(R.drawable.chat_to_voice_call_bg);
                }

            }
            break;
            case Constants.TYPE_END_CONNECT_VOICE: {
                // 结束
                int timeLen = message.getTimeLen();
                mTvContent.setText("通话时长" + "  " + DateUtils.getVoiceCallTime(timeLen));
                if (message.isMySend()) {
                    ivTextImage.setImageResource(R.drawable.chat_from_voice_call_bg);
                } else {
                    ivTextImage.setImageResource(R.drawable.chat_to_voice_call_bg);
                }
            }
            break;
            case Constants.TYPE_IS_MU_CONNECT_VOICE: {
                mTvContent.setText(R.string.tip_invite_voice_meeting);
                if (message.isMySend()) {
                    ivTextImage.setImageResource(R.drawable.chat_from_voice_call_bg);
                } else {
                    ivTextImage.setImageResource(R.drawable.chat_to_voice_call_bg);
                }
            }
            break;
            case Constants.TYPE_IS_MU_CONNECT_VIDEO: {
                mTvContent.setText(R.string.tip_invite_video_meeting);
                if (message.isMySend()) {
                    ivTextImage.setImageResource(R.drawable.chat_from_video_call_bg);
                } else {
                    ivTextImage.setImageResource(R.drawable.chat_to_video_call_bg);
                }
            }
            break;
            case Constants.TYPE_NO_CONNECT_VIDEO: {
                String content;
                if (message.getTimeLen() == 0) {
                    content = InternationalizationHelper.getString("JXSip_Canceled") + InternationalizationHelper.getString("JX_VideoChat");
                } else {
                    content = InternationalizationHelper.getString("JXSip_noanswer");
                }

                mTvContent.setText(content);
                if (message.isMySend()) {
                    ivTextImage.setImageResource(R.drawable.chat_from_video_call_bg);
                } else {
                    ivTextImage.setImageResource(R.drawable.chat_to_video_call_bg);
                }
            }
            break;
            case Constants.TYPE_END_CONNECT_VIDEO: {
                // 结束
                int timeLen = message.getTimeLen();
                mTvContent.setText("通话时长" + "  " + DateUtils.getVoiceCallTime(timeLen));
                if (message.isMySend()) {
                    ivTextImage.setImageResource(R.drawable.chat_from_video_call_bg);
                } else {
                    ivTextImage.setImageResource(R.drawable.chat_to_video_call_bg);
                }
            }
            break;
        }
    }

    @Override
    protected void onRootClick(View v) {

    }


    /**
     * 重写该方法，return true 表示自动发送已读
     *
     * @return
     */
    @Override
    public boolean enableSendRead() {
        return true;
    }
}
