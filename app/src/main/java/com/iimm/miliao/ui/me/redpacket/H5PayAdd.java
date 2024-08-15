package com.iimm.miliao.ui.me.redpacket;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.redpacket.H5Bean;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.tool.WebView2Activity;
import com.iimm.miliao.util.HttpUtil;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.filter.CashierInputFilter;
import com.iimm.miliao.view.window.KeyBoad;
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
public class H5PayAdd extends BaseActivity {
    public static H5PayAdd wxPayAdd = null;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_h5_pay_add);
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

        alipayselector = findViewById(R.id.alipayselector);
        wxpayselector = findViewById(R.id.wxpayselector);
        bankPaySelector = findViewById(R.id.iv_bank_transfer);
        sure = findViewById(R.id.surepay);
        bankTransfer.setVisibility(View.VISIBLE);
        bankTransfer.performClick();
        wxpayll.setVisibility(View.VISIBLE);
        alipayll.setVisibility(View.VISIBLE);
        /*
         * 选择支付方式，跟新选中状态
         * */
        alipayll.setOnClickListener(v -> {
            alipayselector.setBackgroundResource(R.drawable.sel_check_wx_new);
            wxpayselector.setBackgroundResource(R.drawable.sel_nor_wx2);
            bankPaySelector.setBackgroundResource(R.drawable.sel_nor_wx2);
            paytype = 0;
        });
        wxpayll.setOnClickListener(v -> {
            alipayselector.setBackgroundResource(R.drawable.sel_nor_wx2);
            wxpayselector.setBackgroundResource(R.drawable.sel_check_wx_new);
            bankPaySelector.setBackgroundResource(R.drawable.sel_nor_wx2);
            paytype = 1;
        });
        bankTransfer.setOnClickListener(v -> {
            wxpayselector.setBackgroundResource(R.drawable.sel_nor_wx2);
            alipayselector.setBackgroundResource(R.drawable.sel_nor_wx2);
            bankPaySelector.setBackgroundResource(R.drawable.sel_check_wx_new);
            paytype = 2;
        });


        sure.setOnClickListener(v -> {
            String moneys = money.getText().toString().trim();
            if (TextUtils.isEmpty(moneys) || Double.valueOf(moneys) <= 0) {
                ToastUtil.showToast(H5PayAdd.this, "请输入充值金额");
                return;
            }
            switch (paytype) {
                case 0:
                    recharge(moneys, "2");
                    break;
                case 1:
                    recharge(moneys, "1");
                    break;
                case 2:
                    recharge(moneys, "3");
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

    private void recharge(String money, String type) {// 调用服务端接口，由服务端统一下单
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("price", money);
        params.put("payType", "8");
        params.put("payWay", type);//1微信 2支付宝 3银行卡
        params.put("userIp", TextUtils.isEmpty(HttpUtil.getIPAddress(this)) ? "" : HttpUtil.getIPAddress(this));

        HttpUtils.post().url(coreManager.getConfig().VX_RECHARGE)
                .params(params)
                .build()
                .execute(new BaseCallback<H5Bean>(H5Bean.class) {

                    @Override
                    public void onResponse(ObjectResult<H5Bean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            Intent h5Pay = new Intent(H5PayAdd.this, WebView2Activity.class);
                            String url = "https://testht.icloudpay.us:1443/mobile/chongzhi/submit";
                            h5Pay.putExtra(WebView2Activity.EXTRA_URL, url);
                            h5Pay.putExtra(WebView2Activity.EXTRA_URL_POST, result.getData().toString());
                            startActivity(h5Pay);
//                            sd(result.getData());
                        } else {
                            ToastUtil.showErrorData(H5PayAdd.this);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(H5PayAdd.this);
                    }
                });
    }

    /*public void sd(H5Bean h5Bean) {
        String url = "https://testht.icloudpay.us:1443";
        HttpUtils.post().url(url)
                .params("out_trade_no", h5Bean.getOut_trade_no())
                .params("money", h5Bean.getMoney())
                .params("sign", h5Bean.getSign())
                .params("return_url", h5Bean.getReturn_url())
                .params("userip", h5Bean.getUserip())
                .params("pid", h5Bean.getPid())
                .params("type", h5Bean.getType())
                .params("notify_url", h5Bean.getNotify_url())
                .params("userid", h5Bean.getUserid())
                .build()
                .execute(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                    }
                });
    }*/

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
