package com.payeasenet.wepay.net.rxjava

import android.text.TextUtils
import com.ehking.sdk.wepay.net.exception.NetErrorType
import com.ehking.sdk.wepay.net.exception.ResultException
import com.ehking.sdk.wepay.ui.base.BaseActivity
import com.ehking.sdk.wepay.utlis.LogUtil
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException
import com.payeasenet.wepay.utlis.ToastUtil
import com.iimm.miliao.BuildConfig
import io.reactivex.functions.Consumer
import org.json.JSONObject
import retrofit2.Response

/**
 * 网络异常处理
 * @author: wenbin.zhou@ehking.com
 * @version: 1.0
 */
open class FailedFlowable(private val mActivity: BaseActivity) : Consumer<Throwable> {

    protected var mMsg: String? = null

    @Throws(Exception::class)
    override fun accept(e: Throwable) {

        if (BuildConfig.DEBUG) {//debug模式下显示异常信息
            e.printStackTrace()
        }
        onFlowError()
        mActivity.hideLoadingDialog()//隐藏进度条
        when (e) {
            is HttpException -> //如果是HttpException
                handleHttpException(e)
            is ResultException -> //如果是结果异常
                handleResultException(e)
            else -> mMsg = NetErrorType.getErrorType(e, mActivity)
        }
        LogUtil.e(String.format("<-- exception %s", e.message))
        showMsg(mMsg)//展示错误消息
    }
    protected open fun onFlowError () {
    }

    /**
     * 这个方法主要是处理http请求异常。
     *
     *
     * <br>如果http请求状态为500时响应结果为：
     * <br>`{"code":"exception.xxx.xxxxx", message:"xxxxxx"}`
     * <br>json中的code就表示server端抛出的异常代码；
     * <br>json中的message是错误消息。
     *
     * <br>当遇到类似情况是，在网络请求处理中会抛出[HttpException]这个异常，然后就会对这个异常处理；
     * <br>如果需要登录，就跳转到登录页面，需要显示提示信息，从响应数据中提取message。

     * @param err 异常实例化对象
     * @return 没有返回值
     * @see getMessage
     * @see NetErrorType.getErrorType
     * @see NetErrorType
     * @see HttpException
     */
    protected open fun handleHttpException(err: HttpException) {

        val response = err.response()
        when (err.code()) {
            1000 ->

                mMsg = getMessage(response)


            500 ->

                mMsg ="网络繁忙,请稍后重试"
            404 ->

                mMsg ="网络繁忙,请稍后重试"
            501 ->

                mMsg = getMessage(response)
            400 ->

                mMsg = getMessage(response)
            402 ->

                mMsg = getMessage(response)
            403 ->
                mMsg = getMessage(response)
            else ->

                mMsg = NetErrorType.getErrorType(err, mActivity)
        }

        LogUtil.e(String.format("<-- %d", err.code()))
    }

    /**
     * 这个方法是用来在界面提示错误消息的。

     * @param msg 错误消息
     * *
     * @return 没有返回值
     */
    private fun showMsg(msg: String?) {
        if (!TextUtils.isEmpty(msg)) {
            LogUtil.e(String.format("Message: %s", msg))
           ToastUtil.showToast(mActivity.applicationContext,msg!!)
        }
    }

    /**
     * 这个方法主要是处理结果异常。
     *
     *
     * <br>如果http请求响应结果为：
     * <br><code>{"code":100, message:"商户未登录"}</code>
     * <br>json中的code就表示接口请求业务上的状态码，100或者101表示未登录；
     * <br>json中的message是错误消息。
     * <br>当遇到类似情况是，在网络请求处理中会抛出[ResultException]这个异常，然后就会对这个异常处理；
     * <br>如果需要登录，就跳转到登录页面，需要显示提示信息，就返回这个`err.getMessage()`。

     * @param err 异常实例化对象，参考[ResultException]
     *
     * @return 没有返回值
     */
    protected open fun handleResultException(err: ResultException) {

        when (err.code) {


            else -> mMsg = err.message
        }
        LogUtil.e(String.format("<-- %s", err.code))
    }

    //Get message from responese body
    private fun getMessage(response: Response<*>): String? {

        var result: String? = null
        try {
            val obj = JSONObject(response.errorBody()?.string())
            result = obj.getString("message")
        } catch (e: Exception) {
            LogUtil.e(String.format("Message: %s", e.message))
        }

        return result
    }

}
