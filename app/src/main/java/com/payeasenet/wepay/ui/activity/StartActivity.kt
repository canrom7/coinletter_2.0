package com.payeasenet.wepay.ui.activity

import android.databinding.DataBindingUtil
import android.graphics.Color
import com.ehking.sdk.wepay.ui.base.BaseActivity
import com.payeasenet.wepay.constant.Constants
import com.payeasenet.wepay.ui.viewModel.StartModel
import com.iimm.miliao.R
import com.iimm.miliao.databinding.ActivityStartBinding
import kotlinx.android.synthetic.main.toolbar.*

class StartActivity : BaseActivity() {
    var binding: ActivityStartBinding? = null
    override fun setContentView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_start)
    }

    override fun initActionBar() {
        toolbar.title = "钱包配置"
        toolbar.setBackgroundColor(Color.parseColor(Constants.toolBarColor))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)


    }

    override fun fetchData() {
        binding?.data = StartModel(this)

    }
}