package com.iimm.miliao.ui.me;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.QuestionsBean;
import com.iimm.miliao.bean.SecurityQuestion;
import com.iimm.miliao.bean.User;
import com.iimm.miliao.ui.LoginPWvVerify;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.lock.ChangeDeviceLockPasswordActivity;
import com.iimm.miliao.ui.lock.DeviceLockActivity;
import com.iimm.miliao.ui.lock.DeviceLockHelper;
import com.iimm.miliao.ui.me.redpacket.ChangePayPasswordActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.UiUtils;
import com.iimm.miliao.view.NoDoubleClickListener;
import com.iimm.miliao.view.SecurityQuestionDialog;
import com.suke.widget.SwitchButton;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

public class SecureSettingActivity extends BaseActivity implements View.OnClickListener {

    public static final int REQUEST_DISABLE_LOCK = 1;
    private SwitchButton sbDeviceLock;
    private SwitchButton sbDeviceLockFree;
    private View llDeviceLockDetail;
    private View rlChangeDeviceLockPassword;
    private RelativeLayout updatepaypw;
    private RelativeLayout forgotpaypw;
    private TextView mSecurityQuestionTv;
    private boolean mContent;
    private List<QuestionsBean> mQuestionsBeans = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_setting);
        initActionBar();
        initView();

        sbDeviceLock.setOnCheckedChangeListener((view, isChecked) -> {
            if (!isChecked) {
                DeviceLockActivity.verify(this, REQUEST_DISABLE_LOCK);
                return;
            }
            rlChangeDeviceLockPassword.setVisibility(View.VISIBLE);
            ChangeDeviceLockPasswordActivity.start(this);
        });
        rlChangeDeviceLockPassword.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                ChangeDeviceLockPasswordActivity.start(SecureSettingActivity.this);
            }
        });
        sbDeviceLockFree.setChecked(DeviceLockHelper.isAutoLock());
        sbDeviceLockFree.setOnCheckedChangeListener((view, isChecked) -> {
            DeviceLockHelper.setAutoLock(isChecked);
        });
        downloadUserInfo();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDeviceLockSettings();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        switch (requestCode) {
            case REQUEST_DISABLE_LOCK:
                DeviceLockHelper.clearPassword();
                updateDeviceLockSettings();
                break;
        }

    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void updateDeviceLockSettings() {
        boolean enabled = DeviceLockHelper.isEnabled();
        sbDeviceLock.setChecked(enabled);
        if (enabled) {
            llDeviceLockDetail.setVisibility(View.VISIBLE);
        } else {
            llDeviceLockDetail.setVisibility(View.GONE);
        }
        boolean autoLock = DeviceLockHelper.isAutoLock();
        sbDeviceLockFree.setChecked(autoLock);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.secure_settings);
    }

    private void initView() {
        sbDeviceLock = findViewById(R.id.sbDeviceLock);
        sbDeviceLockFree = findViewById(R.id.sbDeviceLockFree);
        llDeviceLockDetail = findViewById(R.id.llDeviceLockDetail);
        updatepaypw = findViewById(R.id.payword_update);
        forgotpaypw = findViewById(R.id.payword_forgotpw);
        rlChangeDeviceLockPassword = findViewById(R.id.rlChangeDeviceLockPassword);
        mSecurityQuestionTv = findViewById(R.id.secure_setting_security_question_tv);
        mSecurityQuestionTv.setOnClickListener(this);
        if (coreManager.getConfig().qqLoginStatus == 1 || coreManager.getConfig().wechatLoginStatus == 1) {
            findViewById(R.id.bind_account_rl).setOnClickListener(this);
            findViewById(R.id.bind_account_rl).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.bind_account_rl).setVisibility(View.GONE);
        }
        if (coreManager.getConfig().isQestionOpen) {
            findViewById(R.id.secure_setting_security_question_rl).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.secure_setting_security_question_rl).setVisibility(View.GONE);
        }
        updatepaypw.setOnClickListener(this);
        forgotpaypw.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (!UiUtils.isNormalClick(v)) {
            return;
        }
        switch (v.getId()) {
            case R.id.secure_setting_security_question_tv:
                if (mContent) {
                    SecurityQuestionDialog securityQuestionDialog = new SecurityQuestionDialog(SecureSettingActivity.this, mQuestionsBeans);
                    securityQuestionDialog.show();
                } else {
                    SecurityQuestionActivity.securityQuestion(SecureSettingActivity.this, 2, mQuestionsBeans);
                }
                break;
            case R.id.bind_account_rl:
                // 绑定第三方
                startActivity(new Intent(mContext, BandAccountActivity.class));
                break;
            case R.id.payword_update:
                Intent intent = new Intent(mContext, ChangePayPasswordActivity.class);
                startActivity(intent);
                break;
            case R.id.payword_forgotpw:
                boolean needOldPassword = PreferenceUtils.getBoolean(this, Constants.IS_PAY_PASSWORD_SET + coreManager.getSelf().getUserId(), true);
                if (needOldPassword) {
                    Intent intent1 = new Intent(mContext, LoginPWvVerify.class);
                    startActivity(intent1);
                } else {
                    Intent intent2 = new Intent(mContext, ChangePayPasswordActivity.class);
                    startActivity(intent2);
                }
                break;
            default:
                break;
        }
    }

    private void downloadUserInfo() {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        HttpUtils.get().url(coreManager.getConfig().USER_GET_URL)
                .params(params)
                .build()
                .execute(new BaseCallback<User>(User.class) {
                    @Override
                    public void onResponse(ObjectResult<User> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            if (result.getData().getQuestions().size() > 0) {
                                mSecurityQuestionTv.setText(getString(R.string.has_been_set));
                                mQuestionsBeans.clear();
                                mQuestionsBeans.addAll(result.getData().getQuestions());
                                mContent = true;
                            } else {
                                mSecurityQuestionTv.setText(getString(R.string.set_the_secret_issue));
                                mContent = false;
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(SecurityQuestion securityQuestion) {
        downloadUserInfo();
    }

}
