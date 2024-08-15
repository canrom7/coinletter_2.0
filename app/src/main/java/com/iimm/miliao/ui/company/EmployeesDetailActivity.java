package com.iimm.miliao.ui.company;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gyf.immersionbar.ImmersionBar;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.company.StructBeanNetInfo;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.other.BasicInfoActivity;
import com.iimm.miliao.util.EventBusCommpanyContorl;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.view.TipDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;
/*
 *
 * 员工详情界面
 * */

public class EmployeesDetailActivity extends BaseActivity implements View.OnClickListener {
    ImageView head;
    ImageView close;
    TextView name;
    TextView postionname;
    TextView companyname;
    TextView departmentname;
    LinearLayout message;
    LinearLayout department;
    LinearLayout position;
    LinearLayout team;
    ImageView department_img;
    TextView department_tv;
    ImageView position_img;
    TextView position_tv;
    ImageView team_img;
    TextView team_tv;
    StructBeanNetInfo.DepartmentsBean.EmployeesBean employeesBean;
    private int userpostion;
    private int departpostion;
    private StructBeanNetInfo structBeanNetInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emplyeesdetail);
        getSupportActionBar().hide();
        ImmersionBar.with(this).init();
        departpostion = getIntent().getIntExtra("departmentposition", 0);
        userpostion = getIntent().getIntExtra("userposition", 0);
        structBeanNetInfo = (StructBeanNetInfo) getIntent().getExtras().getSerializable("companyinfo");
        EventBus.getDefault().register(this);
        initView();
    }

    public void initView() {
        head = findViewById(R.id.head);
        close = findViewById(R.id.close);
        name = findViewById(R.id.name);
        postionname = findViewById(R.id.positionname);
        companyname = findViewById(R.id.company_name);
        message = findViewById(R.id.message);
        department = findViewById(R.id.department);
        position = findViewById(R.id.position);
        team = findViewById(R.id.team);
        department_img = findViewById(R.id.department_img);
        department_tv = findViewById(R.id.department_tv);
        position_img = findViewById(R.id.position_img);
        position_tv = findViewById(R.id.position_tv);
        team_img = findViewById(R.id.team_img);
        team_tv = findViewById(R.id.team_tv);
        departmentname = findViewById(R.id.department_name);

        employeesBean = structBeanNetInfo.getDepartments().get(departpostion)
                .getEmployees().get(userpostion);
        Glide.with(this).load(AvatarHelper.getAvatarUrl(employeesBean.getUserId() + "", false))
                .into(head);
        name.setText(employeesBean.getNickname());
        postionname.setText(employeesBean.getPosition());
        companyname.setText(structBeanNetInfo.getCompanyName());
        departmentname.setText(structBeanNetInfo.getDepartments().get(departpostion).getDepartName());

        close.setOnClickListener(this);
        message.setOnClickListener(this);
        department.setOnClickListener(this);
        position.setOnClickListener(this);
        team.setOnClickListener(this);

        if (!coreManager.getSelf().getUserId().equals(String.valueOf(structBeanNetInfo.getCreateUserId()))) {
            department_img.setEnabled(false);
            department_tv.setEnabled(false);
            department.setEnabled(false);

            team.setEnabled(false);
            team_img.setEnabled(false);
            team_tv.setEnabled(false);

            position.setEnabled(false);
            position_img.setEnabled(false);
            position_tv.setEnabled(false);
        }
        if (employeesBean.getUserId() == structBeanNetInfo.getCreateUserId()) {
            team.setEnabled(false);
            team_img.setEnabled(false);
            team_tv.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close:
                finish();
                break;
            case R.id.message:
                Intent intent = new Intent(getApplicationContext(), BasicInfoActivity.class);
                intent.putExtra(AppConstant.EXTRA_USER_ID, String.valueOf(employeesBean.getUserId()));
                startActivity(intent);
                break;
            case R.id.department:
                Intent intent3 = new Intent(getApplicationContext(), ChangeEmployeeDepartment.class);
                intent3.putExtra("userid", employeesBean.getUserId() + "");
                intent3.putExtra("departmentposition", departpostion);
                Bundle bundle1 = new Bundle();
                bundle1.putSerializable("data", structBeanNetInfo);
                intent3.putExtras(bundle1);
                startActivity(intent3);
                break;
            case R.id.position:
                Intent intent2 = new Intent(getApplicationContext(), PositionUpdateNameActivity.class);
                intent2.putExtra("departmentposition", departpostion);
                intent2.putExtra("userposition", userpostion);
                Bundle bundle = new Bundle();
                bundle.putSerializable("companyinfo", structBeanNetInfo);
                intent2.putExtras(bundle);
                startActivity(intent2);
                break;
            case R.id.team:
                if (coreManager.getSelf().getUserId().equals(structBeanNetInfo.getCreateUserId())) {
                    ToastUtil.showToast(EmployeesDetailActivity.this, R.string.create_connot_dels);
                    return;
                }
                TipDialog tipDialog = new TipDialog(mContext);
                tipDialog.setmConfirmOnClickListener("确定将该成员移出团队？", new TipDialog.ConfirmOnClickListener() {
                    @Override
                    public void confirm() {
                        deleteEmployee(employeesBean.getUserId() + "", employeesBean.getDepartmentId());

                    }
                });
                tipDialog.show();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void updateDepartment(EventBusCommpanyContorl event) {

        /*
         * 修改成员职称
         * */
        if (event.getContorlType() == EventBusCommpanyContorl.POSITION_NAME_UPDATE) {

            postionname.setText(event.getData().getDepartments().get(event.getmDepartPostion())
                    .getEmployees().get(event.getmUserPostion()).getPosition());
        }
        /*
         * 跟换部门
         * */
        if (event.getContorlType() == EventBusCommpanyContorl.POSITION_NAME_UPDATE & event.isRefresh()) {

            finish();
        }
    }

    // 删除员工
    private void deleteEmployee(String userId, String departmentId) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("departmentId", departmentId);
        params.put("userIds", userId);

        HttpUtils.get().url(coreManager.getConfig().DELETE_EMPLOYEE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result.getResultCode() == 1) {
                            ToastUtil.showToast(EmployeesDetailActivity.this, R.string.del_e_succ);
                            EventBus.getDefault().post(EventBusCommpanyContorl.getInstance(structBeanNetInfo, EventBusCommpanyContorl.DELETE_EMPLOYEES));
                            finish();
                        } else {
                            // 删除员工失败
                            ToastUtil.showToast(EmployeesDetailActivity.this, R.string.del_e_fail);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(EmployeesDetailActivity.this);
                    }
                });
    }
}
