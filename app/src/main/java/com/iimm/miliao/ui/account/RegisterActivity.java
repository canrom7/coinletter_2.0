package com.iimm.miliao.ui.account;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.iimm.miliao.BuildConfig;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.adapter.MessageLogin;
import com.iimm.miliao.bean.Code;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.helper.PasswordHelper;
import com.iimm.miliao.helper.UsernameHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.CommonalityTools;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.DeviceInfoUtil;
import com.iimm.miliao.util.EventBusHelper;
import com.iimm.miliao.util.Md5Util;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.SkinUtils;
import com.iimm.miliao.util.StringUtils;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * 注册-1.输入手机号
 */
public class RegisterActivity extends BaseActivity {
    public static final String EXTRA_AUTH_CODE = "auth_code";
    public static final String EXTRA_PHONE_NUMBER = "phone_number";
    public static final String EXTRA_PASSWORD = "password";
    public static final String EXTRA_INVITE_CODE = "invite_code";
    public static final String EXTRA_TYPE = "type";
    public static final String EXTRA_CODE = "code";
    public static int isSmsRegister = 0;
    private static String TAG = "RegisterActivity";
    private EditText mPhoneNumEdit;
    private EditText mPassEdit;
    private EditText mImageCodeEdit;
    private ImageView mImageCodeIv;
    private ImageView mRefreshIv;
    private EditText mAuthCodeEdit;
    private Button mSendAgainBtn;
    private Button mNextStepBtn;
    private Button mNoAuthCodeBtn;
    private TextView tvPrivacy;
    private TextView tv_prefix;
    private RadioGroup registerSelector;
    private RadioButton username_register;
    private RadioButton phone_register;
    private RelativeLayout llInvitationCode;
    private EditText etInvitationCode;

    private int mobilePrefix = 86;
    private int type = 1;
    private String mRandCode;
    private int reckonTime = 60;
    private Handler mReckonHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0x1) {
                mSendAgainBtn.setText(reckonTime + " " + "S" + "重新获取");
                mSendAgainBtn.setAlpha(0.5f);
                if (reckonTime == 30) {
                    // 剩下30秒时显示收不到验证码按钮，
                    mNoAuthCodeBtn.setVisibility(View.GONE);
                }
                reckonTime--;
                if (reckonTime < 0) {
                    mReckonHandler.sendEmptyMessage(0x2);
                } else {
                    mReckonHandler.sendEmptyMessageDelayed(0x1, 1000);
                }
            } else if (msg.what == 0x2) {
                // 60秒结束
                mSendAgainBtn.setAlpha(1f);
                mSendAgainBtn.setText(R.string.get_msg_code);
                mSendAgainBtn.setEnabled(true);
                reckonTime = 60;
            }
        }
    };
    private TextView tvTitle;
    private boolean isFrist = true;


    public RegisterActivity() {
        noLoginRequired();
    }

    public static void registerFromThird(Context ctx, int mobilePrefix, String phone, String password, int type) {
        Intent intent = new Intent(ctx, RegisterActivity.class);
        intent.putExtra("mobilePrefix", mobilePrefix);
        intent.putExtra("phone", phone);
        intent.putExtra("password", password);
        intent.putExtra("type", type);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mobilePrefix = getIntent().getIntExtra("mobilePrefix", 86);
        type = getIntent().getIntExtra("type", 0);

        initActionBar();
        initView();
        initEvent();
        EventBusHelper.register(this);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageLogin message) {
        finish();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        ((ImageView) findViewById(R.id.iv_title_left)).setImageResource(R.drawable.icon_close_circle);
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvTitle = (TextView) findViewById(R.id.title);
        tvTitle.setText("注册");
    }

    private void initView() {
        mPhoneNumEdit = (EditText) findViewById(R.id.phone_numer_edit);
        String phone = getIntent().getStringExtra("phone");
        if (!TextUtils.isEmpty(phone)) {
            mPhoneNumEdit.setText(phone);
        }
        tv_prefix = (TextView) findViewById(R.id.tv_prefix);
        tv_prefix.setText("+" + mobilePrefix);
        mPassEdit = (EditText) findViewById(R.id.password_edit);
        registerSelector = (RadioGroup) findViewById(R.id.registerselector);
        username_register = (RadioButton) findViewById(R.id.username_register);
        phone_register = (RadioButton) findViewById(R.id.phone_register);
        llInvitationCode = (RelativeLayout) findViewById(R.id.llInvitationCode);
        etInvitationCode = (EditText) findViewById(R.id.etInvitationCode);
        etInvitationCode.setVisibility(View.GONE);
        PasswordHelper.bindPasswordEye(mPassEdit, findViewById(R.id.tbEye));
        String password = getIntent().getStringExtra("password");
        if (!TextUtils.isEmpty(password)) {
            mPassEdit.setText(password);
        }
        if (coreManager.getConfig().registerInviteCode > 0) {
            // 启用邀请码，
            findViewById(R.id.llInvitationCode).setVisibility(View.VISIBLE);
            etInvitationCode.setVisibility(View.VISIBLE);
            if (coreManager.getConfig().registerInviteCode == 2) {
                etInvitationCode.setHint("填写邀请码(必填)");
            }
        }
        mImageCodeEdit = (EditText) findViewById(R.id.image_tv);
        mImageCodeIv = (ImageView) findViewById(R.id.image_iv);
        mRefreshIv = (ImageView) findViewById(R.id.image_iv_refresh);
        mAuthCodeEdit = (EditText) findViewById(R.id.auth_code_edit);
        mSendAgainBtn = (Button) findViewById(R.id.send_again_btn);
        mNextStepBtn = (Button) findViewById(R.id.next_step_btn);
        mNoAuthCodeBtn = (Button) findViewById(R.id.go_no_auth_code);
        tvPrivacy = findViewById(R.id.tvPrivacy);

        UsernameHelper.initEditText2(mPhoneNumEdit, type);
        if (coreManager.getConfig().registerUsername == 2) {
            registerSelector.setVisibility(View.VISIBLE);
            tvTitle.setVisibility(View.GONE);
            registerSelector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    mPhoneNumEdit.setText("");
                    mPassEdit.setText("");
                    switch (checkedId) {
                        case R.id.username_register:
                            type = 1;
                            UsernameHelper.initEditText2(mPhoneNumEdit, 1);
                            tv_prefix.setVisibility(View.GONE);
//                            findViewById(R.id.iv_code_ll).setVisibility(View.GONE);
                            findViewById(R.id.iv_code_view).setVisibility(View.GONE);
                            findViewById(R.id.auth_code_ll).setVisibility(View.GONE);
                            break;
                        case R.id.phone_register:
                            type = 0;

                            UsernameHelper.initEditText2(mPhoneNumEdit, 0);
                            tv_prefix.setVisibility(View.VISIBLE);
//                            findViewById(R.id.iv_code_ll).setVisibility(View.VISIBLE);
                            findViewById(R.id.iv_code_view).setVisibility(View.VISIBLE);
                            findViewById(R.id.auth_code_ll).setVisibility(View.GONE);
                            break;
                    }
                }
            });
        } else {
            tvTitle.setVisibility(View.VISIBLE);
            registerSelector.setVisibility(View.GONE);
        }
        if (type == 1) {
            tv_prefix.setVisibility(View.GONE);
            if (username_register.getVisibility() == View.VISIBLE) {
                username_register.setChecked(true);
            }
            // mPhoneNumEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        } else {// 启用短信验证码
            if (phone_register.getVisibility() == View.VISIBLE) {
                phone_register.setChecked(true);
            }
//            findViewById(R.id.iv_code_ll).setVisibility(View.VISIBLE);
            findViewById(R.id.iv_code_view).setVisibility(View.VISIBLE);
            findViewById(R.id.auth_code_ll).setVisibility(View.GONE);
            mPhoneNumEdit.setInputType(InputType.TYPE_CLASS_PHONE);
        }

        findViewById(R.id.main_content).setOnClickListener(v -> {
            // 点击空白区域隐藏软键盘
            InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.hideSoftInputFromWindow(findViewById(R.id.main_content).getWindowToken(), 0); //强制隐藏键盘
            }
        });


    }

    /**
     * 请求图形验证码
     */
    private void requestImageCode() {
        if (coreManager.getConfig().registerUsername == 1) {
            // 用户名注册或者没开启验证码，就不请求图形码，
            return;
        }
        if (TextUtils.isEmpty(mPhoneNumEdit.getText().toString())) {
            ToastUtil.showToast(mContext, getString(R.string.tip_no_phone_number_get_v_code));
            return;
        }
        String url = coreManager.getConfig().USER_GETCODE_IMAGE + "?telephone=" + mobilePrefix + mPhoneNumEdit.getText().toString();
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
                        ToastUtil.showToast(RegisterActivity.this, R.string.tip_verification_code_load_failed);
                    }
                });
    }

    private void verifyPhoneNumber(String phoneNumber, final Runnable onSuccess) {
        if (!UsernameHelper.verify(this, phoneNumber, type)) {
            return;
        }
        if (coreManager.getConfig().registerInviteCode == 1) {
        if (TextUtils.isEmpty(etInvitationCode.getText().toString().trim())) {
                ToastUtil.showToast(this, "请输入邀请码！");
                return;
            }
        }
        Map<String, String> params = new HashMap<>();
        params.put("telephone", phoneNumber);
        params.put("areaCode", "" + mobilePrefix);
        params.put("serial", DeviceInfoUtil.getDeviceId(mContext));
        if (!TextUtils.isEmpty(etInvitationCode.getText().toString().trim())) {
            params.put("inviteCode", "" + etInvitationCode.getText().toString().trim());
        }
        params.put("registerType", "" + type);
        if (type == 0) {
            params.put("smsCode", "" + mAuthCodeEdit.getText().toString().trim());
//            params.put("smsCode", "" + mImageCodeEdit.getText().toString().trim());
        }

        HttpUtils.get().url(coreManager.getConfig().VERIFY_TELEPHONE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result == null) {
                            ToastUtil.showToast(RegisterActivity.this,
                                    R.string.data_exception);
                            return;
                        }

                        if (result.getResultCode() == 1) {
                            onSuccess.run();
                        } else {
                            // 手机号已经被注册
                            if (!TextUtils.isEmpty(result.getResultMsg())) {
                                ToastUtil.showToast(RegisterActivity.this,
                                        result.getResultMsg());
                            } else {
                                ToastUtil.showToast(RegisterActivity.this,
                                        R.string.tip_server_error);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(RegisterActivity.this);
                    }
                });
    }

    /**
     * 跳过短信验证码到下一步，
     */
    private void nextStepWithOutAuthCode(final String phoneStr, final String passStr) {
        verifyPhoneNumber(phoneStr, () -> realNextStep(phoneStr, passStr));
    }

    private void realNextStep(String phoneStr, String passStr) {
        RegisterUserBasicInfoActivity.start(
                this,
                "" + mobilePrefix,
                phoneStr,
                Md5Util.toMD5(passStr),
                type,
                etInvitationCode.getText().toString().trim()
        );
        // 不需要结束，登录后通过EventBus消息结束这些，
//        finish();
    }


    /**
     * 请求验证码
     */
    private void requestAuthCode(String phoneStr, String imageCodeStr,String mPassStr) {
        Map<String, String> params = new HashMap<>();
        String language = Locale.getDefault().getLanguage();
        params.put("language", language);
        params.put("areaCode", String.valueOf(mobilePrefix));
        params.put("telephone", phoneStr);
        params.put("imgCode", imageCodeStr);
        params.put("isRegister", String.valueOf(1));
        params.put("version", "1");

        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().SEND_AUTH_CODE)
                .params(params)
                .build()
                .execute(new BaseCallback<Code>(Code.class) {

                    @Override
                    public void onResponse(ObjectResult<Code> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            Log.e(TAG, "onResponse: " + result.getData().getCode());
                            mSendAgainBtn.setEnabled(false);
                            mRandCode = result.getData().getCode();// 记录验证码
                            mAuthCodeEdit.setText(mRandCode);
                            nextStepWithOutAuthCode(phoneStr, mPassStr);
                            // 开始倒计时
                          //  mReckonHandler.sendEmptyMessage(0x1);
                        } else {
                            if (!TextUtils.isEmpty(result.getResultMsg())) {
                                ToastUtil.showToast(RegisterActivity.this,
                                        result.getResultMsg());
                            } else {
                                ToastUtil.showToast(RegisterActivity.this,
                                        getString(R.string.tip_server_error));
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(mContext);
                    }
                });
    }

    private void initEvent() {
        findViewById(R.id.b_go_login).setOnClickListener(v -> finish());
        mPhoneNumEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && type == 0) {
                // 注册页面手机号输入完成后自动刷新验证码，
                // 只在移开焦点，也就是点击其他EditText时调用，
                requestImageCode();
            }
        });
        mPhoneNumEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // 手机号码修改时让图形验证码和短信验证码失效，
                // 每输入一个字符调用一次，
                mRandCode = null;
                mImageCodeEdit.setText("");
                mAuthCodeEdit.setText("");
            }
        });

        tv_prefix.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, SelectPrefixActivity.class);
            startActivityForResult(intent, SelectPrefixActivity.REQUEST_MOBILE_PREFIX_LOGIN);
        });

        // 刷新图形码
        mImageCodeIv.setOnClickListener(v -> requestImageCode());
        // 刷新图形码
        mNoAuthCodeBtn.setOnClickListener(v -> {
            // 不检查验证码就前往下一步，
            nextStepWithoutAuthCode();
        });

        mSendAgainBtn.setOnClickListener(v -> {
            String mPhoneStr = mPhoneNumEdit.getText().toString().trim();
            String mPassStr = mPassEdit.getText().toString().trim();
            if (mobilePrefix == 86 && !StringUtils.isPhone(mPhoneStr)) {
                ToastUtil.showToast(mContext, getString(R.string.tip_phone_error));
                return;
            }
            if (checkInput(mPhoneStr, mPassStr)) {
                return;
            }
//            String mImageCodeStr = mImageCodeEdit.getText().toString().trim();
//            if (TextUtils.isEmpty(mImageCodeStr)) {
//                ToastUtil.showToast(mContext, getString(R.string.tip_verification_code_empty));
//                return;
//            }
            String mImageCodeStr = "";
            requestAuthCode(mPhoneStr, mImageCodeStr,mPassStr);

        });

        // 注册
        mNextStepBtn.setOnClickListener(v -> {
            PreferenceUtils.putInt(RegisterActivity.this, Constants.AREA_CODE_KEY, mobilePrefix);
            if (type == 0) {
                nextStep();
            } else {
                nextStepWithoutAuthCode();
            }
        });
        initContractViewText();
    }


    /**
     * 初始化 注册协议 文字以及点击事件
     */
    private void initContractViewText() {
        String s = getResources().getString(R.string.register_great);
        String s1 = getResources().getString(R.string.license_service_agreement);
        String s2 = "";
        String s3 = "";
        SpannableStringBuilder sp = new SpannableStringBuilder(s + s1 + s2 + s3);
        sp.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                String language = Locale.getDefault().getLanguage();
                if (language.startsWith("zh")) {
                    language = "zh";
                } else {
                    language = "en";
                }
                String url = String.format(BuildConfig.PROTOCOL_URL + "/%s.html", language);
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    // 无论如何不能在这里崩溃，
                    // 比如手机没有浏览器，
                    Reporter.unreachable(e);
                }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(SkinUtils.getSkin(RegisterActivity.this).getAccentColor());
            }
        }, s.length(), s.length() + s1.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
/*        sp.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {

            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(RegisterActivity.this.getResources().getColor(R.color.color_00));
            }
        }, s.length() + s1.length() + s2.length(), s.length() + s1.length() + s2.length() + s3.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);*/
        tvPrivacy.setText(sp);
        tvPrivacy.setMovementMethod(LinkMovementMethod.getInstance());
    }


    private void nextStepWithoutAuthCode() {
        String mPhoneStr = mPhoneNumEdit.getText().toString().trim();
        String mPassStr = mPassEdit.getText().toString().trim();
        if (checkInput(mPhoneStr, mPassStr)) {
            return;
        }
        if (type == 1 && CommonalityTools.isLetterDigit(mPhoneStr)) {
            ToastUtil.showToast(RegisterActivity.this, "用户名不能全为数字！");
            return;
        }
       if (coreManager.getConfig().registerInviteCode >0) {
           if (TextUtils.isEmpty(etInvitationCode.getText().toString().trim())) {
                ToastUtil.showToast(this, "请输入邀请码！");
                return;
            }
        }
        nextStepWithOutAuthCode(mPhoneStr, mPassStr);
    }

    /**
     * 检查是否需要停止注册，
     *
     * @return 测试不合法返回true, 停止继续注册，
     */
    private boolean checkInput(String mPhoneStr, String mPassStr) {

        if (!UsernameHelper.verify(this, mPhoneStr, type)) {
            return true;
        }
        if (TextUtils.isEmpty(mPassStr)) {
            ToastUtil.showToast(mContext, getString(R.string.tip_password_empty));
            return true;
        }
        if (mPassStr.length() < 6) {
            ToastUtil.showToast(mContext, getString(R.string.tip_password_too_short));
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != SelectPrefixActivity.RESULT_MOBILE_PREFIX_SUCCESS)
            return;
        mobilePrefix = data.getIntExtra(Constants.MOBILE_PREFIX, 86);
        tv_prefix.setText("+" + mobilePrefix);
    }

    /**
     * 验证验证码
     */
    private void nextStep() {
        String mPhoneStr = mPhoneNumEdit.getText().toString().trim();
        String mPassStr = mPassEdit.getText().toString().trim();
        if (checkInput(mPhoneStr, mPassStr))
            return;
        if (mPhoneStr.length() < 7 || mPhoneStr.length() > 11) {
            ToastUtil.showToast(RegisterActivity.this, "电话格式不正确");
            return;
        }
    /*    String mAuthCodeStr = mAuthCodeEdit.getText().toString().trim();
        if (TextUtils.isEmpty(mAuthCodeStr)) {
            ToastUtil.showToast(mContext, getString(R.string.please_input_auth_code));
            return;
        }*/
       // nextStepWithOutAuthCode(mPhoneStr, mPassStr);

//        String mImageCodeStr = mImageCodeEdit.getText().toString().trim();
//        if (TextUtils.isEmpty(mImageCodeStr)) {
//            ToastUtil.showToast(mContext, getString(R.string.tip_verification_code_empty));
//            return;
//        }
        String mImageCodeStr = "";
        requestAuthCode(mPhoneStr, mImageCodeStr,mPassStr);
    }
}