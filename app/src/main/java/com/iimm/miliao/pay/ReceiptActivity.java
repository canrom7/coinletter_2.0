package com.iimm.miliao.pay;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.example.qrcode.Constant;
import com.example.qrcode.utils.CommonUtils;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.bean.Receipt;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.MainActivity;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.DisplayUtil;
import com.iimm.miliao.util.FileUtil;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.StatusBarUtil;
import com.iimm.miliao.util.ToastUtil;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * 收款码
 */
public class ReceiptActivity extends BaseActivity {

    private int resumeCount;
    private String mLoginUserId;
    private ImageView mReceiptQrCodeIv;
    private ConstraintLayout mpay;
    private String money, description;
    private TextView mMoneyTv;
    private TextView mSetMoneyTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setStatusBar(this, getResources().getColor(R.color.color_00));
        setContentView(R.layout.activity_payment);
        mLoginUserId = coreManager.getSelf().getUserId();
        money = PreferenceUtils.getString(mContext, Constants.RECEIPT_SETTING_MONEY + mLoginUserId);
        description = PreferenceUtils.getString(mContext, Constants.RECEIPT_SETTING_DESCRIPTION + mLoginUserId);

        initActionBar();
        initView();
        initEvent();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeCount++;
        if (resumeCount > 1) {
            money = PreferenceUtils.getString(mContext, Constants.RECEIPT_SETTING_MONEY + mLoginUserId);
            description = PreferenceUtils.getString(mContext, Constants.RECEIPT_SETTING_DESCRIPTION + mLoginUserId);
            refreshView();
            refreshReceiptQRCode();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(view -> finish());
        TextView titleTv = findViewById(R.id.tv_title_center);
        titleTv.setText(getString(R.string.rp_receipt));
        titleTv.setTextColor(Color.WHITE);
    }

    private void initView() {
        mReceiptQrCodeIv = findViewById(R.id.rp_qr_code_iv);
        refreshReceiptQRCode();
        mMoneyTv = findViewById(R.id.rp_money_tv);
        mSetMoneyTv = findViewById(R.id.rp_set_money_tv);
        refreshView();
    }

    private void initEvent() {
        findViewById(R.id.rp_set_money_tv).setOnClickListener(v -> {
            if (!TextUtils.isEmpty(money)) { // 清除金额
                money = "";
                description = "";
                PreferenceUtils.putString(mContext, Constants.RECEIPT_SETTING_MONEY + mLoginUserId, money);
                PreferenceUtils.putString(mContext, Constants.RECEIPT_SETTING_DESCRIPTION + mLoginUserId, description);
                mSetMoneyTv.setText(getString(R.string.rp_receipt_tip2));
                refreshView();
                refreshReceiptQRCode();
            } else { // 设置金额
                startActivity(new Intent(mContext, ReceiptSetMoneyActivity.class));
            }
        });

        findViewById(R.id.rp_save_receipt_code_tv).setOnClickListener(v -> {
            FileUtil.saveImageToGallery2(mContext, getBitmap(ReceiptActivity.this.getWindow().getDecorView()));
        });
        findViewById(R.id.go_receipt_ll).setOnClickListener(v -> {
            MainActivity.requestQrCodeScan(ReceiptActivity.this);
        });
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventReceiptSuccess message) {
        DialogHelper.tip(mContext, getString(R.string.payment, message.getPaymentName()));
    }

    private void refreshView() {
        mMoneyTv.setText("￥" + money);

        if (!TextUtils.isEmpty(money)) {
            mSetMoneyTv.setText(getString(R.string.rp_receipt_tip3));
            mMoneyTv.setVisibility(View.VISIBLE);
        } else {
            mSetMoneyTv.setText(getString(R.string.rp_receipt_tip2));
            mMoneyTv.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(description)) {
//            mDescTv.setVisibility(View.VISIBLE);
        } else {
//            mDescTv.setVisibility(View.GONE);
        }
    }

    private void refreshReceiptQRCode() {
        Receipt receipt = new Receipt();
        receipt.setUserId(mLoginUserId);
        receipt.setUserName(coreManager.getSelf().getNickName());
        receipt.setMoney(money);
        receipt.setDescription(description);

        String content = JSON.toJSONString(receipt);
        Bitmap mQRCodeBitmap = CommonUtils.createQRCode(content, DisplayUtil.dip2px(MyApplication.getContext(), 160),
                DisplayUtil.dip2px(MyApplication.getContext(), 160));
        mReceiptQrCodeIv.setImageBitmap(mQRCodeBitmap);
        AvatarHelper.getInstance().displayAvatar(coreManager.getSelf().getUserId(), findViewById(R.id.avatar), false);
    }

    /**
     * 获取这个view的缓存bitmap,
     */
    private Bitmap getBitmap(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap result = Bitmap.createBitmap(view.getDrawingCache());
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(false);
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK & requestCode == 888) {
            if (data == null || data.getExtras() == null) {
                return;
            }
            String result = data.getExtras().getString(Constant.EXTRA_RESULT_CONTENT);
            Log.e("zq", "二维码扫描结果：" + result);
            if (TextUtils.isEmpty(result)) {
                return;
            }
           /* if (result.length() == 20 && RegexUtils.checkDigit(result)) {
                // 长度为20且 && 纯数字 扫描他人的付款码 弹起收款界面
                Intent intent = new Intent(mContext, PaymentReceiptMoneyActivity.class);
                intent.putExtra("PAYMENT_ORDER", result);
                startActivity(intent);
            } else*/
            if (result.contains("userId")
                    && result.contains("userName")) {
                // 扫描他人的收款码 弹起付款界面
                Intent intent = new Intent(mContext, ReceiptPayMoneyActivity.class);
                intent.putExtra("RECEIPT_ORDER", result);
                startActivity(intent);
            } else {
                Reporter.post("二维码无法识别，<" + result + ">");
                ToastUtil.showToast(this, R.string.unrecognized);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
