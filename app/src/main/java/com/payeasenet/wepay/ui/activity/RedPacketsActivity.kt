package com.payeasenet.wepay.ui.activity

import android.databinding.DataBindingUtil
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.text.TextUtils
import com.ehking.sdk.wepay.ui.base.BaseActivity
import com.payeasenet.wepay.net.api.WepayApi
import com.payeasenet.wepay.net.client.RetrofitClient
import com.payeasenet.wepay.net.rxjava.FailedFlowable
import com.payeasenet.wepay.ui.fragment.ReceiveFragment
import com.payeasenet.wepay.ui.fragment.SendFragment
import com.payeasenet.wepay.utlis.ToastUtil
import com.iimm.miliao.MyApplication
import com.iimm.miliao.R
import com.iimm.miliao.databinding.ActivityRedPacketsBinding
import com.iimm.miliao.helper.AvatarHelper
import com.iimm.miliao.ui.base.CoreManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_bill.tab_layout
import kotlinx.android.synthetic.main.activity_bill.view_pager
import kotlinx.android.synthetic.main.activity_red_packets.*
import java.math.BigDecimal
import java.text.DecimalFormat


class RedPacketsActivity : BaseActivity() {
    private lateinit var dis: Disposable
    private lateinit var dis2: Disposable
    var binding: ActivityRedPacketsBinding? = null
    override fun setContentView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_red_packets)
    }

    override fun initActionBar() {
        toolbar.title = "红包明细"
        //toolbar.setBackgroundColor(Color.parseColor(Constants.toolBarColor))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        name.text = intent.getStringExtra("name")


        AvatarHelper.getInstance().displayAvatar(CoreManager.getSelf(MyApplication.getInstance())?.getNickName(), CoreManager.getSelf(MyApplication.getInstance())?.getUserId(), image, false);
        getData()
    }

    override fun fetchData() {
        super.fetchData()
        val titles = arrayOf("我收到的红包", "我发出的红包")
        tab_layout.addTab(tab_layout.newTab().setText("我收到的红包"))
        tab_layout.addTab(tab_layout.newTab().setText("我发出的红包"))
        val tabFragmentList = ArrayList<Fragment>()
        tabFragmentList.add(ReceiveFragment.newInstance(5))
        tabFragmentList.add(SendFragment.newInstance(4))
        view_pager.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getCount(): Int {
                return tabFragmentList.size
            }

            override fun getItem(position: Int): Fragment {
                return tabFragmentList[position]
            }

            override fun getPageTitle(position: Int): CharSequence {
                return titles[position]
            }


        }
        tab_layout.setupWithViewPager(view_pager)
    }

    fun getData() {

        val api1 = RetrofitClient.getInstance().create(applicationContext, WepayApi::class.java)
        val map1 = HashMap<String, String>()
        map1["access_token"] = CoreManager.requireSelfStatus(MyApplication.getInstance()).accessToken
        dis2 = api1.walletQueryCHAT(map1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it.resultCode == 1 && it.data != null) {
                        name.text = it.data.nickName
                    }
                }


        val map = HashMap<String, String>()
        map["access_token"] = CoreManager.requireSelfStatus(MyApplication.getInstance()).accessToken
        map["tradeType"] = "WEBOX_REDPACKET"
//        map["version"] = Constants.version
//        map["walletId"] = Constants.walletId
//        map["merchantId"] = Constants.merchantId
//        map["startDateTime"]= "2018-08-01 00:00:00"
//        map["endDateTime"]= SimpleDateFormat("yyyy-MM-dd").format(Date()) +" 23:59:59"
//        map["tradeType"]= "WEBOX_REDPACKET"
//        map["tradeSubType"]= ""
        val api = RetrofitClient.getInstance().create(applicationContext, WepayApi::class.java)
        showLoadingDialog()
        dis = api.tradeStatisCHAT(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    hideLoadingDialog()
                    var num = "0"
                    if (it.resultCode == 1) {
                        if (it?.data != null && it?.data!!.increaseTradeStatisVO != null) {
                            if (!TextUtils.isEmpty(it.data.increaseTradeStatisVO.sumAmount)) {
//                            amount.text = DecimalFormat("0.00").format(
//                                    BigDecimal(it.data.increaseTradeStatisVO.sumAmount).divide(BigDecimal(100))
//                            )
                                amount.text = DecimalFormat("0.00").format(BigDecimal(it.data.increaseTradeStatisVO.sumAmount))
                            } else {
                                amount.text = "0.00"
                            }
                            num = if (!TextUtils.isEmpty(it.data.increaseTradeStatisVO.count)) {
                                it.data.increaseTradeStatisVO.count
                            } else {
                                "0"
                            }

                        } else {
                            amount.text = "0.00"
                        }
                        count.text = "收到红包总数${num}个"
                    } else {
                        ToastUtil.showToast(this, "获取数据异常")
                    }
                }, FailedFlowable(this))
    }

    override fun onDestroy() {
        if (dis != null && dis.isDisposed) {
            dis.dispose()
        }
        super.onDestroy()
    }
}