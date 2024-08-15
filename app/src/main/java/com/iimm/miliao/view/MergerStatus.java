package com.iimm.miliao.view;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.util.SkinUtils;

/**
 * Created by Administrator on 2017/12/14 0014.
 * 兼容沉浸式状态栏
 */

public class MergerStatus extends Toolbar {
    private String TAG = "MergerStatus";
    private TextView textView;

    public MergerStatus(Context context) {
        this(context, null);
    }

    public MergerStatus(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MergerStatus(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }


    private void setup() {
//        int mCompatPaddingTop = 0;
//        if (SkinUtils.getSkin(getContext()).getColorName() == R.string.skin_simple_white) {  //
//            this.setPadding(getPaddingLeft(), getPaddingTop() + mCompatPaddingTop, getPaddingRight(), getPaddingBottom());
//        }else{
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                mCompatPaddingTop = getStatusHeight();
//            }
//        }
//        this.setPadding(getPaddingLeft(), getPaddingTop() + mCompatPaddingTop, getPaddingRight(), getPaddingBottom());
        //小于了 6.0 系统  状态栏被设置了半透明了 ,直接设置 自定义 toolBar，充当 状态栏背景
        //大于6.0 的时候  直接设置了状态栏的背景  ，所以  不需要 自定义 toolbar  充当，也就不设置 top padding
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            int mCompatPaddingTop = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mCompatPaddingTop = getStatusHeight();
            }
            this.setPadding(getPaddingLeft(), getPaddingTop() + mCompatPaddingTop, getPaddingRight(), getPaddingBottom());
        }


        // 更换Toolbar背景颜色
        int skinColor;
        int colorName = SkinUtils.getSkin(getContext()).getColorName();
        if (colorName == R.string.skin_simple_white) {
            skinColor = ContextCompat.getColor(getContext(), R.color.tb_bg_skin_simple_white);
        } else {
            skinColor = SkinUtils.getSkin(getContext()).getPrimaryColor();
        }
        this.setBackgroundColor(skinColor);
    }

    private int getStatusHeight() {
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        // Log.e("zq", "状态栏高度：" + px2dp(statusBarHeight) + "dp");
        return statusBarHeight;
    }

    private float px2dp(float pxVal) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (pxVal / scale);
    }
}
