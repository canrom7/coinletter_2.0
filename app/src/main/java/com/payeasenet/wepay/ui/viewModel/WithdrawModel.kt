package com.payeasenet.wepay.ui.viewModel

import android.content.Intent
import android.databinding.ObservableField
import android.text.TextUtils
import android.view.View
import com.ehking.sdk.wepay.interfaces.WalletPay
import com.ehking.sdk.wepay.net.bean.AuthType
import com.payeasenet.wepay.constant.Constants
import com.payeasenet.wepay.net.api.WepayApi
import com.payeasenet.wepay.net.client.RetrofitClient
import com.payeasenet.wepay.net.rxjava.FailedFlowable
import com.payeasenet.wepay.ui.activity.EndingActivity
import com.payeasenet.wepay.ui.activity.WithdrawActivity
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
class WithdrawModel(private val mActivity: WithdrawActivity) :BaseModel(){


    val amount = ObservableField<String>("")
    val arrivalAmountText = ObservableField<String>("下一步")
    val arrivalAmount = ObservableField<String>("")
    val fee = ObservableField<String>("")
    val isClick = ObservableField<Boolean>(false)
    var requestId: String = ""
    fun all(view: View) {


        mActivity.showLoadingDialog()
        val api = RetrofitClient.getInstance().create(mActivity.applicationContext, WepayApi::class.java)
        val map = HashMap<String, String>()
//        map["version"] = Constants.version
//        map["merchantId"] = Constants.merchantId
        map["access_token"] = CoreManager.getSelfStatus(mActivity.applicationContext)!!.accessToken
        addSubscribe(api.walletQueryCHAT(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    mActivity.hideLoadingDialog()
                    if (it.resultCode == 1) {
                        if (it.data != null) {
                            val allAmount = BigDecimal(it.data.balance).toDouble()
                            amount.set("" + allAmount)
                        }
                    } else {
                        ToastUtil.showToast(mActivity.applicationContext, it.resultMsg)
                    }

                }, FailedFlowable(mActivity)))
    }

    fun sure(view: View) {

        if (BigDecimal(mActivity.intent.getStringExtra(Constants.amount)).compareTo(BigDecimal((amount.get().toString()))) === -1) {
            ToastUtil.showToast(mActivity.applicationContext, "提现金额超限")
            return
        }
        if (BigDecimal((amount.get().toString())).compareTo(BigDecimal((CoreManager.requireConfig(MyApplication.getInstance()).weiBaoMaxTransferAmount))) === 1) {
            ToastUtil.showToast(mActivity.applicationContext, "最大提现金额"+CoreManager.requireConfig(MyApplication.getInstance()).weiBaoMaxTransferAmount)
            return
        }
        if (BigDecimal((amount.get().toString())).compareTo(BigDecimal(CoreManager.requireConfig(MyApplication.getInstance()).weiBaoMinTransferAmount)) === -1) {
            ToastUtil.showToast(mActivity.applicationContext, "最少提现金额"+CoreManager.requireConfig(MyApplication.getInstance()).weiBaoMinTransferAmount)
            return
        }
        val api = RetrofitClient.getInstance().create(mActivity.applicationContext, WepayApi::class.java)
        isClick.set(true)

        val map = HashMap<String, String>()
        map["access_token"] = CoreManager.getSelfStatus(mActivity.applicationContext)!!.accessToken
        map["amount"] = "" + BigDecimal(amount.get().toString())
//        map["version"] = Constants.version
//        map["requestId"] = ""+System.currentTimeMillis()
//        map["merchantId"] = Constants.merchantId
//        map["walletId"] = Constants.walletId
//        map["currency"] = Constants.currency
//        map["amount"] = ""+ (BigDecimal(amount.get().toString())* BigDecimal(100)).toInt()
//        map["arrivalAmount"] = arrivalAmount.get().toString()
//        map["notifyUrl"] = Constants.notifyUrl
        val walletPay = WalletPay.getInstance()
        walletPay.init(mActivity)
        /*       walletPay.walletPayCallback = object :WalletPay.WalletPayCallback {
                   override fun callback(source: String?, status: String?, errorMessage: String?) {
                       when (status) {
                           "SUCCESS", "PROCESS" -> {
                               //请求服务端提现查询接口 确定充值状态
                               api.walletWithholdingQuery(map)
                                   .subscribeOn(Schedulers.io())
                                   .observeOn(AndroidSchedulers.mainThread())
                                   .subscribe(Consumer {
                                       mActivity.hideLoadingDialog()
                                       mActivity.startActivity(Intent(mActivity.applicationContext,EndingActivity::class.java)
                                           .putExtra(Constants.type,2)
                                           .putExtra(Constants.amount,""+BigDecimal(it.amount).toDouble()/100))
                                       mActivity.finish()
                                   },object :FailedFlowable(mActivity){
                                       override fun accept(e: Throwable) {
                                           super.accept(e)
                                           isClick.set(false)
                                       }
                                   })
                           }
                           else -> {
                               isClick.set(false)
                               ToastUtil.showToast(mActivity.applicationContext,""+errorMessage)
                           }
                       }

                   }

               }*/
        walletPay.walletPayCallback = object : WalletPay.WalletPayCallback {
            override fun callback(source: String?, status: String?, errorMessage: String?) {
                when (status) {
                    "SUCCESS", "PROCESS" -> {
                        //请求服务端提现查询结果 确定提现状态
                        if (!TextUtils.isEmpty(requestId)) {
                            val map = HashMap<String, String>()
                            map["access_token"] = CoreManager.getSelfStatus(mActivity.applicationContext)!!.accessToken
                            map["requestId"] = requestId
                            addSubscribe(api.walletWithholdingQueryCHAT(map)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(Consumer {
                                        mActivity.hideLoadingDialog()
                                       if (it.resultCode==1){
                                           if (it.data!=null){
                                               mActivity.startActivity(Intent(mActivity.applicationContext, EndingActivity::class.java)
                                                       .putExtra(Constants.type, 2)
//                                                       .putExtra(Constants.amount, "" + BigDecimal(it.data.amount).toDouble() / 100))
                                                       .putExtra(Constants.amount, it.data.amount))
                                               mActivity.finish()
                                           }else{
                                               ToastUtil.showToast(mActivity.applicationContext,"获取结果错误");
                                           }
                                       }else{
                                         ToastUtil.showToast(mActivity.applicationContext,it.resultMsg);
                                       }
                                    }, object : FailedFlowable(mActivity) {
                                        override fun accept(e: Throwable) {
                                            super.accept(e)
                                            isClick.set(false)
                                        }
                                    }))
                        }
                    }
                    else -> {
                        isClick.set(false)
                        ToastUtil.showToast(mActivity.applicationContext, "" + errorMessage)
                    }
                }

            }

        }
        mActivity.showLoadingDialog()
        //提现预下单
        addSubscribe(api.withholdingCHAT(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    mActivity.hideLoadingDialog()
                    if (it.resultCode == 1) {
                        if (it.data != null) {
                            requestId = it.data.requestId
                            isClick.set(false)
                            //调起sdk的提现
                            walletPay.evoke(it.data.merchantId, it.data.walletId, it.data.token, AuthType.WITHHOLDING.name)
                            mActivity.hideLoadingDialog()
                        } else {
                            mActivity.hideLoadingDialog()
                            ToastUtil.showToast(mActivity.applicationContext, "返回结果错误")
                        }
                    } else {
                        mActivity.hideLoadingDialog()
                        ToastUtil.showToast(mActivity.applicationContext, it.resultMsg)
                    }
                }, object : FailedFlowable(mActivity) {
                    override fun accept(e: Throwable) {
                        super.accept(e)
                        isClick.set(false)
                    }
                }))

    }


}
