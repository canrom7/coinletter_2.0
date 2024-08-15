package com.iimm.miliao.ui.me.redpacket;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.redpacket.Balance;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.me.redpacket.alipay.AlipayHelper;
import com.iimm.miliao.ui.tool.WebView2Activity;
import com.iimm.miliao.ui.tool.WebViewActivity;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.filter.CashierInputFilter;
import com.iimm.miliao.view.window.KeyBoad;
import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * 充值
 */
public class WxPayAdd extends BaseActivity {
    public static WxPayAdd wxPayAdd = null;

    private IWXAPI api;
    private int paytype = 0;//0支付宝，1微信
    private EditText money;
    private LinearLayout alipayll;
    private LinearLayout wxpayll;
    private ImageView alipayselector;
    private ImageView wxpayselector;
    private Button sure;
    private KeyBoad keyBoad;
    private boolean isUiCreat = false;
    private LinearLayout bankTransfer;
    private ImageView bankPaySelector;
    private LinearLayout mLlH5Transfer;
    private ImageView mIvH5Transfer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wx_pay_add);
        wxPayAdd = this;

        api = WXAPIFactory.createWXAPI(this, coreManager.getConfig().wechatAppId, false);
        api.registerApp(coreManager.getConfig().wechatAppId);

        initActionBar();
        initviews();
        initKeyBoad();
    }

    private void initviews() {
        money = findViewById(R.id.addmoney);
        InputFilter[] inputFilters = new InputFilter[]{new CashierInputFilter()};
        money.setFilters(inputFilters);
        alipayll = findViewById(R.id.alipayll);
        wxpayll = findViewById(R.id.wxll);
        bankTransfer = findViewById(R.id.ll_bank_transfer);
        mLlH5Transfer = findViewById(R.id.ll_h5_transfer);
        alipayselector = findViewById(R.id.alipayselector);
        wxpayselector = findViewById(R.id.wxpayselector);
        bankPaySelector = findViewById(R.id.iv_bank_transfer);
        mIvH5Transfer = findViewById(R.id.iv_h5_transfer);
        sure = findViewById(R.id.surepay);
        int temp = -1;
        if (coreManager.getConfig().bankPayStatus == 1 || coreManager.getConfig().bankPayStatus2 == 1) {
            bankTransfer.setVisibility(View.VISIBLE);
            temp = 23;
            mLlH5Transfer.setVisibility(View.VISIBLE);
        } else {
            bankTransfer.setVisibility(View.GONE);
            mLlH5Transfer.setVisibility(View.GONE);
        }

        if (coreManager.getConfig().wechatPayStatus == 1) {
            wxpayll.setVisibility(View.VISIBLE);
            temp = 1;
        } else {
            wxpayll.setVisibility(View.GONE);
        }

        if (coreManager.getConfig().aliPayStatus == 1) {
            alipayll.setVisibility(View.VISIBLE);
            temp = 0;
        } else {
            alipayll.setVisibility(View.GONE);
        }



        /*
         * 选择支付方式，跟新选中状态
         * */
        alipayll.setOnClickListener(v -> {
            alipayselector.setBackgroundResource(R.drawable.sel_check_wx_new);
            wxpayselector.setBackgroundResource(R.drawable.sel_nor_wx2);
            bankPaySelector.setBackgroundResource(R.drawable.sel_nor_wx2);
            mIvH5Transfer.setBackgroundResource(R.drawable.sel_nor_wx2);
            paytype = 0;
        });
        wxpayll.setOnClickListener(v -> {
            alipayselector.setBackgroundResource(R.drawable.sel_nor_wx2);
            wxpayselector.setBackgroundResource(R.drawable.sel_check_wx_new);
            bankPaySelector.setBackgroundResource(R.drawable.sel_nor_wx2);
            mIvH5Transfer.setBackgroundResource(R.drawable.sel_nor_wx2);
            paytype = 1;
        });
        bankTransfer.setOnClickListener(v -> {
            wxpayselector.setBackgroundResource(R.drawable.sel_nor_wx2);
            alipayselector.setBackgroundResource(R.drawable.sel_nor_wx2);
            bankPaySelector.setBackgroundResource(R.drawable.sel_check_wx_new);
            mIvH5Transfer.setBackgroundResource(R.drawable.sel_nor_wx2);
            paytype = 2;
        });
        mLlH5Transfer.setOnClickListener(v -> {
            wxpayselector.setBackgroundResource(R.drawable.sel_nor_wx2);
            alipayselector.setBackgroundResource(R.drawable.sel_nor_wx2);
            bankPaySelector.setBackgroundResource(R.drawable.sel_nor_wx2);
            mIvH5Transfer.setBackgroundResource(R.drawable.sel_check_wx_new);
            paytype = 3;
        });

        if (temp == 23) {
            bankTransfer.performClick();
        } else if (temp == 1) {
            wxpayll.performClick();
        } else if (temp == 0) {
            alipayll.performClick();
        }

        sure.setOnClickListener(v -> {
            String moneys = money.getText().toString().trim();
            if (TextUtils.isEmpty(moneys) || Double.valueOf(moneys) <= 0) {
                ToastUtil.showToast(WxPayAdd.this, "请输入充值金额");
                return;
            }
            if (moneys.endsWith(".")) {
                moneys = moneys + "00";
            }
            if (!moneys.contains(".")) {
                moneys = moneys + ".00";
            }
            switch (paytype) {
                case 0:
                    AlipayHelper.recharge(WxPayAdd.this, coreManager, moneys);
                    break;
                case 1:
                    if (TextUtils.isEmpty(coreManager.getConfig().wechatAppId)) {
                        ToastUtil.showToast(WxPayAdd.this, "微信AppId为空，请重试");
                        return;
                    }
                    if (api.getWXAppSupportAPI() < Build.PAY_SUPPORTED_SDK_INT) {
                        ToastUtil.showToast(getApplicationContext(), R.string.tip_no_wechat);
                    } else {
                        recharge(moneys);
                    }
                    break;
                case 2:
                    Intent intent = new Intent(WxPayAdd.this, NewBankTransferActivity.class);
                    intent.putExtra("money", moneys);
                    startActivity(intent);
                    break;
                case 3:
                    int i_money = (int) (Double.valueOf(moneys) * 100);
                    Intent h5Pay = new Intent(WxPayAdd.this, WebView2Activity.class);
                    String url = "http://www.baidu.com";
                    h5Pay.putExtra(WebViewActivity.EXTRA_URL, url);
                    Log.i(TAG, url);
                    startActivity(h5Pay);
                    break;
            }
        });

    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.recharge));
    }

    private void recharge(String money) {// 调用服务端接口，由服务端统一下单
        DialogHelper.showDefaulteMessageProgressDialog(this);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("price", money);
        params.put("payType", "2");// 支付方式 1.支付宝 2.微信

        HttpUtils.get().url(coreManager.getConfig().VX_RECHARGE)
                .params(params)
                .build()
                .execute(new BaseCallback<Balance>(Balance.class) {

                    @Override
                    public void onResponse(ObjectResult<Balance> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            PayReq req = new PayReq();
                            req.appId = result.getData().getAppId();
                            req.partnerId = result.getData().getPartnerId();
                            req.prepayId = result.getData().getPrepayId();
                            req.packageValue = "Sign=WXPay";
                            req.nonceStr = result.getData().getNonceStr();
                            req.timeStamp = result.getData().getTimeStamp();
                            req.sign = result.getData().getSign();
                            api.sendReq(req);
                        } else {
                            ToastUtil.showErrorData(WxPayAdd.this);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(WxPayAdd.this);
                    }
                });
    }


    private void initKeyBoad() {
        money.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (keyBoad != null && isUiCreat) {
                    keyBoad.refreshKeyboardOutSideTouchable(!hasFocus);
                } else if (isUiCreat) {
                    keyBoad.show();
                }
                if (hasFocus) {
                    InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(money.getWindowToken(), 0);
                }
            }
        });

        money.setOnClickListener(v -> {
            if (keyBoad != null) {
                keyBoad.show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wxPayAdd = null;
    }
}
