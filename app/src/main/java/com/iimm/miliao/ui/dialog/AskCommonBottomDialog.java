package com.iimm.miliao.ui.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.view.BaseCommonBottomDialog;

public class AskCommonBottomDialog extends BaseCommonBottomDialog implements View.OnClickListener {
    private String content = "";
    private int contentId;
    private TextView mTvContent;
    private TextView mTvCancel;
    private TextView mTvOk;

    public AskCommonBottomDialog(Context context, String content, AskCommonBottomDialogListener listener) {
        super(context, R.style.BottomDialog);
        this.content = content;
        this.listener = listener;
    }

    public AskCommonBottomDialog(Context context, int content, AskCommonBottomDialogListener listener) {
        super(context, R.style.BottomDialog);
        this.contentId = content;
        this.listener = listener;
    }


    @Override
    protected void initView() {
        mTvContent = findViewById(R.id.tv_content);
        mTvCancel = findViewById(R.id.tv_cancel);
        mTvOk = findViewById(R.id.tv_ok);
        mTvContent.setOnClickListener(this);
        mTvCancel.setOnClickListener(this);
        mTvOk.setOnClickListener(this);
        if (TextUtils.isEmpty(content) && contentId != 0) {
            mTvContent.setText(contentId);
        } else if (!TextUtils.isEmpty(content) && contentId == 0) {
            mTvContent.setText(content);
        } else {
            mTvContent.setText(content);
        }

    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_ask_common_bottom;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_content:
                if (listener != null) {
                    listener.onClickContent(mTvContent, mTvContent.getText().toString().trim());
                }
                break;
            case R.id.tv_cancel:
                if (this.isShowing()) {
                    this.dismiss();
                }
                if (listener != null) {
                    listener.onClickCancel(mTvCancel);
                }
                break;
            case R.id.tv_ok:
                if (this.isShowing()) {
                    this.dismiss();
                }
                if (listener != null) {
                    listener.onClickOk(mTvOk);
                }
                break;
        }
    }

    private AskCommonBottomDialogListener listener;

    public interface AskCommonBottomDialogListener {
        void onClickOk(TextView tvOk);

        void onClickCancel(TextView tvCancel);

        void onClickContent(TextView tvContent, String trim);
    }

    public void setListener(AskCommonBottomDialogListener listener) {
        this.listener = listener;
    }
}
