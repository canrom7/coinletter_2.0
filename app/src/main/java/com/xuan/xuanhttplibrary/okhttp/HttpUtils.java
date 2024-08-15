package com.xuan.xuanhttplibrary.okhttp;

import com.iimm.miliao.BuildConfig;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.HttpsUtils;
import com.xuan.xuanhttplibrary.okhttp.builder.GetBuilder;
import com.xuan.xuanhttplibrary.okhttp.builder.GetNoSecretBuilder;
import com.xuan.xuanhttplibrary.okhttp.builder.PostBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * @author liuxan
 * @time 2017/3/29 23:36
 * @des
 */

public class HttpUtils {

    public static String TAG = "HTTP";
    private static final int CONNECT_TIMEOUT = 30;
    private static final int READ_TIMEOUT = 30;
    private static final int WRITE_TIMEOUT = 30;

    private static HttpUtils instance = new HttpUtils();
    private OkHttpClient mOkHttpClient;

    private HttpUtils() {
    }

    public static HttpUtils getInstance() {
        return instance;
    }

    public static PostBuilder post() {
        return new PostBuilder();
    }

    public static GetBuilder get() {
        return new GetBuilder();
    }

    public static GetNoSecretBuilder getNoSecret() {
        return new GetNoSecretBuilder();
    }

    public OkHttpClient getOkHttpClient() {

        if (mOkHttpClient == null) {
            try {
                HttpsUtils.SSLParams sslParams=null;
                if (Constants.IS_OPEN_HTTPS) {
                    InputStream certificates = MyApplication.getContext().getResources().openRawResource(R.raw.server);
                    InputStream pkcs12File = MyApplication.getContext().getAssets().open("client.p12");
                    String password = Constants.HTTPS_P12_PASSWORD;
                    InputStream bksFile = pkcs12ToBks(pkcs12File, password);
                    sslParams = HttpsUtils.getSslSocketFactory(new InputStream[]{certificates}, bksFile, password);
                }
                OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                        .retryOnConnectionFailure(true)
                        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS) //连接超时
                        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS) //读取超时
                        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS) //写超时
                        ;
                if (BuildConfig.LOG_DEBUG) {
                    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                    okHttpClientBuilder.addInterceptor(loggingInterceptor);
                }
                if (sslParams==null || sslParams.trustManager==null || sslParams.sSLSocketFactory == null || !Constants.IS_OPEN_HTTPS) {
                    mOkHttpClient = okHttpClientBuilder.build();
                } else {
                    okHttpClientBuilder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
//                    okHttpClientBuilder.hostnameVerifier(new HostnameVerifier() {
//                        @Override
//                        public boolean verify(String hostname, SSLSession session) {
//                            if (BuildConfig.CONFIG_HOST.contains(hostname)) {
//                                return true;
//                            }else{
//                                return false;
//                            }
//                        }
//                    });
                    mOkHttpClient = okHttpClientBuilder.build();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return mOkHttpClient;

    }

    /**
     * 根据Tag取消请求
     */
    public void cancelTag(Object tag) {
        if (tag == null) return;
        for (Call call : getOkHttpClient().dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : getOkHttpClient().dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }


    protected InputStream pkcs12ToBks(InputStream pkcs12Stream, String pkcs12Password) {
        final char[] password = pkcs12Password.toCharArray();
        try {
            KeyStore pkcs12 = KeyStore.getInstance("PKCS12");
            pkcs12.load(pkcs12Stream, password);
            Enumeration<String> aliases = pkcs12.aliases();
            String alias;
            if (aliases.hasMoreElements()) {
                alias = aliases.nextElement();
            } else {
                throw new Exception("pkcs12 file not contain a alias");
            }
            Certificate certificate = pkcs12.getCertificate(alias);
            final Key key = pkcs12.getKey(alias, password);
            KeyStore bks = KeyStore.getInstance("BKS");
            bks.load(null, password);
            bks.setKeyEntry(alias, key, password, new Certificate[]{certificate});
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bks.store(out, password);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
