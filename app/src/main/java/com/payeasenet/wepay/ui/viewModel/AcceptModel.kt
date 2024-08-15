package com.payeasenet.wepay.ui.viewModel

import android.content.Intent
import android.databinding.ObservableField
import android.view.View
import com.payeasenet.wepay.constant.Constants
import com.payeasenet.wepay.net.api.WepayApi
import com.payeasenet.wepay.net.client.RetrofitClient
import com.payeasenet.wepay.net.rxjava.FailedFlowable
import com.payeasenet.wepay.ui.activity.AcceptActivity
import com.payeasenet.wepay.ui.activity.MainActivity
import com.payeasenet.wepay.ui.activity.OpenWalletActivity
import com.payeasenet.wepay.ui.activity.StartActivity
import com.payeasenet.wepay.utlis.ToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

/**
 * 类 `${CLASS_NAME}`
 *
 *
 * 描述：
 *
 * 创建日期：2017年08月04日

 * @author zhaoyong.chen@ehking.com
 * *
 * @version 1.0
 */
class AcceptModel(private val mActivity: AcceptActivity) {


    val walletId = ObservableField<String>()
    val requestId = ObservableField<String>("")


    fun transfer(view: View) {
        mActivity.showLoadingDialog()
        val api = RetrofitClient.getInstance().create(mActivity.applicationContext, WepayApi::class.java)
        val map = HashMap<String,String>()
        map["version"] = Constants.version
        map["merchantId"] = Constants.merchantId
        //map["requestId"] = requestId.get().toString()
        map["serialNumber"] = requestId.get().toString()
        api.transferConfirmOrder(map)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer{
                mActivity.hideLoadingDialog()
                ToastUtil.showToast(mActivity.applicationContext,"转账确认成功")
            },FailedFlowable(mActivity))
    }
    fun send(view: View) {
        mActivity.showLoadingDialog()
        val api = RetrofitClient.getInstance().create(mActivity.applicationContext, WepayApi::class.java)
        val map = HashMap<String,String>()
        map["version"] = Constants.version
        map["merchantId"] = Constants.merchantId
        var random = ""
       for(index in 1..9){
           random += Random().nextInt(10)
       }
        val requestId2 = requestId.get().toString()+random
        if(requestId2.length>32){
            map["requestId"] = requestId2.substring(0,32)
        }else {
            map["requestId"] = requestId2
        }
        map["serialNumber"] = requestId.get().toString()
        map["walletId"] = walletId.get().toString()
        api.redPacketGrab(map)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer{
                mActivity.hideLoadingDialog()
                ToastUtil.showToast(mActivity.applicationContext,"抢红包成功")
            },FailedFlowable(mActivity))
    }
    fun rejection(view: View) {
        mActivity.showLoadingDialog()
        val api = RetrofitClient.getInstance().create(mActivity.applicationContext, WepayApi::class.java)
        val map = HashMap<String,String>()
        map["version"] = Constants.version
        map["merchantId"] = Constants.merchantId
        map["serialNumber"] = requestId.get().toString()
        //map["requestId"] = requestId.get().toString()
        api.transferRefuseOrder(map)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer{
                mActivity.hideLoadingDialog()
                ToastUtil.showToast(mActivity.applicationContext,"拒收成功")
            },FailedFlowable(mActivity))
    }
}
