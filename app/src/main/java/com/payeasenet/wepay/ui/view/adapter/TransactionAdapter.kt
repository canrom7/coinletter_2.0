package com.payeasenet.wepay.ui.view.adapter

import android.content.Context
import android.databinding.ViewDataBinding
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import com.payeasenet.wepay.net.bean.ResponseBean
import com.iimm.miliao.databinding.ItemTransactionRlBinding
import java.math.BigDecimal
import java.text.DecimalFormat


/**
 * 类 `${CLASS_NAME}`
 *
 *
 * 描述：
 *
 * 创建日期：2017年08月07日

 * @author zhaoyong.chen@ehking.com
 * *
 * @version 1.0
 */
class TransactionAdapter(context: Context, data: List<ResponseBean.Transaction>) : AbsRVAdapter<ResponseBean.Transaction, TransactionAdapter.TransactionHolder>(context, data) {
    var type = 1 //发红包 4 收到红包5
    override fun onBindViewHolder(holder: TransactionHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.bindData(mContext, data[position], position,type)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHolder {
        return TransactionHolder.createViewHolder(ItemTransactionRlBinding.inflate(layoutInflater))
    }


    class TransactionHolder : RecyclerView.ViewHolder {
        constructor(itemView: View) : super(itemView) {}

        constructor(itemView: View, binding: ViewDataBinding) : super(itemView) {
            itemView.tag = binding
        }

        fun bindData(
            mContext: Context,
            transaction: ResponseBean.Transaction,
            position: Int,
            type: Int
        ) {
            val binding = itemView.tag as ItemTransactionRlBinding
            //  充值：WEBOX_RECHARGE
            //  红包：WEBOX_REDPACKET
            //  转账收款：WEBOX_TRANSFER
            //  微包提现：WEBOX_WITHHOLDING
            //  红包-退款：WEBOX_REDPACKET_REFUND
            //  转账-退款：WEBOX_TRANSFER_REFUND

            var paymentType = when (transaction.tradeType){
                "WEBOX_RECHARGE" -> "充值"
                "WEBOX_REDPACKET" -> {
                    when {
                        "GROUP_LUCK" == transaction.tradeSubType -> "红包-拼手气红包"
                        "ONE_TO_ONE" == transaction.tradeSubType -> "红包-一对一红包"
                        "GROUP_NORMAL" == transaction.tradeSubType -> "红包-普通群红包"
                        else -> "红包"
                    }
                }
                "WEBOX_TRANSFER" -> {
                    if ("INCREASE" == transaction.direction) {
                        "转账收款"
                    } else {
                        "转账出款"
                    }
                   }
                "WEBOX_WITHHOLDING" ->  "提现"
                "WEBOX_REDPACKET_REFUND" -> "红包退款"
                "WEBOX_TRANSFER_REFUND" -> "转账-退款"
                else -> ""
            }

//            val amount = DecimalFormat("0.00").format(BigDecimal(transaction.amount).divide(
//                BigDecimal(100)))
            val amount = DecimalFormat("0.00").format(BigDecimal(transaction.amount))
            if(type == 4 || type == 5){
                binding.direction.text = amount
                binding.direction.setTextColor(Color.parseColor("#fe584c"))
                binding.arrow.visibility =View.GONE

                if("GROUP_LUCK".equals(transaction.tradeSubType)){
                    paymentType =  "拼手气红包"
                }else if("ONE_TO_ONE".equals(transaction.tradeSubType)){
                    paymentType = "一对一红包"
                }else if ("GROUP_NORMAL".equals(transaction.tradeSubType)){
                    paymentType =  "普通群红包"
                }else{
                    paymentType = ""
                }
                if(type== 4){
                    var num = ""
                    binding.num.visibility =View.VISIBLE
                    var redPacketReceiveCount = 0
                    if(!TextUtils.isEmpty(transaction.redPacketReceiveCount)) {
                        redPacketReceiveCount =
                            BigDecimal(transaction.redPacketReceiveCount).toInt()
                    }
                    val redPacketCount = BigDecimal(transaction.redPacketCount).toInt()
                    if("TIMEOUT"== transaction.status){
                        num = if(redPacketReceiveCount>0){
                            "已过期$redPacketReceiveCount/$redPacketCount"
                        }else{
                            "已过期"
                        }
                    }else if(redPacketReceiveCount >= redPacketCount) {
                        num = "已抢完"
                    }else if(redPacketReceiveCount>0){
                        "$redPacketReceiveCount/$redPacketCount"
                    }
                    binding.num.text = num
                }else{
                    binding.num.visibility =View.GONE
                }
                binding.direction.text = "$amount 元"
            }else {
                binding.num.visibility =View.GONE
                binding.arrow.visibility =View.VISIBLE
                if ("INCREASE" == transaction.direction) {
                    binding.direction.text = "+$amount"
                    binding.direction.setTextColor(Color.parseColor("#fe584c"))
                } else {
                    binding.direction.setTextColor(Color.parseColor("#333333"))
                    binding.direction.text = "-$amount"
                }
            }
            binding.paymentType.text  = paymentType
            binding.data = transaction

        }

        companion object {
            fun createViewHolder(binding: ViewDataBinding): TransactionHolder {
                return TransactionHolder(binding.root, binding)
            }
        }
    }

}
