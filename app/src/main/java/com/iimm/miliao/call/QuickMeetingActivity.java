package com.iimm.miliao.call;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import com.iimm.miliao.R;
import com.iimm.miliao.helper.LoginHelper;
import com.iimm.miliao.ui.SplashActivity;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.tool.WebViewActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.LogUtils;
import com.iimm.miliao.util.PreferenceUtils;

import java.util.Map;

public class QuickMeetingActivity extends BaseActivity {

    private boolean isNeedExecuteLogin;
    private String room;
    private boolean isVideo;

    public QuickMeetingActivity() {
        noLoginRequired();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 自动解锁屏幕 | 锁屏也可显示 | Activity启动时点亮屏幕 | 保持屏幕常亮
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_quick_meeting);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        // 判断本地登录状态
        int userStatus = LoginHelper.prepareUser(mContext, coreManager);
        switch (userStatus) {
            case LoginHelper.STATUS_USER_FULL:
            case LoginHelper.STATUS_USER_NO_UPDATE:
            case LoginHelper.STATUS_USER_TOKEN_OVERDUE:
                boolean isConflict = PreferenceUtils.getBoolean(this, Constants.LOGIN_CONFLICT, false);
                if (isConflict) {
                    isNeedExecuteLogin = true;
                }
                break;
            case LoginHelper.STATUS_USER_SIMPLE_TELPHONE:
                isNeedExecuteLogin = true;
                break;
            case LoginHelper.STATUS_NO_USER:
            default:
                isNeedExecuteLogin = true;
        }

        if (isNeedExecuteLogin) {// 需要先执行登录操作
            login();
            return;
        }

        parseParam();
        dial();
    }

    private void parseParam() {
        Intent intent = getIntent();
        LogUtils.log(intent);

        Uri data = intent.getData();
        if (data == null) {
            Log.e(TAG, "data异常");
            login();
            return;
        }
        Map<String, String> map = WebViewActivity.URLRequest(data.toString());
        room = map.get("room");
        if (TextUtils.isEmpty(room)) {
            login();
            return;
        }
        isVideo = TextUtils.equals(map.get("type"), "video");
    }

    private void dial() {
        Jitsi_connecting_second.start(this, room, isVideo);
        finish();
    }

    private void login() {
        startActivity(new Intent(mContext, SplashActivity.class));
        finish();
    }
}
