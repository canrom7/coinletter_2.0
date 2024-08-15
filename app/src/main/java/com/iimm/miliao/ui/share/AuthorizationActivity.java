package com.iimm.miliao.ui.share;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iimm.miliao.AppConfig;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.LoginBean;
import com.iimm.miliao.bean.LoginResultBean;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.helper.LoginHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.DeviceInfoUtil;
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
 * 分享 最近联系人
 */
public class AuthorizationActivity extends BaseActivity {
    private String mShareContent;
    private LoginBean mLoginBean;

    private boolean isNeedExecuteLogin;

    public AuthorizationActivity() {
        noLoginRequired();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_result);

        // 已进入授权界面
        ShareConstant.IS_SHARE_L_COME = true;

        mShareContent = getIntent().getStringExtra(ShareConstant.EXTRA_SHARE_CONTENT);
        if (TextUtils.isEmpty(mShareContent)) {// 外部跳转进入
            mShareContent = ShareConstant.ShareContent;
        } else {// 数据下载页面进入
            ShareConstant.ShareContent = mShareContent;
        }
        mLoginBean = JSON.parseObject(mShareContent, LoginBean.class);

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

        initActionBar();
        initView();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setVisibility(View.GONE);
        TextView mTvTitleLeft = findViewById(R.id.tv_title_left);
        mTvTitleLeft.setText(getString(R.string.close));
        mTvTitleLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView() {
        ImageView mAppIconIv = findViewById(R.id.app_icon_iv);
        AvatarHelper.getInstance().displayUrl(mLoginBean.getAppIcon(), mAppIconIv);
        TextView mAppNameTv = findViewById(R.id.app_name_tv);
        mAppNameTv.setText(mLoginBean.getAppName());

        findViewById(R.id.login_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                verificationLogin();
            }
        });
    }

    // 判断是否用户 '登录' 权限
    private void verificationLogin() {
        Map<String, String> params = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis() / 1000);
        String secret = Md5Util.toMD5(AppConfig.apiKey + mLoginBean.getAppId() + coreManager.getSelf().getUserId() +
                Md5Util.toMD5(coreManager.getSelfStatus().accessToken + time) + Md5Util.toMD5(mLoginBean.getAppSecret()));
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", coreManager.getSelf().getUserId());
        params.put("type", String.valueOf(1));// 1.授权 2.分享 3.支付
        params.put("appId", mLoginBean.getAppId());
        params.put("appSecret", mLoginBean.getAppSecret());
        params.put("time", time);
        params.put("secret", secret);

        HttpUtils.get().url(coreManager.getConfig().SDK_OPEN_AUTH_INTERFACE)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {
                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            JSONObject jsonObject = JSONObject.parseObject(result.getData());
                            String userId = jsonObject.getString("userId");
                            loginResult(userId);
                        } else {// 没有 ‘登录’ 权限
                            DialogHelper.tip(mContext, getString(R.string.tip_no_login_permission));
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        // 网络异常
                        ToastUtil.showNetError(mContext);
                    }
                });
    }

    private void loginResult(String userId) {
        LoginResultBean mLoginResultBean = new LoginResultBean();
        mLoginResultBean.setUserId(userId);
        mLoginResultBean.setAvatarUrl(AvatarHelper.getAvatarUrl(coreManager.getSelf().getUserId(), true));
        mLoginResultBean.setNickName(coreManager.getSelf().getNickName());
        mLoginResultBean.setSex(coreManager.getSelf().getSex());
        mLoginResultBean.setBirthday(coreManager.getSelf().getBirthday());
        String skLoginResult = JSON.toJSONString(mLoginResultBean);

        // 这个action要和分享sdk接收的广播action相同，不能直接改，
        Intent intent = new Intent("android.intent.action.SK_Authorization");
        intent.putExtra(ShareConstant.EXTRA_AUTHORIZATION_RESULT, skLoginResult);
        sendBroadcast(intent);
        if (DeviceInfoUtil.isOppoRom()) {
            // 调试发现OPPO手机被调起后当前界面不会自动回到后台，手动调一下
            moveTaskToBack(true);
        }
        finish();
    }
}
