package com.payeasenet.wepay.ui.viewModel

import android.databinding.ObservableField
import android.text.TextUtils
import android.view.View
import com.ehking.sdk.wepay.interfaces.WalletPay
import com.ehking.sdk.wepay.net.bean.AuthType
import com.payeasenet.wepay.constant.Constants
import com.payeasenet.wepay.net.api.WepayApi
import com.payeasenet.wepay.net.client.RetrofitClient
import com.payeasenet.wepay.net.rxjava.FailedFlowable
import com.payeasenet.wepay.ui.activity.RedPacketActivity
import com.payeasenet.wepay.utlis.ToastUtil
import com.iimm.miliao.MyApplication
import com.iimm.miliao.ui.base.CoreManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal

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
class RedPacketModel(private val mActivity: RedPacketActivity) {


    val amount = ObservableField<String>("")
    val allAmount = ObservableField<String>("0.00")
    val allAmountText = ObservableField<String>("¥ 0.00")
    val singleAmount = ObservableField<String>("")
    val num = ObservableField<String>("")
    val targetWalletId = ObservableField<String>()
    val isSingle = ObservableField<Boolean>(false)
    val isClick = ObservableField<Boolean>(false)

    fun sure(view: View) {
        val api = RetrofitClient.getInstance().create(mActivity.applicationContext, WepayApi::class.java)
        isClick.set(true)
        if (mActivity.intent.getIntExtra("type", 1) == 1) {
            allAmount.set(amount.get().toString())
        }
        if (!TextUtils.isEmpty(num.get().toString())) {
            if (BigDecimal(num.get().toString()).toDouble() <= 0) {
                isClick.set(false)
                ToastUtil.showToast(mActivity.applicationContext, "红包领取个数至少一个")
                return
            }
        }
        if (!TextUtils.isEmpty(allAmount.get().toString())) {
            if (BigDecimal(allAmount.get().toString()).toDouble() > Constants.money) {
                isClick.set(false)
                ToastUtil.showToast(mActivity.applicationContext, "红包金额最多为1元")
                return
            }
        }
        val packetType = when (mActivity.intent.getIntExtra("type", 1)) {
            1 -> "ONE_TO_ONE"
            2 -> "GROUP_NORMAL"
            3 -> "GROUP_LUCK"
            else -> "ONE_TO_ONE"
        }
        val map = HashMap<String, String>()
//        map["version"] = Constants.version
//        map["requestId"] = ""+System.currentTimeMillis()
//        map["merchantId"] = Constants.merchantId
//        map["walletId"] = Constants.walletId
//        if(!TextUtils.isEmpty(singleAmount.get().toString())) {
//            if(mActivity.intent.getIntExtra("type",1)==3){
//                map["amount"] =
//                    "" + (BigDecimal(singleAmount.get().toString())* BigDecimal(100)).toInt()
//            }else {
//                map["singleAmount"] =
//                    "" + (BigDecimal(singleAmount.get().toString())* BigDecimal(100)).toInt()
//            }
//        }
//        if(mActivity.intent.getIntExtra("type",1)==1) {
//            map["targetWalletId"] = targetWalletId.get().toString()
//        }
//        if(!TextUtils.isEmpty(num.get().toString())) {
//            map["packetCount"] = num.get().toString()
//        }
//        map["currency"] = Constants.currency
//        if(!TextUtils.isEmpty(amount.get().toString())) {
//            map["singleAmount"] = "" + (BigDecimal(amount.get().toString()) * BigDecimal(100)).toInt()
//        }
//        if(mActivity.intent.getIntExtra("type",1)==1){
//            map["packetCount"] = "1"
//        }
//        map["packetType"] = packetType
//        map["notifyUrl"] = Constants.notifyUrl

        map["access_token"] =CoreManager.requireSelfStatus(MyApplication.getInstance()).accessToken
        map["packetType"] =packetType
        map["greetings"] ="祝福语"
        map["singleAmount"] ="0.1"
        map["targetUserId"] ="10000003"
        map["packetCount"] ="1"
        val walletPay = WalletPay.getInstance()
        walletPay.init(mActivity)

        mActivity.showLoadingDialog()
        //红包预下单
        api.redPacketCHAT(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    mActivity.hideLoadingDialog()
                    isClick.set(false)
                    //调起SDK的发红包
                    walletPay.evoke(it.data.merchantId, it.data.walletId, it.data.token, AuthType.REDPACKET.name)
                }, object : FailedFlowable(mActivity) {
                    override fun accept(e: Throwable) {
                        super.accept(e)
                        isClick.set(false)
                    }
                })

    }


}
