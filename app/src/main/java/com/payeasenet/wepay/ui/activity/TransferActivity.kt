package com.payeasenet.wepay.ui.activity


import android.databinding.DataBindingUtil
import android.text.InputFilter
import android.text.Spanned
import com.ehking.sdk.wepay.ui.base.BaseActivity
import com.payeasenet.wepay.ui.viewModel.TransferModel
import com.iimm.miliao.R
import com.iimm.miliao.databinding.ActivityTransfer2Binding
import kotlinx.android.synthetic.main.activity_transfer2.*
import kotlinx.android.synthetic.main.toolbar.*


class TransferActivity : BaseActivity() {
    var binding: ActivityTransfer2Binding? = null
    override fun setContentView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_transfer2)
    }

    override fun initActionBar() {
        toolbar.title = "转账"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    override fun fetchData() {
        binding?.data = TransferModel(this)
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
    }

}
