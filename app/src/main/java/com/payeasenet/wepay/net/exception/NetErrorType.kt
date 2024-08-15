package com.payeasenet.wepay.net.exception


import android.content.Context
import android.net.ConnectivityManager
import android.support.annotation.StringRes
import com.ehking.sdk.wepay.R
import com.iimm.miliao.BuildConfig


/**
 * @Author: [wenbin.zhou@ehking.com]
 * @CreateDate: [2017/03/15 18:13]
 * @Version: [1.0]
 */

object NetErrorType {

    enum class ErrorType private constructor(@param:StringRes val id: Int) {

        TYPE_ERROR_TIME_OUT(R.string.connection_time_out),
        TYPE_ERROR_UNKNOW_HOST(R.string.unknown_host),
        TYPE_ERROR_CONNECT(R.string.no_network),
        TYPE_ERROR_CONVERSION(R.string.data_handle_fail),
        TYPE_ERROR_UNKNOW(R.string.unknown_err)
    }


    fun getErrorType(t: Throwable, context: Context): String {

        netType(context)
        if (BuildConfig.DEBUG) {
            t.printStackTrace()
        }
        var errorType: ErrorType?
        errorType = when (t) {
            is java.net.SocketTimeoutException -> ErrorType.TYPE_ERROR_TIME_OUT
            is java.net.UnknownHostException -> ErrorType.TYPE_ERROR_UNKNOW_HOST
            is java.net.ConnectException -> ErrorType.TYPE_ERROR_CONNECT
            else -> ErrorType.TYPE_ERROR_UNKNOW
        }
        return context.getString(errorType.id)
    }

    @Suppress("DEPRECATION")
    private fun netType(context: Context): String? {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = cm.activeNetworkInfo
            var typeName = info.typeName.toLowerCase() // WIFI/MOBILE
            if (typeName.equals("wifi", ignoreCase = true)) {
            } else {
                typeName = info.extraInfo.toLowerCase()
                // 3gnet/3gwap/uninet/uniwap/cmnet/cmwap/ctnet/ctwap
            }
            //LogUtil.e(String.format("当前网络类型为--> %s", typeName))
            typeName
        } catch (e: Exception) {
            null
        }

    }
}
