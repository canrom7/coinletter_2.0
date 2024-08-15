package com.iimm.miliao.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.util.DisplayUtil;

import java.security.InvalidParameterException;

public class MyTab extends FrameLayout {
    private int currentIndex = 0;
    private View mSelectBg;
    private TextView mTvTab1;
    private TextView mTvTab2;
    private TextView mTvTab3;
    private View mHint1;
    private View mHint2;
    private View mHint3;
    private int[] hintNum;
    private LinearLayout llTab1;
    private LinearLayout llTab2;
    private LinearLayout llTab3;


    public MyTab(Context context) {
        super(context);
        init();
    }

    public MyTab(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.custom_my_view, this, true);
        Drawable drawable = getResources().getDrawable(R.drawable.main_fragment_tab_bg);
        setBackground(drawable);
        mSelectBg = findViewById(R.id.v_select_bg);
        mTvTab1 = findViewById(R.id.tv_tab1);
        mTvTab2 = findViewById(R.id.tv_tab2);
        mTvTab3 = findViewById(R.id.tv_tab3);
        llTab1 = findViewById(R.id.ll_tab1);
        llTab2 = findViewById(R.id.ll_tab2);
        llTab3 = findViewById(R.id.ll_tab3);
        mHint1 = findViewById(R.id.v_tab_hint1);
        mHint2 = findViewById(R.id.v_tab_hint2);
        mHint3 = findViewById(R.id.v_tab_hint3);
        event();
        switchPage(0);

    }

    private void event() {
        llTab1.setOnClickListener(v -> {
            switchPage(0);
            if (listener != null) {
                listener.setCurrentItem(0);
            }
        });
        llTab2.setOnClickListener(v -> {
            switchPage(1);
            if (listener != null) {
                listener.setCurrentItem(1);
            }
        });
        llTab3.setOnClickListener(v -> {
            switchPage(2);
            if (listener != null) {
                listener.setCurrentItem(2);
            }
        });

    }

    private void switchPage(int newindex) {
        switch (newindex) {
            case 0:
                mTvTab1.setTextColor(getResources().getColor(R.color.white));
                mTvTab2.setTextColor(getResources().getColor(R.color.my_tab_no_select_tv_color));
                mTvTab3.setTextColor(getResources().getColor(R.color.my_tab_no_select_tv_color));
                break;
            case 1:
                mTvTab1.setTextColor(getResources().getColor(R.color.my_tab_no_select_tv_color));
                mTvTab2.setTextColor(getResources().getColor(R.color.white));
                mTvTab3.setTextColor(getResources().getColor(R.color.my_tab_no_select_tv_color));
                break;
            case 2:
                mTvTab1.setTextColor(getResources().getColor(R.color.my_tab_no_select_tv_color));
                mTvTab2.setTextColor(getResources().getColor(R.color.my_tab_no_select_tv_color));
                mTvTab3.setTextColor(getResources().getColor(R.color.white));
                break;
        }
        startSwitchAnim(currentIndex, newindex);
        currentIndex = newindex;
    }
    public void setCurrentTabColor(int newindex) {
        switch (newindex) {
            case 0:
                mTvTab1.setTextColor(getResources().getColor(R.color.white));
                mTvTab2.setTextColor(getResources().getColor(R.color.my_tab_no_select_tv_color));
                mTvTab3.setTextColor(getResources().getColor(R.color.my_tab_no_select_tv_color));
                break;
            case 1:
                mTvTab1.setTextColor(getResources().getColor(R.color.my_tab_no_select_tv_color));
                mTvTab2.setTextColor(getResources().getColor(R.color.white));
                mTvTab3.setTextColor(getResources().getColor(R.color.my_tab_no_select_tv_color));
                break;
            case 2:
                mTvTab1.setTextColor(getResources().getColor(R.color.my_tab_no_select_tv_color));
                mTvTab2.setTextColor(getResources().getColor(R.color.my_tab_no_select_tv_color));
                mTvTab3.setTextColor(getResources().getColor(R.color.white));
                break;
        }
        currentIndex = newindex;
    }
    private void startSwitchAnim(int currentIndex, int newIndex) {
        if (currentIndex == newIndex) {
            return;
        }
        float width = getWidth() / 3.0F;
        float startX = currentIndex * width;
        float endX = newIndex * width;
        ValueAnimator valueAnimator = ObjectAnimator.ofFloat(startX, endX);
        valueAnimator.setInterpolator(new OvershootInterpolator());
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float x = (float) animation.getAnimatedValue();
                if (mSelectBg != null) {
                    mSelectBg.setX(x);
                    requestLayout();
                }
            }
        });
        valueAnimator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = DisplayUtil.dip2px(getContext(), 192);
        int height = DisplayUtil.dip2px(getContext(), 28);
        setMeasuredDimension(width, height);
    }

    public void update(int[] hintNum) {
        this.hintNum = hintNum;
        if (hintNum == null || hintNum.length != 3) {
            throw new InvalidParameterException("数组必须长度为3");
        }
        if (hintNum[0] > 0) {
            mHint1.setVisibility(VISIBLE);
        }
        if (hintNum[1] > 0) {
            mHint2.setVisibility(VISIBLE);
        }

        if (hintNum[2] > 0) {
            mHint3.setVisibility(VISIBLE);
        } else {
            mHint3.setVisibility(GONE);
        }
    }

    public void updateByIndex(int index, int num) {
        if (index < 0 || index > 2) {
            throw new InvalidParameterException("索引不正确");
        }
        if (hintNum != null) {
            hintNum[index] = num;
        } else {
            hintNum = new int[3];
            hintNum[index] = num;
        }
    }


    public int[] getHintNum() {
        if (hintNum != null) {
            return hintNum;
        } else {
            return new int[]{0, 0, 0};
        }
    }


    public int getHintNumByIndex(int index) {
        if (index < 0 || index > 2) {
            throw new InvalidParameterException("索引不正确");
        }
        if (hintNum != null) {
            return hintNum[index];
        } else {
            return 0;
        }
    }


    public void selectCurrentItem(int i) {
        switchPage(i);
    }

    private MyTabListener listener;

    public interface MyTabListener {
        void setCurrentItem(int i);
    }

    public void setListener(MyTabListener listener) {
        this.listener = listener;
    }
}
