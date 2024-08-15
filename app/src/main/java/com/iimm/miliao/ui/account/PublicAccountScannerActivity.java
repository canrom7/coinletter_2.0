package com.iimm.miliao.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

public class PublicAccountScannerActivity extends BaseActivity {
    public static final String TAG = "PublicAccountScanner";
    private Button mButton;
    private TextView mTvCancel;
    private String result = "";
    private TextView mTvHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_account_scanner);
        Intent intent = getIntent();
        if (null != intent) {
            result = intent.getStringExtra("result");
        }
        initView();
        initEvent();
    }

    private void initView() {
        getSupportActionBar().hide();
        mButton = findViewById(R.id.login);
        mTvCancel = findViewById(R.id.tv_cancel);
        mTvHint = findViewById(R.id.tv_hint);
        if (result.contains("pub&acc")) {
            mTvHint.setText(R.string.public_platform_login_confirmation);
        } else if (result.contains("user&login")) {
            mTvHint.setText(R.string.pc_client_login_confirmation);
        } else if (result.contains("pub&open&acc")) {
            mTvHint.setText(R.string.public_open_platform_login_confirmation);
        }
    }


    private void initEvent() {
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mButton.setEnabled(false);
                DialogHelper.showDefaulteMessageProgressDialog(PublicAccountScannerActivity.this);
                String url = coreManager.getConfig().apiUrl;
                if (result.contains("pub&acc")) {
                    url = coreManager.getConfig().LOGIN_PUBLIC_NUMBER;
                } else if (result.contains("user&login")) {
                    url = coreManager.getConfig().LOGIN_PC;
                } else if (result.contains("pub&open&acc")) {
                    url = coreManager.getConfig().LOGIN_PUBLIC_OPEN_NUMBER;
                }
                Map<String, String> par = new HashMap<>();
                par.put("access_token", coreManager.getSelfStatus().accessToken);
                par.put("qcCodeToken", result);
                HttpUtils.get().url(url)
                        .params(par)
                        .build()
                        .execute(new BaseCallback<String>(String.class) {
                            @Override
                            public void onResponse(ObjectResult<String> result) {
                                DialogHelper.dismissProgressDialog();
                                mButton.setEnabled(true);
                                finish();
                            }

                            @Override
                            public void onError(Call call, Exception e) {
                                e.printStackTrace();
                                DialogHelper.dismissProgressDialog();
                                mButton.setEnabled(true);
                                finish();
                            }
                        });
            }
        });
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
