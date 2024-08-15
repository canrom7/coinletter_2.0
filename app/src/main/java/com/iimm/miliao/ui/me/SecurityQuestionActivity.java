package com.iimm.miliao.ui.me;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.iimm.miliao.R;
import com.iimm.miliao.adapter.SecurityQuestionAdapter;
import com.iimm.miliao.bean.QuestionsBean;
import com.iimm.miliao.bean.SecurityQuestion;
import com.iimm.miliao.bean.SecurityQuestionBean;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * MrLiu253@163.com
 * 密保问题
 *
 * @time 2019-08-08
 */
public class SecurityQuestionActivity extends BaseActivity {
    private Spinner mOneSpinner;
    private EditText mOneEt;
    private Spinner mTwoSpinner;
    private EditText mTwoEt;
    private Spinner mThreeSpinner;
    private EditText mThreeEt;
    private TextView mDoneTv;
    private String mOneAnswer;
    private String mTwoAnswer;
    private String mThreeAnswer;
    private List<SecurityQuestionBean.DataBean> mBeanList = new ArrayList<>();
    private List<SecurityQuestionBean.DataBean> mTwoList = new ArrayList<>();
    private List<SecurityQuestionBean.DataBean> mThreeList = new ArrayList<>();
    private SecurityQuestionAdapter mQuestionAdapter;
    private SecurityQuestionAdapter mTwoQuestionAdapter;
    private SecurityQuestionAdapter mThreeQuestionAdapter;
    private int mType;//1注册时候   2安全设置里
    private List<QuestionsBean> mQuestionsBeans;

    public static void securityQuestion(Context ctx) {
        Intent intent = new Intent(ctx, SecurityQuestionActivity.class);
        ctx.startActivity(intent);
    }

    public static void securityQuestion(Context ctx, int type, List<QuestionsBean> list) {
        Intent intent = new Intent(ctx, SecurityQuestionActivity.class);
        intent.putExtra("type", type);
        intent.putParcelableArrayListExtra("list", (ArrayList<? extends Parcelable>) list);
        ctx.startActivity(intent);
    }

    public SecurityQuestionActivity() {
        noLoginRequired();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_question);
        mType = getIntent().getIntExtra("type", 0);
        mQuestionsBeans = getIntent().getParcelableArrayListExtra("list");
        initActionBar();
        initView();
        initClickListener();
    }

    private void initView() {
        mOneSpinner = findViewById(R.id.security_question_spinner_one);
        mOneSpinner.setDropDownVerticalOffset(110);
        mOneEt = findViewById(R.id.security_question_one_et);
        mTwoSpinner = findViewById(R.id.security_question_spinner_two);
        mTwoSpinner.setDropDownVerticalOffset(110);
        mTwoEt = findViewById(R.id.security_question_two_et);
        mThreeSpinner = findViewById(R.id.security_question_spinner_three);
        mThreeSpinner.setDropDownVerticalOffset(110);
        mThreeEt = findViewById(R.id.security_question_three_et);
        mDoneTv = findViewById(R.id.security_question_done);

        mQuestionAdapter = new SecurityQuestionAdapter(this, mBeanList);
        mTwoQuestionAdapter = new SecurityQuestionAdapter(this, mTwoList);
        mThreeQuestionAdapter = new SecurityQuestionAdapter(this, mThreeList);
        mOneSpinner.setAdapter(mQuestionAdapter);
        mTwoSpinner.setAdapter(mTwoQuestionAdapter);
        mThreeSpinner.setAdapter(mThreeQuestionAdapter);
        getDataList();
    }

    private void getDataList() {
        DialogHelper.showDefaulteMessageProgressDialog(SecurityQuestionActivity.this);
        HttpUtils.getNoSecret().url(CoreManager.requireConfig(mContext).QUESTION_LIST)
                .build()
                .execute(new ListCallback<SecurityQuestionBean.DataBean>(SecurityQuestionBean.DataBean.class) {
                    @Override
                    public void onResponse(ArrayResult<SecurityQuestionBean.DataBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            mBeanList.clear();
                            mTwoList.clear();
                            mThreeList.clear();
                            List<List<SecurityQuestionBean.DataBean>> lists = averageAssign(result.getData(), 3);
                            if (result.getData().size() < 3) {
                                ToastUtil.showToast(SecurityQuestionActivity.this, "获取问题列表失败");
                                return;
                            }
                            if (lists != null && lists.size() != 3) {
                                ToastUtil.showLongToast(SecurityQuestionActivity.this, "获取密保问题失败");
                                finish();
                                return;
                            }
                            mBeanList.addAll(lists.get(0));
                            mQuestionAdapter.setData(lists.get(0));
                            mTwoList.addAll(lists.get(1));
                            mTwoQuestionAdapter.setData(lists.get(1));
                            mThreeList.addAll(lists.get(2));
                            mThreeQuestionAdapter.setData(lists.get(2));

                            if (mQuestionsBeans != null && mQuestionsBeans.size() == 3) {
                                for (int i = 0; i < mBeanList.size(); i++) {
                                    if (TextUtils.equals(mBeanList.get(i).getId(), mQuestionsBeans.get(0).getQ())) {
                                        mOneSpinner.setSelection(i);
                                        mOneEt.setText(mQuestionsBeans.get(0).getA());
                                    }
                                }
                                for (int two = 0; two < mTwoList.size(); two++) {
                                    if (TextUtils.equals(mTwoList.get(two).getId(), mQuestionsBeans.get(1).getQ())) {
                                        mTwoSpinner.setSelection(two);
                                        mTwoEt.setText(mQuestionsBeans.get(1).getA());
                                    }
                                }
                                for (int three = 0; three < mThreeList.size(); three++) {
                                    if (TextUtils.equals(mThreeList.get(three).getId(), mQuestionsBeans.get(2).getQ())) {
                                        mThreeSpinner.setSelection(three);
                                        mThreeEt.setText(mQuestionsBeans.get(2).getA());
                                    }
                                }
                            }
                        } else {
                            ToastUtil.showToast(SecurityQuestionActivity.this, result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(SecurityQuestionActivity.this, e.getMessage());
                    }
                });
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.security_question);
    }

    private void initClickListener() {
        mOneSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                if (view != null && view.findViewById(R.id.item_security_question_v) != null) {
                    view.findViewById(R.id.item_security_question_v).setVisibility(View.GONE);
                }
                mOneAnswer = mBeanList.get(pos).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mTwoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                if (view != null && view.findViewById(R.id.item_security_question_v) != null) {
                    view.findViewById(R.id.item_security_question_v).setVisibility(View.GONE);
                }
                mTwoAnswer = mTwoList.get(pos).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mThreeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                if (view != null && view.findViewById(R.id.item_security_question_v) != null) {
                    view.findViewById(R.id.item_security_question_v).setVisibility(View.GONE);
                }
                mThreeAnswer = mThreeList.get(pos).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mDoneTv.setOnClickListener(v -> {
            if (verification()) {
                dComplete();
            }

        });
    }

    private void dComplete() {
        List<QuestionsBean> mData = new ArrayList<>();
        mData.add(new QuestionsBean(mOneAnswer, mOneEt.getText().toString().trim()));
        mData.add(new QuestionsBean(mTwoAnswer, mTwoEt.getText().toString().trim()));
        mData.add(new QuestionsBean(mThreeAnswer, mThreeEt.getText().toString().trim()));
        if (mType == 1) {
            EventBus.getDefault().post(new SecurityQuestion(mData));
            finish();
            return;
        }
        String json = new Gson().toJson(mData);
        DialogHelper.showDefaulteMessageProgressDialog(SecurityQuestionActivity.this);
        HttpUtils.get().url(coreManager.getConfig().QUESTION_SET)
                .params("access_token", coreManager.getSelfStatus().accessToken)
                .params("userId", coreManager.getSelf().getUserId())
                .params("phone", coreManager.getSelf().getPhone())
                .params("questions", json)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            ToastUtil.showToast(SecurityQuestionActivity.this, "设置成功");
                            if (mType == 2) {
                                EventBus.getDefault().post(new SecurityQuestion());
                                finish();
                            }
                        } else {
                            ToastUtil.showToast(SecurityQuestionActivity.this, result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(SecurityQuestionActivity.this, e.getMessage());
                    }
                });
    }

    private boolean verification() {
        if (TextUtils.isEmpty(mOneEt.getText().toString().trim())) {
            ToastUtil.showToast(SecurityQuestionActivity.this, getString(R.string.please_enter_a_secret_answer));
            return false;
        } else if (TextUtils.isEmpty(mTwoEt.getText().toString().trim())) {
            ToastUtil.showToast(SecurityQuestionActivity.this, getString(R.string.please_enter_the_secret_two_answer));
            return false;
        } else if (TextUtils.isEmpty(mThreeEt.getText().toString().trim())) {
            ToastUtil.showToast(SecurityQuestionActivity.this, getString(R.string.please_enter_the_secret_three_answer));
            return false;
        }
        return true;
    }

    public static <T> List<List<T>> averageAssign(List<T> source, int limit) {
        if (null == source || source.isEmpty()) {
            return Collections.emptyList();
        }
        List<List<T>> result = new ArrayList<>();
        int remaider = source.size() % limit;  //(先计算出余数)
        int number = source.size() / limit;  //然后是商
        int offset = 0;//偏移量
        for (int i = 0; i < limit; i++) {
            List<T> value = null;
            if (remaider > 0) {
                value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                remaider--;
                offset++;
            } else {
                value = source.subList(i * number + offset, (i + 1) * number + offset);
            }
            result.add(value);
        }
        return result;
    }
}
