package com.iimm.miliao.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

/**
 * Created by Administrator on 2019/3/6 0006.
 * https://blog.csdn.net/MyronChi/article/details/46955511
 */

public class ScrollTextView extends TextView {

    private int duration = 1500;
    private float number;

    public float getNumber() {
        return number;
    }

    public void setNumber(float number) {
        this.number = number;
        setText(String.format("%,.2f", number));

    }

    public ScrollTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 显示
     *
     * @param number
     */
    public void runWithAnimation(float number) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(
                this, "number", 0, number);
        objectAnimator.setDuration(duration);
        objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        objectAnimator.start();

    }
}
