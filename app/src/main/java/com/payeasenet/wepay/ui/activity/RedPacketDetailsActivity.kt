package com.payeasenet.wepay.ui.activity

import android.databinding.DataBindingUtil
import android.graphics.Color
import com.ehking.sdk.wepay.ui.base.BaseActivity
import com.payeasenet.wepay.constant.Constants
import com.iimm.miliao.R
import com.iimm.miliao.databinding.ActivityRedPacketDetailsBinding
import kotlinx.android.synthetic.main.toolbar.*

class RedPacketDetailsActivity : BaseActivity() {
    var binding: ActivityRedPacketDetailsBinding? = null
    override fun setContentView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_red_packet_details)
    }

    override fun initActionBar() {
        toolbar.title = "红包明细"
        toolbar.setBackgroundColor(Color.parseColor(Constants.toolBarColor))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }


}