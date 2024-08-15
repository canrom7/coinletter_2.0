package com.iimm.miliao.ui.me.redpacket;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.ui.base.BaseActivity;

public class WithDrawSuccessActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdrawal_success);
        initView();
    }

    private void initView() {
        initActionBar();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitleCenter = findViewById(R.id.tv_title_center);
        tvTitleCenter.setText(R.string.withdraw);
    }

}
