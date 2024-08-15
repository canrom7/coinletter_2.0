package com.iimm.miliao.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.helper.LoginHelper;
import com.iimm.miliao.sp.UserSp;
import com.iimm.miliao.ui.account.LoginActivity;
import com.iimm.miliao.ui.account.LoginHistoryActivity;
import com.iimm.miliao.ui.base.ActionBackActivity;
import com.iimm.miliao.ui.base.ActivityStack;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.ui.lock.DeviceLockHelper;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.Md5Util;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.log.LogUtils;
import com.iimm.miliao.view.window.MainWindowShowService;
import com.iimm.miliao.xmpp.XmppConnectionImpl;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;

import okhttp3.Call;

/**
 * 进入到此界面的Activity只可能是4中用户状态 STATUS_USER_TOKEN_OVERDUE
 * 本地Token过期 STATUS_USER_NO_UPDATE
 * 数据不完整
 */
// TODO: 统一继承BaseActivity, 要注意这里不需要登录，
public class UserCheckedActivity extends ActionBackActivity {
    private static final String TAG = "UserCheckedActivity";
    private TextView mTitleTv;
    private TextView mDesTv;
    private Button mLeftBtn;
    private Button mRightBtn;

    public static void start(Context ctx) {
        Log.d(TAG, "start() called with: ctx = [" + ctx + "]");
        Log.w(TAG, "start: 需要重新登录，", new Exception("需要重新登录，"));
        Intent intent = new Intent(ctx, UserCheckedActivity.class);
        // 清空activity栈，
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_checked);
        // Api 11之后，点击外部会使得Activity结束，禁止外部点击结束
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setFinishOnTouchOutside(false);
        }
        initView();
    }

    private void initView() {
        mTitleTv = (TextView) findViewById(R.id.title_tv);
        mDesTv = (TextView) findViewById(R.id.des_tv);
        mLeftBtn = (Button) findViewById(R.id.left_btn);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        // init status

        // 能进入此Activity的只允许三种用户状态
        int status = MyApplication.getInstance().mUserStatus;
        LogUtils.e("mUserStatus", "status:" + status);
        if (status == LoginHelper.STATUS_USER_TOKEN_OVERDUE) {
            mTitleTv.setText(R.string.overdue_title);
            mDesTv.setText(R.string.token_overdue_des);
        } else if (status == LoginHelper.STATUS_USER_NO_UPDATE) {
            mTitleTv.setText(R.string.overdue_title);
            mDesTv.setText(R.string.deficiency_data_des);
        } else if (status == LoginHelper.STATUS_USER_TOKEN_CHANGE) {
            mTitleTv.setText(R.string.logout_title);
            mDesTv.setText(R.string.logout_des);
        } else {// 其他的状态，一般不会出现，为了容错，加个判断
            loginAgain();
            return;
        }

        mLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceUtils.putBoolean(UserCheckedActivity.this, Constants.LOGIN_CONFLICT, true);
                ActivityStack.getInstance().exit();
            }
        });

        mRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginAgain();
            }
        });
        outLogin();
    }

    private void outLogin() {
//        logout();
        // 退出时清除设备锁密码，
        if (Constants.SUPPORT_FLOATING_WINDOW){
            stopService(new Intent(UserCheckedActivity.this, MainWindowShowService.class));
            ActivityStack.getInstance().removeAlertWindowActivity();
        }

        DeviceLockHelper.clearPassword();
        UserSp.getInstance(mContext).clearUserInfo();
        MyApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_SIMPLE_TELPHONE;
        XmppConnectionImpl.getInstance().logoutXmpp();
        LoginHelper.broadcastLogout(mContext);
    }

    private void logout() {
        HashMap<String, String> params = new HashMap<String, String>();
        // 得到电话
        String phoneNumber = CoreManager.getSelf(MyApplication.getContext()).getTelephone();
        // 去掉区号,
        String mobilePrefix = String.valueOf(PreferenceUtils.getInt(MyApplication.getContext(), Constants.AREA_CODE_KEY, 86));
        String phoneNumberRel;
        if (phoneNumber.startsWith(mobilePrefix)) {
            phoneNumberRel = phoneNumber.substring(mobilePrefix.length());
        } else {
            phoneNumberRel = phoneNumber;
        }
        String digestTelephone = Md5Util.toMD5(phoneNumberRel);
        params.put("telephone", digestTelephone);
        params.put("access_token", CoreManager.getSelfStatus(MyApplication.getContext()).accessToken);
        // 默认为86
        params.put("areaCode", String.valueOf(86));
        params.put("deviceKey", "android");

        HttpUtils.get().url(MyApplication.mCoreManager.getConfig().USER_LOGOUT)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    private void loginAgain() {
        boolean idIsEmpty = TextUtils.isEmpty(UserSp.getInstance(this).getUserId(""));
        boolean telephoneIsEmpty = TextUtils.isEmpty(UserSp.getInstance(this).getTelephone(null));
        if (!idIsEmpty && !telephoneIsEmpty) {
            startActivity(new Intent(this, LoginHistoryActivity.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        ActivityStack.getInstance().exit();
        finish();
        this.sendBroadcast(new Intent(com.iimm.miliao.broadcast.OtherBroadcast.FINISH_MAIN));
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        loginAgain();
    }
}
