package com.iimm.miliao.xmpp;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.iimm.miliao.BuildConfig;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.bean.EventBusMsg;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.DeviceInfoUtil;
import com.iimm.miliao.util.HttpUtil;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToolUtils;
import com.iimm.miliao.util.log.LogUtils;
import com.iimm.miliao.xmpp.util.ImHelper;
import com.iimm.miliao.xmppProxy.CheckProxy;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.sasl.SASLErrorException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @Author: wangqx
 * @Date: 2019/6/30
 * xmpp 连接 登录 入群
 */
public class XmppHelpService extends IntentService {
    private final static String TAG = "XmppHelpService";
    private static XmppHelpService instance;//onDestroy 时，必须设置为空
    private Thread handlerThread;
    private String tempDate;

    public static XmppHelpService getInstance() {    //对获取实例的方法进行同步
        //LogUtils.i(TAG, "getInstance: instance:" + instance);
        if (instance == null) {
            synchronized (XmppHelpService.class) {
                if (instance == null) {
                    startService();
                }
            }
        }
//        else if (!ToolUtils.isServiceRunning(ImApplication.getInstance(), XmppHelpService.class.getName())) {
//            startXmppConnectService();
//        }
        else {
            return instance;
        }
        return null;
    }

    /**
     * 判断此service是否运行
     */
    public static void checkServiceIsRuning() {
        getInstance();
    }

    public XmppHelpService() {
        super("XmppHelpService");
    }


    private static void startService() {
        LogUtils.i(TAG, "startXmppConnectService: " + (ToolUtils.isServiceRunning(MyApplication.getInstance(), XmppHelpService.class.getName())));
//        if (ToolUtils.isServiceRunning(ImApplication.getInstance(), XmppHelpService.class.getName())) {
//            return;
//        }
        Intent intent = new Intent(MyApplication.getInstance(), XmppHelpService.class);
        MyApplication.getInstance().startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        LogUtils.i(TAG, "onCreate: instance:" + instance);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        handlerThread = Thread.currentThread();
        try {
            LogUtils.i(TAG, "onHandleIntent: intent:" + intent);
            if (intent != null) {
                String typeXmppAction = intent.getStringExtra(Constants.TYPE_XMPP_ACTION);
                String userName = intent.getStringExtra(Constants.XMPP_ACCOUNT);
                String password = intent.getStringExtra(Constants.XMPP_PASSWORD);
                LogUtils.i(TAG, "onHandleIntent: action:" + typeXmppAction);
                if (!TextUtils.isEmpty(typeXmppAction)) {
                    XMPPTCPConnection xmpptcpConnection = XmppConnectionImpl.getInstance().getXMPPConnection();
                    switch (typeXmppAction) {
                        case Constants.TYPE_XMPP_CONNECT:
                            LogUtils.i(TAG, "action connect: xmpptcpConnection:" + xmpptcpConnection);
                            if (xmpptcpConnection != null) {
                                LogUtils.i(TAG, "action connect isConnected: " + xmpptcpConnection.isConnected());
                            }
                            if (xmpptcpConnection == null) {
                                String xmppDomain = intent.getStringExtra(Constants.XMPP_DOMAIN);
                                String xmppHostName = intent.getStringExtra(Constants.XMPP_HOSTNAME);
                                int xmppPort = intent.getIntExtra(Constants.XMPP_PORT, 0);
                                XMPPTCPConnectionConfiguration configuration;
                                try {
                                    configuration = getConnectionConfigurationWithParams(xmppDomain, xmppHostName, xmppPort, userName, password);
                                    LogUtils.i(TAG, "onHandleIntent: configuration:" + configuration);
                                } catch (Exception e) {
                                    LogUtils.e(TAG, "onHandleIntent: configuration is null。 stop.....");
                                    stopService(new Intent(this, XmppHelpService.class));
                                    reportErrorMessageToServer(e);
                                    return;
                                }
                                if (configuration == null) {
                                    stopService(new Intent(this, XmppHelpService.class));
                                    return;
                                }
                                if (HttpUtil.isGprsOrWifiConnected(MyApplication.getContext())) {
                                    XMPPTCPConnection connection = new XMPPTCPConnection(configuration);
                                    connection.setReplyTimeout(XmppConnectionImpl.RESPONSE_TIME_OUT);
                                    connection.addConnectionListener(XmppConnectionImpl.getInstance());

                                    if (Constants.SUPPORT_MANUAL_NODE) {
                                        try {
                                            connection.connect();
                                        } catch (Exception e) {
                                            CheckProxy.getInstance().start(new CheckProxy.ResultListener() {

                                                @Override
                                                public void result() {

                                                    if (HttpUtil.isGprsOrWifiConnected(MyApplication.getInstance())) {
                                                        ImHelper.checkXmppAuthenticated();//判断重新登录
                                                    }
                                                }
                                            }, MyApplication.getContext());
                                        }
                                    } else {
                                        connection.connect();
                                    }
                                    EventBusMsg eventBusMsg = new EventBusMsg();
                                    eventBusMsg.setMessageType(Constants.EVENTBUS_MSG_AUTH_CONNECT_ING);
                                    EventBus.getDefault().post(eventBusMsg);
                                } else {
                                    EventBusMsg eventBusMsg = new EventBusMsg();
                                    eventBusMsg.setMessageType(Constants.EVENTBUS_MSG_AUTH_NOT);
                                    EventBus.getDefault().post(eventBusMsg);
                                    return;
                                }
                            } else {
                                if (xmpptcpConnection.isConnected()) {
                                    if (xmpptcpConnection.isAuthenticated()) {
                                        //@todo
                                        if (!TextUtils.isEmpty(MyApplication.getXmppAccount())) {//已经有登录账号
                                            //是否是当前账号
                                            //1、   是  return
                                            if (xmpptcpConnection.getUser().getLocalpart().toString().equals(MyApplication.getXmppAccount())) {//不需要重新登陆
                                                LogUtils.i(TAG, "onHandleIntent action connect: 已登录 无需重新连接");
                                                EventBusMsg eventBusMsg = new EventBusMsg();
                                                eventBusMsg.setMessageType(Constants.EVENTBUS_MSG_AUTH_SUCCESS);
                                                EventBus.getDefault().post(eventBusMsg);
                                                return;
                                            } else { //2、不是  退出 重连 登录
                                                xmpptcpConnection.disconnect();
                                                ImHelper.xmppConnect(this);
                                            }
                                        } else {
                                            xmpptcpConnection.disconnect();
                                            //重连
                                            ImHelper.xmppConnect(this);
                                        }

                                    } else {
                                        return;
                                    }
                                }
                            }
                            break;
                        case Constants.TYPE_XMPP_LOGIN:
                            String resource = intent.getStringExtra(Constants.XMPP_RESOURCE);
                            if (xmpptcpConnection != null) {
                                LogUtils.i(TAG, "INTENT_ACTION_LOGIN: xmpptcpConnection:" + xmpptcpConnection);
                                LogUtils.i(TAG, "INTENT_ACTION_LOGIN  isConnected: " + xmpptcpConnection.isConnected());
                                LogUtils.i(TAG, "INTENT_ACTION_LOGIN  isAuthenticated: " + xmpptcpConnection.isAuthenticated());
                                if (xmpptcpConnection.isConnected()) {
                                    if (xmpptcpConnection.isAuthenticated()) {
                                        //1、当前账号  return
                                        //2、非当前账号  退出 重连 登录
                                        if (!TextUtils.isEmpty(userName) && xmpptcpConnection.getUser().getLocalpart().toString().equals(userName)) {
                                            LogUtils.i(TAG, "onHandleIntent: 已登录 无需重新登录");
                                            EventBusMsg eventBusMsg = new EventBusMsg();
                                            eventBusMsg.setMessageType(Constants.EVENTBUS_MSG_AUTH_SUCCESS);
                                            EventBus.getDefault().post(eventBusMsg);
                                            return;
                                        } else {
                                            xmpptcpConnection.disconnect();
                                            //连接  登录
                                            ImHelper.xmppConnect(this);
                                        }
                                    } else {
                                        //登录
                                        Resourcepart mResourcepart;
                                        if (TextUtils.isEmpty(resource)) {
                                            mResourcepart = Resourcepart.from(MyApplication.MULTI_RESOURCE);
                                        } else {
                                            mResourcepart = Resourcepart.from(resource);
                                        }
                                        tempDate = TimeUtils.getCurrentTime();
                                        xmpptcpConnection.login();//关键所在
                                    }
                                } else {
                                    //连接  登录
                                    ImHelper.xmppConnect(this);
                                }
                            } else {
                                //重连、登陆
                                ImHelper.xmppConnect(this);
                            }
                            break;
                        case Constants.TYPE_XMPP_JOIN_GROUP:
                            break;
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "onHandleIntent: 错误:" + Log.getStackTraceString(e));
            instance = null;
            if (HttpUtil.isNetWorkAvailable()) {
                XMPPTCPConnection xmpptcpConnection = XmppConnectionImpl.getInstance().getXMPPConnection();
                LogUtils.e(TAG, "onHandleIntent: " + (e instanceof SmackException));
                LogUtils.e(TAG, "onHandleIntent: " + (e instanceof SASLErrorException));
                reportErrorMessageToServer(e);
                Reporter.post(e.getMessage());
                if (xmpptcpConnection != null && xmpptcpConnection.isConnected()) {
                    xmpptcpConnection.disconnect();
                }
                EventBusMsg eventBusMsg = new EventBusMsg();
                eventBusMsg.setMessageType(Constants.EVENTBUS_MSG_AUTH_NOT);
                EventBus.getDefault().post(eventBusMsg);
            }
        }
    }

    public void reportErrorMessageToServer(Exception e) {
        if (e == null) {
            return;
        }
        String errorInfo = Log.getStackTraceString(e);
        if (TextUtils.isEmpty(errorInfo)) {
            return;
        }
        PackageInfo packageInfo = null;
        try {
            packageInfo = MyApplication.getContext().getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e1) {
        }
        errorInfo = "应用：" + MyApplication.getContext().getResources().getString(R.string.app_name) + "\n";
        if (packageInfo != null) {
            errorInfo = errorInfo + "应用版本名称：" + packageInfo.versionName + "\n";
        }
        if (MyApplication.mCoreManager!=null&&MyApplication.mCoreManager.getSelf()!=null){
            errorInfo = errorInfo + "账号：" + MyApplication.mCoreManager.getSelf().getUserId() + "\n";
            errorInfo = errorInfo + "密码：" + MyApplication.mCoreManager.getSelf().getPassword() + "\n";
        }
        String SIMOperator = HttpUtil.getOperatorName(MyApplication.getContext());
        int netStatus = HttpUtil.getNetworkState(MyApplication.getContext());
        String netStatusStr = null;
        switch (netStatus) {
            case 0: // 没有网络连接
                netStatusStr = "没有网络";
                break;
            case 1: // wifi连接
                netStatusStr = "wifi连接";
                break;
            case 2: // 2G
                netStatusStr = "2G";
                break;
            case 3: // 3G
                netStatusStr = "3G";
                break;
            case 4: // 4G
                netStatusStr = "4G";
                break;
            case 5: // 手机流量
                netStatusStr = "手机流量";
                break;
        }
        if (netStatus == 2 || netStatus == 3 || netStatus == 4 || netStatus == 5) {
            errorInfo = errorInfo + "网络信息: " + SIMOperator + " " + netStatusStr + "\n";
        } else {
            errorInfo = errorInfo + "网络信息: " + netStatusStr + "\n";
        }

        errorInfo = errorInfo + "网络IP：" + HttpUtil.getIPAddress(MyApplication.getContext()) + "\n";
        String xmppInfo = PreferenceUtils.getString(MyApplication.getContext(), "connect_info");
        errorInfo = errorInfo + "消息服务连接信息：" + xmppInfo + "\n";
        errorInfo = errorInfo + "手机系统信息：" + DeviceInfoUtil.getOsVersion() + "  " + DeviceInfoUtil.getModel() + "  " + DeviceInfoUtil.getBrand() + "\n";

        errorInfo = errorInfo + "当前本地时间：" + TimeUtils.getCurrentTime() + "\n";
        errorInfo = errorInfo + "当前服务器时间：" + TimeUtils.getFormatTime(TimeUtils.time_current_time() * 1000) + "\n";
        errorInfo = errorInfo + "发起认证请求时的时间(仅在等待认证异常时有效)：" + tempDate + "\n";
        String connectInfo = "";
        String authInfo = "";
        XMPPTCPConnection xmpptcpConnection = XmppConnectionImpl.getInstance().getXMPPConnection();
        if (ImHelper.isConnected(xmpptcpConnection)) {
            connectInfo = "已连接";
        } else {
            connectInfo = "未连接";
        }
        if (ImHelper.isAuthenticated(xmpptcpConnection)) {
            authInfo = "已认证";
        } else {
            authInfo = "未认证";
        }
        errorInfo = errorInfo + "连接状态：" + connectInfo + " " + authInfo + "\n";
        errorInfo = errorInfo + "服务器是否开启流管理：" + !MyApplication.IS_OPEN_RECEIPT + "\n";
        errorInfo = errorInfo + "当前实例：" + xmpptcpConnection + "\n";
        if (xmpptcpConnection != null && xmpptcpConnection.isAuthenticated() && xmpptcpConnection.getUser() != null) {
            errorInfo = errorInfo + "XMPP账号：" + xmpptcpConnection.getUser().toString() + "\n";
        }
        errorInfo = errorInfo + "异常信息：" + "\n" + Log.getStackTraceString(e) + "\n\n\n";
        LogUtils.e(TAG, "reportErrorMessageToServer errorInfo: " + errorInfo);
        Map<String, String> params = new HashMap<>();
        if (MyApplication.mCoreManager == null
                || MyApplication.mCoreManager.getSelfStatus() == null
                || TextUtils.isEmpty(MyApplication.mCoreManager.getSelfStatus().accessToken)) {
            return;
        }
        params.put("access_token", MyApplication.mCoreManager.getSelfStatus().accessToken);
        params.put("userId", MyApplication.mCoreManager.getSelf().getUserId());
        params.put("logContext", errorInfo);
        if (BuildConfig.LOG_DEBUG) {
            params.put("type", "android_debug");
        } else {
            params.put("type", MyApplication.MULTI_RESOURCE);
        }
        String url = MyApplication.mCoreManager.getConfig().LOG_REPORT;
        if (TextUtils.isEmpty(url)) {
            return;
        }
        HttpUtils.post().url(url)
                .params(params)
                .build()
                .execute(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                    }
                });
    }

    public XMPPTCPConnectionConfiguration getConnectionConfigurationWithParams(String xmppDomain, String xmppHost, int xmppPort, String account, String pwd) throws Exception {
        String mXmppHost;
        if (!TextUtils.isEmpty(xmppHost)) {
            mXmppHost = xmppHost;
        } else if (!TextUtils.isEmpty(MyApplication.getValidXmppHost())) {
            mXmppHost = MyApplication.getValidXmppHost();
        } else {
            mXmppHost = CoreManager.requireConfig(MyApplication.getInstance()).XMPPHost;
        }
        int mXmppPort;
        if (xmppPort != 0) {
            mXmppPort = xmppPort;
        } else if (MyApplication.getValidXmppPort() != 0) {
            mXmppPort = MyApplication.getValidXmppPort();
        } else {
            mXmppPort = CoreManager.requireConfig(MyApplication.getInstance()).mXMPPPort;
        }
        String mXmppDomain;
        if (!TextUtils.isEmpty(xmppDomain)) {
            mXmppDomain = xmppDomain;
        } else {
            mXmppDomain = CoreManager.requireConfig(MyApplication.getInstance()).XMPPDomain;
        }
        try {
            LogUtils.i(TAG, "getConnectionConfigurationWithParams: mXmppHost:" + mXmppHost);
            LogUtils.i(TAG, "getConnectionConfigurationWithParams: mXmppDomain:" + mXmppDomain);
            LogUtils.i(TAG, "getConnectionConfigurationWithParams: mXmppPort:" + mXmppPort);
//            int po = PreferenceUtils.getInt(MyApplication.getContext(), Constants.SELCTORPROXY, 0);
//            boolean b = PreferenceUtils.getBoolean(MyApplication.getContext(), Constants.SELCTORPROXY + po, false);
//            ProxyInfo proxyInfo = null;
            InetAddress addres;

//            List<NodeInfo> nodesInfoList = CoreManager.requireConfig(MyApplication.getContext()).nodesInfoList;
//            if (b && nodesInfoList != null && nodesInfoList.size() > po && nodesInfoList.get(po) != null && nodesInfoList.get(po).getIsSocks() == 1) {
//                proxyInfo = new ProxyInfo(ProxyInfo.ProxyType.SOCKS5, nodesInfoList.get(po).getHostSocks()
//                        , nodesInfoList.get(po).getPostSocks()
//                        , nodesInfoList.get(po).getUserSocks()
//                        , RSAUtils.decryptPublicWithBase64(nodesInfoList.get(po).getPassSocks()));
//
//                mXmppHost = nodesInfoList.get(po).getNodeIp();
//                addres = InetAddress.getByName(nodesInfoList.get(po).getNodeIp());
//                mXmppDomain = nodesInfoList.get(po).getRealmName();
//            } else {
                addres = InetAddress.getByName(mXmppHost);
//            }
            DomainBareJid mDomainBareJid = JidCreate.domainBareFrom(mXmppDomain);
            PreferenceUtils.putString(MyApplication.getContext(), "connect_info", "主机名：" + mXmppHost + "  域名：" + mXmppDomain + "  端口：" + mXmppPort + " 地址：" + addres);
            LogUtils.i(TAG, "getConnectionConfigurationWithParams: address:" + addres);
            XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword(account, pwd)
                    .setHostAddress(addres) // 服务器地址
                    .setPort(mXmppPort) // 服务器端口
                    .setXmppDomain(mDomainBareJid)
                    .setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled) // 是否开启安全模式
                    .setCompressionEnabled(Constants.SUPPORT_COMPRESSION)
                    .setResource(Constants.SUPPORT_YOU_JOB ? MyApplication.MULTI_RESOURCE_PROVISIONAL : MyApplication.MULTI_RESOURCE)
                    .setSendPresence(true);
//            if (proxyInfo != null) {
//                builder.setProxyInfo(proxyInfo);
//            }
            if (Log.isLoggable("SMACK", Log.DEBUG)) {
                // 为方便测试，留个启用方法，命令运行"adb shell setprop log.tag.SMACK D"启用，
                builder.enableDefaultDebugger();
            }
            if (BuildConfig.DEBUG) {
                builder.enableDefaultDebugger();
            }
            Resourcepart mResourcepart = Resourcepart.fromOrThrowUnchecked(Constants.SUPPORT_YOU_JOB ? MyApplication.MULTI_RESOURCE_PROVISIONAL : MyApplication.MULTI_RESOURCE);
            builder.setResource(mResourcepart);
            return builder.build();
        } catch (XmppStringprepException e) {
            LogUtils.e(TAG, "XmppStringprepException: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "Exception: " + e.getMessage());
            throw e;
        }
    }

    public XMPPTCPConnectionConfiguration getConnectionConfiguration() {
        String mXmppHost;
        if (!TextUtils.isEmpty(MyApplication.getValidXmppHost())) {
            mXmppHost = MyApplication.getValidXmppHost();
        } else {
            mXmppHost = CoreManager.requireConfig(MyApplication.getInstance()).XMPPHost;
        }
        int mXmppPort;
        if (MyApplication.getValidXmppPort() != 0) {
            mXmppPort = MyApplication.getValidXmppPort();
        } else {
            mXmppPort = CoreManager.requireConfig(MyApplication.getInstance()).mXMPPPort;
        }
        String mXmppDomain = CoreManager.requireConfig(MyApplication.getInstance()).XMPPDomain;
        DomainBareJid mDomainBareJid = null;
        try {
            mDomainBareJid = JidCreate.domainBareFrom(mXmppDomain);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }

        try {
            InetAddress address = InetAddress.getByName(mXmppHost);
            XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder()
                    .setHostAddress(address) // 服务器地址
                    .setPort(mXmppPort) // 服务器端口
                    .setXmppDomain(mDomainBareJid)
                    .setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled) // 是否开启安全模式
                    .setCompressionEnabled(Constants.SUPPORT_COMPRESSION)
                    .setSendPresence(true);
            if (Log.isLoggable("SMACK", Log.DEBUG)) {
                // 为方便测试，留个启用方法，命令运行"adb shell setprop log.tag.SMACK D"启用，
                builder.enableDefaultDebugger();
            }
            if (BuildConfig.DEBUG) {
                builder.enableDefaultDebugger();
            }
            Resourcepart mResourcepart = Resourcepart.fromOrThrowUnchecked(Constants.SUPPORT_YOU_JOB ? MyApplication.MULTI_RESOURCE_PROVISIONAL : MyApplication.MULTI_RESOURCE);
            builder.setResource(mResourcepart);
            return builder.build();

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handlerThread != null) {
            handlerThread.interrupt();
        }
        instance = null;
        LogUtils.i(TAG, "onDestroy: -------------");
    }
}
