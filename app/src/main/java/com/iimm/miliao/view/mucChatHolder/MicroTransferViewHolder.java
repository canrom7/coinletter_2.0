package com.iimm.miliao.view.mucChatHolder;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.TransferMicro;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.pay.MicroTransferMoneyDetailActivity;
import com.iimm.miliao.pay.TransferMoneyDetailActivity;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.view.NoDoubleClickListener;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;

import okhttp3.Call;

/**
 * 微转账 holder
 */
class MicroTransferViewHolder extends AChatHolderInterface {

    TextView mTvContent;
    TextView mTvMoney;

    public MicroTransferViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_transfer : R.layout.chat_to_item_transfer;
    }

    @Override
    public void initView(View view) {
        mTvContent = view.findViewById(R.id.chat_text_desc);
        mTvMoney = view.findViewById(R.id.chat_text_money);
        mRootView = view.findViewById(R.id.chat_warp_view);
        TextView textView = view.findViewById(R.id.tv_type);
        textView.setText("微转账");
    }

    @Override
    public void fillData(ChatMessage message) {
        if (mdata.getFileSize() == 2) {// 已领取
            mRootView.setAlpha(0.6f);
        } else {
            mRootView.setAlpha(1f);
        }

        if (TextUtils.isEmpty(message.getFilePath())) {
            if (message.getFromUserId().equals(mLoginUserId)) {// 发送方 显示 转账给对方
                Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, message.getToUserId());
                if (friend != null) {
                    mTvContent.setText(getString(R.string.transfer_money_to_someone2,
                            TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName()));
                }
            } else {// 接收方 显示 转账给你
                mTvContent.setText(getString(R.string.transfer_money_to_someone3));
            }
        } else {// 转账说明
            mTvContent.setText(message.getFilePath());
        }
        mTvMoney.setText("￥" + message.getContent());

        mRootView.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                MicroTransferViewHolder.super.onClick(view);
            }
        });
    }

    @Override
    public boolean isOnClick() {
        return false; // 红包消息点击后回去请求接口，所以要做一个多重点击替换
    }

    @Override
    protected void onRootClick(View v) {
        getTransferInfo();
    }

    private void getTransferInfo() {
        final String token = CoreManager.requireSelfStatus(mContext).accessToken;
        final String redId = mdata.getObjectId();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", token);
        params.put("requestId", redId);

        HttpUtils.post().url(CoreManager.requireConfig(mContext).MICRO_RECHARGE_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<TransferMicro>(TransferMicro.class) {

                    @Override
                    public void onResponse(ObjectResult<TransferMicro> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            Intent intent = new Intent(mContext, MicroTransferMoneyDetailActivity.class);
                            intent.putExtra(AppConstant.EXTRA_MESSAGE_ID, mdata.getPacketId());
                            intent.putExtra(TransferMoneyDetailActivity.TRANSFER_DETAIL, JSON.toJSONString(result.getData()));
                            mContext.startActivity(intent);
                        } else {
                            ToastUtil.showToast(mContext, result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showToast(mContext, "网络异常！");
                    }
                });
    }

    @Override
    public boolean enableSendRead() {
        return true;
    }
}
