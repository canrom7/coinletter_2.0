package com.iimm.miliao.helper;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.iimm.miliao.R;
import com.iimm.miliao.ui.dialog.base.BaseDialog;
import com.iimm.miliao.util.ToastUtil;

public class AddBankCardDialog extends BaseDialog {
    private ImageView mCloseView;
    private EditText mEtAccountName;
    private EditText mEtBankCard;
    // private EditText mEtBankName;
    private Button mSureBtn;
    private String mAccountName;
    private String mBankCard;
//    private String mBankName;

    {
        RID = R.layout.dialog_add_bank_card;
    }

    public AddBankCardDialog() {

    }

    public AddBankCardDialog(Activity context, AddBankCardSureListener listener) {
        this.mActivity = context;
        initView(context, listener);
    }

    private void initView(Context context, AddBankCardSureListener listener) {
        super.initView();
        mCloseView = mView.findViewById(R.id.close);
        mEtAccountName = mView.findViewById(R.id.et_account_name);
        mEtBankCard = mView.findViewById(R.id.et_bank_card);
        // mEtBankName = mView.findViewById(R.id.et_bank_name);
        mSureBtn = mView.findViewById(R.id.sure_btn);
        event(context, listener);
    }

    private void event(Context context, AddBankCardSureListener listener) {
        mCloseView.setOnClickListener(v -> AddBankCardDialog.this.mDialog.dismiss());
        mSureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInfo(context)) {
                    if (listener != null) {
                        listener.onClick(v, mAccountName, mBankCard);
                    }
                }
            }
        });
    }

    private boolean checkInfo(Context context) {
        mAccountName = mEtAccountName.getText().toString().trim();
        mBankCard = mEtBankCard.getText().toString().trim();
        // mBankName = mEtBankName.getText().toString().trim();
        if (TextUtils.isEmpty(mAccountName) || TextUtils.isEmpty(mBankCard)) {
            ToastUtil.showToast(context, R.string.please_fill_in_the_complete_information);
            return false;
        }
        return true;
    }


    public interface AddBankCardSureListener {

        void onClick(View v, String accountName, String bankCard);
    }

}
