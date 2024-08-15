package com.payeasenet.wepay.ui.view.dialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;


/**
 * 自定义进度条
 *
 * @Description: [自定义进度条]
 * @Author: [zhaoyong.chen@ehking.com]
 * @CreateDate: [2017/3/9 13:18]
 * @Version: [v1.0]
 */
public class LoadingDialog extends ProgressDialog {

    private String tip;
    private TextView mTvTip;

    public LoadingDialog(Context context) {

        super(context, com.ehking.sdk.wepay.R.style.loading_dialog);
    }

    public LoadingDialog(Context context, int theme) {

        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(com.ehking.sdk.wepay.R.layout.dialog_common_loading);
        mTvTip = (TextView) findViewById(android.R.id.message);

        setCancelable(false);
    }

    @Override
    public void setMessage(CharSequence message) {

        tip = message.toString();
        if (!TextUtils.isEmpty(tip)) {
            mTvTip.setText(tip);
        }
    }
}
