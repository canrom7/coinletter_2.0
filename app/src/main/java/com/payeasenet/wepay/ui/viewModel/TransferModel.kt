package com.payeasenet.wepay.ui.viewModel

import android.content.Intent
import android.databinding.ObservableField
import android.view.View
import com.ehking.sdk.wepay.interfaces.WalletPay
import com.ehking.sdk.wepay.net.bean.AuthType
import com.ehking.sdk.wepay.utlis.LogUtil
import com.payeasenet.wepay.constant.Constants
import com.payeasenet.wepay.net.api.WepayApi
import com.payeasenet.wepay.net.client.RetrofitClient
import com.payeasenet.wepay.net.rxjava.FailedFlowable
import com.payeasenet.wepay.ui.activity.EndingActivity
import com.payeasenet.wepay.ui.activity.TransferActivity
import com.payeasenet.wepay.utlis.ToastUtil
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
class TransferModel(private val mActivity: TransferActivity) {


    val amount = ObservableField<String>("")
    val targetWalletId = ObservableField<String>("")
    val isClick =  ObservableField<Boolean>(false)
    fun sure(view: View) { 
        val api = RetrofitClient.getInstance().create(mActivity.applicationContext, WepayApi::class.java)
        isClick.set(true)
        if(BigDecimal(amount.get().toString()).toDouble()>Constants.money){
               isClick.set(false)
               ToastUtil.showToast(mActivity.applicationContext,"最多转账金额为1元")
               return
        }
        val map = HashMap<String,String>()
        map["version"] = Constants.version
        map["requestId"] = ""+System.currentTimeMillis()
        map["merchantId"] = Constants.merchantId
        map["walletId"] = Constants.walletId
        map["targetWalletId"] = targetWalletId.get().toString()
        map["currency"] = Constants.currency
        map["amount"] = ""+ (BigDecimal(amount.get().toString())* BigDecimal(100)).toInt()
        map["notifyUrl"] = Constants.notifyUrl
        val walletPay = WalletPay.getInstance()
        walletPay.init(mActivity)
        walletPay.walletPayCallback = object :WalletPay.WalletPayCallback {
            override fun callback(source: String?, status: String?, errorMessage: String?) {
                LogUtil.d("status:${status}")
                when (status) {
                    "SUCCESS", "PROCESS" -> {
                        //请求服务端转账查询结果 确定转账状态
                        api.transferQueryOrder(map)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(Consumer {
                                mActivity.hideLoadingDialog()
                                mActivity.startActivity(Intent(mActivity.applicationContext,EndingActivity::class.java)
                                    .putExtra(Constants.type,3)
                                    .putExtra("targetWalletId",targetWalletId.get().toString())
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

        }
        mActivity.showLoadingDialog()
        //转账预下单
        api.transferCreateOrder(map)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {
                mActivity.hideLoadingDialog()
                isClick.set(false)
                //调起SDK的转账
                walletPay.evoke(Constants.merchantId,Constants.walletId,it.token,AuthType.TRANSFER.name)
            },object :FailedFlowable(mActivity){
                override fun accept(e: Throwable) {
                    super.accept(e)
                    isClick.set(false)
                }
            })

    }


}
