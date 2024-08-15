package com.iimm.miliao.ui.xrce;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频录制进度条
 * create by xuan 2018-11-26 18:09:45
 */
public class Xrecprogressbar extends View {

    private static final int TOTAL_TIME = 25 * 1000;
    private static final int ONECE_TIME = 50;

    private int mWidth;
    private int mHeight;

    private Paint mBgPaint;
    private Paint mProPaint;
    private float max;
    private int currt;
    private int margin = 30;
    private List<Integer> target;

    private int inWdith;
    private Paint mTipPaint;
    private OnCompteListener mListener;
    private CountDownTimer countDownTimer = new CountDownTimer(TOTAL_TIME + ONECE_TIME * 10, ONECE_TIME) {
        @Override
        public void onTick(long millisUntilFinished) {
            currt += ONECE_TIME;

            if (currt >= TOTAL_TIME) {
                countDownTimer.cancel();
                currt = TOTAL_TIME;
                if (mListener != null) {
                    target.add(0, TOTAL_TIME + inWdith);
                    mListener.onCompte();
                }
            }

            invalidate();
        }

        @Override
        public void onFinish() {

        }
    };

    public Xrecprogressbar(Context context) {
        this(context, null);
    }

    public Xrecprogressbar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Xrecprogressbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        mProPaint = new Paint();
        mProPaint.setColor(Color.parseColor("#f1ce49"));
        mProPaint.setStyle(Paint.Style.FILL);
        mProPaint.setAntiAlias(true);

        mBgPaint = new Paint();
        mBgPaint.setColor(Color.parseColor("#000000"));
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setAntiAlias(true);
        mBgPaint.setAlpha(153);

        mTipPaint = new Paint();
        mTipPaint.setColor(Color.parseColor("#ffffff"));
        mTipPaint.setStyle(Paint.Style.FILL);
        mTipPaint.setAntiAlias(true);

        target = new ArrayList<>();
        max = TOTAL_TIME;
        currt = 0;


        invalidate();
    }

    public int getCurrentPro() {
        return currt;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        mWidth -= (margin * 2);

        inWdith = (int) (mHeight * 0.3f);
        mProPaint.setStrokeWidth(mHeight);
        mBgPaint.setStrokeWidth(mHeight);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            onDrawLows(canvas);
            return;
        }

        canvas.drawRoundRect(margin, 0, mWidth + margin, mHeight, mHeight >> 1, mHeight >> 1, mBgPaint);
        int pro = (int) (currt / max * mWidth);
        canvas.drawRoundRect(margin, 0, margin + pro, mHeight, mHeight >> 1, mHeight >> 1, mProPaint);

        for (int i = 0; i < target.size(); i++) {
            if (target.get(i) < max) {
                float left = target.get(i) / max * mWidth + margin;
                canvas.drawRect(left - inWdith, 0, left, mHeight, mTipPaint);
            }
        }
    }

    private void onDrawLows(Canvas canvas) {
        canvas.drawRect(margin, 0, mWidth + margin, mHeight, mBgPaint);
        int pro = (int) (currt / max * mWidth);
        canvas.drawRect(margin, 0, margin + pro, mHeight, mProPaint);
    }


    public void record() {
        if (isNotOver()) {
            countDownTimer.start();
        }
    }

    public void pause() {
        if (isNotOver()) {
            target.add(0, currt);
        }
        countDownTimer.cancel();
        invalidate();
    }

    public void popTask() {
        if (target != null && target.size() > 0) {
            target.remove(0);
            if (target.size() > 0) {
                currt = target.get(0);
            } else {
                currt = 0;
            }
        }

        invalidate();
    }

    public void reset() {
        countDownTimer.cancel();
        target.clear();
        currt = 0;
        invalidate();
    }

    public void addOnComptListener(OnCompteListener listener) {
        mListener = listener;
    }

    public boolean isNotOver() {
        return currt < TOTAL_TIME - ONECE_TIME;
    }


    public interface OnCompteListener {
        void onCompte();
    }
}
