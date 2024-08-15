package com.iimm.miliao.ui.share;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.iimm.miliao.AppConfig;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.AuthInterfaceBean;
import com.iimm.miliao.bean.CodeBean;
import com.iimm.miliao.bean.CodeInfoBean;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.helper.LoginHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.Md5Util;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * MrLiu253@163.com
 *
 * @time 2019-08-17
 */
public class AuthorizationLoginActivity extends BaseActivity implements View.OnClickListener {

    private TextView mNameTv;
    private ImageView mImage;
    private TextView mPromptTv;
    private RoundedImageView mRoundedImageView;
    private TextView mNicknameTv;
    private TextView mInfoTv;

    private String mTigType;
    private String mTigName;
    private String mTigAppId;
    private String mTigAppSecret;
    private String mTigCallBackUrl;
    private String mTigImage;

    private boolean isNeedExecuteLogin;

    public AuthorizationLoginActivity() {
        noLoginRequired();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization_login);
        //获取传递的数据
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(ShareConstant.TIG_BUNDLE);
        mTigType = bundle.getString(ShareConstant.TIG_TYPE);
        mTigName = bundle.getString(ShareConstant.TIG_NAME);
        mTigAppId = bundle.getString(ShareConstant.TIG_APP_ID);
        mTigAppSecret = bundle.getString(ShareConstant.TIG_APP_SECRET);
        mTigCallBackUrl = bundle.getString(ShareConstant.TIG_CALLBACK_URL);
        mTigImage = bundle.getString(ShareConstant.TIG_IMAGE);

        // 判断本地登录状态
        int userStatus = LoginHelper.prepareUser(mContext, coreManager);
        switch (userStatus) {
            case LoginHelper.STATUS_USER_FULL:
            case LoginHelper.STATUS_USER_NO_UPDATE:
            case LoginHelper.STATUS_USER_TOKEN_OVERDUE:
                boolean isConflict = PreferenceUtils.getBoolean(this, Constants.LOGIN_CONFLICT, false);
                if (isConflict) {
                    isNeedExecuteLogin = true;
                }
                break;
            case LoginHelper.STATUS_USER_SIMPLE_TELPHONE:
                isNeedExecuteLogin = true;
                break;
            case LoginHelper.STATUS_NO_USER:
            default:
                isNeedExecuteLogin = true;
        }

        if (isNeedExecuteLogin) {// 需要先执行登录操作
            startActivity(new Intent(mContext, ShareLoginActivity.class));
            finish();
            return;
        }
        if (TextUtils.isEmpty(mTigAppId)) {
            ToastUtil.showToast(AuthorizationLoginActivity.this, "AppId不能为空");
            return;
        } else if (TextUtils.isEmpty(mTigAppSecret)) {
            ToastUtil.showToast(AuthorizationLoginActivity.this, "AppSecret不能为空");
            return;
        } else if (TextUtils.isEmpty(mTigName)) {
            ToastUtil.showToast(AuthorizationLoginActivity.this, "名称不能为空");
            return;
        } else if (TextUtils.isEmpty(mTigImage)) {
            ToastUtil.showToast(AuthorizationLoginActivity.this, "logo不能为空");
            return;
        }
        initActionBar();
        initView();
    }

    private void initActionBar() {
        //getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setVisibility(View.GONE);
        TextView textView = findViewById(R.id.tv_title_left);
        textView.setVisibility(View.VISIBLE);
        textView.setText("关闭");
        textView.setOnClickListener(this);
    }

    private void initView() {
        mImage = findViewById(R.id.app_image);
        mNameTv = findViewById(R.id.app_name);
        Glide.with(AuthorizationLoginActivity.this).load(mTigImage).into(mImage);
        mNameTv.setText(mTigName);
        mPromptTv = findViewById(R.id.app_prompt);
        mPromptTv.setText(String.format("你的%s头像、昵称、地区和性别 信息", getString(R.string.app_name)));
        mRoundedImageView = findViewById(R.id.app_avatar_riv);
        AvatarHelper.getInstance().displayAvatar(coreManager.getSelf().getUserId(), mRoundedImageView, true);
        mNicknameTv = findViewById(R.id.app_nickname_tv);
        mNicknameTv.setText(coreManager.getSelf().getNickName());
        mInfoTv = findViewById(R.id.app_info_tv);
        mInfoTv.setText(String.format("%s个人信息", getString(R.string.app_name)));
        findViewById(R.id.app_refuse_tv).setOnClickListener(this);
        findViewById(R.id.app_agree_tv).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_title_left:
                backtrack(null, "取消授权");
                break;
            case R.id.app_refuse_tv://拒绝
                backtrack(null, "拒绝授权");
                break;
            case R.id.app_agree_tv:
                verifyAccount();
                break;
        }
    }

    /**
     * 验证当前应用
     */
    private void verifyAccount() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        String time = String.valueOf(System.currentTimeMillis() / 1000);
        String secret = Md5Util.toMD5(mTigAppId + Md5Util.toMD5(time) + Md5Util.toMD5(mTigAppSecret));
        Map<String, String> params = new ArrayMap<>();
        params.put("appId", mTigAppId);
        params.put("appSecret", mTigAppSecret);
        params.put("secret", secret);
        params.put("time", time);
        HttpUtils.getNoSecret().url(coreManager.getConfig().AUTHORIZATION)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result.getResultCode() == 1) {
                            verificationLogin();
                        } else {
                            DialogHelper.dismissProgressDialog();
                            backtrack(null, result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();

                    }
                });
    }

    /**
     * 验证用户是否有权限
     */
    private void verificationLogin() {
        Map<String, String> params = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis() / 1000);
        String secret = Md5Util.toMD5(AppConfig.apiKey + mTigAppId + coreManager.getSelf().getUserId() +
                Md5Util.toMD5(coreManager.getSelfStatus().accessToken + time) + Md5Util.toMD5(mTigAppSecret));
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", coreManager.getSelf().getUserId());
        params.put("type", mTigType);// 1.授权 2.分享 3.支付
        params.put("appId", mTigAppId);
        params.put("appSecret", mTigAppSecret);
        params.put("time", time);
        params.put("secret", secret);

        HttpUtils.getNoSecret().url(coreManager.getConfig().SDK_OPEN_AUTH_INTERFACE)
                .params(params)
                .build()
                .execute(new BaseCallback<AuthInterfaceBean>(AuthInterfaceBean.class) {
                    @Override
                    public void onResponse(ObjectResult<AuthInterfaceBean> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            getCode();
                        } else {
                            DialogHelper.dismissProgressDialog();
                            backtrack(null, getString(R.string.tip_no_login_permission));
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                    }
                });
    }

    /**
     * 获取Code
     */
    private void getCode() {
        HttpUtils.getNoSecret().url(coreManager.getConfig().AUTHOR_CHECK)
                .params("appId", mTigAppId)
                .params("state", coreManager.getSelfStatus().accessToken)
                .params("callbackUrl", mTigCallBackUrl)
                .build().execute(new BaseCallback<CodeBean>(CodeBean.class) {
            @Override
            public void onResponse(ObjectResult<CodeBean> result) {
                if (result.getResultCode() == 1 && result.getData() != null) {
                    getCodeInfo(result.getData().getCode());
                } else {
                    DialogHelper.dismissProgressDialog();
                    backtrack(null, "获取Code失败");
                }
            }

            @Override
            public void onError(Call call, Exception e) {
                DialogHelper.dismissProgressDialog();
            }
        });
    }

    /**
     * 根据Code获取资料
     */
    private void getCodeInfo(String code) {
        HttpUtils.getNoSecret().url(coreManager.getConfig().CODE_OAUTH)
                .params("code", code)
                .build().execute(new BaseCallback<CodeInfoBean>(CodeInfoBean.class) {
            @Override
            public void onResponse(ObjectResult<CodeInfoBean> result) {
                DialogHelper.dismissProgressDialog();
                if (result.getResultCode() == 1 && result.getData() != null) {
                    String skLoginResult = JSON.toJSONString(result.getData());
                    backtrack(skLoginResult, "授权成功");
                } else {
                    backtrack(null, "获取资料失败");
                }
            }

            @Override
            public void onError(Call call, Exception e) {
                DialogHelper.dismissProgressDialog();
            }
        });
    }


    private void backtrack(String data, String error) {
        Intent intent1 = new Intent();
        if (data != null) {
            intent1.putExtra("TIG_data", data);
        }
        intent1.putExtra("TIG_error", error);
        setResult(RESULT_OK, intent1);
        finish();
    }
}
