package com.iimm.miliao.view.mucChatHolder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.bean.redpacket.CloudQueryRedPacket;
import com.iimm.miliao.bean.redpacket.EventCloudRedReceived;
import com.iimm.miliao.bean.redpacket.OpenRedpacket;
import com.iimm.miliao.bean.redpacket.RedDialogBean;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.ui.me.redpacket.CloudRedDetailsActivity;
import com.iimm.miliao.util.HtmlUtils;
import com.iimm.miliao.util.StringUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.view.NoDoubleClickListener;
import com.iimm.miliao.view.redDialog.RedDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * 云红包
 */
class CloudRedViewHolder extends AChatHolderInterface {

    TextView mTvContent;
    TextView mTvType;

    private RedDialog mRedDialog;

    public CloudRedViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_redpacket : R.layout.chat_to_item_redpacket;
    }

    @Override
    public void initView(View view) {
        mTvContent = view.findViewById(R.id.chat_text);
        mTvType = view.findViewById(R.id.tv_type);
        mRootView = view.findViewById(R.id.chat_warp_view);
    }

    @Override
    public void fillData(ChatMessage message) {
        if (mdata.getFileSize() == 2) {// 已领取
            mRootView.setAlpha(0.6f);
        } else {
            mRootView.setAlpha(1f);
        }

        String s = StringUtils.replaceSpecialChar(message.getContent());
        CharSequence charSequence = HtmlUtils.transform200SpanString(s.replaceAll("\n", "\r\n"), true);
        mTvContent.setText(charSequence);

        mTvType.setText(getString(R.string.cloud_chat_red));

        mRootView.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                CloudRedViewHolder.super.onClick(view);
            }
        });
    }

    @Override
    public boolean isOnClick() {
        return false; // 红包消息点击后回去请求接口，所以要做一个多重点击替换
    }

    @Override
    protected void onRootClick(View v) {
        clickRedpacket();
    }

    // 点击红包
    public void clickRedpacket() {
        final String token = CoreManager.requireSelfStatus(mContext).accessToken;
        final String redId = mdata.getObjectId();

        Map<String, String> params = new HashMap<>();
        params.put("access_token", token);
        params.put("requestId", redId);
        HttpUtils.post().url(CoreManager.requireConfig(mContext).REDPACKET_CREATE_INQUIRE)
                .params(params)
                .build()
                .execute(new BaseCallback<CloudQueryRedPacket>(CloudQueryRedPacket.class) {

                    @Override
                    public void onResponse(ObjectResult<CloudQueryRedPacket> result) {
                        if (result.getData() != null) {
                            // 当resultCode== 1 ：发出   2：已领完  -1：已退款  3:未领完退款
                            int resultCode = result.getData().getOrderStatus();
                            CloudQueryRedPacket cloudQueryRedPacket = result.getData();
                            Bundle bundle = new Bundle();
                            Intent intent = new Intent(mContext, CloudRedDetailsActivity.class);
                            bundle.putString("requestId", redId);
                            bundle.putBoolean("isGroup", isGroup);
                            bundle.putString("mToUserId", mToUserId);
                            intent.putExtras(bundle);

                            // 红包不可领取, 或者我发的单聊红包直接跳转
                            if (resultCode != 1 || (!isGroup && isMysend)) {
                                mContext.startActivity(intent);
                            } else {
                                // 在群里面我领取过的红包直接跳转
                                if (isGroup && mdata.getFileSize() != 1) {
                                    mContext.startActivity(intent);
                                } else {
                                    RedDialogBean redDialogBean = new RedDialogBean(
                                            cloudQueryRedPacket.getUserId(),
                                            cloudQueryRedPacket.getNickName(),
                                            cloudQueryRedPacket.getGreeting(),
                                            cloudQueryRedPacket.getPacketType()+"",
                                            cloudQueryRedPacket.getPacketType(),
                                            0,
                                            isGroup);
                                    mRedDialog = new RedDialog(mContext, redDialogBean, new RedDialog.OnClickRedListener() {
                                        @Override
                                        public void clickRed() {
                                            openCloudRedPacket(token, redId,cloudQueryRedPacket);
                                        }

                                        /**
                                         * 特权红包
                                         * @param money
                                         */
                                        @Override
                                        public void clickPrivilege(String money) {
                                            openCloudRedPacket(token, redId,cloudQueryRedPacket);
                                        }

                                        @Override
                                        public void clickAd() {
                                            Log.e("aaaaaaaa","222222222222");
                                        }
                                    });

                                    mRedDialog.show();
                                }
                            }
                        } else {
                            ToastUtil.showToast(mContext, result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        // 发送红包失败，
                        ToastUtil.showToast(mContext, "获取详情失败");
                    }
                });


  /*      HashMap<String, String> params = new HashMap<>();
        params.put("access_token", token);
        params.put("id", redId);

        HttpUtils.get().url(CoreManager.requireConfig(mContext).RENDPACKET_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<OpenRedpacket>(OpenRedpacket.class) {

                    @Override
                    public void onResponse(ObjectResult<OpenRedpacket> result) {
                        if (result.getData() != null) {
                            // 当resultCode==1时，表示可领取
                            // 当resultCode==0时，表示红包已过期、红包已退回、红包已领完
                            int resultCode = result.getResultCode();
                            OpenRedpacket openRedpacket = result.getData();
                            Bundle bundle = new Bundle();
                            Intent intent = new Intent(mContext, RedDetailsActivity.class);
                            bundle.putSerializable("openRedpacket", openRedpacket);
                            bundle.putInt("redAction", 0);
                            if (!TextUtils.isEmpty(result.getResultMsg())) //resultMsg不为空表示红包已过期
                            {
                                bundle.putInt("timeOut", 1);
                            } else {
                                bundle.putInt("timeOut", 0);
                            }

                            bundle.putBoolean("isGroup", isGroup);
                            bundle.putString("mToUserId", mToUserId);
                            intent.putExtras(bundle);

                            // 红包不可领取, 或者我发的单聊红包直接跳转
                            if (resultCode != 1 || (!isGroup && isMysend)) {
                                mContext.startActivity(intent);
                            } else {
                                // 在群里面我领取过的红包直接跳转
                                if (isGroup && mdata.getFileSize() != 1) {
                                    mContext.startActivity(intent);
                                } else {
                                    if (mdata.getFilePath().equals("3")) {
                                        // 口令红包编辑输入框
                                        changeBottomViewInputText(mdata.getContent());
                                    } else {
                                        RedDialogBean redDialogBean = new RedDialogBean(openRedpacket.getPacket().getUserId(), openRedpacket.getPacket().getUserName(),
                                                openRedpacket.getPacket().getGreetings(), openRedpacket.getPacket().getId(), openRedpacket.getPacket().getType(), CoreManager.getSelf(mContext).getRedPacketVip(), isGroup);
                                        mRedDialog = new RedDialog(mContext, redDialogBean, new RedDialog.OnClickRedListener() {
                                            @Override
                                            public void clickRed() {
                                                openRedPacket(token, redId, null);
                                            }

                                            *//**
         * 特权红包
         * @param money
         *//*
                                            @Override
                                            public void clickPrivilege(String money) {
                                                openRedPacket(token, redId, money);
                                            }
                                        });

                                        mRedDialog.show();
                                    }
                                }
                            }
                        } else {
                            ToastUtil.showToast(mContext, result.getResultMsg());
                        }

                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });*/
    }

    // 打开红包
    public void openCloudRedPacket(final String token, String redId,CloudQueryRedPacket cloudQueryRedPacket) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", token);
        params.put("requestId", redId);
        HttpUtils.post().url(CoreManager.requireConfig(mContext).REDPACKET_GRAD)
                .params(params)
                .build()
                .execute(new BaseCallback<OpenRedpacket>(OpenRedpacket.class) {

                    @Override
                    public void onResponse(ObjectResult<OpenRedpacket> result) {
                        if (mRedDialog != null) {
                            mRedDialog.dismiss();
                        }
                        if (result.getData() != null) {
                            mdata.setFileSize(2);
                            ChatMessageDao.getInstance().updateChatMessageReceiptStatus(mLoginUserId, mToUserId, mdata.getPacketId());
                            fillData(mdata);

                            Bundle bundle = new Bundle();
                            Intent intent = new Intent(mContext, CloudRedDetailsActivity.class);
                            bundle.putSerializable("requestId", redId);
                            bundle.putBoolean("isGroup", isGroup);
                            bundle.putString("mToUserId", mToUserId);
                            intent.putExtras(bundle);
                            mContext.startActivity(intent);

                            EventBus.getDefault().post(new EventCloudRedReceived(cloudQueryRedPacket));
                        } else {
                            ToastUtil.showToast(mContext, result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        if (mRedDialog != null) {
                            mRedDialog.dismiss();
                        }
                    }
                });
    }

    // 通知更新输入框
    private void changeBottomViewInputText(String text) {
        mHolderListener.onChangeInputText(text);
    }

    @Override
    public boolean enableSendRead() {
        return true;
    }
}
