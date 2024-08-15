package com.dhh.easy.chat.wxapi;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import com.iimm.miliao.AppConfig;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.BandUploadResult;
import com.iimm.miliao.bean.WXUploadResult;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.account.LoginActivity;
import com.iimm.miliao.ui.account.LoginWXBean;
import com.iimm.miliao.ui.account.SwitchLoginActivity;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.me.redpacket.PayPasswordVerifyDialog;
import com.iimm.miliao.ui.me.redpacket.QuXianActivity;
import com.iimm.miliao.util.Md5Util;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

public class WXEntryActivity extends BaseActivity implements IWXAPIEventHandler {

    private static Context mContext;
    private IWXAPI api;
    private String mWechatAppId;

    public WXEntryActivity() {
        // 微信登录的回调是到这里，所以可能没有登录，
        noLoginRequired();
    }

    public static void wxLogin(Context ctx, String wxId) {
        mContext = ctx;
        IWXAPI api = WXAPIFactory.createWXAPI(ctx, wxId, false);
        api.registerApp(wxId);
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "login";
        api.sendReq(req);
    }

    public static void wxBand(Context ctx, String wxId) {
        IWXAPI api = WXAPIFactory.createWXAPI(ctx, wxId, false);
        api.registerApp(wxId);
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "band";
        api.sendReq(req);
    }

    public static void wxQuXian(Context ctx, String wxId) {
        IWXAPI api = WXAPIFactory.createWXAPI(ctx, wxId, false);
        api.registerApp(wxId);
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_demo_test";
        api.sendReq(req);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vx_result);
        getSupportActionBar().hide();
        api = WXAPIFactory.createWXAPI(WXEntryActivity.this, coreManager.getConfig().wechatAppId, false);
        api.handleIntent(getIntent(), this);
        mWechatAppId = coreManager.getConfig().wechatAppId;
    }

    // 微信发送请求到第三方应用时，会回调到该方法
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {

    }

    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    @Override
    public void onResp(BaseResp resp) {
        Log.i("WXEntryActivity", resp.toString());
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                try {
                    SendAuth.Resp mSendAuthResp = ((SendAuth.Resp) resp);
                    if (Objects.equals(mSendAuthResp.state, "login")) {
                        if (mContext.getClass().equals(LoginActivity.class)) {
                            EventBus.getDefault().post(new LoginWXBean(mSendAuthResp.code));
                        } else if (mContext.getClass().equals(SwitchLoginActivity.class)) {
                            SwitchLoginActivity.bindThird(WXEntryActivity.this);
                        }
                        finish();
//                        getOpenId(mSendAuthResp.code);
                    } else if (Objects.equals(mSendAuthResp.state, "band")) {
//                        bandAccount(mSendAuthResp.code);
                        bandOpenId(mSendAuthResp.code);
                    } else if (Objects.equals(mSendAuthResp.state, "wechat_sdk_demo_test")) {
                        updateCodeToService(mSendAuthResp.code);
                    } else {
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    finish();
                }
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                finish();
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                finish();
                break;
            default:
                finish();
                break;
        }
    }

    private void bandAccount(String code) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        HttpUtils.get().url(coreManager.getConfig().VX_GET_OPEN_ID)
                .params(params)
                .build()
                .execute(new BaseCallback<WXUploadResult>(WXUploadResult.class) {

                    @Override
                    public void onResponse(ObjectResult<WXUploadResult> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(getApplicationContext(), result)) {
                            String openId = result.getData().getOpenid();
                            if (TextUtils.isEmpty(openId)) {
                                Toast.makeText(WXEntryActivity.this, "绑定服务器失败", Toast.LENGTH_SHORT).show();
                                EventBus.getDefault().post(new EventUpdateBandAccount(2, "result", "err"));
                                finish();
                            } else {
                                bandOpenId(openId);
                            }
                        } else {
                            Toast.makeText(WXEntryActivity.this, "绑定服务器失败", Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().post(new EventUpdateBandAccount(2, "result", "err"));
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        finish();
                    }
                });
    }

    private void bandOpenId(String openId) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("otherType", "2");
        params.put("code", openId);
        HttpUtils.get().url(coreManager.getConfig().BAND_THIRD_PARTY_NEW)
                .params(params)
                .build()
                .execute(new BaseCallback<BandUploadResult.DataBean>(BandUploadResult.DataBean.class) {

                    @Override
                    public void onResponse(ObjectResult<BandUploadResult.DataBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            if (result.getData().getCode().equals("1")) {
                                Toast.makeText(WXEntryActivity.this, "绑定服务器成功", Toast.LENGTH_SHORT).show();
                                EventBus.getDefault().post(new EventUpdateBandAccount(2, "result", "ok"));
                                finish();
                            } else {
                                Toast.makeText(WXEntryActivity.this, result.getData().getMsg(), Toast.LENGTH_SHORT).show();
                                EventBus.getDefault().post(new EventUpdateBandAccount(2, "result", "err"));
                                finish();
                            }
                        } else {
                            Toast.makeText(WXEntryActivity.this, "绑定服务器失败", Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().post(new EventUpdateBandAccount(2, "result", "err"));
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        finish();
                    }
                });
    }


    private void getOpenId(String code) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("code", code);

        HttpUtils.get().url(coreManager.getConfig().VX_GET_OPEN_ID)
                .params(params)
                .build()
                .execute(new BaseCallback<WXUploadResult>(WXUploadResult.class) {

                    @Override
                    public void onResponse(ObjectResult<WXUploadResult> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(getApplicationContext(), result)) {
                            String openId = result.getData().getOpenid();
                            if (TextUtils.isEmpty(openId)) {
                                ToastUtil.showToast(getApplicationContext(), R.string.tip_server_error);
                            } else {
                                if (mContext.getClass().equals(LoginActivity.class)) {
                                    //LoginActivity.bindThird(WXEntryActivity.this, result.getData().getOpenid(), "2");
                                } else if (mContext.getClass().equals(SwitchLoginActivity.class)) {
                                    SwitchLoginActivity.bindThird(WXEntryActivity.this);
                                }
                            }
                        }
                        finish();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        finish();
                    }
                });
    }

    private void updateCodeToService(String code) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("code", code);

        HttpUtils.get().url(coreManager.getConfig().VX_UPLOAD_CODE)
                .params(params)
                .build()
                .execute(new BaseCallback<WXUploadResult>(WXUploadResult.class) {

                    @Override
                    public void onResponse(ObjectResult<WXUploadResult> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            WXUploadResult wxUploadResult = result.getData();
                            transfer(wxUploadResult.getOpenid());
                        } else {
                            Toast.makeText(WXEntryActivity.this, "绑定服务器失败", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        finish();
                    }
                });
    }

    private void transfer(final String vx_openid) {
        PayPasswordVerifyDialog dialog = new PayPasswordVerifyDialog(this, QuXianActivity.amount);
        dialog.setAction(getString(R.string.withdraw));
        // amount单位是分，而且是整元乘100得到的分，一定可以被100整除，
//        dialog.setMoney(String.valueOf(Integer.valueOf(QuXianActivity.amount) / 100));
        dialog.setOnInputFinishListener(new PayPasswordVerifyDialog.OnInputFinishListener() {
            @Override
            public void onInputFinish(String password) {
                DialogHelper.showDefaulteMessageProgressDialog(WXEntryActivity.this);
                String mAccessToken = coreManager.getSelfStatus().accessToken;
                String mLoginUserId = coreManager.getSelf().getUserId();
                String time = String.valueOf(TimeUtils.time_current_time());

                String str1 = AppConfig.apiKey + vx_openid + mLoginUserId;
                String str2 = Md5Util.toMD5(mAccessToken + String.valueOf((Double.parseDouble(QuXianActivity.amount) * 100)) + time);
                String str3 = Md5Util.toMD5(password);
                String secret = Md5Util.toMD5(str1 + str2 + str3);
                Log.d(HttpUtils.TAG, String.format(Locale.CHINA, "addSecret: md5(%s+%s+%s+md5(%s+%s+%s)+md5(%s)) = %s", AppConfig.apiKey, vx_openid, mLoginUserId, mAccessToken, QuXianActivity.amount, time, password, secret));

                final Map<String, String> params = new ArrayMap<>();
                params.put("access_token", mAccessToken);
                params.put("amount", String.valueOf((Double.parseDouble(QuXianActivity.amount) * 100)));
//                params.put("amount", QuXianActivity.amount);
                params.put("time", time);
                params.put("secret", secret);

                HttpUtils.post().url(coreManager.getConfig().VX_TRANSFER_PAY)
                        .params(params)
                        .build()
                        .execute(new BaseCallback<WXUploadResult>(WXUploadResult.class) {
                            @Override
                            public void onResponse(ObjectResult<WXUploadResult> result) {
                                if (result.getResultCode() == 1 && result.getData() != null) {
                                    ToastUtil.showToast(WXEntryActivity.this, R.string.tip_withdraw_success);
                                } else {
                                    ToastUtil.showToast(WXEntryActivity.this, result.getResultMsg());
                                }
                                if (!isDestroyed()) {
                                    finish();
                                }
                            }

                            @Override
                            public void onError(Call call, Exception e) {
                                if (!isDestroyed()) {
                                    finish();
                                }
                                ToastUtil.showErrorData(WXEntryActivity.this);
                            }
                        });
            }
        });
        dialog.setOnDismissListener(dialog1 -> {
            finish();
        });
        dialog.show();
    }
}