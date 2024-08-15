package com.payeasenet.wepay.ui.activity

import android.databinding.DataBindingUtil
import android.graphics.Color
import com.ehking.sdk.wepay.ui.base.BaseActivity
import com.payeasenet.wepay.constant.Constants
import com.payeasenet.wepay.net.bean.ResponseBean
import com.iimm.miliao.R
import com.iimm.miliao.databinding.ActivityTransactionDetailBinding
import kotlinx.android.synthetic.main.activity_transaction_detail.*
import kotlinx.android.synthetic.main.toolbar.*
import java.math.BigDecimal
import java.text.DecimalFormat

class TransactionDetailActivity : BaseActivity() {
    var binding: ActivityTransactionDetailBinding? = null
    override fun setContentView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_transaction_detail)
    }

    override fun initActionBar() {
        val transaction = intent.getSerializableExtra("transaction") as ResponseBean.Transaction
        binding?.data = transaction
        val paymentType = when (transaction.tradeType) {
            "WEBOX_RECHARGE" -> "充值"
            "WEBOX_REDPACKET" -> "抢红包"
            "WEBOX_TRANSFER" -> "转账收款"
            "WEBOX_WITHHOLDING" -> "微包提现"
            "WEBOX_REDPACKET_REFUND" -> "红包退款"
            "WEBOX_TRANSFER_REFUND" -> "转账-退款"
            else -> ""
        }
        type.text = paymentType
        if ("INCREASE" == transaction.direction) {
//            amount.text ="+" + DecimalFormat("0.00").format(
//                BigDecimal(transaction.amount).divide(
//                    BigDecimal(100)
//                ))
            amount.text = "+" + DecimalFormat("0.00").format(
                    BigDecimal(transaction.amount))
            amount.setTextColor(Color.parseColor("#fe584c"))
            toolbar.title = "收入详情"
        } else {
            amount.setTextColor(Color.parseColor("#333333"))
//            amount.text ="-" + DecimalFormat("0.00").format(
//                BigDecimal(transaction.amount).divide(
//                    BigDecimal(100)
//                ))
            amount.text = "-" + DecimalFormat("0.00").format(
                    BigDecimal(transaction.amount))
            toolbar.title = "支出详情"
        }

        toolbar.setBackgroundColor(Color.parseColor(Constants.toolBarColor))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


    }

}