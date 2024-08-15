package com.payeasenet.wepay.ui.viewModel

import android.databinding.ObservableField
import com.payeasenet.wepay.net.api.WepayApi
import com.payeasenet.wepay.net.client.RetrofitClient
import com.payeasenet.wepay.ui.activity.AccountActivity
import com.payeasenet.wepay.utlis.ToastUtil
import com.iimm.miliao.MyApplication
import com.iimm.miliao.R
import com.iimm.miliao.ui.base.CoreManager
import io.reactivex.android.schedulers.AndroidSchedulers
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
class AccountModel(private val mActivity: AccountActivity) :BaseModel() {

    val name = ObservableField<String>("")

    val idCard = ObservableField<String>("")

    val mobile = ObservableField<String>("")
    val idCardRzStatus = ObservableField<Boolean>(false)
    val operatorRzStatus = ObservableField<Boolean>(false)
    val idCardRzStatusImg = ObservableField<Int>(R.mipmap.chahao)
    val operatorRzStatusImg = ObservableField<Int>(R.mipmap.chahao)
    fun walletQuery() {
        mActivity.showLoadingDialog()
        val api = RetrofitClient.getInstance().create(mActivity.applicationContext, WepayApi::class.java)
        val map = HashMap<String,String>()
        /*map["version"] = Constants.version
        map["merchantId"] = Constants.merchantId
        map["walletId"] = Constants.walletId
        api.walletQuery(map)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                mActivity.hideLoadingDialog()
                if("SUCCESS" == it.idCardRzStatus){
                    idCardRzStatus.set(true)
                    idCardRzStatusImg.set(R.mipmap.duihao2)
                }
                if("SUCCESS"==it.operatorRzStatus){
                    operatorRzStatus.set(true)
                    operatorRzStatusImg.set(R.mipmap.duihao2)
                }
                idCard.set(it.idCardNoDesc)
                name.set(it.nameDesc)
                mobile.set(it.mobileDesc)
            }*/
        map["access_token"] = CoreManager.requireSelfStatus(MyApplication.getInstance()).accessToken
        addSubscribe(api.walletQueryCHAT(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    mActivity.hideLoadingDialog()
                    if (it.resultCode == 1 && it.data != null) {
                        if("SUCCESS" == it.data.idCardRzStatus){
                            idCardRzStatus.set(true)
                            idCardRzStatusImg.set(R.mipmap.duihao2)
                        }
                        if("SUCCESS"==it.data.operatorRzStatus){
                            operatorRzStatus.set(true)
                            operatorRzStatusImg.set(R.mipmap.duihao2)
                        }
                        idCard.set(it.data.idCardNoDesc)
                        name.set(it.data.nameDesc)
                        mobile.set(it.data.mobileDesc)
                    } else {
                        ToastUtil.showToast(mActivity.applicationContext, it.resultMsg)
                    }
                })
    }

}
