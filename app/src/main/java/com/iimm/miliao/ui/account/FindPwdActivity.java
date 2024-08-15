package com.iimm.miliao.ui.account;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;
import com.iimm.miliao.AppConfig;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.Code;
import com.iimm.miliao.bean.SecurityQuestionBean;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.helper.LoginHelper;
import com.iimm.miliao.helper.PasswordHelper;
import com.iimm.miliao.sp.UserSp;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.Md5Util;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.StringUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ViewPiexlUtil;
import com.iimm.miliao.xmpp.XmppConnectionImpl;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;

import static com.iimm.miliao.AppConfig.BROADCASTTEST_ACTION;

/**
 * 忘记密码
 */
public class FindPwdActivity extends BaseActivity implements View.OnClickListener {
    private Button btn_getCode, btn_change;
    private EditText mPhoneNumberEdit;
    private EditText mPasswordEdit, mConfigPasswordEdit, mAuthCodeEdit;
    private EditText mUserNameEt;
    private TextView mUserNameProblemTv;
    private TextView tv_prefix;
    private int mobilePrefix = 86;

    // 驗證碼
    private String randcode;
    // 图形验证码
    private EditText mImageCodeEdit;
    private ImageView mImageCodeIv;
    private ImageView mRefreshIv;
    private int reckonTime = 60;
    private Handler mReckonHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0x1) {
                btn_getCode.setText("(" + reckonTime + ")");
                btn_getCode.setAlpha(0.5f);
                reckonTime--;
                if (reckonTime < 0) {
                    mReckonHandler.sendEmptyMessage(0x2);
                } else {
                    mReckonHandler.sendEmptyMessageDelayed(0x1, 1000);
                }
            } else if (msg.what == 0x2) {
                // 60秒结束
                btn_getCode.setAlpha(1f);
                btn_getCode.setText(R.string.get_msg_code);
                btn_getCode.setEnabled(true);
                reckonTime = 60;
            }
        }
    };

    public FindPwdActivity() {
        noLoginRequired();
    }

    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        type = getIntent().getIntExtra("type", 0);
        initView();
    }

    private void initView() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tv_prefix = (TextView) findViewById(R.id.tv_prefix);
        if (type == 1) {
            tv_prefix.setVisibility(View.GONE);
        } else {
            tv_prefix.setOnClickListener(this);
        }
        mobilePrefix = PreferenceUtils.getInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
        tv_prefix.setText("+" + mobilePrefix);

        btn_getCode = (Button) findViewById(R.id.send_again_btn);
        btn_getCode.setOnClickListener(this);
        btn_change = (Button) findViewById(R.id.login_btn);
        btn_change.setOnClickListener(this);

        mPhoneNumberEdit = (EditText) findViewById(R.id.phone_numer_edit);
        tvTitle.setText(InternationalizationHelper.getString("JX_ForgetPassWord"));

        mUserNameEt = findViewById(R.id.username_et);
        mUserNameProblemTv = findViewById(R.id.username_problem_tv);

        mUserNameProblemTv.setOnClickListener(this);
        mPasswordEdit = (EditText) findViewById(R.id.password_edit);
        PasswordHelper.bindPasswordEye(mPasswordEdit, findViewById(R.id.tbEye));
        mConfigPasswordEdit = (EditText) findViewById(R.id.confirm_password_edit);
        PasswordHelper.bindPasswordEye(mConfigPasswordEdit, findViewById(R.id.tbEyeConfirm));
        mImageCodeEdit = (EditText) findViewById(R.id.image_tv);
        mAuthCodeEdit = (EditText) findViewById(R.id.auth_code_edit);
        List<EditText> mEditList = new ArrayList<>();
        mEditList.add(mPasswordEdit);
        mEditList.add(mConfigPasswordEdit);
        mEditList.add(mImageCodeEdit);
        mEditList.add(mAuthCodeEdit);
        setBound(mEditList);

        mImageCodeIv = (ImageView) findViewById(R.id.image_iv);
        mImageCodeIv.setOnClickListener(this);
        mRefreshIv = (ImageView) findViewById(R.id.image_iv_refresh);
        mPhoneNumberEdit.setHint(InternationalizationHelper.getString("JX_InputPhone"));
        mAuthCodeEdit.setHint(InternationalizationHelper.getString("ENTER_VERIFICATION_CODE"));
        mPasswordEdit.setHint(InternationalizationHelper.getString("JX_InputNewPassWord"));
        mConfigPasswordEdit.setHint(InternationalizationHelper.getString("JX_ConfirmNewPassWord"));
        btn_change.setText(InternationalizationHelper.getString("JX_UpdatePassWord"));

        // 请求图形验证码
        if (!TextUtils.isEmpty(mPhoneNumberEdit.getText().toString())) {
            requestImageCode();
        }
        mImageCodeEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (!TextUtils.isEmpty(mPhoneNumberEdit.getText().toString())) {
                        requestImageCode();
                    }
                }
            }
        });
        //是否使用用户名注册
        if (type == 1) {
            findViewById(R.id.username_cl).setVisibility(View.VISIBLE);
            findViewById(R.id.forget_password_phone).setVisibility(View.GONE);
        } else {
            findViewById(R.id.username_cl).setVisibility(View.GONE);
            findViewById(R.id.forget_password_phone).setVisibility(View.VISIBLE);
        }
    }

    public void setBound(List<EditText> mEditList) {// 为Edit内的drawableLeft设置大小
        for (int i = 0; i < mEditList.size(); i++) {
            Drawable[] compoundDrawable = mEditList.get(i).getCompoundDrawables();
            Drawable drawable = compoundDrawable[0];
            if (drawable != null) {
                drawable.setBounds(0, 0, ViewPiexlUtil.dp2px(this, 20), ViewPiexlUtil.dp2px(this, 20));
                mEditList.get(i).setCompoundDrawables(drawable, null, null, null);
            }
        }
    }


    private void getDataList(String name) {
        DialogHelper.showDefaulteMessageProgressDialog(FindPwdActivity.this);
        HttpUtils.get().url(CoreManager.requireConfig(mContext).QUESTION_LIST)
                .params("userName", name)
                .build()
                .execute(new ListCallback<SecurityQuestionBean.DataBean>(SecurityQuestionBean.DataBean.class) {
                    @Override
                    public void onResponse(ArrayResult<SecurityQuestionBean.DataBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            if (result.getData() != null && result.getData().size() > 0) {
                                Intent mIntent = new Intent(FindPwdActivity.this, VerifySecretActivity.class);
                                mIntent.putExtra("name", mUserNameEt.getText().toString().trim());
                                mIntent.putExtra("type", type);
                                mIntent.putExtra("datas", new Gson().toJson(result.getData()));
                                startActivity(mIntent);
                            } else {
                                ToastUtil.showToast(FindPwdActivity.this, "用户尚未设置密保问题，请联系客服修改");
                            }
                        } else {
                            ToastUtil.showToast(FindPwdActivity.this, result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(FindPwdActivity.this, e.getMessage());
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_prefix:
                // 选择国家区号
                Intent intent = new Intent(this, SelectPrefixActivity.class);
                startActivityForResult(intent, SelectPrefixActivity.REQUEST_MOBILE_PREFIX_LOGIN);
                break;
            case R.id.image_iv:
                if (TextUtils.isEmpty(mPhoneNumberEdit.getText().toString())) {
                    ToastUtil.showToast(this, getString(R.string.tip_phone_number_empty_request_verification_code));
                } else {
                    requestImageCode();
                }
                break;
            case R.id.send_again_btn:
                // 获取验证码
                String phoneNumber = mPhoneNumberEdit.getText().toString().trim();
                String imagecode = mImageCodeEdit.getText().toString().trim();
                if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(imagecode)) {
                    ToastUtil.showToast(mContext, getString(R.string.tip_phone_number_verification_code_empty));
                    return;
                }


                verifyTelephone(phoneNumber, imagecode);
                break;
            case R.id.login_btn:
                if (!configPassword()) {// 两次密码是否一致
                    return;
                }


                // 确认修改
                if (nextStep()) {
                    // 如果验证码正确，则可以重置密码
                    resetPassword();
                }
                break;
            case R.id.username_problem_tv://用户名修改密码
                if (verification()) {
                    getDataList(mUserNameEt.getText().toString().trim());
                }
                break;
            default:
                break;
        }
    }

    private boolean verification() {
        if (TextUtils.isEmpty(mUserNameEt.getText().toString().trim())) {
            ToastUtil.showToast(FindPwdActivity.this, getString(R.string.please_input_account));
            return false;
        }
        return true;
    }

    /**
     * 修改密码
     */
    private void resetPassword() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        final String phoneNumber = mPhoneNumberEdit.getText().toString().trim();
        final String password = mPasswordEdit.getText().toString().trim();
        String authCode = mAuthCodeEdit.getText().toString().trim();
        Map<String, String> params = new HashMap<>();
        params.put("telephone", phoneNumber);
        params.put("randcode", authCode);
        params.put("areaCode", String.valueOf(mobilePrefix));
        params.put("newPassword", Md5Util.toMD5(password));
        params.put("registerType", type + "");

        HttpUtils.get().url(coreManager.getConfig().USER_PASSWORD_RESET)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(FindPwdActivity.this, result)) {
                            ToastUtil.showToast(FindPwdActivity.this, InternationalizationHelper.getString("JXAlert_UpdateOK"));
                            if (coreManager.getSelf() != null
                                    && !TextUtils.isEmpty(coreManager.getSelf().getTelephone())) {
                                UserSp.getInstance(mContext).clearUserInfo();
                                MyApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_SIMPLE_TELPHONE;
                                XmppConnectionImpl.getInstance().logoutXmpp();
                                LoginHelper.broadcastLogout(mContext);
                                LoginHistoryActivity.start(FindPwdActivity.this);

                                //发送广播  重新拉起app
                                Intent intent = new Intent(BROADCASTTEST_ACTION);
                                intent.setComponent(new ComponentName(AppConfig.sPackageName, AppConfig.sPackageName + ".MyBroadcastReceiver"));
                                sendBroadcast(intent);
                            } else {// 本地连电话都没有，说明之前没有登录过 修改成功后直接跳转至登录界面
                                startActivity(new Intent(FindPwdActivity.this, LoginActivity.class));
                            }
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(FindPwdActivity.this, InternationalizationHelper.getString("JXServer_ErrorNetwork"));
                    }
                });
    }

    /**
     * 请求图形验证码
     */
    private void requestImageCode() {
        String url = coreManager.getConfig().USER_GETCODE_IMAGE + "?telephone=" + mobilePrefix + mPhoneNumberEdit.getText().toString();
        Glide.with(mContext).load(url)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        mImageCodeIv.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        ToastUtil.showToast(FindPwdActivity.this, R.string.tip_verification_load_failed);
                    }
                });
    }

    /**
     * 请求验证码
     */
    private void verifyTelephone(String phoneNumber, String imageCode) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        String language = Locale.getDefault().getLanguage();
        params.put("language", language);
        params.put("areaCode", String.valueOf(mobilePrefix));
        params.put("telephone", phoneNumber);
        params.put("imgCode", imageCode);
        params.put("isRegister", String.valueOf(0));
        params.put("version", "1");

        /**
         * 只判断中国手机号格式
         */
        if(mobilePrefix == 86){
            if (!StringUtils.isPhone(phoneNumber)) {
                // Toast.makeText(this, "手机格式错误", Toast.LENGTH_SHORT).show();
                ToastUtil.showToast(this, InternationalizationHelper.getString("JX_Input11phoneNumber"));
                return;
            }
        }
        HttpUtils.get().url(coreManager.getConfig().SEND_AUTH_CODE)
                .params(params)
                .build()
                .execute(new BaseCallback<Code>(Code.class) {
                    @Override
                    public void onResponse(ObjectResult<Code> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            btn_getCode.setEnabled(false);
                            // 开始计时
                            mReckonHandler.sendEmptyMessage(0x1);
                            // 得到验证码
                            randcode = result.getData().getCode();
                            ToastUtil.showToast(FindPwdActivity.this, R.string.verification_code_send_success);
                        } else {
                            ToastUtil.showToast(FindPwdActivity.this, R.string.verification_code_send_failed);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(FindPwdActivity.this, InternationalizationHelper.getString("JXServer_ErrorNetwork") );
                    }
                });
    }

    /**
     * 确认两次输入的密码是否一致
     */
    private boolean configPassword() {
        String password = mPasswordEdit.getText().toString().trim();
        String confirmPassword = mConfigPasswordEdit.getText().toString().trim();
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            mPasswordEdit.requestFocus();
            mPasswordEdit.setError(StringUtils.editTextHtmlErrorTip(this, R.string.password_empty_error));
            return false;
        }
        if (TextUtils.isEmpty(confirmPassword) || confirmPassword.length() < 6) {
            mConfigPasswordEdit.requestFocus();
            mConfigPasswordEdit.setError(StringUtils.editTextHtmlErrorTip(this, R.string.confirm_password_empty_error));
            return false;
        }
        if (confirmPassword.equals(password)) {
            return true;
        } else {
            mConfigPasswordEdit.requestFocus();
            mConfigPasswordEdit.setError(StringUtils.editTextHtmlErrorTip(this, R.string.password_confirm_password_not_match));
            return false;
        }
    }

    /**
     * 验证验证码
     */
    private boolean nextStep() {
        final String phoneNumber = mPhoneNumberEdit.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNumber)) {
            //            Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
            ToastUtil.showToast(this, InternationalizationHelper.getString("JX_InputPhone"));
            return false;
        }
        /**
         * 只判断中国手机号格式
         */
        if (mobilePrefix == 86) {
            if (!StringUtils.isMobileNumber(phoneNumber)) {
                //            Toast.makeText(this, "手机号码格式错误", Toast.LENGTH_SHORT).show();
                ToastUtil.showToast(this, InternationalizationHelper.getString("JX_Input11phoneNumber"));
                return false;
            }
        }
        String authCode = mAuthCodeEdit.getText().toString().trim();
        if (TextUtils.isEmpty(authCode)) {
            //            Toast.makeText(this, "请填写验证码", Toast.LENGTH_SHORT).show();
            ToastUtil.showToast(this, InternationalizationHelper.getString("JX_InputMessageCode"));
            return false;
        }
        if (authCode.equals(randcode)) {
            // 验证码正确
            return true;
        } else {
            // 验证码错误
            //            Toast.makeText(this, "验证码错误", Toast.LENGTH_SHORT).show();
            ToastUtil.showToast(this, InternationalizationHelper.getString("inputPhoneVC_MsgCodeNotOK"));
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != SelectPrefixActivity.RESULT_MOBILE_PREFIX_SUCCESS)
            return;
        mobilePrefix = data.getIntExtra(Constants.MOBILE_PREFIX, 86);
        tv_prefix.setText("+" + mobilePrefix);
        // 图形验证码可能因区号失效，
        // 请求图形验证码
        if (!TextUtils.isEmpty(mPhoneNumberEdit.getText().toString())) {
            requestImageCode();
        }
    }
}
