package com.iimm.miliao.util;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;

/**
 * Created by Administrator on 2017/10/26.
 */

public class UiUtils {
    private static final int MIN_CLICK_DELAY_TIME = 800;
    private static long lastClickTime;
    private static int clickedView;

    public static void updateNum(TextView numTv, int unReadNum) {
        if (numTv == null) {
            return;
        }
        if (unReadNum < 1) {
            numTv.setText("");
            numTv.setVisibility(View.INVISIBLE);
        } else if (unReadNum > 99) {
            numTv.setText("99+");
            numTv.setVisibility(View.VISIBLE);
        } else {
            numTv.setText(String.valueOf(unReadNum));
            numTv.setVisibility(View.VISIBLE);
        }
    }


    public static void updateNumToMessageFragment(TextView numTv, int unReadNum, Friend friend) {
        if (numTv == null) {
            return;
        }
        if (friend.getOfflineNoPushMsg() > 0) {
            //设置了免打扰
            Drawable drawable = numTv.getContext().getResources().getDrawable(R.drawable.message_num_bg_by_no_push);
            numTv.setBackground(drawable);
        } else {
            //没有设置
            Drawable drawable = numTv.getContext().getResources().getDrawable(R.drawable.message_num_bg_by_has_push);
            numTv.setBackground(drawable);
        }
        if (unReadNum < 1) {
            numTv.setText("");
            numTv.setVisibility(View.INVISIBLE);
        } else if (unReadNum > 99) {
            numTv.setText("99+");
            numTv.setVisibility(View.VISIBLE);
        } else {
            numTv.setText(String.valueOf(unReadNum));
            numTv.setVisibility(View.VISIBLE);
        }
    }


    /**
     * @deprecated {@link UiUtils#isNormalClick(View)}
     */
    @Deprecated
    public static boolean isNormalClick() {
        boolean isNormal = false;
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            isNormal = true;
        }
        lastClickTime = currentTime;
        return isNormal;
    }

    public static boolean isNormalClick(View view) {
        // hashCode确保同一个view计算出来是一样的，不同view也几乎不会遇到相同hashCode的情况，
        long currentTime = System.currentTimeMillis();
        if (clickedView != view.hashCode()) {
            // 点击不同的view，不限制时间间隔，
            clickedView = view.hashCode();
            lastClickTime = currentTime;
            return true;
        }
        // 同一个view多次点击，限制连续点击时间，
        clickedView = view.hashCode();
        boolean isNormal = false;
        if ((currentTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            isNormal = true;
        }
        lastClickTime = currentTime;
        return isNormal;
    }
}
