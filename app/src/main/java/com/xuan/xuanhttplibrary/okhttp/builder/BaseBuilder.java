package com.xuan.xuanhttplibrary.okhttp.builder;


import android.text.TextUtils;
import android.util.Log;

import com.iimm.miliao.AppConfig;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.bean.UserStatus;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.Md5Util;
import com.iimm.miliao.util.TimeUtils;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;

import java.util.Locale;
import java.util.Map;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * @author Administrator
 * @time 2017/3/30 0:14
 * @des ${TODO}
 */

public abstract class BaseBuilder {

    protected String url;
    protected Object tag;
    protected Map<String, String> headers;
    protected Map<String, String> params;
    protected Request build;

    public abstract BaseBuilder url(String url);

    public abstract BaseBuilder tag(Object tag);

    public abstract BaseCall build();

    public abstract BaseBuilder params(String k, String v);

    /**
     * @return 返回true表示accessToken正常，
     */
    private boolean checkAccessToken(UserStatus status) {
        String mAccessToken;
        // 如果没有accessToken就不添加time和secret,
        if (status == null) {
            return false;
        } else {
            mAccessToken = status.accessToken;
            if (TextUtils.isEmpty(mAccessToken)) {
                return false;
            }
        }
        return true;
    }


    /**
     * 给所有接口调添加secret,
     */
    public BaseBuilder addSecret() {
        if (url.contains("config")// 获取配置的接口，不验证
                || url.contains("getCurrentTime") // 获取服务器时间接口，不验证，
        ) {
            return this;
        }
        // 上面两个接口调用时可能还没有获取到config,
        AppConfig mConfig = CoreManager.requireConfig(MyApplication.getInstance());
        if (url.equals(mConfig.SDK_OPEN_AUTH_INTERFACE) || url.equals(mConfig.QUESTION_LIST) || url.equals(mConfig.QUESTION_CHECK)) {
            return this;
        }

        // 所有接口都需要time与secret参数
        String time = String.valueOf(TimeUtils.time_current_time());
        String secret;
        UserStatus status = CoreManager.getSelfStatus(MyApplication.getContext());

        if (url.equals(mConfig.GET_MY_BANK_CARD_LIST)
                || url.equals(mConfig.ADD_BANK_CARD) || url.equals(mConfig.DEL_MY_BANK_CARD)) {
            //我的银行卡 列表   收款银行卡列表  ，下单
            if (!checkAccessToken(status)) {
                return this;
            }
            //md5(md5(apikey+time)+userid+token)
            String mLoginUserId = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();
            secret = Md5Util.toMD5(Md5Util.toMD5(AppConfig.apiKey + time) + mLoginUserId + status.accessToken);
        } else if (url.equals(mConfig.VX_RECHARGE)
                || url.equals(mConfig.REDPACKET_OPEN)
                || url.equals(mConfig.TRANSFER_RECEIVE_TRANSFER)) {
            // 微信支付 领红包 领取转账 调用的接口
            if (!checkAccessToken(status)) {
                return this;
            }
            String mLoginUserId = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();
            String step1 = Md5Util.toMD5(AppConfig.apiKey + time);
            secret = Md5Util.toMD5(step1 + mLoginUserId + status.accessToken);
        } else if (url.equals(mConfig.USER_LOGIN)
                || url.equals(mConfig.USER_REGISTER)
                || url.equals(mConfig.USER_PASSWORD_RESET)
                || url.equals(mConfig.VERIFY_TELEPHONE)
                || url.equals(mConfig.USER_GETCODE_IMAGE)
                // 未登录之前 && 微信登录相关 调用的接口
                || url.equals(mConfig.VX_GET_OPEN_ID) || url.endsWith(mConfig.USER_THIRD_LOGIN_NEW) || url.equals(mConfig.SEND_AUTH_CODE) || url.equals(mConfig.MEDIA_STARTUP)) {
            secret = Md5Util.toMD5(AppConfig.apiKey + time);
        } else {
            // 其他接口
            if (!checkAccessToken(status)) {
                return this;
            }
            String mLoginUserId = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();
            secret = Md5Util.toMD5(AppConfig.apiKey + time + mLoginUserId + status.accessToken);
        }
//        if (url.equals(mConfig.GET_BANK_LIST) || url.equals(mConfig.SUBMIT_BANK_INFO_PAY)) {
//            return this;
//        }

        params("time", time);
        params("secret", secret);

        return this;
    }

    /**
     * 给需要支付密码的接口调添加secret,
     *
     * @param payPassword 支付密码
     */
    public BaseBuilder addSecret(String payPassword, String money) {
        AppConfig mConfig = CoreManager.requireConfig(MyApplication.getInstance());

        // 所有接口都需要time与secret参数
        String time = String.valueOf(TimeUtils.time_current_time());
        String secret;
        String mAccessToken = CoreManager.requireSelfStatus(MyApplication.getInstance()).accessToken;
        if (url.equals(mConfig.REDPACKET_SEND)
                || url.equals(mConfig.TRANSFER_SEND_TRANSFER)) {
            // 发红包 || 转账 调用的接口
            String mLoginUserId = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();
            String step1 = Md5Util.toMD5(AppConfig.apiKey + time + money);
            String step2 = Md5Util.toMD5(payPassword);
            secret = Md5Util.toMD5(step1 + mLoginUserId + mAccessToken + step2);
            Log.d(HttpUtils.TAG, String.format(Locale.CHINA, "addSecret: md5(md5(%s+%s+%s)+%s+%s+md5(%s)) = %s", AppConfig.apiKey, time, money, mLoginUserId, mAccessToken, payPassword, secret));
        } else {
            // 不走这里，
            Reporter.unreachable();
            String mLoginUserId = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();
            secret = Md5Util.toMD5(AppConfig.apiKey + time + mLoginUserId + mAccessToken);
        }
        /*
        提现接口的secret计算在外面，
         */
        params("time", time);
        params("secret", secret);
        return this;
    }

    /**
     * 给收付款接口进行加密,
     *
     * @param payStr
     */
    public BaseBuilder addSecret2(String payStr, String money) {
        AppConfig mConfig = CoreManager.requireConfig(MyApplication.getInstance());

        // 所有接口都需要time与secret参数
        String time = String.valueOf(TimeUtils.time_current_time());
        String secret;
        String mAccessToken = CoreManager.requireSelfStatus(MyApplication.getInstance()).accessToken;
        if (url.equals(mConfig.PAY_CODE_PAYMENT)) {//付款码支付
            String mLoginUserId = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();
            String step1 = Md5Util.toMD5(AppConfig.apiKey + time + money + payStr);
            secret = Md5Util.toMD5(step1 + mLoginUserId + mAccessToken);
        } else if (url.equals(mConfig.PAY_CODE_RECEIPT)) {//二维码收款
            String mLoginUserId = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();
            String step1 = Md5Util.toMD5(AppConfig.apiKey + time + money + Md5Util.toMD5(payStr));
            secret = Md5Util.toMD5(step1 + mLoginUserId + mAccessToken);
        } else if (url.equals(mConfig.PAY_PASSWORD_PAYMENT)) {// 对外支付
            String mLoginUserId = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();
            String step1 = Md5Util.toMD5(AppConfig.apiKey + time + Md5Util.toMD5(payStr));
            secret = Md5Util.toMD5(mLoginUserId + mAccessToken + step1);
        } else {
            // 不走这里，
            Reporter.unreachable();
            String mLoginUserId = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();
            secret = Md5Util.toMD5(AppConfig.apiKey + time + mLoginUserId + mAccessToken);
        }
        /*
        提现接口的secret计算在外面，
         */
        params("time", time);
        params("secret", secret);
        return this;
    }

    public class BaseCall {
        public void execute(Callback callback) {
            OkHttpClient mOkHttpClient = HttpUtils.getInstance().getOkHttpClient();
            mOkHttpClient.newCall(build).enqueue(callback);
        }
    }
}
