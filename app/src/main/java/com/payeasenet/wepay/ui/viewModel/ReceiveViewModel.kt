package com.payeasenet.wepay.ui.viewModel

import android.arch.lifecycle.ViewModel
import com.ehking.sdk.wepay.ui.base.BaseActivity
import com.payeasenet.wepay.net.api.WepayApi
import com.payeasenet.wepay.net.client.RetrofitClient
import com.payeasenet.wepay.net.rxjava.FailedFlowable
import com.payeasenet.wepay.ui.fragment.ReceiveFragment
import com.payeasenet.wepay.utlis.ToastUtil
import com.iimm.miliao.ui.base.CoreManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

class ReceiveViewModel : ViewModel() {
    private lateinit var dis: Disposable
    fun getData(fragment: ReceiveFragment, pageIndex: Int) {


        val activity = fragment.activity as BaseActivity
        val map = HashMap<String, String>()
        map["access_token"] = CoreManager.getSelfStatus(activity.applicationContext)!!.accessToken
        map["tradeType"] = "WEBOX_REDPACKET"
        map["pageIndex"] = "" + pageIndex
        map["pageSize"] = "20"
        map["direction"] = "INCREASE"
//        map["version"] = Constants.version
//        map["requestId"] = ""
//        map["walletId"] = Constants.walletId
//        map["merchantId"] = Constants.merchantId
//        map["pageSize"] = "20"
//        map["startDateTime"]= "2018-08-01 00:00:00"
//        map["endDateTime"]= SimpleDateFormat("yyyy-MM-dd").format(Date()) +" 23:59:59"
//        map["pageIndex"] = ""+pageIndex
//        map["tradeType"]= "WEBOX_REDPACKET"
//        map["direction"]= "INCREASE"
//        map["tradeSubType"]= ""
        val api = RetrofitClient.getInstance().create(activity.applicationContext, WepayApi::class.java)
        activity.showLoadingDialog()
        dis = api.redEnvelopeQueryCHAT(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    activity.hideLoadingDialog()
                    fragment.finishComplete()
                    if (it.resultCode == 1) {
                        if (it.data != null) {
                            if (it.data.records != null) {
                                if (it.data.records.size < 20) {
                                    fragment.setOpenLoadMore(false)
                                }
                                if (it.data.records.size > 0) {
                                    fragment.setData(it.data.records)
                                }
                            } else {
//                                ToastUtil.showToast(activity, "暂无数据")
                            }
                        } else {
                            ToastUtil.showToast(activity, "获取数据异常")
                        }
                    } else {
                        ToastUtil.showToast(activity, it.resultMsg)
                    }
                }, object : FailedFlowable(activity) {
                    override fun accept(e: Throwable) {
                        super.accept(e)
                        fragment.finishComplete()
                    }
                })
    }

    fun disposeView() {
        if (dis != null && dis.isDisposed) {
            dis.dispose()
        }
    }
}
