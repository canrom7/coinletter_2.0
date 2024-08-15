package com.iimm.miliao.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jungly.gridpasswordview.GridPasswordView;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.CommonalityTools;
import com.iimm.miliao.util.Md5Util;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.xmpp.util.ImHelper;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * 修改支付密码
 */
public class LoginPWvVerify extends BaseActivity {
    TextView alrtcontent;
    View loginpw_view;
    EditText loginPw;
    GridPasswordView payPassword;
    Button next;
    LinearLayout image_codell;
    EditText imagecode_content;
    ImageView imagecode;
    LinearLayout auth_code_ll;
    EditText auth_code_edit;
    Button send_again_btn;
    private int state = 0;//判断状态：0：输入登录密码，1：输入新的支付密码
    private String inpurpw;
    private String payPW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginpwverify);
        initActionBar();
        initView();
        events();
    }

    private void initView() {

        alrtcontent = findViewById(R.id.alrtcontent);
        loginpw_view = findViewById(R.id.loginpw_view);
        loginPw = findViewById(R.id.loginPw);
        payPassword = findViewById(R.id.payPassword);
        next = findViewById(R.id.next);
        image_codell = findViewById(R.id.image_codell);
        auth_code_ll = findViewById(R.id.auth_code_ll);

        /*
         * 判断是手机号登录还是用户名登录
         * */
        if (PreferenceUtils.getInt(LoginPWvVerify.this, AppConstant.LOGIN_TYPE) == 1) {
            image_codell.setVisibility(View.GONE);
            auth_code_ll.setVisibility(View.GONE);
        } else {
            loginPw.setVisibility(View.GONE);
            image_codell.setVisibility(View.VISIBLE);
            auth_code_ll.setVisibility(View.VISIBLE);
            imagecode_content = findViewById(R.id.imagecode_content);
            imagecode = findViewById(R.id.imagecode);
            auth_code_edit = findViewById(R.id.auth_code_edit);
            send_again_btn = findViewById(R.id.send_again_btn);
            String ss = "请输入" + coreManager.getSelf().getPhone() + "手机号码" + "\n" + "收到的验证码，验证身份";
            alrtcontent.setText(CommonalityTools.setStringAreaColor(this, ss, 3, 3 + coreManager.getSelf().getPhone().length(), R.color.color_8F9CBB));
            CommonalityTools.requestImageCode(this, coreManager, coreManager.getSelf().getTelephone(), imagecode);
            imagecode.setOnClickListener(v -> {
                CommonalityTools.requestImageCode(this, coreManager, coreManager.getSelf().getPhone(), imagecode);
            });
            send_again_btn.setOnClickListener(v -> {
                if (TextUtils.isEmpty(imagecode_content.getText().toString())) {
                    ToastUtil.showToast(LoginPWvVerify.this, "请先输入图片验证码！");
                    return;
                }
                CommonalityTools.getCode(LoginPWvVerify.this, coreManager.getSelf().getPhone(), imagecode_content.getText().toString()
                        , coreManager, send_again_btn,"");
            });
        }
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.forgot_payPW);
    }

    private void events() {
        next.setOnClickListener(v -> {
            switch (state) {
                case 0:
                    /*
                     * 判断是手机号登录还是用户名登录
                     * */
                    if (PreferenceUtils.getInt(LoginPWvVerify.this, AppConstant.LOGIN_TYPE) == 1) {
                        boolean b = usernameLogin();
                        if (!b) {
                            return;
                        }
                    } else {
                        if (!phoneLogin()) {
                            return;
                        }
                    }
                    alrtcontent.setText("请设置支付密码，用于支付验证");
                    next.setText("提交");
                    payPassword.setVisibility(View.VISIBLE);
                    loginPw.setVisibility(View.GONE);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    break;
                case 1:
                    if (TextUtils.isEmpty(payPW)) {
                        ToastUtil.showToast(LoginPWvVerify.this, "请输入支付密码！");
                        return;
                    }
                    DialogHelper.showDefaulteMessageProgressDialog(LoginPWvVerify.this);
                    Map<String, String> map = new HashMap<>();
                    map.put("modifyType", "1");
                    map.put("oldPassword", inpurpw);
                    map.put("newPassword", payPW);
                    map.put("access_token", coreManager.getSelfStatus().accessToken);
                    HttpUtils.post()
                            .url(coreManager.getConfig().Pay_Forgot_PassWord)
                            .params(map)
                            .build()
                            .execute(new BaseCallback<Void>(Void.class) {
                                @Override
                                public void onResponse(ObjectResult<Void> result) {
                                    DialogHelper.dismissProgressDialog();
                                    if (Result.checkSuccess(LoginPWvVerify.this, result)) {
                                        // 成功，
                                        ToastUtil.showToast(LoginPWvVerify.this, R.string.tip_pay_password_set_success);
                                        // 记录下支付密码已经设置，
                                        MyApplication.getInstance().initPayPassword(coreManager.getSelf().getUserId(), 1);
                                        ImHelper.syncMyInfoToOtherMachine();
                                    }
                                    finish();
                                }

                                @Override
                                public void onError(Call call, Exception e) {
                                    DialogHelper.dismissProgressDialog();
                                    String reason = e.getMessage();
                                    if (TextUtils.isEmpty(reason)) {
                                        // 提示网络异常，
                                        reason = getString(R.string.net_exception);
                                    }
                                    ToastUtil.showToast(LoginPWvVerify.this, reason);
                                }
                            });

                    break;
            }
        });
        payPassword.setOnPasswordChangedListener(new GridPasswordView.OnPasswordChangedListener() {
            @Override
            public void onTextChanged(String psw) {
                payPW = "";
            }

            @Override
            public void onInputFinish(String psw) {
                payPW = Md5Util.toMD5(psw);
            }
        });
    }

    //    用户登录判断
    public boolean usernameLogin() {
        String pw = CoreManager.getSelf(LoginPWvVerify.this).getPassword();
        inpurpw = Md5Util.toMD5(loginPw.getText().toString());
        if (inpurpw.equals(pw)) {

            loginpw_view.setVisibility(View.GONE);
            loginPw.setVisibility(View.GONE);
            state = 1;
            return true;
        } else {

            ToastUtil.showToast(LoginPWvVerify.this, "登录密码错误！");

        }
        return false;
    }

    //    手机号登录判断
    public boolean phoneLogin() {
        if (!TextUtils.equals(CommonalityTools.mRandCode,auth_code_edit.getText().toString())) {
            ToastUtil.showToast(LoginPWvVerify.this, "验证码错误！");
            return false;
        } else {
            image_codell.setVisibility(View.GONE);
            auth_code_ll.setVisibility(View.GONE);
            inpurpw = coreManager.getSelf().getPassword();
            state = 1;
            return true;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonalityTools.closeTimer();
    }
}
