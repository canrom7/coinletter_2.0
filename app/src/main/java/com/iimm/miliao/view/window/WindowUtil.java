package com.iimm.miliao.view.window;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.util.DisplayUtil;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.ScreenUtil;
import com.iimm.miliao.view.CircleImageView;
import com.iimm.miliao.view.window.rom.RomUtils;

import static android.content.Context.WINDOW_SERVICE;

public class WindowUtil {

    // private static final int mViewWidth = 100;
    private static final int mViewWidth = 60;
    private static final String TAG = "WindowUtil";
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private View mView;
    private View mCancelView;
    private CustomCancelView mCustomCancelView;
    private boolean isShowCancel;
    private WindowManager.LayoutParams mCancelViewLayoutParams;
    private boolean isTouch = true;
    private CircleImageView icon;


    private WindowUtil() {

    }

    public static WindowUtil getInstance() {
        return SingletonInstance.INSTANCE;
    }

    public void showPermissionWindow(Context context, OnPermissionListener onPermissionListener, String viewPath, View.OnClickListener onClickListener) {
        if (RomUtils.checkFloatWindowPermission(context)) {
            showWindow(context, viewPath, onClickListener);
        } else {
            onPermissionListener.showPermissionDialog();
        }
    }

    @SuppressLint("CheckResult")
    private void showWindow(Context context, String viewPath, View.OnClickListener onClickListener) {
        if (null == mWindowManager && null == mView && null == mCancelView) {
            mWindowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
            mView = LayoutInflater.from(context).inflate(R.layout.article_window, null);
            icon = mView.findViewById(R.id.aw_iv_image);
            if (viewPath != null) {
                Glide.with(context)
                        .load(viewPath)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(icon);
            }
            mCancelView = LayoutInflater.from(context).inflate(R.layout.activity_test, null);
            mCustomCancelView = mCancelView.findViewById(R.id.at_cancel_view);
            initListener(context, onClickListener);

            mLayoutParams = new WindowManager.LayoutParams();
            mCancelViewLayoutParams = new WindowManager.LayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                mCancelViewLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                mCancelViewLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }

            mCancelViewLayoutParams.format = PixelFormat.RGBA_8888;   //窗口透明
            mCancelViewLayoutParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;  //窗口位置
            mCancelViewLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            mCancelViewLayoutParams.width = DisplayUtil.dip2px(context, 2 * mViewWidth);
            mCancelViewLayoutParams.height = DisplayUtil.dip2px(context, 2 * mViewWidth);
            // mWindowManager.addView(mCancelView, mCancelViewLayoutParams);

            mLayoutParams.format = PixelFormat.RGBA_8888;   //窗口透明
//            mLayoutParams.gravity = Gravity.NO_GRAVITY;
            mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;  //窗口位置

            mLayoutParams.x = PreferenceUtils.getInt(context, "alertWindow_x", ScreenUtil.getScreenWidth(context));
            mLayoutParams.y = PreferenceUtils.getInt(context, "alertWindow_y", ((int) (ScreenUtil.getScreenHeight(context) / 2.0f)));
            mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            mLayoutParams.width = DisplayUtil.dip2px(context, mViewWidth);
            mLayoutParams.height = DisplayUtil.dip2px(context, mViewWidth);
            mWindowManager.addView(mView, mLayoutParams);
        }else{
            if (onClickListener != null) {
                initOnClick(onClickListener);
            }
        }
    }

    public void dismissWindow(Context context) {
        if (null != mCustomCancelView) {
            mCustomCancelView.destroy();
        }
        if (mLayoutParams != null) {
            PreferenceUtils.putInt(context, "alertWindow_x", mLayoutParams.x);
            PreferenceUtils.putInt(context, "alertWindow_y", mLayoutParams.y);
        }
        if (mWindowManager != null && mView != null) {
            mWindowManager.removeViewImmediate(mView);
            // mWindowManager.removeViewImmediate(mCancelView);
            mWindowManager = null;
            mCancelView = null;
            mView = null;
        }
    }

    private void initListener(final Context context, View.OnClickListener onClickListener) {
        initOnClick(onClickListener);
        //设置触摸滑动事件
        mView.setOnTouchListener(new View.OnTouchListener() {
            private int mStartX, mLastX, mStartY, mLastY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isTouch) {
                    return false;
                }
                int action = event.getAction();
                if (MotionEvent.ACTION_DOWN == action) {
                    mStartX = mLastX = (int) event.getRawX();
                    mStartY = mLastY = (int) event.getRawY();
                } else if (MotionEvent.ACTION_MOVE == action) {
                    Log.i(TAG, "moveX:" + event.getRawX() + "---" + "moveY:" + event.getRawY());
                    float rawX = event.getRawX() - (mLayoutParams.width / 2.0f);
                    float rawY = event.getRawY() - (mLayoutParams.height);
//                    int dx = (int) rawX - mLastX;
//                    int dy = (int) rawY - mLastY;
//                    mLayoutParams.x = mLayoutParams.x + dx;
//                    mLayoutParams.y = mLayoutParams.y + dy;
                    mLayoutParams.x = (int) rawX;
                    mLayoutParams.y = (int) rawY;
                    mWindowManager.updateViewLayout(v, mLayoutParams);
                    mLastX = (int) rawX;
                    mLastY = (int) rawY;
                } else if (MotionEvent.ACTION_UP == action || MotionEvent.ACTION_CANCEL == action) {
                    int upX = (int) event.getRawX();
                    float x = event.getX();
                    int upY = (int) event.getRawY();
                    int dx = upX - mStartX;
                    int dy = upY - mStartY;
                    Log.i(TAG, "upX:" + upX);
                    int screenWidth = ScreenUtil.getScreenWidth(context);
                    if (upX > (screenWidth / 2.0f)) {
                        //向右运动
                        startAnim(mLastX, upX, screenWidth, v);
                    } else {
                        //向左运动
                        startAnim(mLastX, upY, 0, v);
                    }
                    if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
                        return true;
                    }
                }
                return false;
/*
            int startX, startY;  //起始点
            boolean isMove;  //是否在移动
            int finalMoveX;  //最后通过动画将mView的X轴坐标移动到finalMoveX
            int statusBarHeight;

            int mCancelX = mWindowManager.getDefaultDisplay().getWidth();
            int mCancelY = mWindowManager.getDefaultDisplay().getHeight();

            boolean isRemove;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
                        if (resourceId > 0) {
                            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
                        }
                        isShowCancel = false;
                        isMove = false;
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        // 判断是CLICK还是MOVE
                        isMove = Math.abs(startX - event.getX()) >= ViewConfiguration.get(context).getScaledTouchSlop()
                                || Math.abs(startY - event.getY()) >= ViewConfiguration.get(context).getScaledTouchSlop();
                        mLayoutParams.x = (int) (event.getRawX() - startX);
                        //这里修复了刚开始移动的时候，悬浮窗的y坐标是不正确的，要减去状态栏的高度，可以将这个去掉运行体验一下
                        mLayoutParams.y = (int) (event.getRawY() - startY - statusBarHeight);
                        updateViewLayout();   //更新mView 的位置

                        if (!isShowCancel) {
                            isShowCancel = true;
                            if (null != mCustomCancelView) {
                                mCustomCancelView.startAnim(true);
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        // 计算两个View的距离，然后判断是否需要移除
                        isRemove = isRemoveAllView(mLayoutParams.x, mLayoutParams.y, mCancelX, mCancelY);
                        //判断mView是在Window中的位置，以中间为界
                        if (mLayoutParams.x + mView.getMeasuredWidth() / 2 >= mWindowManager.getDefaultDisplay().getWidth() / 2) {
                            finalMoveX = mWindowManager.getDefaultDisplay().getWidth() - mView.getMeasuredWidth();
                        } else {
                            finalMoveX = 0;
                        }
                        if (isRemove) {
                            WebViewActivity.IS_FLOATING = false;
                            dismissWindow();
                        } else {
                            if (isShowCancel) {
                                isShowCancel = false;
                                if (null != mCustomCancelView) {
                                    mCustomCancelView.startAnim(false);
                                }
                            }
                            //使用动画移动mView
                            ValueAnimator animator = ValueAnimator.ofInt(mLayoutParams.x, finalMoveX).setDuration(Math.abs(mLayoutParams.x - finalMoveX));
                            animator.setInterpolator(new AccelerateDecelerateInterpolator());
                            animator.addUpdateListener((ValueAnimator animation) -> {
                                mLayoutParams.x = (int) animation.getAnimatedValue();
                                updateViewLayout();
                            });
                            animator.start();
                        }
                        return isMove;
                }
                return false;
*/
            }
        });
    }

    private void initOnClick(View.OnClickListener onClickListener) {
        if (onClickListener != null) {
            mView.setOnClickListener(onClickListener);
        }
    }

    private void startAnim(int lastX, int startX, int tox, View v) {
        isTouch = false;
        final int[] mLastX = {startX};
        Log.i(TAG, "startX:" + startX + ",toX:" + tox);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(startX, tox);
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (mWindowManager != null) {
                    int x = (int) valueAnimator.getAnimatedValue();
                    int dx = (int) x - mLastX[0];
                    mLayoutParams.x = mLayoutParams.x + dx;
                    mWindowManager.updateViewLayout(v, mLayoutParams);
                    mLastX[0] = x;
                    Log.i(TAG, "动画的值：" + x);
                }
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isTouch = true;
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        valueAnimator.start();
    }

    private boolean isRemoveAllView(int x1, int y1, int x2, int y2) {
        //利用勾股定理计算出两个圆心（悬浮窗，右下角的圆弧）的距离，然后判断两者是否重合
        double radius = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        return radius <= DisplayUtil.dip2px(MyApplication.getContext(), (float) (100 * Math.sqrt(2) + 200));
    }

    private void updateViewLayout() {
        if (null != mView && null != mLayoutParams) {
            mWindowManager.updateViewLayout(mView, mLayoutParams);
        }
    }

    public void show() {
        if (null != mView) {
            mView.setVisibility(View.VISIBLE);
        }
    }

    public void hide() {
        if (null != mView) {
            mView.setVisibility(View.GONE);
        }
    }

    public void changeIcon(Context context,String viewPath) {
        if (viewPath != null && icon!=null) {
            Glide.with(context)
                    .load(viewPath)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(icon);
        }

    }

    public void ChangeZoomIcon(Context context) {
        if (icon!=null) {
            Glide.with(context)
                    .load(R.drawable.zoom_icon)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(icon);
        }
    }

    interface OnPermissionListener {
        void showPermissionDialog();
    }

    private static class SingletonInstance {
        @SuppressLint("StaticFieldLeak")
        private static final WindowUtil INSTANCE = new WindowUtil();
    }


}