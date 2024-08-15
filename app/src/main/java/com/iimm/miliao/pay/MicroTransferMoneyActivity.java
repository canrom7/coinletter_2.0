package com.iimm.miliao.pay;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ehking.sdk.wepay.interfaces.WalletPay;
import com.ehking.sdk.wepay.net.bean.AuthType;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.EventTransfer;
import com.iimm.miliao.bean.TransferMicro;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ToolUtils;
import com.iimm.miliao.view.window.KeyBoad;
import com.iimm.miliao.xmpp.util.ImHelper;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * 微转账
 */
public class MicroTransferMoneyActivity extends BaseActivity {
    private String mTransferredUserId, mTransferredName;

    private ImageView mTransferredIv;
    private TextView mTransferredTv;

    private String money, words;// 转账金额与转账说明
    private TextView mTransferDescClickTv;
    private EditText mTransferDescTv;

    private EditText et_transfer;
    private TextView money_tv;
    private KeyBoad keyBoad;
    private boolean isUiCreat = false;
    private WalletPay mWalletPay;
    private String requestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_money_micro);
        mTransferredUserId = getIntent().getStringExtra(AppConstant.EXTRA_USER_ID);
        mTransferredName = getIntent().getStringExtra(AppConstant.EXTRA_NICK_NAME);
        initActionBar();
        initView();
        initEvent();
        initKeyBoad();
        checkHasPayPassword();
    }

    private void checkHasPayPassword() {

    }

    private void initActionBar() {
        getSupportActionBar().hide();
        ImageView imageView = findViewById(R.id.iv_title_left);
        imageView.setImageResource(R.drawable.icon_close_circle);
        imageView.setOnClickListener(view -> finish());
        TextView titleTv = findViewById(R.id.tv_title_center);
        titleTv.setText(getString(R.string.micro_transfer_money));
    }

    private void initView() {
        mTransferredIv = findViewById(R.id.tm_iv);
        mTransferredTv = findViewById(R.id.tm_tv);
        AvatarHelper.getInstance().displayAvatar(mTransferredUserId, mTransferredIv);
        mTransferredTv.setText(mTransferredName);

        mTransferDescTv = findViewById(R.id.transfer_desc_tv);
        mTransferDescClickTv = findViewById(R.id.transfer_edit_desc_tv);


        et_transfer = findViewById(R.id.et_transfer);
        money_tv = findViewById(R.id.money_tv);
        keyBoad = new KeyBoad(MicroTransferMoneyActivity.this, getWindow().getDecorView(), et_transfer);

        mWalletPay = WalletPay.Companion.getInstance();
        mWalletPay.init(this);
        mWalletPay.setWalletPayCallback(new WalletPay.WalletPayCallback() {
            @Override
            public void callback(@Nullable String s, @Nullable String status, @Nullable String s2) {
                if ((TextUtils.equals("SUCCESS", status) || TextUtils.equals("PROCESS", status)) && !TextUtils.isEmpty(requestId)) {
                    quireTransfer();
                } else {
                    ToastUtil.showToast(mContext, "转账失败");
                }

            }
        });
    }

    private void initEvent() {
        /*
        mTransferDescClickTv.setOnClickListener(v -> {
            VerifyDialog verifyDialog = new VerifyDialog(mContext);
            verifyDialog.setVerifyClickListener(getString(R.string.transfer_money_desc), getString(R.string.transfer_desc_max_length_10),
                    words, 10, new VerifyDialog.VerifyClickListener() {
                        @Override
                        public void cancel() {

                        }

                        @Override
                        public void send(String str) {
                            words = str;
                            if (TextUtils.isEmpty(words)) {
                                mTransferDescTv.setText("");
                                mTransferDescTv.setVisibility(View.GONE);
                                mTransferDescClickTv.setText(getString(R.string.transfer_money_desc));
                            } else {
                                mTransferDescTv.setText(str);
                                mTransferDescTv.setVisibility(View.VISIBLE);
                                mTransferDescClickTv.setText(getString(R.string.transfer_modify));
                            }
                            keyBoad.show();
                        }

                    });
            verifyDialog.setOkButton(R.string.sure);
            keyBoad.dismiss();
            Window window = verifyDialog.getWindow();

            if (window != null) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); // 软键盘弹起
            }
            verifyDialog.show();
        });*/

//        findViewById(R.id.transfer_btn).setBackgroundColor(SkinUtils.getSkin(this).getAccentColor());
        findViewById(R.id.transfer_btn).setOnClickListener(v -> {
            money = et_transfer.getText().toString().trim();

            if (TextUtils.isEmpty(money) || Double.parseDouble(money) <= 0) {
                Toast.makeText(mContext, getString(R.string.transfer_input_money), Toast.LENGTH_SHORT).show();
                return;
            }

            if (money.endsWith(".")) {
                money = money.replace(".", "");
            }
            words = mTransferDescTv.getText().toString().trim();
            transfer(money, words);
            /*PayPasswordVerifyDialog dialog = new PayPasswordVerifyDialog(this);
            dialog.setAction(getString(R.string.transfer_money_to_someone, mTransferredName));
            dialog.setMoney(money);
            words = mTransferDescTv.getText().toString().trim();
            dialog.setOnInputFinishListener(password -> transfer(money, words, password));
            dialog.show();*/
        });
    }

    private void initKeyBoad() {
        et_transfer.setOnFocusChangeListener((v, hasFocus) -> {
            if (keyBoad != null && isUiCreat) {
                keyBoad.refreshKeyboardOutSideTouchable(!hasFocus);
            } else if (isUiCreat) {
                keyBoad.show();
            }
            if (hasFocus) {
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(et_transfer.getWindowToken(), 0);
            }
        });
        et_transfer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (text.startsWith(".")) {
                    et_transfer.setText("0" + text);

                } else if (text.startsWith("0") && !text.contains(".") && text.length() > 1) {
                    et_transfer.setText(text.substring(1, text.length()));
                }
                if (TextUtils.isEmpty(et_transfer.getText().toString().trim())) {
                    money_tv.setText("￥0.00");
                } else {
                    money_tv.setText(String.format("￥%s", et_transfer.getText().toString().trim()));
                }


            }
        });

        et_transfer.setOnClickListener(v -> {
            if (keyBoad != null) {
                keyBoad.show();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        isUiCreat = true;
    }

    /**
     * 预下单
     *
     * @param money
     * @param words
     */
    public void transfer(String money, final String words) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("targetUserId", mTransferredUserId);
        params.put("amount", money);
        params.put("remark", words);

        HttpUtils.post().url(coreManager.getConfig().MICRO_TRANSFER_SEND_TRANSFER)
                .params(params)
                .build()
                .execute(new BaseCallback<TransferMicro>(TransferMicro.class) {

                    @Override
                    public void onResponse(ObjectResult<TransferMicro> result) {
                        TransferMicro transfer = result.getData();
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() != 1) {
                            ToastUtil.showToast(mContext, result.getResultMsg());
                        } else {
                            requestId = transfer.getRequestId();
                            mWalletPay.evoke(transfer.getMerchantId(), transfer.getWalletId(), transfer.getToken(), AuthType.TRANSFER.name(), "");
                           /* ChatMessage message = new ChatMessage();
                            message.setType(Constants.TYPE_CLOUD_TRANSFER);
                            message.setFromUserId(coreManager.getSelf().getUserId());
                            message.setFromUserName(coreManager.getSelf().getNickName());
                            message.setToUserId(mTransferredUserId);
                            message.setContent(money);// 转账金额
                            message.setFilePath(words); // 转账说明
                            message.setObjectId(requestId); // 红包id
                            message.setPacketId(ToolUtils.getUUID());
                            message.setDoubleTimeSend(TimeUtils.time_current_time_double());
                            EventBus.getDefault().post(new EventTransfer(message));
                            finish();*/
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(MicroTransferMoneyActivity.this, "请求失败");
                    }
                });
    }


    /**
     * 查询转账
     */
    public void quireTransfer() {
        if (!ImHelper.checkXmppAuthenticated()) {
            return;
        }
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("requestId", requestId);
        HttpUtils.post().url(coreManager.getConfig().MICRO_RECHARGE_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<TransferMicro>(TransferMicro.class) {

                    @Override
                    public void onResponse(ObjectResult<TransferMicro> result) {
                        TransferMicro transfer = result.getData();
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() != 1 && transfer != null) {
                            ToastUtil.showToast(mContext, result.getResultMsg());
                        } else {
                            requestId = transfer.getRequestId();
                            ChatMessage message = new ChatMessage();
                            message.setType(Constants.TYPE_CLOUD_TRANSFER);
                            message.setFromUserId(coreManager.getSelf().getUserId());
                            message.setFromUserName(coreManager.getSelf().getNickName());
                            message.setToUserId(mTransferredUserId);
                            message.setContent(money);// 转账金额
                            message.setFilePath(words); // 转账说明
                            message.setObjectId(requestId); // 红包id
                            message.setPacketId(ToolUtils.getUUID());
                            message.setDoubleTimeSend(TimeUtils.time_current_time_double());
                            EventBus.getDefault().post(new EventTransfer(message));
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(MicroTransferMoneyActivity.this, "请求失败");
                    }
                });
    }
}
