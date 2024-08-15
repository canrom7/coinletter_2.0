package com.payeasenet.wepay.ui.activity

import android.databinding.DataBindingUtil
import android.graphics.Color
import com.ehking.sdk.wepay.ui.base.BaseActivity
import com.payeasenet.wepay.constant.Constants
import com.payeasenet.wepay.ui.viewModel.AcceptModel
import com.iimm.miliao.R
import com.iimm.miliao.databinding.ActivityAcceptBinding
import kotlinx.android.synthetic.main.toolbar.*

class AcceptActivity : BaseActivity() {
    var binding: ActivityAcceptBinding? = null
    override fun setContentView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_accept)
    }

    override fun initActionBar() {
        toolbar.title = "转账接收&红包领取"
        toolbar.setBackgroundColor(Color.parseColor(Constants.toolBarColor))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


    }

    override fun fetchData() {
        binding?.data = AcceptModel(this)
    }
}