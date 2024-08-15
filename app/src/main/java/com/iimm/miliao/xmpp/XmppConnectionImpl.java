package com.iimm.miliao.xmpp;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.iimm.miliao.AppConstant;
import com.iimm.miliao.BuildConfig;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.EventBusMsg;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.DeviceInfoUtil;
import com.iimm.miliao.util.HttpUtil;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.log.LogUtils;
import com.iimm.miliao.xmpp.spare.SpareConnectionHelper;
import com.iimm.miliao.xmpp.util.ImHelper;
import com.iimm.miliao.xmppProxy.CheckProxy;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.StreamError;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.chatstates.ChatStateListener;
import org.jivesoftware.smackx.chatstates.ChatStateManager;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @Author: wangqx
 * @Date: 2019/7/31
 * @Description:描述
 */
public class XmppConnectionImpl implements ConnectionListener, ReconnectionListener, PingFailedListener {
    private String TAG = "XmppConnectionImpl";
    private volatile XMPPTCPConnection mXMPPConnection;
    private OutgoingChatMessageListener mOutgoingChatMessageListener;
    private IncomingChatMessageListener mIncomingChatMessageListener;
    public static final int PING_SECONDS = 10;//App前台心跳时间 10s
    public static final int BACK_APP_PING_SECONDS = 30;//App前台心跳时间30s
    public static final int RESPONSE_TIME_OUT = 30 * 1000;//60S
    public static final int BACK_APP_RESPONSE_TIME_OUT = 3 * 60 * 1000;//3分钟
    public static final int CONNECTED_TIMEOUT_SECONDS = 15;
    private DeliveryReceiptManager mDeliveryReceiptManager;
    private ReceiptReceivedListener mReceiptReceivedListener;
    private StanzaListener mStanzaListener;
    public PingManager mPingManager;
    private ReconnectionManager mReconnectionManager;
    private ChatManager mChatManager;
    private ChatStateManager mChatStateManager;
    private ChatStateListener mChatStateListener;
    private PingFailedListener mPingFailedListener;
    private MyIqProvider myIqProvider;
    private EnableIQRequestHandler enableIQRequestHandler;

    private static class XmppConnectionImplHolder {
        private static final XmppConnectionImpl INSTANCE = new XmppConnectionImpl();
    }

    private XmppConnectionImpl() {
    }

    public static final XmppConnectionImpl getInstance() {
        return XmppConnectionImplHolder.INSTANCE;
    }

    @Override
    public void connected(XMPPConnection connection) {
        LogUtils.i(TAG, "connected currentThread:" + Thread.currentThread().getName());
        if (connection instanceof XMPPTCPConnection) {
            mXMPPConnection = (XMPPTCPConnection) connection;
            LogUtils.i(TAG, "connected: connection:" + mXMPPConnection);
            LogUtils.i(TAG, "connected: account:" + MyApplication.getXmppAccount());
            LogUtils.i(TAG, "connected: password:" + MyApplication.getXmppPassword());
            EventBusMsg eventBusMsg = new EventBusMsg();
            eventBusMsg.setMessageType(Constants.EVENTBUS_MSG_AUTH_CONNECTED);
            EventBus.getDefault().post(eventBusMsg);
            if (!TextUtils.isEmpty(MyApplication.getXmppAccount())
                    && !TextUtils.isEmpty(MyApplication.getXmppPassword())) {
                ImHelper.xmppLogin(MyApplication.getInstance(), MyApplication.getXmppAccount() + "", MyApplication.getXmppPassword());
            }
        }
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        LogUtils.i(TAG, "authenticated currentThread:" + Thread.currentThread().getName());
        //ToastUtil.showToast("XMPP登录成功");
        LogUtils.i(TAG, "authenticated connection:" + connection + "  " + (connection instanceof XMPPTCPConnection));
        if (connection instanceof XMPPTCPConnection) {
            mXMPPConnection = (XMPPTCPConnection) connection;
            //20191209添加，防止发送回执from为空
            mXMPPConnection.setFromMode(XMPPConnection.FromMode.UNCHANGED);
            if (mXMPPConnection.isSmResumptionPossible()) {
                LogUtils.e(TAG, "服务端开启了流");
                MyApplication.IS_OPEN_RECEIPT = false;
            } else {
                LogUtils.e(TAG, "服务端关闭了流");
                MyApplication.IS_OPEN_RECEIPT = true;// 检查服务器是否启用了流管理，如关闭本地请求回执标志位一定为true
            }
            LogUtils.i(TAG, "authenticated: mXMPPConnection:" + mXMPPConnection);
            registerAllListener();// 注册监听其他的事件，比如新消息
            ImHelper.authenticatedOperating();
            presenceOnline();
            EventBusMsg eventBusMsg = new EventBusMsg();
            eventBusMsg.setMessageType(Constants.EVENTBUS_MSG_AUTH_SUCCESS);
            EventBus.getDefault().post(eventBusMsg);
            MyApplication.noResponseConnectCount = 3;
        }
    }

    @Override
    public void connectionClosed() {
        String moreInfo = getMoreInfo(false);
        String errorInfo = "关闭信息:" + "\n";
        errorInfo = errorInfo + Log.getStackTraceString(new Exception()) + "\n\n\n";
        reportErrorMessageToServer(moreInfo, errorInfo);
        LogUtils.i(TAG, "connectionClosed: " + mXMPPConnection);
        LogUtils.i(TAG, "connectionClosed currentThread: " + Thread.currentThread().getName());
        if (mXMPPConnection != null) {
            if (mXMPPConnection.isConnected()) {
                mXMPPConnection.disconnect();
            }
            LogUtils.i(TAG, "connectionClosed: " + mXMPPConnection.isConnected());
        }
        EventBusMsg eventBusMsg = new EventBusMsg();
        eventBusMsg.setMessageType(Constants.EVENTBUS_MSG_AUTH_NOT);
        EventBus.getDefault().post(eventBusMsg);
        removeAllListener();
        if (PreferenceUtils.getBoolean(MyApplication.getContext(), AppConstant.LOGINSTATU, false) &&
                HttpUtil.isGprsOrWifiConnected(MyApplication.getInstance())) {
            ImHelper.checkXmppAuthenticated();
        }

    }

    @Override
    public void connectionClosedOnError(Exception e) {
        String moreInfo = getMoreInfo(true);
        String errorInfo = Log.getStackTraceString(e);
        reportErrorMessageToServer(moreInfo, errorInfo);
        try {
            LogUtils.i(TAG, "connectionClosedOnError: mXMPPConnection:" + mXMPPConnection);
            LogUtils.i(TAG, "connectionClosedOnError currentThread:" + Thread.currentThread().getName());
            if (mXMPPConnection != null) {
                LogUtils.i(TAG, "connectionClosedOnError: 是否连接:" + mXMPPConnection.isConnected());
            }
            if (mXMPPConnection != null) {
                if (mXMPPConnection.isConnected()) {
                    mXMPPConnection.disconnect();
                }
                mXMPPConnection = null;
                EventBusMsg eventBusMsg = new EventBusMsg();
                eventBusMsg.setMessageType(Constants.EVENTBUS_MSG_AUTH_NOT);
                EventBus.getDefault().post(eventBusMsg);
            } else {
                EventBusMsg eventBusMsg = new EventBusMsg();
                eventBusMsg.setMessageType(Constants.EVENTBUS_MSG_AUTH_NOT);
                EventBus.getDefault().post(eventBusMsg);
                removeAllListener();
            }
            if (e instanceof XMPPException.StreamErrorException) {// 登录冲突
                XMPPException.StreamErrorException streamErrorException = (XMPPException.StreamErrorException) e;
                StreamError streamError = streamErrorException.getStreamError();
                if (streamError.getCondition().equals(StreamError.Condition.conflict)) {// 下线通知
                    LogUtils.d(TAG, "异常断开，有另外设备登陆啦");
                    conflict();
                    return;
                }
            } else {
                if (Constants.SUPPORT_MANUAL_NODE) {
                    CheckProxy.getInstance().start(new CheckProxy.ResultListener() {

                        @Override
                        public void result() {
                            if (HttpUtil.isGprsOrWifiConnected(MyApplication.getInstance())) {
                                ImHelper.checkXmppAuthenticated();
                            }
                        }
                    }, MyApplication.getContext());
                }
            }
        } catch (Exception exception) {
            Log.e(TAG, "connectionClosedOnError: " + e.getMessage());
        }
    }

    private void presenceOnline() {
        if (mXMPPConnection != null && mXMPPConnection.isConnected() && mXMPPConnection.isAuthenticated()) {
            Presence presence = new Presence(Presence.Type.available);
            try {
                mXMPPConnection.sendStanza(presence);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void presenceOffline() {
        if (mXMPPConnection != null && mXMPPConnection.isConnected() && mXMPPConnection.isAuthenticated()) {
            Presence presence = new Presence(Presence.Type.unavailable);
            try {
                mXMPPConnection.sendStanza(presence);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void logoutXmpp() {
        if (Constants.SUPPORT_SECOND_CHANNEL){
            SpareConnectionHelper.getInstance().disconnection();
        }
        PreferenceUtils.putBoolean(MyApplication.getContext(), AppConstant.LOGINSTATU, false);
        presenceOffline();
        //不走这个，通过IntentService执行断开连接，防止调用这个方法时，IntentService在运行
        //todo
        // 停止连接服务
        MyApplication.getInstance().stopService(new Intent(MyApplication.getInstance(), XmppHelpService.class));
        ImHelper.resetXmppMap();
        if (mXMPPConnection != null) {
            if (mXMPPConnection.isConnected()) {
                mXMPPConnection.disconnect();
            }
            mXMPPConnection = null;
        } else {
            EventBusMsg eventBusMsg = new EventBusMsg();
            eventBusMsg.setMessageType(Constants.EVENTBUS_MSG_AUTH_NOT);
            EventBus.getDefault().post(eventBusMsg);
            removeAllListener();
        }
    }

    public void conflict() {
        EventBusMsg eventBusMsg = new EventBusMsg();
        eventBusMsg.setMessageType(Constants.EVENTBUS_MSG_ACTION_CONFLICT);
        EventBus.getDefault().post(eventBusMsg);
    }

    @Override
    public void reconnectingIn(int seconds) {
        LogUtils.e(TAG, "reconnectingIn: " + seconds);
    }

    @Override
    public void reconnectionFailed(Exception e) {
        LogUtils.e(TAG, "reconnectionFailed: e:" + e.getMessage());
    }

    @Override
    public void pingFailed() {
        LogUtils.e(TAG, "pingFailed: xxxxx");
        //releaseAndDisconnect();
    }

    public synchronized XMPPTCPConnection getXMPPConnection() {
        return mXMPPConnection;
    }


    private void registerAllListener() {
        if (mXMPPConnection == null || !mXMPPConnection.isConnected() || !mXMPPConnection.isAuthenticated()) {
            LogUtils.i(TAG, "registerAllListener: return");
            return;
        }
        LogUtils.i(TAG, "registerAllListener: 注册监听");
        mDeliveryReceiptManager = DeliveryReceiptManager.getInstanceFor(mXMPPConnection);
        // 自动发送消息回执
        mDeliveryReceiptManager.setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.disabled);// 现在都是由服务端代发回执，客户端就不发回执了

        mReceiptReceivedListener = XmppReceiptImpl.getInstance();
        // 添加消息回执监听
        mDeliveryReceiptManager.addReceiptReceivedListener(mReceiptReceivedListener);
        //消息延迟
        mDeliveryReceiptManager.autoAddDeliveryReceiptRequests();
        //HttpFileUploadManager.getInstanceFor(mXMPPConnection).discoverUploadService()
        //ping监听
        mPingManager = PingManager.getInstanceFor(mXMPPConnection);
        mPingManager.setPingInterval(PING_SECONDS);
        mPingFailedListener = this;
        mPingManager.registerPingFailedListener(mPingFailedListener);

        //消息ACK确认监听
        mStanzaListener = XmppReceiptImpl.getInstance();
        mXMPPConnection.removeAllStanzaAcknowledgedListeners();
        mXMPPConnection.addStanzaAcknowledgedListener(mStanzaListener);

        //重连监听
        mReconnectionManager = ReconnectionManager.getInstanceFor(mXMPPConnection);
        //    mReconnectionManager.enableAutomaticReconnection();
        //   mReconnectionManager.setReconnectionPolicy(ReconnectionManager.ReconnectionPolicy.RANDOM_INCREASING_DELAY);
        //    mReconnectionManager.addReconnectionListener(this);
        //输入状态监听
        mChatStateManager = ChatStateManager.getInstance(mXMPPConnection);
        mChatStateListener = XmppChatImpl.getInstance();
        mChatStateManager.addChatStateListener(mChatStateListener);

        //来新消息监听
        mIncomingChatMessageListener = XmppChatImpl.getInstance();
        mChatManager = ChatManager.getInstanceFor(mXMPPConnection);
        mChatManager.removeIncomingListener(mIncomingChatMessageListener);
        mChatManager.setXhmtlImEnabled(true);
        mChatManager.addIncomingListener(mIncomingChatMessageListener);

        //发送消息监听
        mOutgoingChatMessageListener = XmppChatImpl.getInstance();
        mChatManager.removeOutgoingListener(mOutgoingChatMessageListener);
        mChatManager.addOutgoingListener(mOutgoingChatMessageListener);

        myIqProvider = new MyIqProvider();
        ProviderManager.addIQProvider(Enable.ELEMENT, Enable.NAMESPACE, myIqProvider);
        enableIQRequestHandler = new EnableIQRequestHandler();
        XmppReceiptImpl.receiptThreadStop = false;
        mXMPPConnection.registerIQRequestHandler(enableIQRequestHandler);
        Enable enable = new Enable();
        try {
            mXMPPConnection.sendStanza(enable);
        } catch (Exception e) {
            Log.e(TAG, "send enable failed", e);
        }
    }

    public void removeAllListener() {
        if (mDeliveryReceiptManager != null && mReceiptReceivedListener != null) {
            mDeliveryReceiptManager.removeReceiptReceivedListener(mReceiptReceivedListener);
        }
        if (mPingManager != null && mPingFailedListener != null) {
            mPingManager.unregisterPingFailedListener(mPingFailedListener);
        }
        if (mXMPPConnection != null) {
            mXMPPConnection.removeAllStanzaAcknowledgedListeners();
            mXMPPConnection.removeConnectionListener(this);
        }
        //mReconnectionManager.removeReconnectionListener(this);
        if (mChatStateManager != null && mChatStateListener != null) {
            mChatStateManager.removeChatStateListener(mChatStateListener);
        }
        if (mIncomingChatMessageListener != null && mChatManager != null) {
            mChatManager.removeIncomingListener(mIncomingChatMessageListener);
        }
        if (mOutgoingChatMessageListener != null && mChatManager != null) {
            mChatManager.removeOutgoingListener(mOutgoingChatMessageListener);
        }
        if (mXMPPConnection != null && enableIQRequestHandler != null) {
            mXMPPConnection.unregisterIQRequestHandler(enableIQRequestHandler);
        }
        ProviderManager.removeIQProvider(Enable.ELEMENT, Enable.NAMESPACE);
        XmppReceiptImpl.getInstance().releaseBatchReceipt();
        mDeliveryReceiptManager = null;
        mReceiptReceivedListener = null;
        mPingManager = null;
        mPingFailedListener = null;
        mChatStateManager = null;
        mChatStateListener = null;
        mIncomingChatMessageListener = null;
        mOutgoingChatMessageListener = null;
        mChatManager = null;
        enableIQRequestHandler = null;
        mXMPPConnection = null;
    }

    public String getMoreInfo(boolean isException) {
        String errorInfo = "";
        PackageInfo packageInfo = null;
        try {
            packageInfo = MyApplication.getContext().getPackageManager().getPackageInfo(MyApplication.getContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e1) {
        }
        errorInfo = "应用：" + MyApplication.getContext().getResources().getString(R.string.app_name) + "\n";
        if (packageInfo != null) {
            errorInfo = errorInfo + "应用版本名称：" + packageInfo.versionName + "\n";
        }
        errorInfo = errorInfo + "账号：" + MyApplication.mCoreManager.getSelf().getUserId() + "\n";
        errorInfo = errorInfo + "密码：" + MyApplication.mCoreManager.getSelf().getPassword() + "\n";
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
        if (isException) {
            errorInfo = errorInfo + "连接状态：" + connectInfo + " " + authInfo + "\n";
        }
        errorInfo = errorInfo + "服务器是否开启流管理：" + !MyApplication.IS_OPEN_RECEIPT + "\n";
        errorInfo = errorInfo + "断开时实例：" + xmpptcpConnection + "\n";
        if (xmpptcpConnection != null && xmpptcpConnection.isAuthenticated() && xmpptcpConnection.getUser() != null) {
            errorInfo = errorInfo + "XMPP账号：" + xmpptcpConnection.getUser().toString() + "\n";
        }
        return errorInfo;
    }

    public void reportErrorMessageToServer(String moreInfo, String errorInfo) {
        if (TextUtils.isEmpty(moreInfo)) {
            return;
        }
        String reportInfo = moreInfo;
        if (!TextUtils.isEmpty(errorInfo)) {
            reportInfo = moreInfo + errorInfo;
        }
        LogUtils.e(TAG, "reportErrorMessageToServer reportInfo: " + reportInfo);
        Map<String, String> params = new HashMap<>();
        if (MyApplication.mCoreManager == null
                || MyApplication.mCoreManager.getSelfStatus() == null
                || TextUtils.isEmpty(MyApplication.mCoreManager.getSelfStatus().accessToken)) {
            return;
        }
        params.put("access_token", MyApplication.mCoreManager.getSelfStatus().accessToken);
        params.put("userId", MyApplication.mCoreManager.getSelf().getUserId());
        params.put("logContext", reportInfo);
        if (BuildConfig.LOG_DEBUG) {
            params.put("type", "android_close_debug");
        } else {
            params.put("type", "android_close");
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
}
