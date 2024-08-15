package com.iimm.miliao.ui.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.iimm.miliao.AppConfig;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.bean.ConfigBean;
import com.iimm.miliao.bean.User;
import com.iimm.miliao.bean.UserStatus;
import com.iimm.miliao.bean.redpacket.Balance;
import com.iimm.miliao.db.dao.UserDao;
import com.iimm.miliao.helper.LoginHelper;
import com.iimm.miliao.sp.UserSp;
import com.iimm.miliao.ui.UserCheckedActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.OBSUtils;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.log.LogUtils;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CoreManager {
    public static final String KEY_CONFIG_BEAN = "configBean";
    private static final String TAG = "CoreManager";
    private static AppConfig staticConfig = null;
    private static User staticSelf = null;
    private static UserStatus staticSelfStatus = null;
    private static Context mContext;
    private boolean loginRequired;
    private boolean configRequired;
    private User self = null;
    private UserStatus selfStatus = null;
    private Limit limit = new Limit(this);
    private AppConfig config = null;
    private BaseLoginActivity ctx;
    private CoreStatusListener coreStatusListener;
    // 绑定Service成功的回调，
    // TODO: 要改成登录成功才回调，
    // 当前绑定服务的连接，

    CoreManager(BaseLoginActivity ctx, CoreStatusListener coreStatusListener) {
        this.ctx = ctx;
        this.coreStatusListener = coreStatusListener;
    }

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return ctx.getSharedPreferences("core_manager", Context.MODE_PRIVATE);
    }

    @SuppressWarnings("WeakerAccess")
    public static AppConfig requireConfig(Context ctx) {
        mContext = ctx;
        if (staticConfig == null) {
            synchronized (CoreManager.class) {
                if (staticConfig == null) {
                    ConfigBean configBean = readConfigBean(ctx);
                    if (configBean == null) {
                        configBean = getDefaultConfig(ctx);
                    }
                    setStaticConfig(ctx, AppConfig.initConfig(configBean));
                }
            }
        }
        return staticConfig;
    }

    private static ConfigBean readConfigBean(Context ctx) {
        String configBeanJson = getSharedPreferences(ctx)
                .getString(KEY_CONFIG_BEAN, null);
        if (TextUtils.isEmpty(configBeanJson)) {
            return null;
        }
        return JSON.parseObject(configBeanJson, ConfigBean.class);
    }


    // 刷新用户余额
    public static void updateMyBalance() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", staticSelfStatus.accessToken);
        HttpUtils.get().url(CoreManager.requireConfig(MyApplication.getInstance()).RECHARGE_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<Balance>(Balance.class) {

                    @Override
                    public void onResponse(ObjectResult<Balance> result) {
                        Balance b = result.getData();
                        if (b != null) {
                            staticSelf.setBalance(b.getBalance());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    public static void appBackstage(Context context, boolean isBack) {
        if (staticSelf == null || staticSelfStatus == null) {
            return;
        }
        String str;
        User self = staticSelf;
        if (isBack) {
            str = "程序已到后台";
            Log.e(TAG, str + "，开始--》将离线时间本存至本地");
            long time = System.currentTimeMillis() / 1000;
            CoreManager.saveOfflineTime(context, self.getUserId(), time);
            UserDao.getInstance().updateUnLineTime(self.getUserId(), time);
            Log.e(TAG, str + "，结束--》将离线时间本存至本地");
        } else {
            str = "程序已到前台";
        }

        Log.e(TAG, str + "，开始--》调用outTime接口");
        Map<String, String> params = new HashMap<>();
        params.put("access_token", staticSelfStatus.accessToken);
        params.put("userId", self.getUserId());
        HttpUtils.get().url(CoreManager.requireConfig(MyApplication.getInstance()).USER_OUTTIME).params(params).build().execute(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
//        StringJsonObjectRequest<User> request = new StringJsonObjectRequest<>(CoreManager.requireConfig(MyApplication.getInstance()).USER_OUTTIME,
//                null, null, null, params);
//
//        MyApplication.getInstance().getFastVolley().addShortRequest(TAG + "@", request);
    }

    public static void saveOfflineTime(Context context, String userId, long outTime) {
        if (Constants.OFFLINE_TIME_IS_FROM_SERVICE) {
            Log.e(TAG, "服务端获取到的离线时间--》" + outTime);
        } else {
            Log.e(TAG, "本地生成的离线时间--》" + outTime);
        }
        PreferenceUtils.putLong(context, Constants.OFFLINE_TIME + userId, outTime);
        if (staticSelf != null) {
            staticSelf.setOfflineTime(outTime);
        }
    }

    @SuppressWarnings("WeakerAccess")
    @NonNull
    public static User requireSelf(Context ctx) {
        if (staticSelf == null) {
            synchronized (CoreManager.class) {
                if (staticSelf == null) {
                    String userId = UserSp.getInstance(ctx).getUserId("");
                    User user = UserDao.getInstance().getUserByUserId(userId);
                    if (user == null) {
                        Reporter.post("登录的User为空，");
                        LogUtils.e("mUserStatus","user is null");
                        // 无论如何没有登录信息就跳到重新登录，
                        MyApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_TOKEN_CHANGE;
                        // 弹出对话框
                        UserCheckedActivity.start(ctx);
                        // 就算没有登录也不能返回null, 直接把本地过期的返回以便正常初始化，然后finish页面，
                        user = new User();
                    }
                    setStaticSelf(user);
                }
            }
        }
        return staticSelf;
    }

    @SuppressWarnings("WeakerAccess")
    @Nullable
    public static User getSelf(Context ctx) {
        if (staticSelf == null) {
            synchronized (CoreManager.class) {
                if (staticSelf == null) {
                    String userId = UserSp.getInstance(ctx).getUserId("");
                    setStaticSelf(UserDao.getInstance().getUserByUserId(userId));
                }
            }
        }
        return staticSelf;
    }

    @SuppressWarnings("WeakerAccess")
    @NonNull
    public static UserStatus requireSelfStatus(Context ctx) {
        if (staticSelfStatus == null) {
            synchronized (CoreManager.class) {
                if (staticSelfStatus == null) {
                    UserStatus info = new UserStatus();
                    info.accessToken = UserSp.getInstance(ctx).getAccessToken(null);

                    if (TextUtils.isEmpty(info.accessToken)) {
                        LogUtils.e("mUserStatus","info.accessToken is null");
                        Reporter.post("登录的accessToken为空，");
                        // 无论如何没有登录信息就跳到重新登录，
                        MyApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_TOKEN_CHANGE;
                        // 弹出对话框
                        UserCheckedActivity.start(ctx);
                        // 就算没有登录也不能返回null, 直接把本地过期的返回以便正常初始化，然后finish页面，
                    }
                    setStaticSelfStatus(info);
                }
            }
        }
        return staticSelfStatus;
    }

    @SuppressWarnings("WeakerAccess")
    @Nullable
    public static UserStatus getSelfStatus(Context ctx) {
        if (staticSelfStatus == null) {
            synchronized (CoreManager.class) {
                if (staticSelfStatus == null) {
                    UserStatus info = new UserStatus();
                    info.accessToken = UserSp.getInstance(ctx).getAccessToken(null);
                    if (!TextUtils.isEmpty(info.accessToken)) {
                        setStaticSelfStatus(info);
                    }
                }
            }
        }
        return staticSelfStatus;
    }

    private static void setStaticConfig(Context ctx, AppConfig config) {
        staticConfig = config;
        Reporter.putUserData("configUrl", AppConfig.readConfigUrl(ctx));
        if (config != null) {
            Reporter.putUserData("apiUrl", config.apiUrl);
        }
    }

    /**
     * 登录和读取本地用户信息时会调用，
     * 顺便设置到bugly上，
     */
    private static void setStaticSelf(User self) {
        staticSelf = self;
        if (self != null) {
            Reporter.setUserId(self.getTelephone());
            Reporter.putUserData("userId", self.getUserId());
            Reporter.putUserData("telephone", self.getTelephone());
            // Reporter.putUserData("password", self.getPassword());
            Reporter.putUserData("nickName", self.getNickName());
        }
    }

    /**
     * 登录和读取本地用户信息时会调用，
     * 顺便设置到bugly上，
     */
    private static void setStaticSelfStatus(UserStatus selfStatus) {
        staticSelfStatus = selfStatus;
        if (selfStatus != null) {
            Reporter.putUserData("accessToken", selfStatus.accessToken);
        }
    }

    /**
     * 可能是获取服务器config失败，也可能是没获取config时就被拉起了其他页面，
     */
    @NonNull
    public static ConfigBean getDefaultConfig(Context ctx) {
        try {
            // 手动下载一份config接口返回值放在assets里作为默认config,
            ObjectResult<ConfigBean> result = JSON.parseObject(ctx.getAssets().open("default_config"), new TypeReference<ObjectResult<ConfigBean>>() {
            }.getType());
            if (result == null || result.getData() == null) {
                // 就算有个万一，也不要返回null,
                return new ConfigBean();
            }
            return result.getData();
        } catch (IOException e) {
            // 不可到达，本地assets一定要提供默认config,
            Reporter.unreachable();
            return new ConfigBean();
        }
    }

    public ConfigBean readConfigBean() {
        return readConfigBean(ctx);
    }

    public void saveConfigBean(ConfigBean configBean) {
        getSharedPreferences(ctx)
                .edit()
                .putString(KEY_CONFIG_BEAN, JSON.toJSONString(configBean))
                .apply();
        config = AppConfig.initConfig(configBean);
        setStaticConfig(ctx, config);
        OBSUtils.saveObsInfo(ctx, configBean);
    }

    public AppConfig getConfig() {
        if (config == null) {
            config = requireConfig(ctx);
        }
        return config;
    }

    public User getSelf() {
        return self;
    }

    /**
     * 登录时会调用该方法保存用户信息，
     */
    public void setSelf(User self) {
        this.self = self;
        setStaticSelf(self);
    }

    public UserStatus getSelfStatus() {
        return selfStatus;
    }

    /**
     * 登录时会调用该方法保存用户信息，
     */
    public void setSelfStatus(UserStatus selfStatus) {
        this.selfStatus = selfStatus;
        setStaticSelfStatus(selfStatus);
    }

    public Limit getLimit() {
        return limit;
    }

    void init(boolean loginRequired, boolean configRequired) {
        Log.d(TAG, "init() called");
        this.loginRequired = loginRequired;
        this.configRequired = configRequired;
        if (loginRequired) {
            this.self = requireSelf(ctx);
            this.selfStatus = requireSelfStatus(ctx);
        } else {
            this.self = getSelf(ctx);
            this.selfStatus = getSelfStatus(ctx);
        }
        if (configRequired) {
            this.config = requireConfig(ctx);
        }
        if (loginRequired) {
            //todo
            //ImHelper.checkXmppAuthenticated();
        }
        MyApplication.mCoreManager = this;
    }
}
