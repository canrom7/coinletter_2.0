package com.payeasenet.wepay.ui.activity

import android.databinding.DataBindingUtil
import android.graphics.Color
import android.view.View
import android.widget.AdapterView
import com.ehking.sdk.wepay.ui.base.BaseActivity
import com.payeasenet.wepay.constant.Constants
import com.payeasenet.wepay.ui.viewModel.OpenWalletModel
import com.iimm.miliao.R
import com.iimm.miliao.databinding.ActivityOpenWalletBinding
import kotlinx.android.synthetic.main.activity_open_wallet.*
import kotlinx.android.synthetic.main.toolbar.*

class OpenWalletActivity : BaseActivity() {
    var binding: ActivityOpenWalletBinding? = null
    override fun setContentView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_open_wallet)
    }

    override fun initActionBar() {
        toolbar.title = "开通钱包账户"
        toolbar.setBackgroundColor(Color.parseColor(Constants.toolBarColor))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }
    val profession = arrayListOf("A","B","C","D","E","F","G")
    override fun fetchData() {
        binding?.data = OpenWalletModel(this)
        spinner.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                binding?.data?.profession?.set("")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
               binding?.data?.profession?.set(profession[position])
            }

        }
    }

    override fun onDestroy() {
        binding?.data?.unSubscribe()
        super.onDestroy()
    }
}
