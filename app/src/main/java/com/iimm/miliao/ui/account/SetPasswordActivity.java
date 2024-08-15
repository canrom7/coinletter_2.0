package com.iimm.miliao.ui.account;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.iimm.miliao.AppConfig;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.helper.LoginHelper;
import com.iimm.miliao.sp.UserSp;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.Md5Util;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.xmpp.XmppConnectionImpl;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.Map;

import okhttp3.Call;

import static com.iimm.miliao.AppConfig.BROADCASTTEST_ACTION;

/**
 * MrLiu253@163.com
 *
 * @time 2019-08-13
 */
public class SetPasswordActivity extends BaseActivity {

    private EditText mNewLoginPasswordEt;
    private EditText mLoginPasswordAgainEt;
    private TextView mSureTv;
    private String mName;
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);
        mName = getIntent().getStringExtra("name");
        type = getIntent().getIntExtra("type", 0);
        if (TextUtils.isEmpty(mName)) {
            ToastUtil.showToast(SetPasswordActivity.this, "用户名为空");
            return;
        }
        initView();
        initClickListener();
    }

    private void initView() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.setting_login_password));

        mNewLoginPasswordEt = findViewById(R.id.new_login_password_et);
        mLoginPasswordAgainEt = findViewById(R.id.login_password_again_et);
        mSureTv = findViewById(R.id.sure_tv);
    }

    private void initClickListener() {
        mSureTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mNewLoginPasswordEt.getText().toString().trim())) {
                    ToastUtil.showToast(SetPasswordActivity.this, "请输入新密码");
                } else if (TextUtils.isEmpty(mLoginPasswordAgainEt.getText().toString().trim())) {
                    ToastUtil.showToast(SetPasswordActivity.this, "请确认密码");
                } else {
                    resetPassword();
                }
            }
        });
    }

    private void resetPassword() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        final String password = mNewLoginPasswordEt.getText().toString().trim();
        Map<String, String> params = new ArrayMap<>();
        params.put("telephone", mName);
        params.put("modifyType", "1");
        params.put("newPassword", Md5Util.toMD5(password));
        params.put("registerType", type + "");
        HttpUtils.get().url(coreManager.getConfig().USER_PASSWORD_RESET)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            if (coreManager.getSelf() != null
                                    && !TextUtils.isEmpty(coreManager.getSelf().getTelephone())) {
                                UserSp.getInstance(mContext).clearUserInfo();
                                MyApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_SIMPLE_TELPHONE;
                                XmppConnectionImpl.getInstance().logoutXmpp();
                                LoginHelper.broadcastLogout(mContext);
                                LoginHistoryActivity.start(SetPasswordActivity.this);

                                //发送广播  重新拉起app
                                Intent intent = new Intent(BROADCASTTEST_ACTION);
                                intent.setComponent(new ComponentName(AppConfig.sPackageName, AppConfig.sPackageName + ".MyBroadcastReceiver"));
                                sendBroadcast(intent);
                            } else {// 本地连电话都没有，说明之前没有登录过 修改成功后直接跳转至登录界面
                                startActivity(new Intent(SetPasswordActivity.this, LoginActivity.class));
                            }
                            ToastUtil.showToast(SetPasswordActivity.this, "修改成功");
                            finish();
                        } else {
                            ToastUtil.showToast(SetPasswordActivity.this, result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(SetPasswordActivity.this, getString(R.string.qequest_error_please_try_again));
                    }
                });
    }

}
