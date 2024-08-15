package com.payeasenet.wepay.ui.activity

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.payeasenet.wepay.ui.viewModel.MainModel
import com.iimm.miliao.R
import com.iimm.miliao.databinding.ActivityMain2Binding
import com.iimm.miliao.util.StatusBarUtil
import kotlinx.android.synthetic.main.toolbar.*

class MainActivity : AppCompatActivity() {
    var binding: ActivityMain2Binding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        StatusBarUtil.setStatusBar(this, getResources().getColor(R.color.colorPrimary));
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main2)
        initActionBar()
        fetchData()
    }


    private fun initActionBar() {
        toolbar.title = "微钱包"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)


    }

    private fun fetchData() {
        binding?.data = MainModel(this)
        binding?.data?.init()
        binding?.data?.walletQuery()
    }

    override fun onResume() {
        super.onResume()
        binding?.data?.walletQuery()
    }

    override fun onDestroy() {
        binding?.data?.unSubscribe()
        super.onDestroy()
    }


}