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
import com.payeasenet.wepay.ui.activity.RechargeActivity
import com.payeasenet.wepay.utlis.ToastUtil
import com.iimm.miliao.ui.base.CoreManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

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
class RechargeModel(private val mActivity: RechargeActivity) : BaseModel() {


    val amount = ObservableField<String>("")
    val isClick = ObservableField<Boolean>(false)
    private val map = HashMap<String, String>()
    var requestId:String = ""

    fun sure(view: View) {
        val api = RetrofitClient.getInstance().create(mActivity.applicationContext, WepayApi::class.java)
        isClick.set(true)
//        if(BigDecimal(amount.get().toString()).toDouble()>Constants.money){
//               isClick.set(false)
//               ToastUtil.showToast(mActivity.applicationContext,"最多充值金额为1元")
//               return
//        }
        val map = HashMap<String, String>()
//        map["version"] = Constants.version
//        map["requestId"] = ""+System.currentTimeMillis()
//        map["merchantId"] = Constants.merchantId
//        map["walletId"] = Constants.walletId
//        map["currency"] = Constants.currency
//        map["amount"] = ""+ (BigDecimal(amount.get().toString())* BigDecimal(100)).toInt()
//        map["notifyUrl"] = Constants.notifyUrl
        val walletPay = WalletPay.getInstance()
        walletPay.init(mActivity)
        walletPay.walletPayCallback = object : WalletPay.WalletPayCallback {
            override fun callback(source: String?, status: String?, errorMessage: String?) {
                when (status) {
                    "SUCCESS", "PROCESS" -> {
                        ToastUtil.showToast(mActivity.applicationContext, "充值成功")
                        if (TextUtils.isEmpty(requestId)){
                            return
                        }
                        val map = HashMap<String, String>()
                        map["access_token"] = CoreManager.getSelfStatus(mActivity.applicationContext)!!.accessToken
                        map["requestId"] = requestId
                        //请求服务端充值查询结果 确定充值状态
                       addSubscribe(api.walletRechargeQueryCHAT(map)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(Consumer {
                                    mActivity.hideLoadingDialog()
                                    if (it.resultCode == 1) {
                                        if (it.data != null) {
                                            mActivity.startActivity(Intent(mActivity.applicationContext, EndingActivity::class.java)
                                                    .putExtra(Constants.type, 1)
//                                    .putExtra(Constants.amount,""+BigDecimal(it.amount).toDouble()/100))
                                                    .putExtra(Constants.amount, it.data.amount))
                                            mActivity.finish()
                                        } else {
                                            ToastUtil.showToast(mActivity.applicationContext, "结果解析失败")
                                        }
                                    } else {
                                        ToastUtil.showToast(mActivity.applicationContext, it.resultMsg)
                                    }

                                }, object : FailedFlowable(mActivity) {
                                    override fun accept(e: Throwable) {
                                        super.accept(e)
                                        isClick.set(false)
                                    }
                                }))
                    }
                    else -> {
                        isClick.set(false)
                        ToastUtil.showToast(mActivity.applicationContext, "" + errorMessage)
                    }
                }

            }

        }

        map["access_token"] = CoreManager.getSelfStatus(mActivity.applicationContext)!!.accessToken
        map["amount"] = amount.get().toString()
        mActivity.showLoadingDialog()
        addSubscribe(api.walletRechargeCHAT(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    mActivity.hideLoadingDialog()
                    if (it.resultCode==1) {
                        if (it.data!=null) {
                            isClick.set(false)
                            requestId = it.data.requestId
                            //调起SDK的充值
                            val walletPay = WalletPay.getInstance()
                            walletPay.init(mActivity)
                            walletPay.evoke(it.data.merchantId,it.data.walletId,it.data.token,AuthType.RECHARGE.name)
                        }else{
                            ToastUtil.showToast(mActivity.applicationContext, "解析失败")
                        }
                    }else{
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
