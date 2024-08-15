package com.iimm.miliao.ui.me.redpacket;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.CommonalityTools;
import com.iimm.miliao.util.ToastUtil;

/**
 *
 */
@Deprecated
public class ModifyBindPhoneActivity extends BaseActivity {
    TextView alrtcontent;
    ImageView imagecode;
    EditText imagecode_content;
    EditText auth_code_edit;
    Button send_again_btn;
    Button next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifypohe_verification);
        initActionBar();
        initView();
        initEvent();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText("更改绑定手机号");
    }

    private void initView() {
        next = findViewById(R.id.next);
        send_again_btn = findViewById(R.id.send_again_btn);
        alrtcontent = findViewById(R.id.alrtcontent);
        imagecode = findViewById(R.id.imagecode);
        auth_code_edit = findViewById(R.id.auth_code_edit);
        imagecode_content = findViewById(R.id.imagecode_content);
        String ss = "请输入" + coreManager.getSelf().getPhone() + "手机号码" + "\n" + "收到的验证码，验证身份";
        alrtcontent.setText(CommonalityTools.setStringAreaColor(this, ss, 3, 3 + coreManager.getSelf().getPhone().length(), R.color.color_8F9CBB));
    }

    private void initEvent() {
        imagecode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestImageCode();
            }
        });
        send_again_btn.setOnClickListener(v -> {
            if (TextUtils.isEmpty(imagecode_content.getText().toString().trim())) {
                ToastUtil.showToast(ModifyBindPhoneActivity.this, getString(R.string.tip_verification_code_empty));
                return;
            }
            CommonalityTools.getCode(ModifyBindPhoneActivity.this, coreManager.getSelf().getPhone(), imagecode_content.getText().toString()
                    , coreManager, send_again_btn,"");

        });
        next.setOnClickListener(v -> {
            if (TextUtils.isEmpty(auth_code_edit.getText().toString())) {
                ToastUtil.showToast(ModifyBindPhoneActivity.this, "请输入验证码");
                return;
            }
            if (CommonalityTools.mRandCode.equals(auth_code_edit.getText().toString())) {
                startActivity(new Intent(this, BindNewPhoneActivity.class));
                finish();
            } else {
                ToastUtil.showToast(ModifyBindPhoneActivity.this, "验证码错误");
            }
        });
    }

    private void requestImageCode() {
        CommonalityTools.requestImageCode(this, coreManager, coreManager.getSelf().getPhone(), imagecode);
    }
}
