package com.iimm.miliao.ui.me.redpacket;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jungly.gridpasswordview.GridPasswordView;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.Md5Util;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.UiUtils;
import com.iimm.miliao.view.NoDoubleClickListener;
import com.iimm.miliao.xmpp.util.ImHelper;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import okhttp3.Call;

/**
 * 修改支付密码
 */
public class ChangePayPasswordActivity extends BaseActivity {

    private boolean needOldPassword = true;
    private boolean needTwice = true;

    private String oldPayPassword;
    private String newPayPassword;

    private TextView tvTip;
    LinearLayout payPasswordll;
    LinearLayout loginPasswordll;
    EditText loginPw;
    TextView login_verify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pay_password);
        if (coreManager.getSelf() != null && PreferenceUtils.getInt(ChangePayPasswordActivity.this, AppConstant.LOGIN_other)==7&&TextUtils.isEmpty(coreManager.getSelf().getTelephone())) {
            startActivity(new Intent(ChangePayPasswordActivity.this, ChangePhoneActivity.class));
            finish();
        }
        initActionBar();
        initView();
        initData();
    }

    private void initData() {
        String userId = coreManager.getSelf().getUserId();
        if (TextUtils.isEmpty(userId)) {
            ToastUtil.showToast(this, R.string.tip_no_user_id);
            finish();
            return;
        }
        // 如果没有设置过支付密码，就不需要输入旧密码，
        needOldPassword = PreferenceUtils.getBoolean(this, Constants.IS_PAY_PASSWORD_SET + userId, true);
        TextView tvTitle = findViewById(R.id.tv_title_center);
        ((TextView) findViewById(R.id.tv_title_center)).setText(getString(R.string.change_password));
        if (!needOldPassword) {
            // 如果不需要旧密码，直接传空字符串，
            oldPayPassword = "";
            tvTip.setText(R.string.tip_change_pay_password_input_new);
            tvTitle.setText(R.string.btn_set_pay_password);
            payPasswordll.setVisibility(View.GONE);
            loginPasswordll.setVisibility(View.VISIBLE);
        } else {
            payPasswordll.setVisibility(View.VISIBLE);
            loginPasswordll.setVisibility(View.GONE);
            tvTitle.setText(R.string.btn_change_pay_password);
        }
    }

    private void initView() {
        tvTip = findViewById(R.id.tvTip);
        payPasswordll = findViewById(R.id.payPasswordll);
        loginPasswordll = findViewById(R.id.loginPasswordll);
        loginPw = findViewById(R.id.loginPw);
        login_verify = findViewById(R.id.login_verify);
        final TextView tvFinish = findViewById(R.id.tvFinish);
        login_verify.setOnClickListener(v -> {
            if (!UiUtils.isNormalClick(v)) {
                return;
            }
            if (TextUtils.isEmpty(loginPw.getText().toString().trim())) {
                ToastUtil.showToast(ChangePayPasswordActivity.this, "请输入登录密码！");
            } else {
                if (coreManager.getSelf().getPassword().equals(Md5Util.toMD5(loginPw.getText().toString().trim()))) {
                    payPasswordll.setVisibility(View.VISIBLE);
                    loginPasswordll.setVisibility(View.GONE);
                } else {
                    ToastUtil.showToast(ChangePayPasswordActivity.this, "登录密码错误！");
                }
            }
        });
        tvFinish.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                DialogHelper.showDefaulteMessageProgressDialog(ChangePayPasswordActivity.this);
                HttpUtils.get().url(coreManager.getConfig().UPDATE_PAY_PASSWORD)
                        .params("access_token", coreManager.getSelfStatus().accessToken)
                        .params("oldPayPassword", Md5Util.toMD5(oldPayPassword))
                        .params("payPassword", Md5Util.toMD5(newPayPassword))
                        .build()
                        .execute(new BaseCallback<Void>(Void.class) {
                            @Override
                            public void onResponse(ObjectResult<Void> result) {
                                DialogHelper.dismissProgressDialog();
                                if (Result.checkSuccess(ChangePayPasswordActivity.this, result)) {
                                    // 成功，
                                    if (TextUtils.isEmpty(oldPayPassword)) {//设置支付密码
                                        ToastUtil.showToast(ChangePayPasswordActivity.this, R.string.tip_pay_password_set_success);
                                    } else {//修改支付密码
                                        ToastUtil.showToast(ChangePayPasswordActivity.this, R.string.tip_change_pay_password_success);
                                    }
                                    // 记录下支付密码已经设置，
                                    MyApplication.getInstance().initPayPassword(coreManager.getSelf().getUserId(), 1);
                                    ImHelper.syncMyInfoToOtherMachine();
                                }
                                finish();
                            }

                            @Override
                            public void onError(Call call, Exception e) {
                                Reporter.post("修改支付密码接口调用失败，", e);
                                DialogHelper.dismissProgressDialog();
                                String reason = e.getMessage();
                                if (TextUtils.isEmpty(reason)) {
                                    // 提示网络异常，
                                    reason = getString(R.string.net_exception);
                                }
                                ToastUtil.showToast(ChangePayPasswordActivity.this, reason);
                                finish();
                            }
                        });
            }
        });
        final GridPasswordView gpvPassword = findViewById(R.id.gpvPassword);
        gpvPassword.setOnPasswordChangedListener(new GridPasswordView.OnPasswordChangedListener() {
            @Override
            public void onTextChanged(String psw) {
                tvFinish.setVisibility(View.GONE);
            }

            @Override
            public void onInputFinish(String psw) {
                if (needOldPassword) {
                    oldPayPassword = psw;
                    DialogHelper.showDefaulteMessageProgressDialog(ChangePayPasswordActivity.this);
                    HttpUtils.get().url(coreManager.getConfig().CHECK_PAY_PASSWORD)
                            .params("access_token", coreManager.getSelfStatus().accessToken)
                            .params("payPassword", Md5Util.toMD5(oldPayPassword))
                            .build()
                            .execute(new BaseCallback<Void>(Void.class) {
                                @Override
                                public void onResponse(ObjectResult<Void> result) {
                                    DialogHelper.dismissProgressDialog();
                                    gpvPassword.clearPassword();
                                    if (Result.checkSuccess(ChangePayPasswordActivity.this, result)) {
                                        needOldPassword = false;
                                        tvTip.setText(R.string.tip_change_pay_password_input_new);
                                    }
                                }

                                @Override
                                public void onError(Call call, Exception e) {
                                    Reporter.post("修改支付密码接口调用失败，", e);
                                    DialogHelper.dismissProgressDialog();
                                    String reason = e.getMessage();
                                    if (TextUtils.isEmpty(reason)) {
                                        // 提示网络异常，
                                        reason = getString(R.string.net_exception);
                                    }
                                    ToastUtil.showToast(ChangePayPasswordActivity.this, reason);
                                    finish();
                                }
                            });
                } else if (needTwice) {
                    needTwice = false;
                    newPayPassword = psw;
                    gpvPassword.clearPassword();
                    tvTip.setText(R.string.tip_change_pay_password_input_twice);
                } else if (psw.equals(newPayPassword)) {
                    // 二次确认成功，
                    tvFinish.setVisibility(View.VISIBLE);
                } else {
                    // 二次确认失败，重新输入新密码，
                    gpvPassword.clearPassword();
                    needTwice = true;
                    tvTip.setText(R.string.tip_change_pay_password_input_incorrect);
                    tvFinish.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initActionBar() {
        getSupportActionBar().hide();

        ImageView iv_title_left = findViewById(R.id.iv_title_left);
        iv_title_left.setImageResource(R.drawable.icon_close_circle);
        iv_title_left.setOnClickListener(v -> finish());
    }
}
