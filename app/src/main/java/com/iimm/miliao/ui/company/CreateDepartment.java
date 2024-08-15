package com.iimm.miliao.ui.company;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.company.StructBeanNetInfo;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.EventBusCommpanyContorl;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

public class CreateDepartment extends BaseActivity implements View.OnClickListener {
    private EditText mDepartmentEdit;
    private String mDepartmentName;
    private String mCompanyId;
    private String mRootDepartmentId;
    private StructBeanNetInfo struct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_department);
        initView();
    }

    private void initView() {
        Intent intent = getIntent();
        if (intent != null) {
            mCompanyId = intent.getStringExtra("companyId");
            mRootDepartmentId = intent.getStringExtra("rootDepartmentId");
            struct = (StructBeanNetInfo) intent.getExtras().getSerializable("data");
        } else {
            finish();
        }
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(this);
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.create_department);

        mDepartmentEdit = (EditText) findViewById(R.id.department_edit);
        findViewById(R.id.create_department_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_title_left:
                finish();
                break;
            case R.id.create_department_btn:
                mDepartmentName = mDepartmentEdit.getText().toString().trim();
                if (TextUtils.isEmpty(mDepartmentName)) {
                    // 部门名不能为空
                    ToastUtil.showToast(this, R.string.name_connot_null);
                } else {
                    createDepartment(mDepartmentName, mCompanyId);
                }
                break;
        }
    }

    private void createDepartment(String departmentName, String companyId) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("companyId", companyId);
        params.put("departName", departmentName);
        params.put("createUserId", coreManager.getSelf().getUserId());
        if (TextUtils.isEmpty(mRootDepartmentId)) {
            mRootDepartmentId = "";
        }
        params.put("parentId", mRootDepartmentId);

        HttpUtils.get().url(coreManager.getConfig().CREATE_DEPARTMENT)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result.getResultCode() == 1) {
                            ToastUtil.showToast(CreateDepartment.this, R.string.create_department_succ);
                            EventBus.getDefault().post(EventBusCommpanyContorl.getInstance(struct,
                                    EventBusCommpanyContorl.DEPARTMENT_ADD));// 数据有更新
                            finish();
                        } else {
                            // 创建部门失败
                            ToastUtil.showToast(CreateDepartment.this, R.string.create_department_fail);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(CreateDepartment.this);
                    }
                });
    }
}
