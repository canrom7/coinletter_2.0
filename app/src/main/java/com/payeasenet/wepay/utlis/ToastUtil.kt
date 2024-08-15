package com.payeasenet.wepay.utlis

import android.content.Context
import android.widget.Toast

/**
 * Created by chenchaoyong on 2017/4/18.
 */

object ToastUtil {
    private var mToast: Toast? = null

    /**
     * 简版不重复的Toast

     * @param text 要显示的文字
     */
    fun showToast(context: Context, text: String) {

        if (mToast != null) {
            mToast!!.cancel()
            mToast = null
        }
        mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
        mToast!!.show()
    }
}


//fun Context.showToast(text: String) {
//    val toast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
//    val viewLayout = toast.view as LinearLayout
//    val textView = TextView(this)
//    textView.setTextSize(15.toFloat())
//    textView.setPadding(20, 10, 20 , 10)
//    textView.setTextColor(0x1494fc)
//    textView.setText(text)
//    viewLayout.background = getDrawable(R.drawable.bg_toast)
//    viewLayout.gravity = Gravity.CENTER
//    viewLayout.removeAllViews()
//    viewLayout.addView(textView)
//    toast.view = viewLayout
//    toast.show()
//}

fun Context.showToast(text: String) {

    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}