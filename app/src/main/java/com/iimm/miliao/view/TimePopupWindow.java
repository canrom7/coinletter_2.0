package com.iimm.miliao.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.contrarywind.listener.OnItemSelectedListener;
import com.contrarywind.view.WheelView;
import com.iimm.miliao.R;
import com.iimm.miliao.adapter.ArrayWheelViewAdapter;
import com.iimm.miliao.util.ToastUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimePopupWindow extends PopupWindow {

    private Calendar mCalendar = Calendar.getInstance();
    private int mYear, mMonth;
    private List<String> leftData = new ArrayList<>();
    private List<String> rightData = new ArrayList<>();
    private WheelView mLeftWheelView, mRightWheelView;
    private Context mContext;
    private String mLeft, mRight;
    private onSelectedTimeClick mOnSelectedTimeClick;

    public TimePopupWindow(Context context,onSelectedTimeClick onSelectedTimeClick) {
        this.mContext = context;
        this.mOnSelectedTimeClick = onSelectedTimeClick;
        initView();
    }

    public void setOnSelectedTimeClick(onSelectedTimeClick onSelectedTimeClick) {
        mOnSelectedTimeClick = onSelectedTimeClick;
    }

    private void initView() {
        mYear = mCalendar.get(Calendar.YEAR);
        mMonth = mCalendar.get(Calendar.MONTH);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mMenuView = inflater.inflate(R.layout.time_popup, null);
        setContentView(mMenuView);
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        // 因为某些机型是虚拟按键的,所以要加上以下设置防止挡住按键.
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setFocusable(true);
        setAnimationStyle(R.style.Buttom_Popwindow);
        mLeftWheelView = mMenuView.findViewById(R.id.left_wv);
        mRightWheelView = mMenuView.findViewById(R.id.right_wv);

        setData();
        mLeftWheelView.setCyclic(false);
        mRightWheelView.setCyclic(false);
        mLeftWheelView.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                mLeft = leftData.get(index);
            }
        });
        mRightWheelView.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                mRight = rightData.get(index);
            }
        });
        mLeftWheelView.setCurrentItem(0);
        mRightWheelView.setCurrentItem(0);
        mRight = "不限";
        mLeft = "不限";


        mMenuView.findViewById(R.id.time_popup_rl).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mMenuView.findViewById(R.id.time_popup_bottom).setOnClickListener(v -> {
        });
        mMenuView.findViewById(R.id.time_popup_close_iv).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mMenuView.findViewById(R.id.time_popup_complete_tv).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.equals(mRight, "不限")&&TextUtils.equals(mLeft, "不限")){
                    ToastUtil.showToast("请选择年份");
                    return;
                }



                String year = "0", month = "0";
                if (!TextUtils.equals(mLeft, "不限")) {
                    year = mLeft.replace("年", "");
                }
                if (!TextUtils.equals(mRight, "不限")) {
                    month = mRight.replace("月", "");
                }

                if (TextUtils.equals(mLeft, "不限")) {
                    if (TextUtils.equals(mRight, "不限")) {
                        if (mOnSelectedTimeClick != null) {
                            mOnSelectedTimeClick.onSelectedTime(0, "", "不限");
                        }
                    } else {
                        if (mOnSelectedTimeClick != null) {
                            mOnSelectedTimeClick.onSelectedTime(3, month, month + "月");
                        }
                    }

                    dismiss();
                } else if (TextUtils.equals(mRight, "不限")) {
                    if (TextUtils.equals(mLeft, "不限")) {
                        if (mOnSelectedTimeClick != null) {
                            mOnSelectedTimeClick.onSelectedTime(0, "", "不限");
                        }
                    } else {
                        if (mOnSelectedTimeClick != null) {
                            mOnSelectedTimeClick.onSelectedTime(2, year, year + "年");
                        }
                    }
                    dismiss();
                } else {
                    if (Integer.valueOf(year) == mYear) {
                        if (Integer.valueOf(month) > (mMonth + 1)) {
                            ToastUtil.showToast("请选择正确时间");
                        } else {
                            if (mOnSelectedTimeClick != null) {
                                mOnSelectedTimeClick.onSelectedTime(1, year + "-" + month, year + "年-" + month + "月");
                            }
                            dismiss();
                        }
                    } else {
                        if (mOnSelectedTimeClick != null) {
                            mOnSelectedTimeClick.onSelectedTime(1, year + "-" + month, year + "年-" + month + "月");
                        }
                        dismiss();
                    }
                }
            }
        });


    }

    public interface onSelectedTimeClick {
        void onSelectedTime(int type, String monthStr, String time);
    }

    private void setData() {
        leftData.add("不限");

        leftData.add(mYear + "年");
        leftData.add(mYear - 1 + "年");
        leftData.add(mYear - 2 + "年");
        leftData.add(mYear - 3 + "年");
        leftData.add(mYear - 4 + "年");

        rightData.add("不限");
        rightData.add("1月");
        rightData.add("2月");
        rightData.add("3月");
        rightData.add("4月");
        rightData.add("5月");
        rightData.add("6月");
        rightData.add("7月");
        rightData.add("8月");
        rightData.add("9月");
        rightData.add("10月");
        rightData.add("11月");
        rightData.add("12月");
        mLeftWheelView.setAdapter(new ArrayWheelViewAdapter(leftData));
        mRightWheelView.setAdapter(new ArrayWheelViewAdapter(rightData));
    }


}
