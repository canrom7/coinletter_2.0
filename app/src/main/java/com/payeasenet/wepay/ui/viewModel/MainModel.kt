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
import com.payeasenet.wepay.ui.activity.*
import com.payeasenet.wepay.utlis.ToastUtil
import com.iimm.miliao.MyApplication
import com.iimm.miliao.helper.DialogHelper
import com.iimm.miliao.ui.base.CoreManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import java.text.DecimalFormat

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
class MainModel(private val mActivity: MainActivity) : BaseModel() {
    val amount = ObservableField<String>("")
    private val walletPay = WalletPay.getInstance()
    fun init() {
        walletPay.setEnvironment(Constants.environment)
        walletPay.init(mActivity)
        walletPay.walletPayCallback = object : WalletPay.WalletPayCallback {
            override fun callback(source: String?, status: String?, errorMessage: String?) {
                // Toast.makeText(applicationContext, "来在app的回调 来源:$source 处理结果:$status 错误原因:$errorMessage",
                // Toast.LENGTH_LONG).show()
            }

        }
    }

    /**
     * 获取钱包余额
     */
    fun walletQuery() {
        val api = RetrofitClient.getInstance().create(mActivity.applicationContext, WepayApi::class.java)
        val map = HashMap<String, String>()
        map["access_token"] = CoreManager.requireSelfStatus(MyApplication.getInstance()).accessToken
        addSubscribe(api.walletQueryCHAT(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it.resultCode == 1 && it.data != null) {
                        if (TextUtils.isEmpty(it.data.balance)) {
                            amount.set("--")
                        } else {
//                            amount.set("" + DecimalFormat("0.00").format(BigDecimal(it.data.balance).divide(BigDecimal(100))))
                            amount.set("" + DecimalFormat("0.00").format(BigDecimal(it.data.balance)))
                        }
                    } else {
                        amount.set("--")
                        ToastUtil.showToast(mActivity.applicationContext, it.resultMsg)
                    }
                })
    }

    fun back(view: View) {
        if (mActivity != null) {
            mActivity.finish()
        }
    }

    /**
     * 充值
     */
    fun recharge(view: View) {
        mActivity.startActivity(Intent(mActivity.applicationContext, RechargeActivity::class.java))
    }

    /**
     * 账户信息
     */
    fun account(view: View) {
        mActivity.startActivity(Intent(mActivity.applicationContext, AccountActivity::class.java))
    }

    /**
     * 交易明细
     */
    fun bill(view: View) {
        mActivity.startActivity(Intent(mActivity.applicationContext, BillActivity::class.java))
    }

    /**
     * 红包明细
     */
    fun redPacket(view: View) {
        mActivity.startActivity(Intent(mActivity.applicationContext, RedPacketsActivity::class.java))
    }

    /**
     * 提现
     */
    fun withdraw(view: View) {
        mActivity.startActivity(Intent(mActivity.applicationContext, WithdrawActivity::class.java)
                .putExtra(Constants.amount, amount.get().toString()))

    }

    fun myCard(view: View) {
        /* val api = RetrofitClient.getInstance().create(mActivity.applicationContext, WepayApi::class.java)
         val map = HashMap<String, String>()
         map["version"] = Constants.version
         map["merchantId"] = Constants.merchantId
         map["walletId"] = Constants.walletId
         map["requestId"] = "" + System.currentTimeMillis()
         map["businessType"] = "ACCESS_CARDlIST"
         //获取token
         api.clientTokenCreate(map)
                 .subscribeOn(Schedulers.io())
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe {
                     //调起SDK银行卡列表
                     walletPay.evoke(Constants.merchantId, Constants.walletId, it.token, AuthType.ACCESS_CARDlIST.name)
                 }*/
        DialogHelper.showDefaulteMessageProgressDialog(mActivity)
        val api = RetrofitClient.getInstance().create(mActivity.applicationContext, WepayApi::class.java)
        val map = HashMap<String, String>()
        map["access_token"] = CoreManager.requireSelfStatus(MyApplication.getInstance()).accessToken
        map["businessType"] = "ACCESS_CARDlIST"
        //获取token
        addSubscribe(api.clientTokenCreateCHAT(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it.resultCode == 1) {
                        if (it.data != null) {
                            //调起SDK银行卡列表
                            walletPay.evoke(it.data.merchantId, it.data.walletId, it.data.token, AuthType.ACCESS_CARDlIST.name)
                        } else {
                            ToastUtil.showToast(mActivity.applicationContext, "获取数据异常")
                        }
                        DialogHelper.dismissProgressDialog()
                    } else {
                        DialogHelper.dismissProgressDialog()
                        ToastUtil.showToast(mActivity.applicationContext, it.resultMsg)
                    }
                })
    }

    fun settings(view: View) {
        /* val api = RetrofitClient.getInstance().create(mActivity.applicationContext, WepayApi::class.java)
         val map = HashMap<String, String>()
         map["version"] = Constants.version
         map["merchantId"] = Constants.merchantId
         map["walletId"] = Constants.walletId
         map["requestId"] = "" + System.currentTimeMillis()
         map["businessType"] = "ACCESS_SAFETY"
         //获取token
         api.clientTokenCreate(map)
                 .subscribeOn(Schedulers.io())
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe {
                     //调起SDK 设置页面
                     walletPay.evoke(Constants.merchantId, Constants.walletId, it.token, AuthType.ACCESS_SAFETY.name)
                 }*/
        DialogHelper.showDefaulteMessageProgressDialog(mActivity)
        val api = RetrofitClient.getInstance().create(mActivity.applicationContext, WepayApi::class.java)
        val map = HashMap<String, String>()
        map["access_token"] = CoreManager.requireSelfStatus(MyApplication.getInstance()).accessToken
        map["businessType"] = "ACCESS_SAFETY"
        //获取token
        addSubscribe(api.clientTokenCreateCHAT(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it.resultCode == 1) {
                        if (it.data != null) {
                            //调起SDK银行卡列表
                            walletPay.evoke(it.data.merchantId, it.data.walletId, it.data.token, AuthType.ACCESS_SAFETY.name)
                        } else {
                            ToastUtil.showToast(mActivity.applicationContext, "获取数据异常")
                        }
                        DialogHelper.dismissProgressDialog()
                    } else {
                        DialogHelper.dismissProgressDialog()
                        ToastUtil.showToast(mActivity.applicationContext, it.resultMsg)
                    }
                })
    }

    fun transfer(view: View) {
        mActivity.startActivity(Intent(mActivity.applicationContext, TransferActivity::class.java))
    }

    fun send(view: View) {
        mActivity.startActivity(Intent(mActivity.applicationContext, RedPacketListActivity::class.java))
    }

    fun receive(view: View) {
        mActivity.startActivity(Intent(mActivity.applicationContext, AcceptActivity::class.java))
    }
}
