package com.payeasenet.wepay.ui.activity


import android.databinding.DataBindingUtil
import android.text.InputFilter
import android.text.Spanned
import com.ehking.sdk.wepay.ui.base.BaseActivity
import com.payeasenet.wepay.ui.viewModel.WithdrawModel
import com.iimm.miliao.MyApplication
import com.iimm.miliao.R
import com.iimm.miliao.databinding.ActivityWithdrawBinding
import com.iimm.miliao.ui.base.CoreManager
import kotlinx.android.synthetic.main.activity_withdraw.*
import kotlinx.android.synthetic.main.toolbar.*

class WithdrawActivity : BaseActivity() {
    var binding: ActivityWithdrawBinding? = null
    override fun setContentView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_withdraw)
    }

    override fun initActionBar() {
        toolbar.title = "余额提现"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    override fun fetchData() {
        binding?.data = WithdrawModel(this)
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

        binding?.data?.fee?.set("提现手续费" + CoreManager.requireConfig(MyApplication.getInstance()).weiBaoTransferRate + "%")
        binding?.data?.arrivalAmountText?.set("下一步")
       /* amount.addTextChangedListener(object :TextWatcher{
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {


                    try {
                        val decimal= DecimalFormat("0.00")
                        val feeAmount = BigDecimal(s.toString()).multiply(BigDecimal(CoreManager.requireConfig(MyApplication.getInstance()).weiBaoTransferRate).divide(BigDecimal(100)))
                            .setScale(2, BigDecimal.ROUND_FLOOR).toDouble()

                        val inputAmount = BigDecimal(s.toString()).toDouble()

                        val arrivalAmountTx = BigDecimal(inputAmount - feeAmount)
                            .setScale(2, BigDecimal.ROUND_FLOOR).toDouble()

//                        binding?.data?.arrivalAmount?.set("" + (100 * arrivalAmountTx).toInt())
                        binding?.data?.arrivalAmountText?.set("下一步（实际到账金额：${decimal.format(arrivalAmountTx)} 元)")
                        binding?.data?.fee?.set("手续费金额" + decimal.format(feeAmount) + "元")
                    }catch (e:Exception){

                    }

            }

        })*/
    }

}
