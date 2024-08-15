package com.payeasenet.wepay.ui.view.adapter;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

/**
 * 类 <code>BindingAdapters</code>
 * <p>
 * 描述：
 * </p>
 * 创建日期：2019年11月23日
 *
 * @author zhaoyong.chen@ehking.com
 * @version 1.0
 */
public class BindingAdapters {

    @BindingAdapter("android:src")
    public static void setSrc(ImageView view, int resId) {
        view.setImageResource(resId);
    }
}
