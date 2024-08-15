package com.iimm.miliao.ui.me.redpacket;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.jungly.gridpasswordview.GridPasswordView;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.ScreenUtil;

import java.math.BigDecimal;

public class PayPasswordVerifyDialog extends Dialog {
    private TextView tvAction;
    private TextView tvMoney;
    private GridPasswordView gpvPassword;
    private String action;
    private String money;
    private String type;
    private OnInputFinishListener onInputFinishListener;
    private ConstraintLayout mHandlingFeeCl, mRateCl;
    private TextView mHandlingFeeTv, mRateTv;

    public PayPasswordVerifyDialog(@NonNull Context context, String type) {
        super(context, R.style.MyDialog);
        this.type = type;
        this.money = type;
    }

    public PayPasswordVerifyDialog(@NonNull Context context) {
        super(context, R.style.MyDialog);
    }

    public PayPasswordVerifyDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected PayPasswordVerifyDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_password_verify_dialog);
        setCanceledOnTouchOutside(false);
        initView();
    }

    private void initView() {
        mHandlingFeeCl = findViewById(R.id.handling_fee_cl);
        mHandlingFeeTv = findViewById(R.id.handling_fee_tv);
        mRateCl = findViewById(R.id.rate_cl);
        mRateTv = findViewById(R.id.rate_tv);

        tvAction = findViewById(R.id.tvAction);
        if (action != null) {
            tvAction.setText(action);
        }
        tvMoney = findViewById(R.id.tvMoney);
        if (money != null) {
            tvMoney.setText(money);
        }

        if (!TextUtils.isEmpty(type)) {
            mHandlingFeeCl.setVisibility(View.VISIBLE);
            mRateCl.setVisibility(View.VISIBLE);
            double fee = (Double.valueOf(CoreManager.requireConfig(MyApplication.getContext()).transferRate) / 100) * Double.valueOf(money);
            if (fee == 0) {
                fee = 0.00;
            } else if (0.01 > fee) {
                fee = 0.01;
            } else {
                BigDecimal b = new BigDecimal(String.valueOf(fee));
                b = b.divide(BigDecimal.ONE, 2, BigDecimal.ROUND_CEILING);
                fee = b.doubleValue();
            }
            mHandlingFeeTv.setText(String.format("%s", fee));
            mRateTv.setText(String.format("%s%%", CoreManager.requireConfig(MyApplication.getContext()).transferRate));
        }
        findViewById(R.id.close_iv).setOnClickListener(v -> {
            dismiss();
        });
        gpvPassword = findViewById(R.id.gpvPassword);
        gpvPassword.setOnPasswordChangedListener(new GridPasswordView.OnPasswordChangedListener() {
            @Override
            public void onTextChanged(String psw) {

            }

            @Override
            public void onInputFinish(String psw) {
                dismiss();
                if (onInputFinishListener != null) {
                    onInputFinishListener.onInputFinish(psw);
                }
            }
        });
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = (int) (ScreenUtil.getScreenWidth(getContext()) * 0.89);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public void setAction(String action) {
        this.action = action;
        if (tvAction != null) {
            tvAction.setText(action);
        }
    }

    public void setMoney(String money) {
        this.money = money;
        if (tvMoney != null) {
            tvMoney.setText(money);
        }
    }

    public void setOnInputFinishListener(OnInputFinishListener onInputFinishListener) {
        this.onInputFinishListener = onInputFinishListener;
    }

    public interface OnInputFinishListener {
        void onInputFinish(String password);
    }
}
