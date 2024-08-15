package com.payeasenet.wepay.net.client

import android.content.Context
import com.ehking.sdk.wepay.utlis.LogUtil
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.payeasenet.wepay.constant.Constants
import com.payeasenet.wepay.net.exception.UnknowException
import com.payeasenet.wepay.net.factory.CustomGsonConverterFactory
import com.iimm.miliao.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import retrofit2.Retrofit
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


/**
 * Retrofit
 *
 * @Author: [wenbin.zhou@ehking.com]
 * @CreateDate: [2017/03/21 10:54]
 * @Version: [1.0]
 */
class RetrofitClient private constructor() {
    private val CERT_ALIAS: String = "test"
    private lateinit var mContext: Context
    private val mRetrofit: Retrofit
    // private val certificates = intArrayOf(R.raw.encrypt_server,R.raw.encrypt_server,R.raw.encrypt_server, R.raw.encrypt_server)

    //初始化http拦截器
    private val mHttpInterceptor = Interceptor { chain ->
        //在request中设置一些参数
        var request = getRequest(chain.request())
        val buffer = Buffer()
        if (request.body() != null) {
            request.body()!!.writeTo(buffer)
        }
        //对respones做解密
        getResponse(chain.proceed(request))
    }


    init {
        mRetrofit = Retrofit.Builder()
                .client(buildOkHttpClient())
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(CustomGsonConverterFactory.create())
                .callbackExecutor(Executors.newCachedThreadPool())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

    }


    private fun createRetrofitInstance(context: Context): Retrofit {
        return Retrofit.Builder()
                .client(buildOkHttpClient(context))
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(CustomGsonConverterFactory.create())
                .callbackExecutor(Executors.newCachedThreadPool())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }


    //获取OkHttpClient的实例
    private fun buildOkHttpClient(): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()
        clientBuilder.connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(mHttpInterceptor)
                .addInterceptor(buildLogInterceptor())
        return clientBuilder.build()
    }

    //获取OkHttpClient的实例
    @Suppress("DEPRECATED_IDENTITY_EQUALS", "DEPRECATION")
    private fun buildOkHttpClient(context: Context): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()
        clientBuilder.connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(mHttpInterceptor)
                .addInterceptor(CommonInterceptor())
//                .addInterceptor(AddCookiesInterceptor(context))
                //.addInterceptor(ReceivedCookiesInterceptor(context))
                .addInterceptor(buildLogInterceptor())

        return clientBuilder.build()
    }

    //获取日志拦截器
    private fun buildLogInterceptor(): Interceptor {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = if (BuildConfig.DEBUG)
            HttpLoggingInterceptor.Level.BODY
        else
            HttpLoggingInterceptor.Level.NONE
        return loggingInterceptor
    }

    //修改请求头参数信息
    @Throws(UnsupportedEncodingException::class)
    private fun getRequest(original: Request): Request {
        val builder = original.newBuilder()
        /* builder.addHeader("merchantId",Constants.merchantId)
         builder.addHeader("walletId",Constants.walletId)
         builder.addHeader("deviceNo",Constants.deviceNo)
         builder.addHeader("requestId",Constants.requestId)
         builder.addHeader("Accept-Language","zh_CN")*/
        //builder.addHeader("Content-Type", "application/vnd.ehking-v1.0+json")
        builder.method(original.method(), original.body())
        return builder.build()
    }

    //对响应数据解密以及异常数据处理
    @Throws(IOException::class)
    private fun getResponse(response: Response): Response {
        var response1 = response

        val pathSegments = response.request().url().pathSegments()
        if (pathSegments.contains("error") && pathSegments.contains("page")) {
            LogUtil.e("error page" + response.request().url().url().toString())
            //throw new ResultException(response.code(), "error page");
            throw UnknowException(String.format("<-- http %d message: %s", response.code(), "error page"))
        }



        return response1
    }

    fun <T> create(api: Class<T>): T {
        return mRetrofit.create(api)
    }

    fun <T> create(context: Context, api: Class<T>): T {
        mContext = context
        return createRetrofitInstance(context).create(api)
    }

    companion object {

        private var INSTANCE: RetrofitClient? = null

        //由于okhttp header 中的 value 不支持 null, \n 和 中文这样的特殊字符,所以这里
        //会首先替换 \n ,然后使用 okhttp 的校验方式,校验不通过的话,就返回 encode 后的字符串


        fun getInstance(): RetrofitClient {
            if (INSTANCE == null) {
                synchronized(lock = RetrofitClient::class.java) {
                    INSTANCE = RetrofitClient()
                }
            }
            return this.INSTANCE!!
        }

    }


}
