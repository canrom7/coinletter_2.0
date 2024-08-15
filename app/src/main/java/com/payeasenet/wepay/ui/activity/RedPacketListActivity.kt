package com.payeasenet.wepay.ui.activity

import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import com.ehking.sdk.wepay.ui.base.BaseActivity
import com.payeasenet.wepay.constant.Constants
import com.iimm.miliao.R
import com.iimm.miliao.databinding.ActivityRedPacketListBinding
import kotlinx.android.synthetic.main.activity_red_packet_list.*
import kotlinx.android.synthetic.main.toolbar.*

class RedPacketListActivity : BaseActivity() {
    var binding: ActivityRedPacketListBinding? = null
    override fun setContentView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_red_packet_list)
    }

    override fun initActionBar() {
        toolbar.title = "发红包"
        toolbar.setBackgroundColor(Color.parseColor(Constants.toolBarColor))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val intent = Intent(applicationContext,RedPacketActivity::class.java)
        rl_one.setOnClickListener {
            startActivity(intent.putExtra("type",1))
        }
        rl_normal.setOnClickListener {
            startActivity(intent.putExtra("type",2))
        }
        rl_luck.setOnClickListener {
            startActivity(intent.putExtra("type",3))
        }
    }


}