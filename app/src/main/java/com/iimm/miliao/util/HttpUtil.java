package com.iimm.miliao.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.WorkerThread;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.iimm.miliao.MyApplication;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by zq on 2017/5/2 0002.
 */

public class HttpUtil {
    public static final String REGEX_URL = "((((ht|f)tp(s?))\\:\\/\\/)([\\w\\-]+)(\\.[\\w\\-]+)+|([\\w\\-]+\\.)+(com|cn|cc|top|xyz|edu|gov|mil|net|org|biz|info|name|museum|us|ca|uk))(\\:\\d+)?(\\/([\\w_\\-\\.~!*\\'()\\;\\:@&=+&$,/?#%]*))*";
    private static final String TAG = "HttpUtil";

    public static boolean isURL(String text) {
        return getURLList(text).size() > 0;
    }

    public static List<String> getURLList(String str) {
        List<String> URLListStr = new ArrayList<>();
        // Pattern pattern = Patterns.WEB_URL;// 系统检测URL的正则
        Pattern pattern = Pattern.compile(REGEX_URL);
        Matcher matcher = pattern.matcher(str);
        StringBuilder stringBuffer = new StringBuilder();
        String s;
        while (matcher.find()) {
            s = matcher.group();
            stringBuffer.append(s);
        }
        if (TextUtils.isEmpty(stringBuffer.toString()))
            return URLListStr;

        String[] split = stringBuffer.toString().split("(http|https)://");
        for (String aSplit : split) {
            Log.e("html", aSplit);
            String mFilterChineseStr = filterChinese(aSplit);
            if (!TextUtils.isEmpty(mFilterChineseStr)) {
                URLListStr.add("http://" + mFilterChineseStr);
            }
        }
        return URLListStr;
    }

    // 过滤掉中文
    private static String filterChinese(String str) {
        String REGEX_CHINESE = "[\u4e00-\u9fa5]";
        Pattern pattern = Pattern.compile(REGEX_CHINESE);
        Matcher matcher = pattern.matcher(str);
        return matcher.replaceAll("");
    }

    /************************************************************/

    // 是否连接了网络
    public static boolean isGprsOrWifiConnected(Context context) {
        ConnectivityManager mConnectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo gprs = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isConnectedGprs = gprs != null && gprs.isConnected();
        boolean isConnectedWifi = wifi != null && wifi.isConnected();
        return isConnectedGprs || isConnectedWifi;
    }

    public static boolean isNetWorkAvailable() {
        Context context = MyApplication.getInstance();
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null) {
            //返回true
            return activeNetworkInfo.isAvailable();
        }
        return false;
    }

    // 是否使用的4G网络
    public static boolean isConnectedGprs(Context context) {
        ConnectivityManager mConnectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo gprs = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isConnectedGprs = gprs != null && gprs.isConnected();
        return isConnectedGprs;
    }

    public static boolean isConnectedWifi(Context context) {
        ConnectivityManager mConnectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isConnectedWifi = wifi != null && wifi.isConnected();
        return isConnectedWifi;
    }

    // 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）

    /**
     * ip == www.google.com 可以用来检测手机是否翻墙
     *
     * @param ip
     * @return
     */
    public static boolean ping(String ip) {
        String result = null;
        try {
            /**
             * -c 次数
             * -w 超时时长
             */
            Process p = Runtime.getRuntime().exec("ping -c 5 -w 2 " + ip);
            int status = p.waitFor();
            if (status == 0) {
                result = "success";
                return true;
            } else {
                result = "failed";
            }
        } catch (IOException e) {
            result = "IOException";
        } catch (InterruptedException e) {
            result = "InterruptedException";
        } finally {
            Log.d("----result---", "result = " + result);
        }
        return false;
    }

    /**
     * 通过204 No Content测试网络连接状态，
     * 比起ping测试，http受代理影响，
     * 204测试可以排除网络需要登录的干扰，
     * 墙内可用测试服务器 https://www.google.cn/generate_204
     * https://captive.v2ex.co/generate_204
     * https://httpbin.org/status/204
     * 墙外可用测试服务器 https://www.google.com/generate_204
     */
    @WorkerThread
    public static boolean test204(String url) {
        Log.i(TAG, "test204() called with: url = [" + url + "]");
        boolean ret;
        try {
            HttpURLConnection connection = (HttpURLConnection) (new URL(url).openConnection());
            // 5秒超时，墙内访问谷歌被墙会一直阻塞直到timeout,
            connection.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(5));
            connection.setReadTimeout((int) TimeUnit.SECONDS.toMillis(5));
            ret = connection.getResponseCode() == 204;
            Log.i(TAG, "test204: " + url + " responseCode = " + connection.getResponseCode());
        } catch (Exception e) {
            Log.e(TAG, "test204 error: " + url, e);
            ret = false;
        }
        return ret;
    }

    @WorkerThread
    public static boolean testGoogle() {
        return test204("https://www.google.com/generate_204");
    }

    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {    // 当前使用2G/3G/4G网络
                try {
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {    // 当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());    // 得到IPV4地址
                return ipAddress;
            }
        } else {
            // 当前无网络连接,请在设置中打开网络
        }
        return null;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }


    public static final int NETWORK_NONE = 0; // 没有网络连接
    public static final int NETWORK_WIFI = 1; // wifi连接
    public static final int NETWORK_2G = 2; // 2G
    public static final int NETWORK_3G = 3; // 3G
    public static final int NETWORK_4G = 4; // 4G
    public static final int NETWORK_MOBILE = 5; // 手机流量

    /**
     * 获取运营商名字
     * getSimOperatorName()就可以直接获取到运营商的名字
     * 也可以使用IMSI获取，getSimOperator()，然后根据返回值判断，例如"46000"为移动
     * IMSI相关链接：http://baike.baidu.com/item/imsi
     */
    public static String getOperatorName(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        // getSimOperatorName就可以直接获取到运营商的名字
        if (android.os.Build.VERSION.SDK_INT < 29) {
            @SuppressLint("MissingPermission") String IMSI = telephonyManager.getSimOperator();//获取SIM卡的IMSI
            if (IMSI.length() > 0) {
                if (IMSI.contains("46000") || IMSI.contains("46002") || IMSI.contains("46007")) {//通过前五位判断连的wifi或者数据流量是移动、联通还是电信
                    //移动
                    return "SimOperator:"+IMSI+"  参考值：移动";
                } else if (IMSI.contains("46001")) {
                    //联通
                    return "SimOperator:"+IMSI+"  参考值：联通";
                } else if (IMSI.contains("46003")) {
                    //电信
                    return "SimOperator:"+IMSI+"  参考值：电信";
                }
            }
        }
        return telephonyManager.getSimOperatorName();
    }

    /**
     * 获取当前网络连接的类型
     *
     * @return int
     */
    public static int getNetworkState(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); // 获取网络服务
        if (null == connManager) { // 为空则认为无网络
            return NETWORK_NONE;
        }
        // 获取网络类型，如果为空，返回无网络
        @SuppressLint("MissingPermission") NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();
        if (activeNetInfo == null || !activeNetInfo.isAvailable()) {
            return NETWORK_NONE;
        }
        // 判断是否为WIFI
        @SuppressLint("MissingPermission") NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (null != wifiInfo) {
            NetworkInfo.State state = wifiInfo.getState();
            if (null != state) {
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    return NETWORK_WIFI;
                }
            }
        }
        // 若不是WIFI，则去判断是2G、3G、4G网
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = telephonyManager.getNetworkType();
        switch (networkType) {
            /*
             GPRS : 2G(2.5) General Packet Radia Service 114kbps
             EDGE : 2G(2.75G) Enhanced Data Rate for GSM Evolution 384kbps
             UMTS : 3G WCDMA 联通3G Universal Mobile Telecommunication System 完整的3G移动通信技术标准
             CDMA : 2G 电信 Code Division Multiple Access 码分多址
             EVDO_0 : 3G (EVDO 全程 CDMA2000 1xEV-DO) Evolution - Data Only (Data Optimized) 153.6kps - 2.4mbps 属于3G
             EVDO_A : 3G 1.8mbps - 3.1mbps 属于3G过渡，3.5G
             1xRTT : 2G CDMA2000 1xRTT (RTT - 无线电传输技术) 144kbps 2G的过渡,
             HSDPA : 3.5G 高速下行分组接入 3.5G WCDMA High Speed Downlink Packet Access 14.4mbps
             HSUPA : 3.5G High Speed Uplink Packet Access 高速上行链路分组接入 1.4 - 5.8 mbps
             HSPA : 3G (分HSDPA,HSUPA) High Speed Packet Access
             IDEN : 2G Integrated Dispatch Enhanced Networks 集成数字增强型网络 （属于2G，来自维基百科）
             EVDO_B : 3G EV-DO Rev.B 14.7Mbps 下行 3.5G
             LTE : 4G Long Term Evolution FDD-LTE 和 TDD-LTE , 3G过渡，升级版 LTE Advanced 才是4G
             EHRPD : 3G CDMA2000向LTE 4G的中间产物 Evolved High Rate Packet Data HRPD的升级
             HSPAP : 3G HSPAP 比 HSDPA 快些
             */
            // 2G网络
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NETWORK_2G;
            // 3G网络
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return NETWORK_3G;
            // 4G网络
            case TelephonyManager.NETWORK_TYPE_LTE:
                return NETWORK_4G;
            default:
                return NETWORK_MOBILE;
        }
    }

    /**
     * 判断网络是否连接
     *
     * @return true/false
     */
    public static boolean isNetConnected(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            @SuppressLint("MissingPermission") NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断是否wifi连接
     *
     * @return true/false
     */
    public static synchronized boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            @SuppressLint("MissingPermission") NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                int networkInfoType = networkInfo.getType();
                if (networkInfoType == ConnectivityManager.TYPE_WIFI || networkInfoType == ConnectivityManager.TYPE_ETHERNET) {
                    return networkInfo.isConnected();
                }
            }
        }
        return false;
    }

    public static void getOutInternetIp(){
        Map<String, String> params = new HashMap<>();
        params.put("access_token", MyApplication.mCoreManager.getSelfStatus().accessToken);
        params.put("userId", MyApplication.mCoreManager.getSelf().getUserId());
        String url = "http://pv.sohu.com/cityjson";
        //String url = "http://ip.chinaz.com/getip.aspx";
        //String url = "http://xx.nstool.netease.com";
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
