package com.payeasenet.wepay.ui.activity

import android.databinding.DataBindingUtil
import android.text.TextUtils
import android.view.View
import com.ehking.sdk.wepay.ui.base.BaseActivity
import com.payeasenet.wepay.constant.Constants
import com.iimm.miliao.R
import com.iimm.miliao.databinding.ActivityEndingBinding
import kotlinx.android.synthetic.main.activity_ending.*
import kotlinx.android.synthetic.main.toolbar.*
import java.math.BigDecimal
import java.text.DecimalFormat

class EndingActivity : BaseActivity() {
    var binding: ActivityEndingBinding? = null
    override fun setContentView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ending)
    }
    override fun initActionBar() {
        var title: String
        var contentText: String
        var status:String
        when(intent.getIntExtra(Constants.type,1)){
            1 -> {
                title ="充值结果"
                contentText = "充值金额"
                status ="充值成功"
                time.visibility= View.GONE
                rl_amount.visibility = View.VISIBLE
                amount1.visibility = View.GONE
            }
            2 -> {
                title ="提现结果"
                contentText = "提现金额"
                status ="提现申请成功，等待银行处理"
                time.visibility= View.VISIBLE
                rl_amount.visibility = View.VISIBLE
                amount1.visibility = View.GONE
            }
            3 -> {
                title ="转账结果"
                contentText = "转账金额"
                time.text = "待张三确认收款"
                status ="支付成功"
                time.visibility= View.VISIBLE
                rl_amount.visibility = View.GONE
                amount1.visibility = View.VISIBLE
            }
            4 -> {
                title ="红包结果"
                contentText = "支付金额"
                time.text = "红包待领取"
                status ="支付成功"
                time.visibility= View.VISIBLE
                rl_amount.visibility = View.GONE
                amount1.visibility = View.VISIBLE
            }
            else -> { 
                title ="充值结果"
                contentText = "充值金额"
                status ="充值成功"
                time.visibility= View.GONE
                rl_amount.visibility = View.VISIBLE
                amount1.visibility = View.GONE
            }
        }
        content.text = status
        tv_amount.text = contentText
        toolbar.title = title
        if(!TextUtils.isEmpty(intent.getStringExtra(Constants.amount))) {
            amount.text = "¥${DecimalFormat("0.00").format(BigDecimal(intent.getStringExtra(Constants.amount)))}"
            amount1.text = "¥   ${DecimalFormat("0.00").format(BigDecimal(intent.getStringExtra(Constants.amount)))}"
        }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        finish.setOnClickListener { 
            finish()
        }

    }
    
}