package com.iimm.miliao;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mobads.sdk.api.BDAdConfig;
import com.baidu.mobads.sdk.api.BDDialogParams;
import com.baidu.mobads.sdk.api.MobadsPermissionSettings;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.Utils;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.gson.Gson;
import com.iimm.miliao.NetWorkObservable.NetWorkObserver;
import com.iimm.miliao.ad.TTAdManagerHolder;
import com.iimm.miliao.adapter.MessageEventBG;
import com.iimm.miliao.bean.AdvertisingList;
import com.iimm.miliao.bean.NodeInfo;
import com.iimm.miliao.bean.PrivacySetting;
import com.iimm.miliao.db.SQLiteHelper;
import com.iimm.miliao.helper.PrivacySettingHelper;
import com.iimm.miliao.map.MapHelper;
import com.iimm.miliao.sp.UserSp;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.ui.tool.MyFileNameGenerator;
import com.iimm.miliao.util.AppUtils;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.LocaleHelper;
import com.iimm.miliao.util.Md5Util;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.ScreenShotListenManager;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.volley.FastVolley;
import com.iimm.miliao.xmpp.XmppChatImpl;
import com.iimm.miliao.xmpp.spare.SpareConnectionHelper;
import com.qq.e.comm.managers.GDTAdSdk;
import com.sigmob.windad.WindAdOptions;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.WindAgeRestrictedUserStatus;
import com.sigmob.windad.WindConsentStatus;
import com.sigmob.windad.WindCustomController;
import com.ycbjie.webviewlib.X5WebUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

public class MyApplication extends MultiDexApplication {
    public static final String TAG = "MyApplication";
    public static final String MULTI_RESOURCE = "android";
    public static final String MULTI_RESOURCE_PROVISIONAL = "youjob";
    public static boolean IS_OPEN_CLUSTER = false;// 服务器是否开启集群 如开启，在登录、自动登录时需要传area，在发起音视频通话(单人)时会要调接口获取通话地址
    public static boolean IS_OPEN_RECEIPT = true;
    // 是否支持多端登录
    public static boolean IS_SUPPORT_MULTI_LOGIN = true;
    // 是否将消息转发给所有设备,当且仅当消息类型为上、下线消息(检测消息除外),该标志位才为true
    public static boolean IS_SEND_MSG_EVERYONE;
    public static String[] machine = new String[]{"ios", "pc", "mac", "web"};
    public static String IsRingId = "Empty";// 当前聊天对象的id/jid 用于控制消息来时是否响铃通知
    public static String mRoomKeyLastCreate = "compatible";// 本地建群时的jid(给个初始值坐下兼容) 用于防止收到服务端的907消息时本地也在建群而造成群组重复
    private static MyApplication INSTANCE = null;
    private static Context context;
    public static volatile int noResponseConnectCount = 3;
    /* 文件缓存的目录 */
    public String mAppDir01;
    public String mPicturesDir01;
    public String mVoicesDir01;
    public String mVideosDir01;
    public String mFilesDir01;
    public int mActivityCount = 0;
    /* 文件缓存的目录 */
    public String mAppDir;
    public String mPicturesDir;
    public String mVoicesDir;
    public String mVideosDir;
    public String mFilesDir;
    public int mUserStatus;
    public boolean mUserStatusChecked = false;
    /*********************
     * 百度地图定位服务
     ************************/
    private BdLocationHelper mBdLocationHelper;
    /*********************
     * 提供网络全局监听
     ************************/
    private NetWorkObservable mNetWorkObservable;
    /*****************
     * 提供全局的Volley
     ***************************/

    private FastVolley mFastVolley;
    private LruCache<String, Bitmap> mMemoryCache;
    // 抖音模块缓存
    private HttpProxyCacheServer proxy;
    public static CoreManager mCoreManager;
    public static Handler applicationHandler = new Handler();
    public static String validXmppHost;//可以连上的XMPP 主机
    public static int validXmppPort = 0;//可以连上的XMPP 端口号
    public static int alarmCount = 0;

    public static MyApplication getInstance() {
        return INSTANCE;
    }

    public static Context getContext() {
        return context;
    }

    public static HttpProxyCacheServer getProxy(Context context) {
        MyApplication app = (MyApplication) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        context = getApplicationContext();

        //  setClockBroadcastReceiver();
        SDKInitializer.initialize(getApplicationContext());
        //MobSDK.init(this, Constants.MOB_APPKEY, Constants.MOB_APPSECRET);
        if (AppConfig.DEBUG) {
            Log.d(AppConfig.TAG, "MyApplication onCreate");
        }
        initMulti();
        // 在7.0的设备上，开启该模式访问相机或裁剪居然不会抛出FileUriExposedException异常，记录一下
        if (AppConfig.DEBUG) {
            //StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
            //StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
        }
        // 初始化网络监听
        mNetWorkObservable = new NetWorkObservable(this);
        // 初始化数据库
        SQLiteHelper.copyDatabaseFile(this);
        // 初始化定位
        getBdLocationHelper();
        // 初始化App目录
        initAppDir();
        initAppDirsecond();
        // 初始化图片加载 缓存
        initLruCache();
        // 判断前后台切换
        getAppBackground();
        // 监听屏幕截图
        ListeningScreenshots();
        int launchCount = PreferenceUtils.getInt(this, Constants.APP_LAUNCH_COUNT, 0);// 记录app启动的次数
        PreferenceUtils.putInt(this, Constants.APP_LAUNCH_COUNT, ++launchCount);
        initMap();
        initLanguage();
        initReporter();
        initX5();
        if (Constants.SUPPORT_SECOND_CHANNEL) {
            SpareConnectionHelper.getInstance().setSpareChatEvent(new XmppChatImpl());
        }
        Utils.init(this);
        //1.腾讯优量汇2.百度百青藤3，快手联盟4，穿山甲5，sigmob (聚合sdk)
        String jsonSplash = SPUtils.getInstance().getString("splashKey", null);
        if (!TextUtils.isEmpty(jsonSplash)) {
            try {
                AdvertisingList advertisingList = new Gson().fromJson(jsonSplash, AdvertisingList.class);
                if (advertisingList != null && advertisingList.getAdvertisings() != null) {
                    for (AdvertisingList.Advertising advertising : advertisingList.getAdvertisings()) {
                        if ("1".equals(advertising.getId())) {
                            initGdt(advertising.getAdvertisingKey());
                        } else if ("2".equals(advertising.getId())) {
                            initBaidu(advertising.getAdvertisingKey());
                        } else if ("3".equals(advertising.getId())) {
                            if (BuildConfig.DEBUG) {
                                ToastUtil.showToast("快手 初始化失败");
                            }
                        } else if ("4".equals(advertising.getId())) {
                            initCsj(advertising.getAdvertisingKey());
                        } else if ("5".equals(advertising.getId())) {
                            initSigmob(advertising.getAdvertisingKey());
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }

    private void initGdt(String code) {
        if (code == null) {
            if (BuildConfig.DEBUG) {
                ToastUtil.showToast("initGdt 为空");
            }
            return;
        };
        GDTAdSdk.initWithoutStart(this, code); // 该接口不会采集用户信息
// 调用initWithoutStart后请尽快调用start，否则可能影响广告填充，造成收入下降
        GDTAdSdk.start(new GDTAdSdk.OnStartListener() {
            @Override
            public void onStartSuccess() {
                // 推荐开发者在onStartSuccess回调后开始拉广告
                Log.e("gdt onStartSuccess:", "onStartSuccess");

            }

            @Override
            public void onStartFailed(Exception e) {
                if (BuildConfig.DEBUG) {
                    ToastUtil.showToast("code:" + code + ";因为" + e.getMessage());
                }
                Log.e("gdt onStartFailed:", e.toString());
            }
        });
    }

    private void initSigmob(String code) {
        if (code == null) {
            if (BuildConfig.DEBUG) {
                ToastUtil.showToast("initSigmob 为空");
            }
            return;
        };
        if (!code.contains(",")) {
            if (BuildConfig.DEBUG) {
                ToastUtil.showToast("格式错误，需要id和key，英文逗号隔开");
            }
            return;
        }

        String[] codes= code.split(",");

        WindAds ads = WindAds.sharedAds();
        WindAds.setOAIDCertPem(loadOaidCertPem(this));

        ads.setUserAge(18);
        ads.setAdult(true);//是否成年
        ads.setPersonalizedAdvertisingOn(true);//是否开启个性化推荐接口
        ads.setIsAgeRestrictedUser(WindAgeRestrictedUserStatus.NO);//coppa//是否年龄限制
        ads.setUserGDPRConsentStatus(WindConsentStatus.ACCEPT);//是否接受gdpr协议


        WindAdOptions windAdOptions = new WindAdOptions(codes[0], codes[1]);
        windAdOptions.setCustomController(new WindCustomController() {
            @Override
            public boolean isCanUseLocation() {
                return super.isCanUseLocation();
            }

            @Override
            public boolean isCanUsePhoneState() {
                return super.isCanUsePhoneState();
            }

            @Override
            public String getDevImei() {
                return super.getDevImei();
            }

            @Override
            public String getDevOaid() {
                return super.getDevOaid();
            }

            @Override
            public boolean isCanUseAndroidId() {
                return super.isCanUseAndroidId();
            }

            @Override
            public String getAndroidId() {
                return super.getAndroidId();
            }

            @Override
            public boolean isCanUseAppList() {
                return false;
            }
        });
        ads.startWithOptions(this, windAdOptions);
    }

    private String loadOaidCertPem(Context context) {
        try {

            InputStream is;
            String defaultPemCert = context.getPackageName() + ".cert.pem";
            AssetManager assetManager = context.getAssets();

            is = assetManager.open(defaultPemCert);

            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
            return builder.toString();

        } catch (Throwable th) {

        }
        return "";
    }

    private void initBaidu(String code) {
        if (code == null) {
            if (BuildConfig.DEBUG) {
                ToastUtil.showToast("initBaidu 为空");
            }
            return;
        };
        BDAdConfig bdAdConfig = new BDAdConfig.Builder()
                // 1、设置app名称，可选
                .setAppName(getString(R.string.app_name))
                // 2、应用在mssp平台申请到的appsid，和包名一一对应，此处设置等同于在AndroidManifest.xml里面设置
                .setAppsid(code)
                // 3、设置下载弹窗的类型和按钮动效样式，可选
                .setDialogParams(new BDDialogParams.Builder()
                        .setDlDialogType(BDDialogParams.TYPE_BOTTOM_POPUP)
                        .setDlDialogAnimStyle(BDDialogParams.ANIM_STYLE_NONE)
                        .build())
                // 4、控制台debug日志调试开关 接入调试阶段可以打开，上线前需关闭
                .setDebug(false)
                // 5、设置微信openSDK 应用id
                .setWXAppid("")
                // 6、如需获知SDK初始化结果，可选择性注册监听
                .setBDAdInitListener(new BDAdConfig.BDAdInitListener() {
                    @Override
                    public void success() {
                        Log.e("MobadsApplication", "SDK初始化成功");
                    }

                    @Override
                    public void fail() {
                        if (BuildConfig.DEBUG) {
                            ToastUtil.showToast("initBaidu SDK初始化失败");
                        }
                        Log.e("MobadsApplication", "SDK初始化失败");
                    }
                })
                .build(this);
        bdAdConfig.init();


// 设置SDK可以使用的权限，包含：设备信息、定位、存储
// 注意：建议授权SDK读取设备信息，SDK会在应用获得系统权限后自行获取IMEI等设备信息
// 授权SDK获取设备信息会有助于提升ECPM
        MobadsPermissionSettings.setPermissionReadDeviceID(true);
        MobadsPermissionSettings.setPermissionLocation(true);
        MobadsPermissionSettings.setPermissionStorage(true);
    }

    private void initCsj(String code) {
        if (code == null) {
            if (BuildConfig.DEBUG) {
                ToastUtil.showToast("initCsj 为空");
            }
            return;
        };
        //穿山甲
        TTAdManagerHolder.init(this, "5587347");
        TTAdSdk.start(new TTAdSdk.Callback() {
            @Override
            public void success() {
                Log.i(TAG, "success:  6666666666");
            }

            @Override
            public void fail(int code, String msg) {
                if (BuildConfig.DEBUG) {
                    ToastUtil.showToast("initCsj "+"fail:  code = " + code + " msg = " + msg);
                }
                Log.i(TAG, "fail:  code = " + code + " msg = " + msg);
            }
        });
    }

    private void setClockBroadcastReceiver() {
        // Intent intent = new Intent("ELITOR_CLOCK");
        Log.i("info", "准备进来了");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
        Date dateCurrent = new Date(System.currentTimeMillis());
        Log.i("info", "simpleDateFormat.format(date)" + simpleDateFormat.format(dateCurrent));
        Intent intent = new Intent(this, MyReceiver.class);
        String dateStrg = "2022-01-20 00:00:00";//"2021-11-17 14:01:00";//
        SimpleDateFormat formatter = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        try {
            Date dateEnd = formatter.parse(dateStrg);
            if (dateEnd.getTime() - dateCurrent.getTime() <= 0) {
                PendingIntent pi = PendingIntent.getBroadcast(this, alarmCount++, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        System.currentTimeMillis(), pi);
                Log.i("info", "准备进来了==a====");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.i("info", "准备进来了==b====");

                    am.set(AlarmManager.RTC_WAKEUP,
                            System.currentTimeMillis(), pi);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    private void initX5() {
        X5WebUtils.init(this);
    }

    private void initReporter() {
        Reporter.init(this);
    }

    private void initLanguage() {
        // 应用程序里设置的语言，否则程序杀死后重启又会是系统语言，
        LocaleHelper.setLocale(this, LocaleHelper.getLanguage(this));
    }

    private void initMap() {
        MapHelper.initContext(this);
        // 默认为百度地图，
        PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(this);
        boolean isGoogleMap = privacySetting.getIsUseGoogleMap() == 1;
        if (isGoogleMap) {
            MapHelper.setMapType(MapHelper.MapType.GOOGLE);
        } else {
            MapHelper.setMapType(MapHelper.MapType.BAIDU);
        }
    }

    public static int getValidXmppPort() {
        if (INSTANCE == null) {
            return validXmppPort;
        }
        if (validXmppPort == 0) {
            validXmppPort = PreferenceUtils.getInt(INSTANCE, AppConstant.VALID_XMPP_PORT, 0);//一定要自己添加默认值；工具类中getInt的默认值 给的是 -1。
        }
        return validXmppPort;
    }

    public static int getLoginType() {
        return PreferenceUtils.getInt(INSTANCE, AppConstant.LOGIN_TYPE, 0);
    }

    public static String getValidXmppHost() {
        if (INSTANCE == null) {
            return validXmppHost;
        }
        validXmppHost = "";
        List<NodeInfo> nodesInfoList = CoreManager.requireConfig(context).nodesInfoList;
        if (nodesInfoList != null) {
            if (!TextUtils.isEmpty(PreferenceUtils.getString(INSTANCE, AppConstant.VALID_XMPP_HOST))) {
                for (int i = 0; i < nodesInfoList.size(); i++) {
                    if (nodesInfoList.get(i).getNodeIp().equals(PreferenceUtils.getString(INSTANCE, AppConstant.VALID_XMPP_HOST))) {
                        validXmppHost = PreferenceUtils.getString(INSTANCE, AppConstant.VALID_XMPP_HOST);
                    }
                }
            }


        }
        return validXmppHost;
    }

    public static String getLoginUserId() {
        return CoreManager.getSelf(MyApplication.getContext()).getUserId();
    }

    public static String getXmppAccount() {
        return CoreManager.getSelf(MyApplication.getContext()).getUserId();
    }

    public static String getXmppPassword() {
        String password = CoreManager.getSelf(MyApplication.getContext()).getPassword();
        String salt = UserSp.getInstance(MyApplication.getInstance()).getSalt(null);
        if (TextUtils.isEmpty(salt)) {
            return password;
        } else {
            return Md5Util.toMD5(Md5Util.toMD5(salt + Md5Util.toMD5(password + salt)));
        }


    }

    private void getAppBackground() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                if (mActivityCount == 0) {
                    Log.e(TAG, "程序已到前台,检查XMPP是否验证");
                  /*  XMPPTCPConnection xmpptcpConnection = XmppConnectionImpl.getInstance().getXMPPConnection();
                    if (xmpptcpConnection != null && xmpptcpConnection.isConnected() && XmppConnectionImpl.getInstance().mPingManager != null) {
                        XmppConnectionImpl.getInstance().mPingManager.setPingInterval(PING_SECONDS);
                        xmpptcpConnection.setReplyTimeout(RESPONSE_TIME_OUT);
                    }*/
                }
                EventBus.getDefault().post(new MessageEventBG(true));
                mActivityCount++;
                Log.e(TAG, "onActivityStarted-->" + mActivityCount);
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                /*
                现在程序到后台，XMPP不置为离线，保持TCP的长连接，但是离线时间还是要保存
                 */
                mActivityCount--;
                Log.e(TAG, "onActivityStopped-->" + mActivityCount);
                if (!AppUtils.isAppForeground(getContext())) {// 在app启动时，当启动页stop，而MainActivity还未start时，又会回调到该方法内，所以需要判断到底是不是真的处于后台
                    appBackstage(true);
                    EventBus.getDefault().post(new MessageEventBG(false));
                   /* XMPPTCPConnection xmpptcpConnection = XmppConnectionImpl.getInstance().getXMPPConnection();
                    if (xmpptcpConnection != null && xmpptcpConnection.isConnected() && XmppConnectionImpl.getInstance().mPingManager != null) {
                        XmppConnectionImpl.getInstance().mPingManager.setPingInterval(BACK_APP_PING_SECONDS);
                        xmpptcpConnection.setReplyTimeout(BACK_APP_RESPONSE_TIME_OUT);
                    }*/
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    public void appBackstage(boolean isBack) {
        AsyncUtils.doAsync(this, c -> {
            CoreManager.appBackstage(getApplicationContext(), isBack);
        });
    }

    /*******************
     * 初始化图片加载
     **********************//*
    // 显示的设置
    public static DisplayImageOptions mNormalImageOptions;
    public static DisplayImageOptions mAvatarRoundImageOptions;
    public static DisplayImageOptions mAvatarNormalImageOptions;

    private void initImageLoader() {
        int memoryCacheSize = (int) (Runtime.getRuntime().maxMemory() / 5);
        MemoryCacheAware<String, Bitmap> memoryCache;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            memoryCache = new LruMemoryCache(memoryCacheSize);
        } else {
            memoryCache = new LRULimitedMemoryCache(memoryCacheSize);
        }

        mNormalImageOptions = new DisplayImageOptions.Builder().bitmapConfig(Config.RGB_565).cacheInMemory(true).cacheOnDisc(true)
                .resetViewBeforeLoading(false).showImageForEmptyUri(R.drawable.image_download_fail_icon)
                .showImageOnFail(R.drawable.image_download_fail_icon).build();

        mAvatarRoundImageOptions = new DisplayImageOptions.Builder().bitmapConfig(Config.RGB_565).cacheInMemory(true).cacheOnDisc(true)
                .displayer(new RoundedBitmapDisplayer(10)).resetViewBeforeLoading(true).showImageForEmptyUri(R.drawable.avatar_normal)
                .showImageOnFail(R.drawable.avatar_normal).showImageOnLoading(R.drawable.avatar_normal).build();

        mAvatarNormalImageOptions = new DisplayImageOptions.Builder().bitmapConfig(Config.RGB_565).cacheInMemory(true).cacheOnDisc(true)
                .resetViewBeforeLoading(true).showImageForEmptyUri(R.drawable.avatar_normal).showImageOnFail(R.drawable.avatar_normal)
                .showImageOnLoading(R.drawable.avatar_normal).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).defaultDisplayImageOptions(mNormalImageOptions)
                // .denyCacheImageMultipleSizesInMemory()
                .discCache(new TotalSizeLimitedDiscCache(new File(mPicturesDir), 50 * 1024 * 1024))
                // 最多缓存50M的图片
                .discCacheFileNameGenerator(new Md5FileNameGenerator()).memoryCache(memoryCache).tasksProcessingOrder(QueueProcessingType.LIFO)
                .threadPriority(Thread.NORM_PRIORITY - 2).threadPoolSize(4).build();
        // Initialize ImageLoader with configuration.
        com.nostra13.universalimageloader.utils.L.disableLogging();
        ImageLoader.getInstance().init(config);
    }*/
    private void ListeningScreenshots() {
        ScreenShotListenManager manager = ScreenShotListenManager.newInstance(this);
        manager.setListener(new ScreenShotListenManager.OnScreenShotListener() {
            @Override
            public void onShot(String imagePath) {
                PreferenceUtils.putString(getApplicationContext(), Constants.SCREEN_SHOTS, imagePath);
            }
        });
        manager.startListen();
    }

    public void initMulti() {
        // 只能在登录的时候修改，所以不能放到 setPrivacySettings 内
        PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(this);
        boolean isSupport = privacySetting.getMultipleDevices() == 1;
        if (isSupport) {
            IS_SUPPORT_MULTI_LOGIN = true;
        } else {
            //IS_SUPPORT_MULTI_LOGIN = false;
        }
    }

    /*
    保存某群组的部分属性
     */
    public void saveGroupPartStatus(String groupJid, int mGroupShowRead, int mGroupAllowSecretlyChat,
                                    int mGroupAllowConference, int mGroupAllowSendCourse, long mGroupTalkTime) {
        // 是否显示群消息已读人数
        PreferenceUtils.putBoolean(this, Constants.IS_SHOW_READ + groupJid, mGroupShowRead == 1);
        // 是否允许普通成员私聊
        PreferenceUtils.putBoolean(this, Constants.IS_SEND_CARD + groupJid, mGroupAllowSecretlyChat == 1);
        // 是否允许普通成员发起会议
        PreferenceUtils.putBoolean(this, Constants.IS_ALLOW_NORMAL_CONFERENCE + groupJid, mGroupAllowConference == 1);
        // 是否允许普通成员发送课程
        PreferenceUtils.putBoolean(this, Constants.IS_ALLOW_NORMAL_SEND_COURSE + groupJid, mGroupAllowSendCourse == 1);
        // 是否开启了全体禁言
        PreferenceUtils.putBoolean(this, Constants.GROUP_ALL_SHUP_UP + groupJid, mGroupTalkTime > 0);
    }

    /**
     * 初始化支付密码设置状态，
     * 登录接口返回支付密码是否设置，在这里保存起来，
     *
     * @param payPassword 支付密码是否已经设置，
     */
    public void initPayPassword(String userId, int payPassword) {
        Log.d(TAG, "initPayPassword() called with: userId = [" + userId + "], payPassword = [" + payPassword + "]");
        // 和initPrivateSettingStatus中的其他变量保存方式统一，
        PreferenceUtils.putBoolean(this, Constants.IS_PAY_PASSWORD_SET + userId, payPassword == 1);
    }

    public BdLocationHelper getBdLocationHelper() {
        if (mBdLocationHelper == null) {
            mBdLocationHelper = new BdLocationHelper(this);
        }
        return mBdLocationHelper;
    }

    public boolean isNetworkActive() {
        if (mNetWorkObservable != null) {
            return mNetWorkObservable.isNetworkActive();
        }
        return true;
    }

    public void registerNetWorkObserver(NetWorkObserver observer) {
        if (mNetWorkObservable != null) {
            mNetWorkObservable.registerObserver(observer);
        }
    }

    public void unregisterNetWorkObserver(NetWorkObserver observer) {
        if (mNetWorkObservable != null) {
            mNetWorkObservable.unregisterObserver(observer);
        }
    }

    private void initAppDirsecond() {
        File file = getExternalFilesDir(null);

        if (file == null) {
            return;
        }

        if (!file.exists()) {
            file.mkdirs();
        }
        mAppDir01 = file.getAbsolutePath();

        file = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!file.exists()) {
            file.mkdirs();
        }
        mPicturesDir01 = file.getAbsolutePath();

        file = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (!file.exists()) {
            file.mkdirs();
        }
        mVoicesDir01 = file.getAbsolutePath();

        file = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if (!file.exists()) {
            file.mkdirs();
        }
        mVideosDir01 = file.getAbsolutePath();

        file = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (!file.exists()) {
            file.mkdirs();
        }
        mFilesDir01 = file.getAbsolutePath();
    }

    private void initAppDir() {
        File file = getExternalFilesDir(null);
        if (file != null && !file.exists()) {
            file.mkdirs();
        }
        if (file != null) {
            mAppDir = file.getAbsolutePath();
        }

        file = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (file != null && !file.exists()) {
            file.mkdirs();
        }
        if (file != null) {
            mPicturesDir = file.getAbsolutePath();
        }

        file = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (file != null && !file.exists()) {
            file.mkdirs();
        }
        if (file != null) {
            mVoicesDir = file.getAbsolutePath();
        }

        file = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if (file != null && !file.exists()) {
            file.mkdirs();
        }
        if (file != null) {
            mVideosDir = file.getAbsolutePath();
        }

        file = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (file != null && !file.exists()) {
            file.mkdirs();
        }
        if (file != null) {
            mFilesDir = file.getAbsolutePath();
        }
    }

    public FastVolley getFastVolley() {
        if (mFastVolley == null) {
            synchronized (MyApplication.class) {
                if (mFastVolley == null) {
                    mFastVolley = new FastVolley(this);
                    mFastVolley.start();
                }
            }
        }
        return mFastVolley;
    }

    private void releaseFastVolley() {
        if (mFastVolley != null) {
            mFastVolley.stop();
        }
    }

    /**
     * 在程序内部关闭时，调用此方法
     */
    public void destory() {
        if (AppConfig.DEBUG) {
            Log.d(AppConfig.TAG, "MyApplication destory");
        }
        // 结束百度定位
        if (mBdLocationHelper != null) {
            mBdLocationHelper.release();
        }
        // 关闭网络状态的监听
        if (mNetWorkObservable != null) {
            mNetWorkObservable.release();
        }
        // 清除图片加载
        // ImageLoader.getInstance().destroy();
        releaseFastVolley();
        // 释放数据库
        // SQLiteHelper.release();
        android.os.Process.killProcess(android.os.Process.myPid());

    }

    public void destoryRestart() {
        if (AppConfig.DEBUG) {
            Log.d(AppConfig.TAG, "MyApplication destory");
        }
        // 结束百度定位
        if (mBdLocationHelper != null) {
            mBdLocationHelper.release();
        }
        // 关闭网络状态的监听
        if (mNetWorkObservable != null) {
            mNetWorkObservable.release();
        }
        // 清除图片加载
        // ImageLoader.getInstance().destroy();
        releaseFastVolley();
        // 释放数据库
        // SQLiteHelper.release();

        //  android.os.Process.killProcess(android.os.Process.myPid());

    }

    /***********************
     * 保存其他用户坐标信息
     ***************/

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public void initLruCache() {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer.Builder(this)
                .maxCacheSize(1024 * 1024 * 1024)       // 1 Gb for cache
                .fileNameGenerator(new MyFileNameGenerator()).build();
    }
}
