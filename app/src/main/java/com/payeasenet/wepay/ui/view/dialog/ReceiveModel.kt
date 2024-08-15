package com.payeasenet.wepay.ui.view.dialog

import android.arch.lifecycle.ViewModel
import com.payeasenet.wepay.constant.Constants
import com.payeasenet.wepay.net.api.WepayApi
import com.payeasenet.wepay.net.client.RetrofitClient
import com.payeasenet.wepay.net.rxjava.FailedFlowable
import com.payeasenet.wepay.ui.activity.BillActivity
import com.payeasenet.wepay.ui.fragment.AllFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ReceiveModel : ViewModel() {
    fun getData( fragment:  AllFragment,pageIndex:Int) {
        /*version: 3.0
        requestId:
        merchantId: 896667318
        walletId: 10000000000000002
        pageIndex: 1
        pageSize: 20
        startDateTime: 2018-08-01 00:00:00
        endDateTime: 2019-12-01 00:00:00
        tradeType:
        direction:
        tradeSubType:*/

        val activity = fragment.activity as BillActivity
        val map = HashMap<String,String>()
        map["version"] = Constants.version
        map["requestId"] = ""
        map["walletId"] = Constants.walletId
        map["merchantId"] = Constants.merchantId
        map["pageSize"] = "11"
        map["startDateTime"]= "2018-08-01 00:00:00"
        map["endDateTime"]= SimpleDateFormat("yyyy-MM-dd").format(Date()) +" 23:59:59"
        map["pageIndex"] = ""+pageIndex
        map["tradeType"]= ""
        map["direction"]= ""
        map["tradeSubType"]= ""
        val api = RetrofitClient.getInstance().create(activity.applicationContext, WepayApi::class.java)
        activity.showLoadingDialog()
        api.tradeRecordQuery(map)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {
               activity.hideLoadingDialog()
                fragment.finishComplete()
                if(it.records!=null && it.records.size<11){
                 fragment.setOpenLoadMore(false)
                }
                if(it.records.size>0) {
                    fragment.setData(it.records)
                }
            }, FailedFlowable(activity))
    }
}
