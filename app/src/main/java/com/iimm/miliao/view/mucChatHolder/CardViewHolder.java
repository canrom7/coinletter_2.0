package com.iimm.miliao.view.mucChatHolder;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.ui.other.BasicInfoActivity;


class CardViewHolder extends AChatHolderInterface {

    RoundedImageView ivCardImage;
    TextView tvPersonName;
    ImageView ivUnRead;

    public CardViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_card : R.layout.chat_to_item_card;
    }

    @Override
    public void initView(View view) {
        ivCardImage = view.findViewById(R.id.iv_card_head);
        tvPersonName = view.findViewById(R.id.person_name);
        ivUnRead = view.findViewById(R.id.unread_img_view);
        mRootView = view.findViewById(R.id.chat_warp_view);
    }

    @Override
    public void fillData(ChatMessage message) {
        AvatarHelper.getInstance().displayAvatar(message.getContent(), message.getObjectId(), ivCardImage, true);
        tvPersonName.setText(String.valueOf(message.getContent()));

        if (!isMysend) {
            ivUnRead.setVisibility(message.isSendRead() ? View.GONE : View.VISIBLE);
        }

        if (isMysend && enableFire() && message.getIsReadDel()) {
            ReadDelManager.getInstants().addReadMsg(message, this);
        }
    }

    @Override
    protected void onRootClick(View v) {
        sendReadMessage(mdata);
        ivUnRead.setVisibility(View.GONE);
        setRead();
        BasicInfoActivity.start(mContext, mdata.getObjectId(), BasicInfoActivity.FROM_ADD_TYPE_CARD);
        if (!isMysend && enableFire() && mdata.getIsReadDel()) {
            ReadDelManager.getInstants().addReadMsg(mdata, this);
        }
    }

    /**
     * 重写该方法，return true 表示显示红点
     *
     * @return
     */
    @Override
    public boolean enableUnRead() {
        return true;
    }

    @Override
    public boolean enableFire() {
        return true;
    }
}
