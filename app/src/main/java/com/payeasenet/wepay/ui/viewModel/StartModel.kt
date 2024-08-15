package com.payeasenet.wepay.ui.viewModel

import android.content.Intent
import android.content.SharedPreferences
import android.databinding.ObservableField
import android.text.TextUtils
import android.view.View
import com.ehking.sdk.wepay.utlis.SharedPreferencesUtil
import com.payeasenet.wepay.constant.Constants
import com.payeasenet.wepay.net.api.WepayApi
import com.payeasenet.wepay.net.client.RetrofitClient
import com.payeasenet.wepay.net.rxjava.FailedFlowable
import com.payeasenet.wepay.ui.activity.MainActivity
import com.payeasenet.wepay.ui.activity.OpenWalletActivity
import com.payeasenet.wepay.ui.activity.StartActivity
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
class StartModel(private val mActivity: StartActivity) :BaseModel(){


    val walletId = ObservableField<String>("")
    init{
        val wallet = SharedPreferencesUtil.getPreference(mActivity.applicationContext,Constants.wallet,Constants.wallet)
        if(!TextUtils.isEmpty(wallet)) {
            walletId.set(wallet)
        }
    }


    private val map = HashMap<String,String>()
    fun open(view: View) {
      mActivity.startActivity(Intent(mActivity.applicationContext,OpenWalletActivity::class.java))
    }

    fun sure(view: View) {
        mActivity.showLoadingDialog()
        Constants.walletId = walletId.get().toString()

        val api = RetrofitClient.getInstance().create(mActivity.applicationContext, WepayApi::class.java)
        val map = HashMap<String,String>()
        map["version"] = Constants.version
        map["merchantId"] = Constants.merchantId
        map["walletId"] = Constants.walletId
        api.walletQuery(map)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer{
                mActivity.hideLoadingDialog()
                SharedPreferencesUtil.savePreference(mActivity.applicationContext,
                    Constants.wallet,Constants.wallet,
                    Constants.walletId)
                mActivity.startActivity(Intent(mActivity.applicationContext,MainActivity::class.java)
                    .putExtra("name",it.nameDesc))
            },FailedFlowable(mActivity))

    }


}
