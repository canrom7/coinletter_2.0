package com.iimm.miliao.ui.notification;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.helper.LoginHelper;
import com.iimm.miliao.ui.MainActivity;
import com.iimm.miliao.ui.SplashActivity;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.message.ChatActivity;
import com.iimm.miliao.ui.message.MucChatActivity;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.LogUtils;
import com.iimm.miliao.util.PreferenceUtils;

import java.util.Map;

/**
 * 通知的点击事件统一跳到这个页面处理，
 */
public class NotificationProxyActivity extends BaseActivity {
    private boolean isNeedExecuteLogin;

    public NotificationProxyActivity() {
        noLoginRequired();
    }

    public static void start(Context ctx, Map<String, String> data) {
        Intent intent = new Intent(ctx, NotificationProxyActivity.class);
        Bundle bundle = new Bundle();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }
        intent.putExtras(bundle);
        ctx.startActivity(intent);
    }

    public static boolean processIntent(Intent intent) {
        if (intent == null) {
            return false;
        }
        return !TextUtils.isEmpty(intent.getStringExtra("userId"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_proxy);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Intent intent = getIntent();
        LogUtils.log(TAG, intent);

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
            startActivity(new Intent(mContext, SplashActivity.class));
            finish();
            return;
        }

        MainActivity.start(this);

        if (intent.getData() != null) {
            try {
                Uri data = intent.getData();
                for (String key : data.getQueryParameterNames()) {
                    String value = data.getQueryParameter(key);
                    // 参数统一存在intent.extras里，有的推送不支持，所以要提前处理一下，
                    intent.putExtra(key, value);
                }
            } catch (Exception e) {
                Reporter.post("通知点击intent.data解析失败", e);
            }
        }

        String userId = intent.getStringExtra("userId");
        String url = intent.getStringExtra("url");
        Log.i(TAG, "args: " + "userId=" + userId + ", url=" + url);

        AsyncUtils.doAsync(this, t -> {
            Reporter.post("解析通知点击参数失败， intent=" + intent.toUri(Intent.URI_INTENT_SCHEME));
            runOnUiThread(this::finish);
        }, c -> {
            Friend friend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), userId);
            c.uiThread(r -> {
                if (friend == null) {
                    Reporter.post("朋友不存在， userId=" + userId);
                } else if (friend.getRoomFlag() == 1) {
                    MucChatActivity.start(r, friend);
                } else {
                    ChatActivity.start(r, friend);
                }
                r.finish();
            });
        });

    }
}
