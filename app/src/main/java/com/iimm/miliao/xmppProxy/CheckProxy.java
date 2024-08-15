package com.iimm.miliao.xmppProxy;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.iimm.miliao.MyApplication;
import com.iimm.miliao.bean.NodeInfo;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.rsa.RSAUtils;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.proxy.ProxyInfo;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class CheckProxy implements ConnectionListener {
    private static XMPPTCPConnection connection;
    private static int postion = 0;
    private static int slectorPostion = 0;
    private static boolean isDedug = true;
    private static CheckProxy checkProxy;
    private String TGA = "CheckProxy";

    public static void setDedug(boolean dedug) {
        isDedug = dedug;
    }

    List<NodeInfo> list;
    Long startTime;
    Long endTime;
    int count;//记录循环次数
    Context context;
    ResultListener resultListener;

    //    boolean isRuning;
    public static CheckProxy getInstance() {
        if (checkProxy == null) {
            checkProxy = new CheckProxy();
        }
        return checkProxy;
    }

    public void start(ResultListener resultListener, Context context) {
//        if(isRuning){
//            return;
//        }
        this.context = context;
        this.resultListener = resultListener;
        List<NodeInfo> nodesInfoList = CoreManager.requireConfig(MyApplication.getContext()).nodesInfoList;
        list = new ArrayList<>();

        if (nodesInfoList != null) {
            Log.e(TGA, "start   :" + nodesInfoList.size());
            list.addAll(nodesInfoList);
        } else {
            if (resultListener != null) {
                resultListener.result();
            }
            return;
        }

        postion = PreferenceUtils.getInt(context, Constants.SELCTORPROXY, 0);
        if (postion >= list.size()) {
            postion = 0;
        }
        count = 0;
//        isRuning =true;
        proxyTest();
    }

    public void proxyTest() {
        if (count >= list.size()) {
            if (resultListener != null) {
                resultListener.result();
            }
//            isRuning = false;
            return;
        }
        count = count + 1;
        try {
            if (isDedug) {
                Log.e(TGA, "start   :");
            }

            startTime = System.currentTimeMillis();
            InetAddress address = null;
            ProxyInfo proxyInfo = null;
            if (list.get(postion).getIsSocks() == 1) {
                proxyInfo = new ProxyInfo(ProxyInfo.ProxyType.SOCKS5, list.get(postion).getHostSocks()
                        , list.get(postion).getPostSocks()
                        , list.get(postion).getUserSocks()
                        , RSAUtils.decryptPublicWithBase64(list.get(postion).getPassSocks()));
            }

            String mXmppHost;

            if (!TextUtils.isEmpty(list.get(postion).getNodeIp())) {
                mXmppHost = list.get(postion).getNodeIp();
            } else {
                isCheckeSuccess();
                return;
            }
            int mXmppPort;
            if (!TextUtils.isEmpty(list.get(postion).getNodePort())) {
                mXmppPort = Integer.parseInt(list.get(postion).getNodePort());
            } else {
                isCheckeSuccess();
                return;
            }
            String mXmppDomain;
            if (!TextUtils.isEmpty(list.get(postion).getRealmName())) {
                mXmppDomain = list.get(postion).getRealmName();
            } else {
                isCheckeSuccess();
                return;
            }
            DomainBareJid mDomainBareJid = JidCreate.domainBareFrom(mXmppDomain);
            address = InetAddress.getByName(mXmppHost);
            XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword(MyApplication.getXmppAccount(), MyApplication.getXmppPassword())
                    .setHostAddress(address) // 服务器地址
                    .setPort(mXmppPort) // 服务器端口
                    .setXmppDomain(mDomainBareJid)
                    .setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled) // 是否开启安全模式
                    .setCompressionEnabled(Constants.SUPPORT_COMPRESSION)
                    .setResource(Constants.SUPPORT_YOU_JOB ? MyApplication.MULTI_RESOURCE_PROVISIONAL : MyApplication.MULTI_RESOURCE)
                    .setSendPresence(true);
            if (list.get(postion).getIsSocks() == 1 && proxyInfo != null) {
                config.setProxyInfo(proxyInfo);
            }

            config.enableDefaultDebugger();
            XMPPTCPConnectionConfiguration build = config.build();
            connection = new XMPPTCPConnection(build);//根据配置生成一个连接
            connection.setReplyTimeout(5000);
            connection.addConnectionListener(CheckProxy.this);
            connection.connect();//连接到服务器
        } catch (Exception e) {
            if (isDedug) {
                Log.e(TGA, "Exception   :" + e.getMessage() + "");
            }
            if (postion < list.size()) {
                this.list.get(postion).setAvailable(false);
            }
            isCheckeSuccess();
        }
    }

    public void isCheckeSuccess() {
        postion++;
        if (postion >= list.size()) {
            postion = 0;
        }
        proxyTest();


    }

    @Override
    public void connected(XMPPConnection connection) {
        if (isDedug) {
            Log.e(TGA, "connected   :");
        }
        endTime = System.currentTimeMillis();
        list.get(postion).setDelayTime(endTime - startTime);
        XMPPTCPConnection connection1 = (XMPPTCPConnection) connection;
        if (connection1.isConnected()) {
            connection1.disconnect();
            PreferenceUtils.putInt(context, Constants.SELCTORPROXY, postion);
            PreferenceUtils.putBoolean(context, Constants.SELCTORPROXY + postion, true);
            if (resultListener != null) {
                resultListener.result();
//                    isRuning = false;
            }
        }
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {

    }

    @Override
    public void connectionClosed() {
        if (isDedug) {
            Log.e(TGA, "connectionClosed   :");
        }
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        if (isDedug) {
            Log.e(TGA, "connectionClosedOnError   :" + postion + "    " + e.getMessage());
        }
        if (connection != null) {
            if (connection.isConnected()) {
                connection.disconnect();
            }
        }
        this.list.get(postion).setAvailable(false);
        isCheckeSuccess();
    }

    public interface ResultListener {
        void result();
    }
}
