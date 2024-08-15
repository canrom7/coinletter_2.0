package com.iimm.miliao.ui.me.redpacket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.redpacket.Balance;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.UiUtils;
import com.iimm.miliao.view.NoDoubleClickListener;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.text.DecimalFormat;
import java.util.HashMap;

import okhttp3.Call;

/**
 * 我的钱包
 */
public class WxPayBlance extends BaseActivity {

    public static final String RSA_PRIVATE = "";
    private static final int SDK_PAY_FLAG = 1;
    private static final int SDK_AUTH_FLAG = 2;
    private TextView mBalanceTv;
    private TextView mRechargeTv;
    private TextView mWithdrawTv;
    private TextView mH5RechargeTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wx_pay_blance);
        initActionBar();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText(getString(R.string.my_purse));
        TextView mTvTitleRight = (TextView) findViewById(R.id.tv_title_right);
        mTvTitleRight.setText("账单");
        mTvTitleRight.setOnClickListener(view -> {
            if (!UiUtils.isNormalClick(view)) {
                return;
            }
            // 访问接口 获取记录
            Intent intent = new Intent(WxPayBlance.this, MyConsumeRecord.class);
            startActivity(intent);
        });
    }

    private void initView() {
        mBalanceTv = (TextView) findViewById(R.id.myblance);
        mRechargeTv = (TextView) findViewById(R.id.chongzhi);
        mWithdrawTv = (TextView) findViewById(R.id.quxian);
        mH5RechargeTv = findViewById(R.id.recharge_tv);
        mRechargeTv.setVisibility(View.VISIBLE);
        mWithdrawTv.setVisibility(View.VISIBLE);
        if (coreManager.getConfig().hmPayStatus == 1) {
            mH5RechargeTv.setVisibility(View.VISIBLE);
        } else {
            mH5RechargeTv.setVisibility(View.GONE);
        }
        mH5RechargeTv.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                Intent intent = new Intent(WxPayBlance.this, H5PayAdd.class);
                startActivity(intent);
            }
        });
        mRechargeTv.setOnClickListener(view -> {
            if (!UiUtils.isNormalClick(view)) {
                return;
            }
            if (coreManager.getConfig().wechatPayStatus == 1 ||
                    coreManager.getConfig().aliPayStatus == 1 ||
                    coreManager.getConfig().bankPayStatus == 1 ||
                    coreManager.getConfig().bankPayStatus2 == 1) {
                Intent intent = new Intent(WxPayBlance.this, WxPayAdd.class);
                startActivity(intent);
            } else {
                ToastUtil.showToast(R.string.tip_not_yet_support);
            }
        });
        mWithdrawTv.setOnClickListener(view -> {
            if (!UiUtils.isNormalClick(view)) {
                return;
            }
            if (coreManager.getConfig().wechatWithdrawStatus == 1 || coreManager.getConfig().aliWithdrawStatus == 1 || coreManager.getConfig().isWithdrawToAdmin == 1) {
                Intent intent = new Intent(WxPayBlance.this, QuXianActivity.class);
                startActivity(intent);
            } else {
                ToastUtil.showToast(R.string.tip_not_yet_support);
            }
        });
        findViewById(R.id.tvPayPassword).setOnClickListener(v -> {
            if (!UiUtils.isNormalClick(v)) {
                return;
            }
            Intent intent = new Intent(WxPayBlance.this, ChangePayPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void initData() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);

        HttpUtils.get().url(coreManager.getConfig().RECHARGE_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<Balance>(Balance.class) {

                    @Override
                    public void onResponse(ObjectResult<Balance> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            DecimalFormat decimalFormat = new DecimalFormat("0.00");
                            Balance balance = result.getData();
                            coreManager.getSelf().setBalance(Double.parseDouble(decimalFormat.format(balance.getBalance())));
                            mBalanceTv.setText("￥" + decimalFormat.format(Double.parseDouble(decimalFormat.format(balance.getBalance()))));
                        } else {
                            ToastUtil.showErrorData(WxPayBlance.this);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(WxPayBlance.this);
                    }
                });
    }
}
