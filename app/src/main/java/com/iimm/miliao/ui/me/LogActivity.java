package com.iimm.miliao.ui.me;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.AppExecutors;
import com.iimm.miliao.util.UiUtils;
import com.iimm.miliao.util.log.FileController;

/**
 * 设置
 */
public class LogActivity extends BaseActivity implements View.OnClickListener {
    private String mLoginUserId;
    public static String LOG_TEXT_ACTION = "com.action.log";
    public static String LOG_TEXT_EXTRA = "log_extra";

    private TextView tv_title_right;
    public static int count;

    //监听广播，实时添加textview
    private class LogReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                if (action.equals(LOG_TEXT_ACTION)) {
                    String text = intent.getStringExtra(LOG_TEXT_EXTRA);
                    logTextToScroll(text);
                }
            }
        }
    }

    private ScrollView scrollView;
    private LinearLayout linearLayout;

    private LogReceiver logReceiver;

    //将带有log的textview 添加进布局中
    private void logTextToScroll(final String s) {
        linearLayout.post(new Runnable() {
            @Override
            public void run() {
                if (s == null || s.isEmpty()) {
                    return;
                }
                count++;
                TextView textView = new TextView(LogActivity.this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(10, 10, 10, 10);//4个参数按顺序分别是左上右下
                textView.setLayoutParams(layoutParams);
                String str = s + "\n";
                textView.setText(count + "__" + str);
                linearLayout.addView(textView);
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    //清除界面的log，可以在界面添加button 或者菜单，通过点击实现清除log
    private void clearLog() {
//        for (int i = 0; i < linearLayout.getChildCount(); i++) {
//            TextView t = (TextView) linearLayout.getChildAt(i);
//            t.setText("");
//        }
        linearLayout.removeAllViews();
        FileController.getFileControl().deleteLogFile();
        count = 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText("日志");
        tv_title_right = (TextView) findViewById(R.id.tv_title_right);
        tv_title_right.setText("清空");
        tv_title_right.setVisibility(View.VISIBLE);
        tv_title_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearLog();
            }
        });
        mLoginUserId = coreManager.getSelf().getUserId();
        initView();

        IntentFilter intentFilter = new IntentFilter();
        logReceiver = new LogReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(LOG_TEXT_ACTION);
        registerReceiver(logReceiver, intentFilter);
    }


    private void initView() {
        scrollView = findViewById(R.id.scrollview);
        linearLayout = findViewById(R.id.linear_layout);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                String content = FileController.getFileControl().readFromLogFile();
                logTextToScroll(content);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        count = 0;
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (!UiUtils.isNormalClick(v)) {
            return;
        }
        switch (v.getId()) {
            case R.id.about_us_rl:
                // 关于我们
                startActivity(new Intent(mContext, AboutActivity.class));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (logReceiver != null) {
            unregisterReceiver(logReceiver);
        }
    }
}
