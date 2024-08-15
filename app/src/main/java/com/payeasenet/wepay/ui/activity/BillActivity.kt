package com.payeasenet.wepay.ui.activity

import android.databinding.DataBindingUtil
import android.graphics.Color
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import com.ehking.sdk.wepay.ui.base.BaseActivity
import com.payeasenet.wepay.constant.Constants
import com.payeasenet.wepay.ui.fragment.AllFragment
import com.payeasenet.wepay.ui.fragment.CostFragment
import com.payeasenet.wepay.ui.fragment.IncomeFragment
import com.iimm.miliao.R
import com.iimm.miliao.databinding.ActivityBillBinding
import kotlinx.android.synthetic.main.activity_bill.*
import kotlinx.android.synthetic.main.toolbar.*

class BillActivity : BaseActivity() {
    var binding: ActivityBillBinding? = null
    override fun setContentView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bill)
    }

    override fun initActionBar() {
        toolbar.title = "交易记录"
        toolbar.setBackgroundColor(Color.parseColor(Constants.toolBarColor))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun fetchData() {
        super.fetchData()
        val titles = arrayOf("全部","收入","支出")
        tab_layout.addTab(tab_layout.newTab().setText("全部"))
        tab_layout.addTab(tab_layout.newTab().setText("收入"))
        tab_layout.addTab(tab_layout.newTab().setText("支出"))
        val tabFragmentList = ArrayList<Fragment>()
        tabFragmentList.add(AllFragment.newInstance(1))
        tabFragmentList.add(IncomeFragment.newInstance(2))
        tabFragmentList.add(CostFragment.newInstance(3))
        view_pager.adapter = object :FragmentStatePagerAdapter(supportFragmentManager){
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


}