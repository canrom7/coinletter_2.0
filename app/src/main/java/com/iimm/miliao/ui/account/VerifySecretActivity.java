package com.iimm.miliao.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iimm.miliao.R;
import com.iimm.miliao.adapter.SecurityQuestionAdapter;
import com.iimm.miliao.bean.SecurityQuestionBean;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * MrLiu253@163.com
 *
 * @time 2019-09-25
 */
public class VerifySecretActivity extends BaseActivity implements View.OnClickListener {

    private AppCompatSpinner mUserNameACS;
    private EditText mProblemEt;
    private TextView mSubmitTv;
    private String mName;
    private int type;
    private String mAnswer;
    private List<SecurityQuestionBean.DataBean> mBeanList = new ArrayList<>();
    private SecurityQuestionAdapter mQuestionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_secret);
        mName = getIntent().getStringExtra("name");
        type = getIntent().getIntExtra("type", 0);
        String mDatas = getIntent().getStringExtra("datas");


        ArrayList<SecurityQuestionBean.DataBean> jsonObjects = new Gson().fromJson(mDatas, new TypeToken<ArrayList<SecurityQuestionBean.DataBean>>(){}.getType());
        mBeanList.clear();
        mBeanList.addAll(jsonObjects);
        initView();
        initClickListener();
    }

    private void initView() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText("忘记密码");

        mUserNameACS = findViewById(R.id.username_acs);
        mProblemEt = findViewById(R.id.username_problem_cet);
        mSubmitTv = findViewById(R.id.username_problem_tv);
        mQuestionAdapter = new SecurityQuestionAdapter(this, mBeanList);
        mUserNameACS.setAdapter(mQuestionAdapter);
        mSubmitTv.setOnClickListener(this);
    }

    private void initClickListener() {
        mUserNameACS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                if (view != null && view.findViewById(R.id.item_security_question_v) != null) {
                    view.findViewById(R.id.item_security_question_v).setVisibility(View.GONE);
                }
                mAnswer = mBeanList.get(pos).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.username_problem_tv:
                if (verification()) {
                    verificationName();
                }
                break;
        }
    }

    private boolean verification() {
        if (TextUtils.isEmpty(mName)) {
            ToastUtil.showToast(VerifySecretActivity.this, getString(R.string.please_input_account));
            return false;
        } else if (TextUtils.isEmpty(mProblemEt.getText().toString().trim())) {
            ToastUtil.showToast(VerifySecretActivity.this, getString(R.string.please_enter_the_answer));
            return false;
        }
        return true;
    }

    /**
     * 验证用户名和密保
     */
    private void verificationName() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new ArrayMap<>();
        params.put("userName", mName);
        params.put("qid", mAnswer);
        params.put("answer", mProblemEt.getText().toString().trim());
        HttpUtils.get().url(coreManager.getConfig().QUESTION_CHECK)
                .params(params)
                .build()
                .execute(new BaseCallback<Boolean>(Boolean.class) {
                    @Override
                    public void onResponse(ObjectResult<Boolean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1 && result.getData()) {
                            Intent mIntent = new Intent(VerifySecretActivity.this, SetPasswordActivity.class);
                            mIntent.putExtra("name", mName);
                            mIntent.putExtra("id", mAnswer);
                            mIntent.putExtra("answer", mProblemEt.getText().toString().trim());
                            mIntent.putExtra("type", type);
                            startActivity(mIntent);
                        } else {
                            ToastUtil.showToast(VerifySecretActivity.this, TextUtils.isEmpty(result.getResultMsg()) ? "校验失败" : result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(VerifySecretActivity.this, e.getMessage());
                    }
                });
    }

}
