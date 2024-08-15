package com.payeasenet.wepay.ui.viewModel

import android.content.Intent
import android.databinding.ObservableField
import android.view.View
import com.ehking.sdk.wepay.utlis.SharedPreferencesUtil
import com.payeasenet.wepay.constant.Constants
import com.payeasenet.wepay.net.api.WepayApi
import com.payeasenet.wepay.net.client.RetrofitClient
import com.payeasenet.wepay.net.rxjava.FailedFlowable
import com.payeasenet.wepay.ui.activity.MainActivity
import com.payeasenet.wepay.ui.activity.OpenWalletActivity
import com.payeasenet.wepay.utlis.NetUtils
import com.payeasenet.wepay.utlis.ToastUtil
import com.iimm.miliao.MyApplication
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
class OpenWalletModel(private val mActivity: OpenWalletActivity):BaseModel() {


    val name = ObservableField<String>("")
    val idCardNo = ObservableField<String>("")
    val mobile = ObservableField<String>("")
    val nickName = ObservableField<String>("")
    val profession = ObservableField<String>("")
    private val map = HashMap<String, String>()

    fun sure(view: View) {
        Thread(Runnable { map["ip"] = NetUtils.getNetIp() }).start()
        Thread.sleep(1000)
        map["access_token"] = CoreManager.requireSelfStatus(MyApplication.getInstance()).accessToken
        map["name"] = name.get().toString()
        map["idCardNo"] = idCardNo.get().toString()
        map["mobile"] = mobile.get().toString()
        map["profession"] = profession.get().toString()
        map["mac"] = NetUtils.getMacAddress(mActivity.applicationContext)

//        map["version"] = Constants.version
//        map["requestId"] = ""+System.currentTimeMillis()
//        map["merchantUserId"] = ""+System.currentTimeMillis()
//        map["merchantId"] = Constants.merchantId
//        map["idCardType"] = "IDCARD"
//        map["name"] = name.get().toString()
//        map["idCardNo"] = idCardNo.get().toString()
//        map["mobile"] = mobile.get().toString()
//        map["mac"] = NetUtils.getMacAddress(mActivity.applicationContext)
//        map["nickName"] = nickName.get().toString()
//        map["profession"] = profession.get().toString()
        val api = RetrofitClient.getInstance().create(mActivity.applicationContext, WepayApi::class.java)
        mActivity.showLoadingDialog()
        addSubscribe(api.walletCreateCHAT(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    mActivity.hideLoadingDialog()
                    if(it.resultCode==1){
                        if (it.data != null) {
                            Constants.walletId = it.data.walletId
                            SharedPreferencesUtil.savePreference(mActivity.applicationContext, Constants.wallet, Constants.wallet,
                                    Constants.walletId
                            )
                            var name = name.get().toString()
                            name = if (name.length > 2) {
                                name.substring(0, 1) + "*" + name.substring(name.length - 1, name.length)
                            } else {
                                name.substring(0, 1) + "*"
                            }
                            mActivity.startActivity(Intent(mActivity.applicationContext, MainActivity::class.java)
                                    .putExtra("name", name))
                            mActivity.finish()
                        } else{
                            ToastUtil.showToast(mActivity.applicationContext,"数据获取失败")
                        }
                    }else{
                        ToastUtil.showToast(mActivity.applicationContext,it.resultMsg)
                    }

                }, FailedFlowable(mActivity)))
    }


}
