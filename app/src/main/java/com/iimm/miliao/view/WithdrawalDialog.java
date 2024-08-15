package com.iimm.miliao.view;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.iimm.miliao.R;

public class WithdrawalDialog extends BaseCommonBottomDialog implements View.OnClickListener {

    private Button mCancelBt;
    private Button mDetermineBt;


    public WithdrawalDialog(Context context, WithdrawalDialogListener listener) {
        super(context, R.style.BottomDialog);
        this.listener = listener;
    }

    public WithdrawalDialog(Context context) {
        super(context, R.style.BottomDialog);
    }

    @Override
    protected void initView() {
        mCancelBt = findViewById(R.id.cancel_bt);
        mDetermineBt = findViewById(R.id.determine_bt);
        mCancelBt.setOnClickListener(this);
        mDetermineBt.setOnClickListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.commom_dialog_withdrawal;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_bt:
                if (this.isShowing()) {
                    this.dismiss();
                }
                break;
            case R.id.determine_bt:
                if (this.isShowing()) {
                    this.dismiss();
                }
                if (listener != null) {
                    listener.onClickDetermineWithdrawal();
                }
                break;
           default:
               break;
        }

    }

    WithdrawalDialogListener listener;

    public interface WithdrawalDialogListener {
        void onClickDetermineWithdrawal();

    }


}
