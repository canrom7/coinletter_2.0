package com.payeasenet.wepay.ui.activity

import android.databinding.DataBindingUtil
import android.graphics.Color
import com.ehking.sdk.wepay.ui.base.BaseActivity
import com.payeasenet.wepay.constant.Constants
import com.payeasenet.wepay.ui.viewModel.AccountModel
import com.iimm.miliao.R
import com.iimm.miliao.databinding.ActivityAccountBinding
import kotlinx.android.synthetic.main.toolbar.*

class AccountActivity : BaseActivity() {
    var binding: ActivityAccountBinding? = null
    override fun setContentView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_account)
    }

    override fun initActionBar() {
        toolbar.title = "账户信息"
        toolbar.setBackgroundColor(Color.parseColor(Constants.toolBarColor))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


    }
    override fun fetchData() {
        binding?.data = AccountModel(this)
        binding?.data?.walletQuery()
    }
    override fun onDestroy() {
        binding?.data?.unSubscribe()
        super.onDestroy()

    }
}