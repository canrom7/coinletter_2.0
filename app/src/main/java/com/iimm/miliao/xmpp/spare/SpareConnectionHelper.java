package com.iimm.miliao.xmpp.spare;

import android.text.TextUtils;
import android.util.Log;

import com.here.oksse.OkSse;
import com.here.oksse.ServerSentEvent;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.HttpUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SpareConnectionHelper implements ServerSentEvent.Listener {
    private String HostUrl = CoreManager.requireConfig(MyApplication.getContext()).second_channel_url;
    public static String HISTORY_RECORD = CoreManager.requireConfig(MyApplication.getContext()).second_channel_url+":8095/messages/sync";
    private String Host = HostUrl + ":8095/messages/feed?device=youjob&token=";
    private String HostOut = HostUrl + ":8095/messages/feed/off?device=youjob&token=";
    private static final String TAG = "SpareConnectionHelper";
    private static SpareConnectionHelper mSpareConnectionHelper;
    private static Thread mThread;
    private static final int ON_LINE = 1;
    private static final int OFF_LINE = -1;
    private static final int CONNECTIONING = 0;
    private int onLineStatus = OFF_LINE;
    private static final int DETECTION_INTERVAL = 2000; //检测间隔时间
    private ServerSentEvent sse;
    private SpareChatEvent mSpareChatEvent;
    private boolean mLocalDeviceNetworkOk = false;
    private static int i = 0;

    private SpareConnectionHelper() {

    }

    private void setOnLineStatus(int status) {
        synchronized (SpareConnectionHelper.class) {
            this.onLineStatus = status;
        }
    }


    public static SpareConnectionHelper getInstance() {
        synchronized (SpareConnectionHelper.class) {
            if (mSpareConnectionHelper == null) {
                mSpareConnectionHelper = new SpareConnectionHelper();
            }
            return mSpareConnectionHelper;
        }
    }

    public void setSpareChatEvent(SpareChatEvent spareChatEvent) {
        this.mSpareChatEvent = spareChatEvent;
    }


    protected static void reStartCheckTread() {
        if (mThread != null && !mThread.isInterrupted()) {
            return;
        }
        mThread = new Thread("备用连接检测线程--" + (i++)) {
            @Override
            public void run() {
                super.run();
                try {
                    while (mThread != null && !mThread.isInterrupted()) {
                        Log.d(TAG, "备用连接检测线程正在运行...当前线程=" + Thread.currentThread().getName());
                        if (mSpareConnectionHelper.isOnLine()) {
                            Log.d(TAG, "备用连接连接中...");
//                        } else if (mSpareConnectionHelper.mLocalDeviceNetworkOk) {
                        } else if (HttpUtil.isNetWorkAvailable()) {

                            Log.d(TAG, "连接已断开...");
                            SpareConnectionHelper.getInstance().connect();
                        } else {
                            Log.e(TAG, "备用连接无法重新连接，本地无网络...");
                        }
                        Thread.sleep(DETECTION_INTERVAL);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
        mThread.start();
    }

    public boolean isOnLine() {
        return onLineStatus == ON_LINE;
    }

    public synchronized void connect() {
        if (onLineStatus == OFF_LINE) {
            Request request = new Request.Builder().url(Host + CoreManager.requireSelfStatus(MyApplication.getContext()).accessToken).build();
            OkSse mOkSse = new OkSse();
            if (sse != null) {
                sse.close();
            }
            setOnLineStatus(CONNECTIONING);
            sse = mOkSse.newServerSentEvent(request, this);
            reStartCheckTread();
            Log.i(TAG, "开始连接...");
        } else {
            Log.i(TAG, "正在连接...");
        }
    }


    public void disconnection() {
        try {
            if (mThread != null) {
                mThread.interrupt();
                mThread = null;
            }
            if (sse != null) {
                sse.close();
                sse = null;
                Log.i(TAG, "关闭连接...");
            }
            logout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void logout() {
        Request request = new Request.Builder().url(HostOut + CoreManager.requireSelfStatus(MyApplication.getContext()).accessToken).build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "备用连接退出异常....");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int code = response.code();
                if (code == 200) {
                    Log.i(TAG, "备用连接退出成功....");
                } else {
                    Log.e(TAG, "备用连接退出异常..code:" + code);
                }
            }
        });
    }

    @Override
    public void onOpen(ServerSentEvent sse, Response response) {
        Log.i(TAG, "onOpen");
        setOnLineStatus(ON_LINE);
        if (mSpareChatEvent != null) {
            mSpareChatEvent.onOpen();
        }
    }

    @Override
    public void onMessage(ServerSentEvent sse, String id, String event, String message) {
        Log.i(TAG, "onMessage:" + message);
        if (message.equals("ok")) {
            setOnLineStatus(ON_LINE);
        } else {
            if (mSpareChatEvent != null) {
                mSpareChatEvent.onMessage(id, event, message);
            }
        }
    }

    @Override
    public void onComment(ServerSentEvent sse, String comment) {
        Log.i(TAG, "onComment:");

    }

    @Override
    public boolean onRetryTime(ServerSentEvent sse, long milliseconds) {
        Log.i(TAG, "onRetryTime");
        return false;
    }

    @Override
    public boolean onRetryError(ServerSentEvent sse, Throwable throwable, Response response) {
        Log.i(TAG, "onRetryError");
        setOnLineStatus(CONNECTIONING);
        return true;
    }

    @Override
    public void onClosed(ServerSentEvent sse) {
        Log.i(TAG, "onClosed");
        setOnLineStatus(OFF_LINE);
        if (mSpareChatEvent != null) {
            mSpareChatEvent.onClosed();
        }
    }

    @Override
    public Request onPreRetry(ServerSentEvent sse, Request originalRequest) {
        Log.i(TAG, "onPreRetry");
        setOnLineStatus(CONNECTIONING);
        return null;
    }

    public void setNetWorkStatus(boolean localDeviceNetworkOk) {
        this.mLocalDeviceNetworkOk = localDeviceNetworkOk;
        if (!localDeviceNetworkOk) {
            setOnLineStatus(OFF_LINE);
        }
    }

    public void init(String spareMsgServerConfigHost) {
        if (TextUtils.isEmpty(spareMsgServerConfigHost)) {
            return;
        }
        this.Host = spareMsgServerConfigHost;
    }
}
