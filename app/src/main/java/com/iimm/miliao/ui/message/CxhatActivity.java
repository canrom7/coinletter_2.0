package com.iimm.miliao.ui.message;

import android.os.Bundle;
import android.util.Log;

import com.iimm.miliao.R;
import com.iimm.miliao.ui.base.BaseActivity;


/**
 * 单聊界面
 */
public class CxhatActivity extends BaseActivity {

    long lastTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lastTime = System.currentTimeMillis();
        setContentView(R.layout.chat);
        Log.e(TAG, "timexxx  oncreate: " + (System.currentTimeMillis() - lastTime));
    }
}
