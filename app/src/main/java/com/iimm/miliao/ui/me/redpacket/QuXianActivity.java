package com.iimm.miliao.ui.me.redpacket;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dhh.easy.chat.wxapi.WXEntryActivity;
import com.iimm.miliao.AppConfig;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.BlanceInfo;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.me.SelectorBlanceActivity;
import com.iimm.miliao.ui.me.redpacket.alipay.AlipayHelper;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.Md5Util;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.view.window.KeyBoad;
import com.iimm.miliao.volley.Result;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

public class QuXianActivity extends BaseActivity {
    private static final int ALI_TO_ADMIN = 0;
    private static final int BANK_TO_ADMIN = 1;
    public static String amount;// 提现金额 单位:分
    private IWXAPI api;
    private RadioGroup mMentionMoneyEdit;
    private TextView mBalanceTv;
    private TextView mAllMentionTv;
    private TextView mSureMentionTv;
    private TextView tvAliPay;
    private TextView tvWxPay;
    private TextView mMember;
    private String mLoginUserId;
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private LinearLayout alipayll;
    private LinearLayout wxpayll;
    private ImageView alipayselector;
    private ImageView wxpayselector;
    private Button sure;
    private View view_pay_type_line;
    private View ll_with_draw_type;
    private int paytype = 0;
    private int currentWithDrawToAdminType = ALI_TO_ADMIN;
    private KeyBoad keyBoad;
    private boolean isUiCreat = false;
    private LinearLayout mLlWithDrawToAdmin, mLlWithDrawToAdmin2;
    private LinearLayout mLlaliPay;
    private ImageView mIvSelect1;
    private LinearLayout mLlInputAli;
    private EditText mEtInputAliAccount;
    private EditText mEtInputAliName;
    private LinearLayout mLlBankCard;
    private ImageView mIvSelect2;
    private LinearLayout mLlInputBankCard;
    private EditText mEtInputBankId;
    private EditText mEtInputBankAccountName;
    private EditText mEtInputOpenAccountBank;
    private RelativeLayout addway;
    private ImageView logo;
    private TextView name;
    private BlanceInfo.MethodBean methodBean;
    private String money;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qu_xian);

        api = WXAPIFactory.createWXAPI(QuXianActivity.this, coreManager.getConfig().wechatAppId, false);
        api.registerApp(coreManager.getConfig().wechatAppId);
        mLoginUserId = coreManager.getSelf().getUserId();

        initActionbar();
        initView();
        intEvent();
        initKeyBoad();
        checkHasPayPassword();
    }

    private void initKeyBoad() {
        mMentionMoneyEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (keyBoad != null && isUiCreat) {
                    keyBoad.refreshKeyboardOutSideTouchable(!hasFocus);
                } else if (isUiCreat) {
                    keyBoad.show();
                }
                if (hasFocus) {
                    InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(mMentionMoneyEdit.getWindowToken(), 0);
                }
            }
        });


        mMentionMoneyEdit.setOnClickListener(v -> {
            if (keyBoad != null) {
                keyBoad.show();
            }
        });
    }

    private void checkHasPayPassword() {
        boolean hasPayPassword = PreferenceUtils.getBoolean(this, Constants.IS_PAY_PASSWORD_SET + coreManager.getSelf().getUserId(), true);
        if (!hasPayPassword) {
            Intent intent = new Intent(this, ChangePayPasswordActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initActionbar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText(getString(R.string.withdraw));
/*
        TextView mTvTitleRight = (TextView) findViewById(R.id.tv_title_right);
        mTvTitleRight.setText(getString(R.string.withdrawal_instructions));
*/
    }

    private void initView() {
        mMentionMoneyEdit = (RadioGroup) findViewById(R.id.tixianmoney);
//        mMentionMoneyEdit.setHighlightColor(getResources().getColor(R.color.color_00));
        mBalanceTv = (TextView) findViewById(R.id.blance_weixin);
        mBalanceTv.setText("可用余额：" + decimalFormat.format(coreManager.getSelf().getBalance()));
        mAllMentionTv = (TextView) findViewById(R.id.all_withdrawal);
//        mSureMentionTv = (TextView) findViewById(R.id.tixian);
        tvAliPay = (TextView) findViewById(R.id.alipay_tv);
        tvWxPay = findViewById(R.id.wxpay_tv);
        tvAliPay.setText(getString(R.string.alipay_withdrawal));
        tvWxPay.setText(getString(R.string.wechat_withdrawal));
        mLlWithDrawToAdmin = findViewById(R.id.ll_withdraw_to_admin);
        mLlWithDrawToAdmin2 = findViewById(R.id.ll_withdraw_to_admin2);
        mLlaliPay = findViewById(R.id.ll_ali_pay);
        mIvSelect1 = findViewById(R.id.iv_select1);
        mLlInputAli = findViewById(R.id.ll_input_ali);
        mEtInputAliAccount = findViewById(R.id.et_input_ali_account);
        mEtInputAliName = findViewById(R.id.et_input_ali_name);
        mLlBankCard = findViewById(R.id.ll_bank_card);
        mIvSelect2 = findViewById(R.id.iv_select2);
        mLlInputBankCard = findViewById(R.id.ll_input_bank_card);
        mEtInputBankId = findViewById(R.id.et_input_bank_id);
        mEtInputBankAccountName = findViewById(R.id.et_input_bank_account_name);
        addway = findViewById(R.id.addway);
        logo = findViewById(R.id.logo);
        name = findViewById(R.id.name);

//        mMember = findViewById(R.id.tv_tixian);
        alipayll = findViewById(R.id.alipayll);
        wxpayll = findViewById(R.id.wxll);
        alipayselector = findViewById(R.id.alipayselector);
        wxpayselector = findViewById(R.id.wxpayselector);
        sure = findViewById(R.id.surepay);
        view_pay_type_line = findViewById(R.id.view_pay_type_line);
        ll_with_draw_type = findViewById(R.id.ll_with_draw_type);
        if (coreManager.getConfig().isWithdrawToAdmin == 1) {  //1 是开启
            ll_with_draw_type.setVisibility(View.GONE);
            if (Constants.SUPPORT_CASH_BACK) {
                mLlWithDrawToAdmin.setVisibility(View.GONE);
                mLlWithDrawToAdmin2.setVisibility(View.VISIBLE);
                initDataList();
            } else {
                mLlWithDrawToAdmin.setVisibility(View.VISIBLE);
                mLlWithDrawToAdmin2.setVisibility(View.GONE);
            }
            paytype = 2;
        } else {
            ll_with_draw_type.setVisibility(View.VISIBLE);
            mLlWithDrawToAdmin.setVisibility(View.GONE);
            mLlWithDrawToAdmin2.setVisibility(View.GONE);
        }
        if (coreManager.getConfig().wechatWithdrawStatus == 1) {
            wxpayll.setVisibility(View.VISIBLE);
        } else {
            wxpayll.setVisibility(View.GONE);
        }
        if (coreManager.getConfig().aliWithdrawStatus == 1) {
            alipayll.setVisibility(View.VISIBLE);
        } else {
            alipayll.setVisibility(View.GONE);
        }


    }

    private void intEvent() {
        mLlaliPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLlInputAli.setVisibility(View.VISIBLE);
                mLlInputBankCard.setVisibility(View.GONE);
                mIvSelect1.setBackgroundResource(R.drawable.sel_check_wx_new);
                mIvSelect2.setBackgroundResource(R.drawable.sel_nor_wx);
                currentWithDrawToAdminType = ALI_TO_ADMIN;
            }
        });
        mLlBankCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLlInputAli.setVisibility(View.GONE);
                mLlInputBankCard.setVisibility(View.VISIBLE);
                mIvSelect1.setBackgroundResource(R.drawable.sel_nor_wx);
                mIvSelect2.setBackgroundResource(R.drawable.sel_check_wx_new);
                currentWithDrawToAdminType = BANK_TO_ADMIN;
            }
        });

        mMentionMoneyEdit.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                for(int i=0;i<group.getChildCount();i++){
                    if(group.getChildAt(i).getId() == checkedId){
                        money = ((RadioButton)group.getChildAt(i)).getText().toString();
                        return;
                    }
                }
            }
        });


        mAllMentionTv.setOnClickListener(v -> {
//            mMentionMoneyEdit.setText(decimalFormat.format(coreManager.getSelf().getBalance()) + "");
        });
        /*
         * 选择支付方式，跟新选中状态
         * */
        alipayll.setOnClickListener(v -> {
            alipayselector.setBackgroundResource(R.drawable.sel_check_wx_new);
            wxpayselector.setBackgroundResource(R.drawable.sel_nor_wx2);
            paytype = 0;
        });
        wxpayll.setOnClickListener(v -> {
            wxpayselector.setBackgroundResource(R.drawable.sel_check_wx_new);
            alipayselector.setBackgroundResource(R.drawable.sel_nor_wx2);
            paytype = 1;
        });
        sure.setOnClickListener(v -> {
//            String moneys = mMentionMoneyEdit.getText().toString().trim();
            Log.e("6666",money);
            String moneys = money;
            if (TextUtils.isEmpty(moneys)) {
                ToastUtil.showToast(QuXianActivity.this, "请输入提现金额");
                return;
            }
            switch (paytype) {
                case 0:
                    if (checkMoney(moneys)) {
                        amount = moneys;

                        AlipayHelper.auth(this, coreManager, userId -> {
                            AlipayHelper.withdraw(this, coreManager, amount, userId);
                        });
                    }
                    break;
                case 1:
                    if (checkMoney(moneys)) {
                        amount = moneys;
                        WXEntryActivity.wxQuXian(QuXianActivity.this, coreManager.getConfig().wechatAppId);
                    }
                    break;
                case 2:
                    if (Constants.SUPPORT_CASH_BACK) {
                        if (methodBean == null) {
                            ToastUtil.showToast(QuXianActivity.this, "请选择提现方式");
                            return;
                        }
                        if (!checkMoney(moneys)) {
                            return;
                        }
                        showPasswordDialog("", moneys, "", "");
                    } else {
                        if (currentWithDrawToAdminType == ALI_TO_ADMIN) {
                            //支付宝到后台
                            String memberAccount = mEtInputAliAccount.getText().toString().trim();
                            String memberName = mEtInputAliName.getText().toString().trim();
                            if (TextUtils.isEmpty(memberAccount)) {
                                ToastUtil.showToast(QuXianActivity.this, getResources().getString(R.string.please_fill_in_the_alipay_account));
                                return;
                            }
                            if (TextUtils.isEmpty(memberName)) {
                                ToastUtil.showToast(QuXianActivity.this, getResources().getString(R.string.fill_in_the_alipay_name));
                                return;
                            }
                            if (!checkMoney(moneys)) {
                                return;
                            }
                            showPasswordDialog(memberAccount, moneys, memberName, "");
                        } else {
                            //银行卡到后台
                            String inputBankId = mEtInputBankId.getText().toString().trim();
                            String inputBankAccoutName = mEtInputBankAccountName.getText().toString().trim();
                            String inputOpenAccountBank = mEtInputOpenAccountBank.getText().toString().trim();
                            if (TextUtils.isEmpty(inputBankId)) {
                                ToastUtil.showToast(QuXianActivity.this, R.string.please_enter_the_bank_card_number);
                                return;
                            }
                            if (TextUtils.isEmpty(inputBankAccoutName)) {
                                ToastUtil.showToast(QuXianActivity.this, R.string.enter_bank_account_name);
                                return;
                            }
                            if (TextUtils.isEmpty(inputOpenAccountBank)) {
                                ToastUtil.showToast(QuXianActivity.this, R.string.enter_the_open_account_bank);
                                return;
                            }
                            if (!checkMoney(moneys)) {
                                return;
                            }
                            showPasswordDialog(inputBankId, moneys, inputBankAccoutName, inputOpenAccountBank);
                        }
                    }
                    break;
            }
        });
      /*  tvAlipay.setOnClickListener(v -> {
            String moneyStr = mMentionMoneyEdit.getText().toString();
            if (checkMoney(moneyStr)) {
                amount = moneyStr;

                AlipayHelper.auth(this, coreManager, userId -> {
                    AlipayHelper.withdraw(this, coreManager, amount, userId);
                });
            }
        });

        mMemberAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String money = mMentionMoneyEdit.getText().toString().trim();
                String name = mMemberAccount.getText().toString().trim();

                if (!TextUtils.isEmpty(money) && !TextUtils.isEmpty(name)) {
                    resetTiXianStatus(true);
                } else {
                    resetTiXianStatus(false);
                }
            }
        });

        mMember.setOnClickListener(v -> {
            //TODO  提现到后台
            String memberAccount = mMemberAccount.getText().toString().trim();
            String moneyStr = mMentionMoneyEdit.getText().toString();
            if (checkMoney(moneyStr) && !TextUtils.isEmpty(memberAccount)) {
                showPasswordDialog(memberAccount, moneyStr);
            }
        });*/
        addway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QuXianActivity.this, SelectorBlanceActivity.class);
                if (methodBean != null) {
                    if (methodBean.getType() == 1) {
                        intent.putExtra("id", methodBean.getAlipayId());
                    } else if (methodBean.getType() == 5) {
                        intent.putExtra("id", methodBean.getBankId());
                    } else {
                        intent.putExtra("id", methodBean.getOtherId());
                    }
                }
                startActivityForResult(intent, 10086);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 10086) {
                methodBean = (BlanceInfo.MethodBean) data.getSerializableExtra("datas");
                if (methodBean == null) {
                    logo.setWillNotDraw(true);
                    name.setText("");
                    return;
                }
                logo.setWillNotDraw(false);
                if (methodBean.getType() == 1) {
                    logo.setImageResource(R.drawable.alipay_logo);
                    name.setText(methodBean.getAlipayNumber());
                } else if (methodBean.getType() == 5) {
                    logo.setImageResource(R.mipmap.bank_icon);
                    name.setText(String.format("%s (%s)", methodBean.getBankName(), methodBean.getBankCardNo()));
                } else {
                    logo.setImageResource(R.mipmap.icon_dynamic);
                    name.setText(methodBean.getOtherNode1() + " " + methodBean.getOtherNode2());
                }
            }
        }
    }

    private void initDataList() {
        HttpUtils.get().url(coreManager.getConfig().LIST_BLANCE)
                .params("access_token", coreManager.getSelfStatus().accessToken)
                .build()
                .execute(new BaseCallback<BlanceInfo>(BlanceInfo.class) {
                    @Override
                    public void onResponse(ObjectResult<BlanceInfo> result) {
                        if (result.getData() != null) {
                            List<BlanceInfo.MethodBean> datas = new ArrayList<>();
                            datas.clear();
                            datas.addAll(result.getData().getAlipayMethod());
                            datas.addAll(result.getData().getBankCardMethod());
                            datas.addAll(result.getData().getOtherMethod());
                            if (datas.size() != 0) {
                                methodBean = datas.get(0);
                                if (methodBean == null) {
                                    return;
                                }
                                if (methodBean.getType() == 1) {
                                    logo.setImageResource(R.drawable.alipay_logo);
                                    name.setText(methodBean.getAlipayNumber());
                                } else if (methodBean.getType() == 5) {
                                    logo.setImageResource(R.mipmap.bank_icon);
                                    name.setText(String.format("%s (%s)", methodBean.getBankName(), methodBean.getBankCardNo()));
                                } else {
                                    logo.setImageResource(R.mipmap.icon_dynamic);
                                    name.setText(methodBean.getOtherNode1() + " " + methodBean.getOtherNode2());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(QuXianActivity.this);
                    }
                });
    }

    private void showPasswordDialog(String memberAccount, String money, String name, String inputOpenAccountBank) {
        PayPasswordVerifyDialog dialog = new PayPasswordVerifyDialog(this, money);
        dialog.setAction(this.getString(R.string.withdraw));
        // amount单位是分，而且是整元乘100得到的分，一定可以被100整除，
        dialog.setOnInputFinishListener(password -> {
            DialogHelper.showDefaulteMessageProgressDialog(QuXianActivity.this);
            String mAccessToken = coreManager.getSelfStatus().accessToken;
            String mLoginUserId = coreManager.getSelf().getUserId();
            String time = String.valueOf(TimeUtils.time_current_time());
            String str1 = Md5Util.toMD5(AppConfig.apiKey + time + money);
            String str2 = mLoginUserId + mAccessToken;
            String str3 = Md5Util.toMD5(password);
            String secret = Md5Util.toMD5(str1 + str2 + str3);
            tixian(secret, memberAccount, money, name, inputOpenAccountBank);
        });
        dialog.show();
    }

    /**
     * 提现到后台
     *
     * @param secret
     * @param memberAccount
     * @param money
     * @param inputOpenAccountBank
     */
    private void tixian(String secret, String memberAccount, String money, String memberName, String inputOpenAccountBank) {
        String context = "";
        String type = "";
        if (Constants.SUPPORT_CASH_BACK) {
            if (methodBean.getType() == 1) {
                context = methodBean.getAlipayId();
            } else if (methodBean.getType() == 5) {
                context = methodBean.getBankId();
            }else {
                context = methodBean.getOtherId();
            }
            type = String.valueOf(methodBean.getType());
        } else {
            if (currentWithDrawToAdminType == ALI_TO_ADMIN) {
                context = memberName + ":" + memberAccount;
            } else {
                context = memberName + ":" + memberAccount + ":" + inputOpenAccountBank;
            }
        }
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("secret", secret);
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", mLoginUserId);
        params.put("context", context);
        params.put("amount", money);
        params.put("type", type);
        HttpUtils.get().url(coreManager.getConfig().TI_XIAN)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) { //获取通证成功
                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == Result.CODE_SUCCESS) {
                            Intent intent = new Intent(QuXianActivity.this, WithDrawSuccessActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            ToastUtil.showToast(QuXianActivity.this, result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(QuXianActivity.this);
                    }
                });
    }


    /**
     * 提现按钮状态
     *
     * @param enable
     */
    public void resetTiXianStatus(boolean enable) {
        if (enable) {
            mMember.setEnabled(true);
            mMember.setBackgroundResource(R.drawable.weixin_text_yuanjiao);
        } else {
            mMember.setBackgroundResource(R.drawable.weixin_text_yuanjiao_no);
            mMember.setEnabled(false);
        }
    }

    private boolean checkMoney(String moneyStr) {
        if (TextUtils.isEmpty(moneyStr)) {
            DialogHelper.tip(QuXianActivity.this, getString(R.string.tip_withdraw_empty));
        } else {
            try {
                double money = Double.valueOf(moneyStr);
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtil.showToast(QuXianActivity.this, getResources().getString(R.string.the_amount_is_illegal));
                return false;
            }
//            double little = coreManager.getConfig().minWithdrawToAdmin <= 0 ? 1 : coreManager.getConfig().minWithdrawToAdmin;
            double little = coreManager.getConfig().minWithdrawToAdmin;
            if (Double.valueOf(moneyStr) < little) {
                DialogHelper.tip(QuXianActivity.this, getString(R.string.tip_withdraw_too_little, little + ""));
            } else if (Double.valueOf(moneyStr) > coreManager.getSelf().getBalance()) {
                DialogHelper.tip(QuXianActivity.this, getString(R.string.tip_balance_not_enough));
            } else {// 获取用户code
                return true;
            }
        }
        return false;
    }
}
