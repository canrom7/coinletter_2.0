package com.iimm.miliao.ui.message.multi;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.iimm.miliao.AppConfig;
import com.iimm.miliao.R;
import com.iimm.miliao.adapter.GroupSignRewardAdapter;
import com.iimm.miliao.bean.GroupSignDate;
import com.iimm.miliao.bean.GroupSignReward;
import com.iimm.miliao.bean.redpacket.GroupSignDetail;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.CalendarUtil;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.view.CalendarView;
import com.iimm.miliao.view.listener.OnPagerChangeListener;
import com.iimm.miliao.volley.Result;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

public class GroupSignActivity extends BaseActivity {
    RoundedImageView headview;
    TextView nickname;
    TextView sign_day;
    TextView record_status;
    Button sign;
    RecyclerView award_record;
    private CalendarView calendarView;
    private String mLoginUserId;
    private Dialog mSignInCalendarDialog;
    private String startDateStr = "2019.1";
    private String endDateStr = "2020.12";
    private int startDate[] = CalendarUtil.strToArray(startDateStr);
    private List<String> signInDateList = new ArrayList<>();
    private Map<Integer, HashSet<String>> mSignInMap = new HashMap<>();
    private int signCount = 0;
    private String roomid;
    GroupSignRewardAdapter groupSignRewardAdapter;
    List<GroupSignReward> lists;
    private GroupSignDetail groupSignDetail;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_group);
        roomid = getIntent().getStringExtra("roomId");
        initActionBar();
        initView();
        signDetail();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setVisibility(View.VISIBLE);
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText("群签到");
        TextView tvRight = (TextView) findViewById(R.id.tv_title_right);
        tvRight.setVisibility(View.VISIBLE);
        tvRight.setText(getResources().getString(R.string.sign_calendar));
        tvRight.setOnClickListener(view -> {
            int currentDate[] = CalendarUtil.getCurrentDate();
            if (mSignInCalendarDialog == null || !mSignInCalendarDialog.isShowing()) {
                getSignInMonthDate(currentDate);
            }

        });
        findViewById(R.id.iv_title_left).setOnClickListener(v -> {
            finish();
        });
    }

    private void initView() {
        headview = findViewById(R.id.headview);
        nickname = findViewById(R.id.nickname);
        sign_day = findViewById(R.id.sign_day);
        record_status = findViewById(R.id.record_status);
        sign = findViewById(R.id.sign);
        award_record = findViewById(R.id.award_record);
        lists = new ArrayList<>();

        groupSignRewardAdapter = new GroupSignRewardAdapter(lists, this);
        award_record.setLayoutManager(new LinearLayoutManager(this));
        award_record.setAdapter(groupSignRewardAdapter);
        sign.setOnClickListener(v -> {
            sign();
        });

        Glide.with(this).load(AvatarHelper.getAvatarUrl(coreManager.getSelf().getUserId(), false))
                .into(headview);
        nickname.setText(coreManager.getSelf().getNickName());
    }

    /*
     * 签到
     * */
    public void sign() {
        if (isLoading) {
            return;
        }
        isLoading = true;
        Map<String, String> map = new HashMap<>();
        map.put("access_token", coreManager.getSelfStatus().accessToken);
        map.put("roomJid", roomid);
        map.put("nickName", coreManager.getSelf().getNickName());
        HttpUtils.post().params(map).url(AppConfig.ROOM_GROUP_SIGN).build().execute(new BaseCallback<String>(String.class) {
            @Override
            public void onResponse(ObjectResult<String> result) {
                isLoading = false;
                if (result.getResultCode() == 1) {
                    String string = result.getData();
                    sign_day.setText("你已连续签到" + string + "天");
                    signCount = Integer.parseInt(string);
                    sign.setEnabled(false);
                    sign.setText("已签到");
                    record_status.setText("今天已签到");

                } else {
                    ToastUtil.showLongToast(GroupSignActivity.this, result.getResultMsg());
                }
            }

            @Override
            public void onError(Call call, Exception e) {
                isLoading = false;
                ToastUtil.showLongToast(GroupSignActivity.this, e.getMessage());
            }
        });
    }

    /*
     *
     * 获得当前页面信息
     * */
    public void signDetail() {
        Map<String, String> map = new HashMap<>();
        map.put("access_token", coreManager.getSelfStatus().accessToken);
        map.put("roomJid", roomid);
        HttpUtils.post().params(map).url(AppConfig.ROOM_GROUP_SIGN_DETAIL).build().execute(new BaseCallback<String>(String.class) {
            @Override
            public void onResponse(ObjectResult<String> result) {
                if (result.getResultCode() == 1) {
                    groupSignDetail = JSON.parseObject(result.getData(), GroupSignDetail.class);
                    sign_day.setText("你已连续签到" + groupSignDetail.getSerialCount() + "天");
                    signCount = Integer.parseInt(groupSignDetail.getSerialCount());
                    if (groupSignDetail.getStatus() == 1) {
                        sign.setEnabled(false);
                        sign.setText("已签到");
                        record_status.setText("今天已签到");
                    }
                    lists.addAll(groupSignDetail.getRoomSignInGift());
                    groupSignRewardAdapter.notifyDataSetChanged();
                } else {
                    ToastUtil.showLongToast(GroupSignActivity.this, result.getResultMsg());
                }
            }

            @Override
            public void onError(Call call, Exception e) {
                ToastUtil.showLongToast(GroupSignActivity.this, e.getMessage());
            }
        });
    }

    protected void onDestroy() {
        super.onDestroy();
        if (mSignInMap != null) {
            mSignInMap.clear();
        }
        if (mSignInCalendarDialog != null) {
            mSignInCalendarDialog.dismiss();
            mSignInCalendarDialog = null;
        }
        if (calendarView != null) {
            calendarView = null;
        }
    }

    /**
     * 显示签到日历弹出框
     */
    private void showSignInCalendarDialog(int position, List<String> signInDateList) {
        if (mSignInCalendarDialog == null) {
            initSignInCalendarDialog();
        } else {
            if (calendarView != null) {
                calendarView.refreshSignInMonthDate(position, signInDateList);
            } else {
                initSignInCalendarDialog();
            }
        }
        mSignInCalendarDialog.show();
    }

    /**
     * 获取某月的签到的信息
     */
    public void getSignInMonthDate(final int date[]) {
        String yearAndMonth = date[0] + "-" + date[1];
        final int datePosition = CalendarUtil.dateToPosition(date[0], date[1], startDate[0], startDate[1]);
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomJid ", roomid);
        params.put("monthStr", yearAndMonth);
        HttpUtils.get().url(coreManager.getConfig().ROOM_GROUP_INFO_BY_MONTH)
                .params(params)
                .build()
                .execute(new ListCallback<GroupSignDate>(GroupSignDate.class) {
                    @Override
                    public void onResponse(ArrayResult<GroupSignDate> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == Result.CODE_SUCCESS && result.getData() != null) {
                            List<GroupSignDate> signInfoListEntity = result.getData();
                            for (GroupSignDate infoBean : signInfoListEntity) {
                                long time = Long.parseLong(infoBean.getSignInDate());
                                String date = TimeUtils.millis2String(time, new SimpleDateFormat("yyyy.MM.dd"));
                                Log.i(TAG, "onNext: date:" + date);
                                HashSet<String> signInSet;
                                if (mSignInMap.get(datePosition) == null) {
                                    signInSet = new HashSet<>();
                                } else {
                                    signInSet = mSignInMap.get(datePosition);
                                }
                                if (!signInSet.contains(date)) {
                                    signInDateList.add(date);
                                    signInSet.add(date);
                                }
                                mSignInMap.put(datePosition, signInSet);
                            }
                            showSignInCalendarDialog(datePosition, signInDateList);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(GroupSignActivity.this);
                    }
                });

    }

    /**
     * 初始化签到日历弹出框
     */
    private void initSignInCalendarDialog() {
        mSignInCalendarDialog = new Dialog(this, R.style.dialog_bottom_full);
        mSignInCalendarDialog.setCanceledOnTouchOutside(true);
        mSignInCalendarDialog.setCancelable(true);
        Window window = mSignInCalendarDialog.getWindow();
        window.setGravity(Gravity.CENTER);
        //window.setWindowAnimations(R.style.share_animation);
        View dialogView = View.inflate(this, R.layout.activity_sign_in_calendar, null);
        final TextView title = dialogView.findViewById(R.id.title);
        TextView tv_sign_in_calendar_continuous = dialogView.findViewById(R.id.tv_sign_in_calendar_continuous);
        View iv_last = dialogView.findViewById(R.id.iv_last);
        View iv_next = dialogView.findViewById(R.id.iv_next);
        View iv_close = dialogView.findViewById(R.id.iv_close);
        calendarView = dialogView.findViewById(R.id.calendar);
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSignInCalendarDialog.dismiss();
            }
        });
        tv_sign_in_calendar_continuous.setText(String.format(getResources().getString(R.string.sign_in_day_count), signCount + ""));
        iv_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarView.lastMonth();
            }
        });
        iv_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarView.nextMonth();
            }
        });
        int currentDate[] = CalendarUtil.getCurrentDate();
        calendarView
                .setStartEndDate(startDateStr, endDateStr)
                .setInitDate(currentDate[0] + "." + currentDate[1])
                .showCurrentDate(true)
                .setMultiDate(signInDateList)
                .init();
        title.setText(currentDate[0] + " - " + currentDate[1]);
        calendarView.setOnPagerChangeListener(new OnPagerChangeListener() {
            @Override
            public void onPagerChanged(int[] date) {
                Log.i(TAG, "onPagerChanged: onPagerChanged");
                title.setText(date[0] + " - " + date[1]);
                getSignInMonthDate(date);
            }
        });
        calendarView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // TODO Auto-generated method stub

            }
        });
        window.setContentView(dialogView);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
    }
}
