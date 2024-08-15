package com.iimm.miliao.pay;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.payeasenet.wepay.ui.activity.MainActivity;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.EventTransfer;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.MicroTransferReceive;
import com.iimm.miliao.bean.TransferMicro;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.SkinUtils;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

import static com.iimm.miliao.pay.TransferMoneyDetailActivity.EVENT_SURE_RECEIPT;
import static com.iimm.miliao.pay.TransferMoneyDetailActivity.TRANSFER_DETAIL;

/**
 * 微转账详情
 */
public class MicroTransferMoneyDetailActivity extends BaseActivity {

    private String mMsgId;
    private TransferMicro mTransfer;

    private boolean isMySend;// 转账人为我
    private String mToUserName;// 收账人昵称

    private ImageView mTransferStatusIv;
    private TextView mTransferTips1Tv, mTransferTips2Tv, mTransferTips3Tv;
    private TextView mTransferMoneyTv;
    private Button mTransferSureBtn;
    private TextView mTransferTime1Tv, mTransferTime2Tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_money_detail);
        mMsgId = getIntent().getStringExtra(AppConstant.EXTRA_MESSAGE_ID);
        String detail = getIntent().getStringExtra(TRANSFER_DETAIL);
        mTransfer = JSON.parseObject(detail, TransferMicro.class);
        if (mTransfer == null) {
            return;
        }
        isMySend = TextUtils.equals(mTransfer.getUserId(), coreManager.getSelf().getUserId());
        if (isMySend) {
            Friend friend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), mTransfer.getTargetUserId());
            mToUserName = TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName();
        }
        initActionBar();
        initView();
        initData();
        initEvent();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
    }

    private void initView() {
        mTransferStatusIv = findViewById(R.id.ts_status_iv);
        mTransferMoneyTv = findViewById(R.id.ts_money);
        mTransferTips1Tv = findViewById(R.id.ts_tip1_tv);
        mTransferTips2Tv = findViewById(R.id.ts_tip2_tv);
        mTransferTips3Tv = findViewById(R.id.ts_tip3_tv);
        mTransferTime1Tv = findViewById(R.id.ts_time1_tv);

        mTransferSureBtn = findViewById(R.id.ts_sure_btn);
        mTransferSureBtn.setBackgroundColor(SkinUtils.getSkin(this).getAccentColor());

        mTransferTime2Tv = findViewById(R.id.ts_time2_tv);
    }

    private void initData() {
        mTransferSureBtn.setVisibility(View.GONE);
        mTransferMoneyTv.setText("￥" + String.valueOf(mTransfer.getAmount()));
        if (!TextUtils.isEmpty(mTransfer.getCreateDateTime())){
            mTransferTime1Tv.setText(getString(R.string.transfer_time, mTransfer.getCreateDateTime()));
        }

        if (TextUtils.equals(mTransfer.getOrderStatus(),"SEND")) {// 待领取
            mTransferStatusIv.setImageResource(R.drawable.ic_ts_status2);
            if (isMySend) {
                mTransferTips1Tv.setText(getString(R.string.transfer_wait_receive1, mToUserName));
                mTransferTips2Tv.setText(getString(R.string.transfer_receive_status1));
//                mTransferTips3Tv.setText(getString(R.string.transfer_receive_click_status1));
            } else {
                mTransferSureBtn.setVisibility(View.VISIBLE);
                mTransferTips1Tv.setText(getString(R.string.transfer_push_receive1));
                mTransferTips2Tv.setText(getString(R.string.transfer_push_receive2));
            }
        } else if (TextUtils.equals(mTransfer.getOrderStatus(),"SUCCESS")) {// 已收钱
            mTransferStatusIv.setImageResource(R.drawable.ic_ts_status1);
            if (isMySend) {
                mTransferTips1Tv.setText(getString(R.string.transfer_wait_receive2, mToUserName));
                mTransferTips2Tv.setText(getString(R.string.micro_transfer_receive_status2));
                mTransferTips3Tv.setVisibility(View.GONE);
            } else {
                mTransferTips1Tv.setText(getString(R.string.transfer_push_receive3));
                mTransferTips2Tv.setVisibility(View.GONE);
                mTransferTips3Tv.setText(getString(R.string.micro_transfer_receive_click_status2));
            }
            mTransferTime2Tv.setText(getString(R.string.transfer_receive_time, mTransfer.getCompleteDateTime()));
        } else {// 已退回
            mTransferStatusIv.setImageResource(R.drawable.ic_ts_status3);
            mTransferTips1Tv.setText(getString(R.string.transfer_wait_receive3));
            if (isMySend) {
                mTransferTips2Tv.setText(getString(R.string.micro_transfer_receive_status3));
                mTransferTips3Tv.setText(getString(R.string.micro_transfer_receive_click_status2));
            }
            if (!TextUtils.isEmpty(mTransfer.getCompleteDateTime())){
                mTransferTime2Tv.setText(getString(R.string.transfer_out_time, mTransfer.getCompleteDateTime()));
            }
        }
    }

    private void initEvent() {
        mTransferTips3Tv.setOnClickListener(v -> {
            /*if (TextUtils.equals(mTransfer.getOrderStatus(),"SEND")) {
                // 通知到聊天界面刷新ui
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setType(EVENT_REISSUE_TRANSFER);
                chatMessage.setPacketId(mMsgId);
                EventBus.getDefault().post(new EventTransfer(chatMessage));
                finish();
                return;
            }*/
            // 查看零钱
            startActivity(new Intent(mContext, MainActivity.class));
        });

        mTransferSureBtn.setOnClickListener(v -> acceptTransfer(coreManager.getSelfStatus().accessToken, mTransfer.getRequestId()));
    }

    // 接受转账
    private void acceptTransfer(String token, String redId) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", token);
        params.put("requestId", redId);

        HttpUtils.post().url(CoreManager.requireConfig(mContext).MICRO_RECHARGE_ACCEPT)
                .params(params)
                .build()
                .execute(new BaseCallback<MicroTransferReceive>(MicroTransferReceive.class) {

                    @Override
                    public void onResponse(ObjectResult<MicroTransferReceive> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            getTransferInfo(redId);
                            // 通知到聊天界面刷新ui
                            ChatMessage chatMessage = new ChatMessage();
                            chatMessage.setType(EVENT_SURE_RECEIPT);
                            chatMessage.setPacketId(mMsgId);
                            EventBus.getDefault().post(new EventTransfer(chatMessage));
                        } else {
                            ToastUtil.showToast(MicroTransferMoneyDetailActivity.this,result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showToast(MicroTransferMoneyDetailActivity.this,"获取数据失败");
                    }
                });
    }

    /**
     * 刷新界面
     */
    private void getTransferInfo(String redId) {
        final String token = CoreManager.requireSelfStatus(mContext).accessToken;
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
                            mTransfer = result.getData();
                            initData();
                        } else {
                            mTransfer.setOrderStatus("SUCCESS");
//                            mTransfer.setDebitDateTime();
                            mTransferTips1Tv.setVisibility(View.GONE);
                            initData();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        mTransfer.setOrderStatus("SUCCESS");
//                            mTransfer.setDebitDateTime();
                        mTransferTips1Tv.setVisibility(View.GONE);
                        initData();
                    }
                });
    }
}
