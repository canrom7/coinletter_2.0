package com.payeasenet.wepay.ui.activity


import android.databinding.DataBindingUtil
import android.text.*
import com.ehking.sdk.wepay.ui.base.BaseActivity
import com.ehking.sdk.wepay.utlis.LogUtil
import com.payeasenet.wepay.ui.viewModel.RedPacketModel
import com.iimm.miliao.R
import com.iimm.miliao.databinding.ActivityRedPacketBinding
import kotlinx.android.synthetic.main.activity_recharge.amount
import kotlinx.android.synthetic.main.activity_red_packet.*
import kotlinx.android.synthetic.main.toolbar.*
import java.math.BigDecimal
import java.text.DecimalFormat

class RedPacketActivity : BaseActivity() {
    var binding: ActivityRedPacketBinding? = null
    override fun setContentView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_red_packet)
    }

    override fun initActionBar() {
        when(intent.getIntExtra("type",1)){
            1->{
                toolbar.title = "发送一对一红包"
            }
            2->{
                toolbar.title = "发送普通群红包"
                tv_amount.text ="单个金额"
            }
            3->{
                toolbar.title = "发送手气群红包"
                tv_amount.text ="总金额"
            }
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    override fun fetchData() {
        binding?.data = RedPacketModel(this)
        when(intent.getIntExtra("type",1)){
            1->{
                binding?.data?.isSingle?.set(true)
            }
            2->{
                binding?.data?.isSingle?.set(false)
            }
            3->{
                binding?.data?.isSingle?.set(false)
            }
        }
        singleAmount.filters = arrayOf(object : InputFilter{
            override fun filter(
                source: CharSequence?,
                start: Int,
                end: Int,
                dest: Spanned?,
                dstart: Int,
                dend: Int
            ): CharSequence? {
                if (source.toString() == "." && dstart == 0 && dend == 0) {//判断小数点是否在第一位
                    singleAmount.setText("0$source$dest")//给小数点前面加0
                    singleAmount.setSelection(2)//设置光标
                }

                if (dest.toString().indexOf(".") != -1 && (dest?.length!! - dest.toString().indexOf(".")) > 2) {//判断小数点是否存在并且小数点后面是否已有两个字符

                    if ((dest?.length!! - dstart) < 3) {//判断现在输入的字符是不是在小数点后面
                        return ""//过滤当前输入的字符
                    }
                }
                return null
            }

        })
        amount.filters = arrayOf(object : InputFilter{
            override fun filter(
                source: CharSequence?,
                start: Int,
                end: Int,
                dest: Spanned?,
                dstart: Int,
                dend: Int
            ): CharSequence? {
                if (source.toString() == "." && dstart == 0 && dend == 0) {//判断小数点是否在第一位
                    amount.setText("0$source$dest")//给小数点前面加0
                    amount.setSelection(2)//设置光标
                }

                if (dest.toString().indexOf(".") != -1 && (dest?.length!! - dest.toString().indexOf(".")) > 2) {//判断小数点是否存在并且小数点后面是否已有两个字符

                    if ((dest?.length!! - dstart) < 3) {//判断现在输入的字符是不是在小数点后面
                        return ""//过滤当前输入的字符
                    }
                }
                return null
            }

        })
        val decimalFormat = DecimalFormat("0.00")
        singleAmount.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                try {

                    if (intent.getIntExtra("type", 1) == 3) {
                        LogUtil.d("ss:"+s.toString())
                        binding?.data?.allAmount?.set(decimalFormat.format(BigDecimal(s.toString())))
                        binding?.data?.allAmountText?.set("¥  "+decimalFormat.format(BigDecimal(s.toString())))
                    } else if (!TextUtils.isEmpty(num.text.toString()) && intent.getIntExtra(
                            "type",
                            1
                        ) == 2
                    ) {
                        val amountText  = decimalFormat.format(BigDecimal((num.text.toString())).multiply(BigDecimal(s.toString())))
                        binding?.data?.allAmount?.set("$amountText")
                        binding?.data?.allAmountText?.set("¥  $amountText")

                    }
                } catch (e :Exception){

              }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
        num.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                try {
                    if(!TextUtils.isEmpty(singleAmount.text.toString()) &&intent.getIntExtra("type",1)==2){
                        val amountText  = decimalFormat.format(BigDecimal((singleAmount.text.toString())).multiply(BigDecimal(s.toString())))
                        binding?.data?.allAmount?.set("$amountText")
                        binding?.data?.allAmountText?.set("¥  $amountText")

                    }
                }catch (e :Exception){

                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
    }

}
