package com.iimm.miliao.ui.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.ui.dialog.base.BaseDialog;

public class CommonDialog extends BaseDialog {
    private View.OnClickListener mOnClickListener;
    private AutoCompleteTextView mContentET;
    private Button mCommitBtn;
    private TextView mTitleTv;
    private LinearLayout mLlBg;

    {
        RID = R.layout.dialog_single_input_name;
    }


    public CommonDialog(Activity activity, String title, String hint, View.OnClickListener onClickListener) {
        mActivity = activity;
        initView();
        setView(title, hint);
        this.mOnClickListener = onClickListener;
    }

    private void setView(String title, String hint) {
        mTitleTv.setText(title);
        mLlBg.setBackground(null);
        mContentET.setTextSize(18);
        mContentET.setMaxLines(5);
        mContentET.setSingleLine(false);
        mContentET.setText(hint);
        mContentET.setTextColor(mActivity.getResources().getColor(R.color.Grey_3A4));
        mContentET.setEnabled(false);
        mContentET.setBackground(null);
        mContentET.setPadding(0, 0, 0, 0);
        mCommitBtn.setOnClickListener(v -> {
            mDialog.dismiss();
            if (mOnClickListener != null)
                mOnClickListener.onClick(mContentET);
        });
    }


    protected void initView() {
        super.initView();
        mTitleTv = (TextView) mView.findViewById(R.id.title);
        mTitleTv.setText(InternationalizationHelper.getString("JXNewRoomVC_CreatRoom"));
        mView.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mView.findViewById(R.id.colse_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mContentET = (AutoCompleteTextView) mView.findViewById(R.id.content);
        mCommitBtn = (Button) mView.findViewById(R.id.sure_btn);
        mLlBg = mView.findViewById(R.id.ll_start_live);
    }
}
