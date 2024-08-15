package com.iimm.miliao.ui.me.redpacket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.BlanceInfo;
import com.iimm.miliao.bean.DynamicWithdrawalBean;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.UiUtils;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.Map;

import okhttp3.Call;

/**
 * MrLiu253@163.com
 * 添加银行卡或支付宝
 *
 * @time 2019-11-19
 */
public class AddWithdrawActivity extends BaseActivity {

    private int mType;
    private BlanceInfo.MethodBean mMethodBean;
    private EditText mCardholdersNameEt, mBankCardNumberEt, mBankNameEt, mBranchNameEt, mRemarksEt,
            mAlipayNameEt, mAlipayAccountEt, mDynamicEt1, mDynamicEt2, mDynamicEt3, mDynamicEt4, mDynamicEt5;
    private ImageView dynamicIv1, dynamicIv2, dynamicIv3, dynamicIv4, dynamicIv5;
    private TextView dynamicTv1, dynamicTv2, dynamicTv3, dynamicTv4, dynamicTv5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_withdraw);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        initView();
    }

    private void initView() {
        mType = getIntent().getIntExtra("type", 0);
        mMethodBean = (BlanceInfo.MethodBean) getIntent().getSerializableExtra("methodBean");
        LinearLayoutCompat bankCardCl = findViewById(R.id.bank_card_cl);
        LinearLayoutCompat alipayCl = findViewById(R.id.alipay_cl);
        LinearLayoutCompat dynamicCl = findViewById(R.id.dynamic_cl);
        TextView sTitle = findViewById(R.id.tv_title_center);

        mCardholdersNameEt = findViewById(R.id.cardholders_name_et);
        mBankCardNumberEt = findViewById(R.id.bank_card_number_et);
        mBankNameEt = findViewById(R.id.bank_name_et);
        mBranchNameEt = findViewById(R.id.branch_name_et);
        mRemarksEt = findViewById(R.id.remarks_et);
        mAlipayNameEt = findViewById(R.id.alipay_name_et);
        mAlipayAccountEt = findViewById(R.id.alipay_account_et);

        ConstraintLayout dynamicCl1 = findViewById(R.id.dynamic_cl1);
        dynamicIv1 = findViewById(R.id.dynamic_iv1);
        dynamicTv1 = findViewById(R.id.dynamic_tv1);
        mDynamicEt1 = findViewById(R.id.dynamic_et1);

        ConstraintLayout dynamicCl2 = findViewById(R.id.dynamic_cl2);
        dynamicIv2 = findViewById(R.id.dynamic_iv2);
        dynamicTv2 = findViewById(R.id.dynamic_tv2);
        mDynamicEt2 = findViewById(R.id.dynamic_et2);

        ConstraintLayout dynamicCl3 = findViewById(R.id.dynamic_cl3);
        dynamicIv3 = findViewById(R.id.dynamic_iv3);
        dynamicTv3 = findViewById(R.id.dynamic_tv3);
        mDynamicEt3 = findViewById(R.id.dynamic_et3);

        ConstraintLayout dynamicCl4 = findViewById(R.id.dynamic_cl4);
        dynamicIv4 = findViewById(R.id.dynamic_iv4);
        dynamicTv4 = findViewById(R.id.dynamic_tv4);
        mDynamicEt4 = findViewById(R.id.dynamic_et4);

        ConstraintLayout dynamicCl5 = findViewById(R.id.dynamic_cl5);
        dynamicIv5 = findViewById(R.id.dynamic_iv5);
        dynamicTv5 = findViewById(R.id.dynamic_tv5);
        mDynamicEt5 = findViewById(R.id.dynamic_et5);

        if (mType == 1) {
            setViewVisible(alipayCl);
            setViewGone(bankCardCl, dynamicCl);
        } else if (mType == 5) {
            setViewVisible(bankCardCl);
            setViewGone(alipayCl, dynamicCl);
        } else {
            setViewVisible(dynamicCl);
            setViewGone(bankCardCl, alipayCl);
            if (mMethodBean != null && mMethodBean.getWithdrawKeyDetails().size() > 0) {
                switch (mMethodBean.getWithdrawKeyDetails().size()) {
                    case 1:
                        setViewVisible(dynamicCl1);
                        setViewGone(dynamicCl2, dynamicCl3, dynamicCl4, dynamicCl5);
                        setViewStatus(mMethodBean.getWithdrawKeyDetails().get(0), dynamicIv1, dynamicTv1);
                        break;
                    case 2:
                        setViewVisible(dynamicCl1, dynamicCl2);
                        setViewGone(dynamicCl3, dynamicCl4, dynamicCl5);
                        setViewStatus(mMethodBean.getWithdrawKeyDetails().get(0), dynamicIv1, dynamicTv1);
                        setViewStatus(mMethodBean.getWithdrawKeyDetails().get(1), dynamicIv2, dynamicTv2);
                        break;
                    case 3:
                        setViewVisible(dynamicCl1, dynamicCl2, dynamicCl3);
                        setViewGone(dynamicCl4, dynamicCl5);
                        setViewStatus(mMethodBean.getWithdrawKeyDetails().get(0), dynamicIv1, dynamicTv1);
                        setViewStatus(mMethodBean.getWithdrawKeyDetails().get(1), dynamicIv2, dynamicTv2);
                        setViewStatus(mMethodBean.getWithdrawKeyDetails().get(2), dynamicIv3, dynamicTv3);
                        break;
                    case 4:
                        setViewVisible(dynamicCl1, dynamicCl2, dynamicCl3, dynamicCl4);
                        setViewGone(dynamicCl5);
                        setViewStatus(mMethodBean.getWithdrawKeyDetails().get(0), dynamicIv1, dynamicTv1);
                        setViewStatus(mMethodBean.getWithdrawKeyDetails().get(1), dynamicIv2, dynamicTv2);
                        setViewStatus(mMethodBean.getWithdrawKeyDetails().get(2), dynamicIv3, dynamicTv3);
                        setViewStatus(mMethodBean.getWithdrawKeyDetails().get(3), dynamicIv4, dynamicTv4);
                        break;
                    case 5:
                        setViewVisible(dynamicCl1, dynamicCl2, dynamicCl3, dynamicCl4, dynamicCl5);
                        setViewStatus(mMethodBean.getWithdrawKeyDetails().get(0), dynamicIv1, dynamicTv1);
                        setViewStatus(mMethodBean.getWithdrawKeyDetails().get(1), dynamicIv2, dynamicTv2);
                        setViewStatus(mMethodBean.getWithdrawKeyDetails().get(2), dynamicIv3, dynamicTv3);
                        setViewStatus(mMethodBean.getWithdrawKeyDetails().get(3), dynamicIv4, dynamicTv4);
                        setViewStatus(mMethodBean.getWithdrawKeyDetails().get(4), dynamicIv5, dynamicTv5);
                        break;
                    default:
                        break;
                }
            }
        }

        if (mMethodBean != null) {
            sTitle.setText(String.format("添加%s", mMethodBean.getWithdrawWayName()));
        }


        findViewById(R.id.binding_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UiUtils.isNormalClick(v)) {
                    return;
                }
                if (verification()) {
                    submit();
                }
            }
        });

    }

    public void setViewStatus(DynamicWithdrawalBean.WithdrawKeyDetailsBean data, View view, TextView textView) {
        if (data.getWithdrawStatus() == 1) {
            setViewVisible(view);
        }
        if (!TextUtils.isEmpty(data.getWithdrawName())) {
            textView.setText(data.getWithdrawName());
        }
    }

    public void setViewVisible(View... viewVisible) {
        for (View view : viewVisible) {
            view.setVisibility(View.VISIBLE);
        }
    }

    public void setViewGone(View... viewVisible) {
        for (View view : viewVisible) {
            view.setVisibility(View.GONE);
        }
    }

    private void submit() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> par = new ArrayMap<>();
        par.put("access_token", coreManager.getSelfStatus().accessToken);
        par.put("type", String.valueOf(mType));
        par.put("alipayName", mAlipayNameEt.getText().toString().trim());
        par.put("alipayNumber", mAlipayAccountEt.getText().toString().trim());

        par.put("bankUserName", mCardholdersNameEt.getText().toString().trim());
        par.put("bankCardNo", mBankCardNumberEt.getText().toString().trim());
        par.put("bankName", mBankNameEt.getText().toString().trim());
        par.put("subBankName", mBranchNameEt.getText().toString().trim());
        par.put("remarks", mRemarksEt.getText().toString().trim());

        par.put("otherNode1", mDynamicEt1.getText().toString().trim());
        par.put("otherNode2", mDynamicEt2.getText().toString().trim());
        par.put("otherNode3", mDynamicEt3.getText().toString().trim());
        par.put("otherNode4", mDynamicEt4.getText().toString().trim());
        par.put("otherNode5", mDynamicEt5.getText().toString().trim());
        HttpUtils.get().url(coreManager.getConfig().WITHDRAW_METHOD_SET).params(par).build().execute(new BaseCallback<Void>(Void.class) {
            @Override
            public void onResponse(ObjectResult<Void> result) {
                DialogHelper.dismissProgressDialog();
                Intent intent = new Intent();
                setResult(Activity.RESULT_OK, intent);
                finish();
                ToastUtil.showToast(AddWithdrawActivity.this, "添加成功");
            }

            @Override
            public void onError(Call call, Exception e) {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showErrorNet(AddWithdrawActivity.this);
            }
        });
    }

    private boolean verification() {
        if (mType == 1) {
            if (TextUtils.isEmpty(mAlipayNameEt.getText().toString().trim())) {
                ToastUtil.showLongToast(this, "请输入支付宝姓名");
                return false;
            } else if (TextUtils.isEmpty(mAlipayAccountEt.getText().toString().trim())) {
                ToastUtil.showLongToast(this, "请输入支付宝账号");
                return false;
            }
        } else if (mType == 5) {
            if (TextUtils.isEmpty(mCardholdersNameEt.getText().toString().trim())) {
                ToastUtil.showLongToast(this, "请输入持卡人姓名");
                return false;
            } else if (TextUtils.isEmpty(mBankCardNumberEt.getText().toString().trim())) {
                ToastUtil.showLongToast(this, "请输入银行卡号");
                return false;
            } else if (TextUtils.isEmpty(mBankNameEt.getText().toString().trim())) {
                ToastUtil.showLongToast(this, "请输入银行名称");
                return false;
            }
        } else {
            if (dynamicIv1.getVisibility() == View.VISIBLE && TextUtils.isEmpty(mDynamicEt1.getText().toString().trim())) {
                ToastUtil.showLongToast(this, "请输入" + dynamicTv1.getText().toString().trim());
                return false;
            } else if (dynamicIv2.getVisibility() == View.VISIBLE && TextUtils.isEmpty(mDynamicEt2.getText().toString().trim())) {
                ToastUtil.showLongToast(this, "请输入" + dynamicTv2.getText().toString().trim());
                return false;
            } else if (dynamicIv3.getVisibility() == View.VISIBLE && TextUtils.isEmpty(mDynamicEt3.getText().toString().trim())) {
                ToastUtil.showLongToast(this, "请输入" + dynamicTv3.getText().toString().trim());
                return false;
            } else if (dynamicIv4.getVisibility() == View.VISIBLE && TextUtils.isEmpty(mDynamicEt4.getText().toString().trim())) {
                ToastUtil.showLongToast(this, "请输入" + dynamicTv4.getText().toString().trim());
                return false;
            } else if (dynamicIv5.getVisibility() == View.VISIBLE && TextUtils.isEmpty(mDynamicEt5.getText().toString().trim())) {
                ToastUtil.showLongToast(this, "请输入" + dynamicTv5.getText().toString().trim());
                return false;
            }
        }
        return true;
    }
}
