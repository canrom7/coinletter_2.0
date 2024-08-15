package com.iimm.miliao.ui.me;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.dhh.easy.chat.wxapi.EventUpdateBandAccount;
import com.dhh.easy.chat.wxapi.WXEntryActivity;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.BandBean;
import com.iimm.miliao.bean.BandUploadResult;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.base.BaseUiListener;
import com.iimm.miliao.ui.me.redpacket.BindNewPhoneActivity;
import com.iimm.miliao.ui.me.redpacket.ChangePhoneActivity;
import com.iimm.miliao.util.AppUtils;
import com.iimm.miliao.util.EventBusHelper;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.UiUtils;
import com.iimm.miliao.view.SelectionFrame;
import com.iimm.miliao.xmpp.util.ImHelper;
import com.suke.widget.SwitchButton;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.JsonCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import org.json.JSONObject;

import java.util.Map;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * 绑定账号 更新
 */
public class BandAccountActivity extends BaseActivity implements View.OnClickListener {

    private SwitchButton tvBindWx;
    private SwitchButton tvBindQQ;
    private boolean isBandWx;
    private boolean isBandQQ;
    private TextView mPhoneTv;
    private RelativeLayout local_course_rl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_band_account);
        EventBusHelper.register(this);
        initActionBar();
        initView();
        getBindInfo();

    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.bind_account_set));
    }

    private void initView() {
        local_course_rl = findViewById(R.id.local_course_rl);
        tvBindWx = findViewById(R.id.tv_bind_wx);
        tvBindQQ = findViewById(R.id.tv_bind_qq);
        mPhoneTv = findViewById(R.id.tv_bind_phone);
        local_course_rl.setOnClickListener(this);
        findViewById(R.id.tv_bind_wx_view).setOnClickListener(this);
        findViewById(R.id.tv_bind_qq_view).setOnClickListener(this);
        if (coreManager.getConfig().qqLoginStatus == 1) {
            findViewById(R.id.qq_band_rl).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.qq_band_rl).setVisibility(View.GONE);
        }
        if (coreManager.getConfig().wechatLoginStatus == 1) {
            findViewById(R.id.wx_band_rl).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.wx_band_rl).setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(coreManager.getSelf().getTelephone())) {
            mPhoneTv.setText("暂无");
        } else {
            mPhoneTv.setText("+" + coreManager.getSelf().getTelephone());
        }
    }

    private void updateUi() {
        tvBindWx.setChecked(isBandWx);
        tvBindQQ.setChecked(isBandQQ);
    }

    // 获取用户的设置状态
    private void getBindInfo() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HttpUtils.get().url(coreManager.getConfig().USER_GET_BAND_ACCOUNT)
                .params("access_token", coreManager.getSelfStatus().accessToken)
                .build()
                .execute(new JsonCallback() {

                    @Override
                    public void onResponse(String result) {
                        DialogHelper.dismissProgressDialog();
                        BandBean bandBean = JSON.parseObject(result, BandBean.class);
                        if (bandBean != null) {
                            for (int pos = 0; pos < bandBean.getData().size(); pos++) {
                                int type = bandBean.getData().get(pos).getType();
                                if (type == 2) {
                                    isBandWx = true;
                                } else if (type == 1) {
                                    isBandQQ = true;
                                }
                            }
                        }

                        updateUi();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        updateUi();
                    }
                });
    }

    private void showSelectDialog(boolean type) {
        String content = "";
        String buttonText = "";
        if (type) {
            content = isBandWx ? getResources().getString(R.string.dialog_toast) : getResources().getString(R.string.dialog_being_go);
            buttonText = isBandWx ? getResources().getString(R.string.dialog_Relieve) : getResources().getString(R.string.dialog_go);
        } else {
            content = isBandQQ ? getResources().getString(R.string.dialog_toast_qq) : getResources().getString(R.string.dialog_being_go_qq);
            buttonText = isBandQQ ? getResources().getString(R.string.dialog_Relieve) : getResources().getString(R.string.dialog_go);
        }


        SelectionFrame selectionFrame = new SelectionFrame(mContext);
        selectionFrame.setSomething(null, content, getString(R.string.cancel), buttonText,
                new SelectionFrame.OnSelectionFrameClickListener() {

                    @Override
                    public void cancelClick() {

                    }

                    @Override
                    public void confirmClick() {
                        if (type) {
                            if (!AppUtils.isAppInstalled(mContext, "com.tencent.mm")) {
                                ToastUtil.showToast(mContext, getString(R.string.tip_no_wx_chat));
                                return;
                            }
                            if (isBandWx) {
                                unBindInfo(type);
                            } else {
                                WXEntryActivity.wxBand(mContext, coreManager.getConfig().wechatAppId);
                            }

                        } else {
                            if (isBandQQ) {
                                unBindInfo(type);
                            } else {
                                loginQQ();
                            }

                        }

                    }
                });
        selectionFrame.show();
    }

    // 修改用户绑定
    private void unBindInfo(boolean type) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HttpUtils.get().url(coreManager.getConfig().USER_UN_BAND_ACCOUNT)
                .params("access_token", coreManager.getSelfStatus().accessToken)
                .params("type", type ? "2" : "1")
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            if (type) {
                                isBandWx = false;
                            } else {
                                isBandQQ = false;
                            }
                            ImHelper.syncMyInfoToOtherMachine();
                            updateUi();
                        } else {
                            ToastUtil.showToast(BandAccountActivity.this, result.getResultMsg());
                        }

                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        updateUi();
                        ToastUtil.showToast(BandAccountActivity.this, e.getMessage());
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventUpdateBandAccount message) {
        if (message.type == 1) {
            isBandQQ = "ok".equals(message.msg);
        } else if (message.type == 2) {
            isBandWx = "ok".equals(message.msg);
        }
        ImHelper.syncMyInfoToOtherMachine();
        updateUi();
    }

    Tencent mTencent;

    public void loginQQ() {
        mTencent = Tencent.createInstance(coreManager.getConfig().qqLoginAppId, this);
        mTencent.login(this, "all", loginListener);
    }

    IUiListener loginListener = new BaseUiListener() {
        @Override
        protected void doComplete(JSONObject jsonObject) {
            try {
                String token = jsonObject.getString(com.tencent.connect.common.Constants.PARAM_ACCESS_TOKEN);
                String expires = jsonObject.getString(com.tencent.connect.common.Constants.PARAM_EXPIRES_IN);
                String openId = jsonObject.getString(com.tencent.connect.common.Constants.PARAM_OPEN_ID);
                if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires)
                        && !TextUtils.isEmpty(openId)) {
                    mTencent.setAccessToken(token, expires);
                    mTencent.setOpenId(openId);
                    bandQQ(openId, token);
                }
            } catch (Exception e) {
            }
        }
    };

    private void bandQQ(String openId, String token) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new ArrayMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("otherType", "1");
        params.put("code", openId);
        params.put("otherToken", token);
        HttpUtils.get().url(coreManager.getConfig().BAND_THIRD_PARTY_NEW)
                .params(params)
                .build()
                .execute(new BaseCallback<BandUploadResult.DataBean>(BandUploadResult.DataBean.class) {

                    @Override
                    public void onResponse(ObjectResult<BandUploadResult.DataBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            if (result.getData().getCode().equals("1")) {
                                ToastUtil.showToast(BandAccountActivity.this, "绑定服务器成功");
                                EventBus.getDefault().post(new EventUpdateBandAccount(1, "result", "ok"));
                            } else {
                                ToastUtil.showToast(BandAccountActivity.this, result.getData().getMsg());
                                EventBus.getDefault().post(new EventUpdateBandAccount(1, "result", "err"));
                            }
                        } else {
                            ToastUtil.showToast(BandAccountActivity.this, "绑定服务器失败");
                            EventBus.getDefault().post(new EventUpdateBandAccount(1, "result", "err"));
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == com.tencent.connect.common.Constants.REQUEST_LOGIN ||
                requestCode == com.tencent.connect.common.Constants.REQUEST_APPBAR) {
            Tencent.onActivityResultData(requestCode, resultCode, data, loginListener);
        }
        if (requestCode == 8000 || requestCode == 8001 && resultCode == RESULT_OK) {
            if (TextUtils.isEmpty(coreManager.getSelf().getTelephone())) {
                mPhoneTv.setText("暂无");
            } else {
                mPhoneTv.setText("+" + coreManager.getSelf().getTelephone());
            }
            ImHelper.syncMyInfoToOtherMachine();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        if (!UiUtils.isNormalClick(v)) {
            return;
        }
        switch (v.getId()) {
            case R.id.local_course_rl://更改绑定手机号
                if (PreferenceUtils.getInt(BandAccountActivity.this, AppConstant.LOGIN_other)==7&&TextUtils.isEmpty(coreManager.getSelf().getTelephone())) {
                    startActivityForResult(new Intent(BandAccountActivity.this, ChangePhoneActivity.class), 8000);
                } else {
                    startActivityForResult(new Intent(this, BindNewPhoneActivity.class), 8001);
                }
                break;
            case R.id.tv_bind_wx_view:
                BandAccountActivity.this.showSelectDialog(true);
                break;
            case R.id.tv_bind_qq_view:
                BandAccountActivity.this.showSelectDialog(false);
                break;
            default:
                break;
        }
    }

}
