package com.iimm.miliao.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.QuestionsBean;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.ui.me.SecurityQuestionActivity;
import com.iimm.miliao.util.Md5Util;

import java.util.List;

/**
 * 密保问题前验证密码
 */
public class SecurityQuestionDialog extends Dialog implements View.OnClickListener {
    private Context context;
    private Button mSecurityQuestionSendTv;
    private TextView mSecurityQuestionErrorTv;
    private EditText mSecurityQuestionEt;
    private List<QuestionsBean> mQuestionsBeans;

    public SecurityQuestionDialog(Context context, List<QuestionsBean> beans) {
        super(context, R.style.BottomDialog);
        this.context = context;
        this.mQuestionsBeans = beans;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.security_question_dialog);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView() {

        mSecurityQuestionEt = findViewById(R.id.security_question_et);
        mSecurityQuestionErrorTv = findViewById(R.id.security_question_error);
        mSecurityQuestionSendTv = findViewById(R.id.security_question_send);
        findViewById(R.id.security_question_close).setOnClickListener(this);
        findViewById(R.id.security_question_cancel).setOnClickListener(this);
        mSecurityQuestionSendTv.setOnClickListener(this);

//        Window o = getWindow();
//        WindowManager.LayoutParams lp = o.getAttributes();
//        lp.width = ScreenUtil.getScreenWidth(getContext());
//        o.setAttributes(lp);
//        this.getWindow().setGravity(Gravity.CENTER);
//        this.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        mSecurityQuestionEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mSecurityQuestionErrorTv.getVisibility() == View.VISIBLE) {
                    mSecurityQuestionErrorTv.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.security_question_close:
            case R.id.security_question_cancel:
                dismiss();
                break;
            case R.id.security_question_send:
                if (TextUtils.isEmpty(mSecurityQuestionEt.getText().toString().trim())) {
                    mSecurityQuestionErrorTv.setVisibility(View.VISIBLE);
                    mSecurityQuestionErrorTv.setText("请输入密码");
                } else if (!Md5Util.toMD5(mSecurityQuestionEt.getText().toString().trim()).equals(CoreManager.getSelf(context).getPassword())) {
                    mSecurityQuestionErrorTv.setVisibility(View.VISIBLE);
                    mSecurityQuestionErrorTv.setText("密码有误，请重新输入");
                } else {
                    SecurityQuestionActivity.securityQuestion(context, 2, mQuestionsBeans);
                    dismiss();
                }
                break;
            default:
                break;
        }
    }

    /*private void pay(String password) {
        DialogHelper.showDefaulteMessageProgressDialog(context);
        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", CoreManager.requireSelfStatus(MyApplication.getContext()).accessToken);
        params.put("appId", appId);
        params.put("prepayId", prepayId);
        params.put("sign", sign);

        // 获取订单信息
        HttpUtils.get().url(CoreManager.requireConfig(MyApplication.getContext()).PAY_PASSWORD_PAYMENT)
                .params(params)
                .addSecret2(password, orderInfo.getMoney())
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result != null && result.getResultCode() == 1) {
                            payResultListener.payResult(String.valueOf(1));
                            dismiss();
                        } else {
                            if (result != null && !TextUtils.isEmpty(result.getResultMsg())) {
                                Toast.makeText(context, result.getResultMsg(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(context);
                    }
                });
    }*/

}
