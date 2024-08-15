package com.iimm.miliao.view.mucChatHolder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.ClickRedAd;
import com.iimm.miliao.bean.ClickRedAdRetrun;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.bean.redpacket.EventRedReceived;
import com.iimm.miliao.bean.redpacket.OpenRedpacket;
import com.iimm.miliao.bean.redpacket.RedDialogBean;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.ui.me.redpacket.RedDetailsActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.HtmlUtils;
import com.iimm.miliao.util.StringUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.view.NoDoubleClickListener;
import com.iimm.miliao.view.redDialog.RedDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * 红包
 */
class RedViewHolder extends AChatHolderInterface {

    TextView mTvContent;
    TextView mTvType;

    boolean isKeyRed;
    private RedDialog mRedDialog;

    public RedViewHolder(@NonNull View itemView) {
        super(itemView);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(ClickRedAdRetrun message) {
        final String token = CoreManager.requireSelfStatus(mContext).accessToken;
        final String redId = mdata.getObjectId();
        if (mdata != null && redId != null) {
            if (redId.equals(message.getRedId())) {
                openRedPacket(token, redId, null);
            }else{
                if(mRedDialog!=null){
                    mRedDialog.dismiss();
                }
            }
        }else {
            if(mRedDialog!=null){
                mRedDialog.dismiss();
            }
        }
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

        isKeyRed = "3".equals(message.getFilePath());
        if (message.getType() == Constants.TYPE_READ_EXCLUSIVE) {
            mTvType.setText(getString(isKeyRed ? R.string.chat_kl_red_exclusive : R.string.chat_red_exclusive));
        } else {
            mTvType.setText(getString(isKeyRed ? R.string.chat_kl_red : R.string.chat_red));
        }

        mRootView.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                RedViewHolder.super.onClick(view);
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

        HashMap<String, String> params = new HashMap<>();
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
                            if (!TextUtils.isEmpty(result.getResultMsg()) && !result.getResultMsg().contains("未领完")) //resultMsg不为空表示红包已过期
                            {
                                bundle.putInt("timeOut", 1);
                            } else {
                                bundle.putInt("timeOut", 0);
                            }
                            bundle.putString("msg", result.getResultMsg());
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

                                            /**
                                             * 特权红包
                                             * @param money
                                             */
                                            @Override
                                            public void clickPrivilege(String money) {
                                                openRedPacket(token, redId, money);
                                            }

                                            @Override
                                            public void clickAd() {
                                                Log.e("aaaaaaaa", "clickAd");
                                                EventBus.getDefault().post(new ClickRedAd(redId));
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
                        ToastUtil.showToast(mContext, getString(R.string.data_exception));
                    }
                });
    }

    // 打开红包
    public void openRedPacket(final String token, String redId, String money) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", token);
        params.put("id", redId);
        if (!TextUtils.isEmpty(money)) {
            params.put("money", money);
        }

        HttpUtils.get().url(CoreManager.requireConfig(mContext).REDPACKET_OPEN)
                .params(params)
                .build()
                .execute(new BaseCallback<OpenRedpacket>(OpenRedpacket.class) {

                    @Override
                    public void onResponse(ObjectResult<OpenRedpacket> result) {
                        if (mRedDialog != null) {
                            mRedDialog.dismiss();
                        }
                        if (result.getResultCode() == 1) {
                            if (result.getData() != null) {
                                mdata.setFileSize(2);
                                ChatMessageDao.getInstance().updateChatMessageReceiptStatus(mLoginUserId, mToUserId, mdata.getPacketId());
                                fillData(mdata);

                                OpenRedpacket openRedpacket = result.getData();
                                Bundle bundle = new Bundle();
                                Intent intent = new Intent(mContext, RedDetailsActivity.class);
                                bundle.putSerializable("openRedpacket", openRedpacket);
                                bundle.putInt("redAction", 1);
                                bundle.putInt("timeOut", 0);
                                bundle.putString("msg", "未领完!");
                                bundle.putBoolean("isGroup", isGroup);
                                bundle.putString("mToUserId", mToUserId);
                                intent.putExtras(bundle);
                                mContext.startActivity(intent);
                                // 更新余额
                                CoreManager.updateMyBalance();

                                EventBus.getDefault().post(new EventRedReceived(openRedpacket));
                            } else {
                                ToastUtil.showToast(mContext, TextUtils.isEmpty(result.getResultMsg()) ? getString(R.string.data_exception) : result.getResultMsg());
                            }
                        } else {
                            ToastUtil.showToast(mContext, TextUtils.isEmpty(result.getResultMsg()) ? getString(R.string.data_exception) : result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        if (mRedDialog != null) {
                            mRedDialog.dismiss();
                        }
                        ToastUtil.showToast(mContext, getString(R.string.data_exception));
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
