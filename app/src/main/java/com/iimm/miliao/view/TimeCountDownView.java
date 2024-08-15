package com.iimm.miliao.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.iimm.miliao.util.DisplayUtil;

public class TimeCountDownView extends View {
    private Paint linePaint;
    private Paint circlePaint;
    private int progress = 0;  //进度


    private final int DEFAULT_SIZE = DisplayUtil.dip2px(getContext(), 20);
    private RectF mRectF;


    public TimeCountDownView(Context context) {
        super(context);
        init();
    }


    public TimeCountDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        circlePaint = new Paint();


    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int resultWidth = 0;
        int resultHeight = 0;
        switch (widthMode) {
            case MeasureSpec.AT_MOST:
                if (widthSize < DEFAULT_SIZE) {
                    resultWidth = widthSize;
                    resultHeight = resultWidth;
                } else {
                    resultWidth = DEFAULT_SIZE;
                    resultHeight = DEFAULT_SIZE;
                }
                break;
            case MeasureSpec.EXACTLY:
                resultWidth = widthSize;
                resultHeight = widthSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                resultWidth = DEFAULT_SIZE;
                resultHeight = DEFAULT_SIZE;
                break;
        }
        setMeasuredDimension(resultWidth, resultHeight);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawExternal(canvas);
        drawInternal(canvas);

    }


    /**
     * 画内部
     *
     * @param canvas
     */
    private void drawInternal(Canvas canvas) {
        circlePaint.setAntiAlias(true);
        circlePaint.setStrokeWidth(2);
        circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        circlePaint.setColor(Color.parseColor("#8C9AB8"));
        if (mRectF == null) {
            mRectF = new RectF();
        }
        mRectF.left = 2;
        mRectF.right = getWidth() - 2;
        mRectF.top = 2;
        mRectF.bottom = getHeight() - 2;
        canvas.drawArc(mRectF, -90, progress, true, circlePaint);
    }

    /**
     * 画外部
     *
     * @param canvas
     */
    private void drawExternal(Canvas canvas) {
        canvas.save();
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(1);
        linePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        linePaint.setColor(Color.parseColor("#8C9AB8"));
        for (int i = 0; i < 12; i++) {
            canvas.drawCircle(getWidth() / 2, 5, 1, linePaint);
            canvas.rotate(30, getWidth() / 2, getWidth() / 2);
        }
        canvas.restore();
    }


    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        postInvalidate();
    }
}
