package com.iimm.miliao.ui.company;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.company.StructBeanNetInfo;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.EventBusCommpanyContorl;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.view.ClearEditText;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/*
 * 修改职位名称
 * */
public class PositionUpdateNameActivity extends BaseActivity implements View.OnClickListener {
    Button button;
    private int userpostion;
    private int departpostion;
    private StructBeanNetInfo structBeanNetInfo;
    private ClearEditText position_edit;
    private String mPostionname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updateposition_name);
        departpostion = getIntent().getIntExtra("departmentposition", 0);
        userpostion = getIntent().getIntExtra("userposition", 0);
        structBeanNetInfo = (StructBeanNetInfo) getIntent().getExtras().getSerializable("companyinfo");
        initView();
    }

    private void initView() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(this);
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText("修改职位");
        position_edit = (ClearEditText) findViewById(R.id.position_edit);
        mPostionname = structBeanNetInfo.getDepartments().get(departpostion).getEmployees().get(userpostion).getPosition();
        position_edit.setHint(mPostionname);
        button = findViewById(R.id.create_position_btn);
        button.setOnClickListener(this);
    }

    // 更改职位
    private void changeEmployeeIdentity(String companyId, String userId, String position) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("companyId", companyId);
        params.put("userId", userId);
        params.put("position", position);

        HttpUtils.get().url(coreManager.getConfig().CHANGE_EMPLOYEE_IDENTITY)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result.getResultCode() == 1) {
                            ToastUtil.showToast(mContext, getString(R.string.modify_succ));
                            structBeanNetInfo.getDepartments().get(departpostion).getEmployees().get(userpostion).setPosition(position);
                            EventBusCommpanyContorl instance = EventBusCommpanyContorl.getInstance(structBeanNetInfo,
                                    EventBusCommpanyContorl.POSITION_NAME_UPDATE);
                            instance.setmDepartPostion(departpostion);
                            instance.setmUserPostion(userpostion);
                            EventBus.getDefault().post(instance);
                            finish();
                        } else {
                            ToastUtil.showToast(mContext, getString(R.string.modify_fail));
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(PositionUpdateNameActivity.this);
                    }
                });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.iv_title_left:
                finish();
                break;
            case R.id.create_position_btn:
                String name = position_edit.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    ToastUtil.showToast(this, "职位名称不能为空！");
                } else {
                    changeEmployeeIdentity(structBeanNetInfo.getId(), structBeanNetInfo.getDepartments()
                            .get(departpostion)
                            .getEmployees()
                            .get(userpostion)
                            .getUserId() + "", name);
                }
                break;
        }
    }
}
