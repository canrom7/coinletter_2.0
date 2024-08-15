package com.payeasenet.wepay.net.client;

import com.iimm.miliao.AppConfig;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.Md5Util;
import com.iimm.miliao.util.TimeUtils;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * MrLiu253@163.com
 *
 * @time 2020-01-06
 */
public class CommonInterceptor implements Interceptor {
    private final String time;
    private final String secret;

    public CommonInterceptor() {
        time = String.valueOf(TimeUtils.time_current_time());
        secret = Md5Util.toMD5(AppConfig.apiKey + time + CoreManager.requireSelf(MyApplication.getInstance()).getUserId() + CoreManager.requireSelfStatus(MyApplication.getInstance()).accessToken);
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request oldRequest = chain.request();

        // 添加新的参数
        HttpUrl.Builder authorizedUrlBuilder = oldRequest.url()
                .newBuilder()
                .scheme(oldRequest.url().scheme())
                .host(oldRequest.url().host())
                .addQueryParameter("time", time)
                .addQueryParameter("secret", secret);

        // 新的请求
        Request newRequest = oldRequest.newBuilder()
                .method(oldRequest.method(), oldRequest.body())
                .url(authorizedUrlBuilder.build())
                .build();

        return chain.proceed(newRequest);
    }
}
