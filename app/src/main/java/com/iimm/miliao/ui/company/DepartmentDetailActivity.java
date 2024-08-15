package com.iimm.miliao.ui.company;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iimm.miliao.R;
import com.iimm.miliao.adapter.EmployeesAdapter;
import com.iimm.miliao.bean.company.StructBeanNetInfo;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.EventBusCommpanyContorl;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/*
 * 部门详情界面
 * */
public class DepartmentDetailActivity extends BaseActivity {
    TextView company_name;
    TextView department_name;
    LinearLayout add_member;
    RecyclerView rcv;
    StructBeanNetInfo structBeanNetInfo;
    EmployeesAdapter adapter;
    List<StructBeanNetInfo.DepartmentsBean.EmployeesBean> datas;
    RelativeLayout contorll;
    TextView department_updatename;
    TextView department_delete;
    EditText search;
    private int companypostion;
    private int departpostion;
    private TextView tvTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_departmentdetail);
        structBeanNetInfo = (StructBeanNetInfo) getIntent().getExtras().getSerializable("company");
        companypostion = getIntent().getIntExtra("companypostion", 0);
        departpostion = getIntent().getIntExtra("departpostion", 0);
        EventBus.getDefault().register(this);
        initActionBar();
        initView();

    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(structBeanNetInfo.getDepartments().get(departpostion).getDepartName());
        ImageView right = findViewById(R.id.iv_title_right);
        right.setImageResource(R.drawable.ic_app_add);
        right.setOnClickListener(v -> {
            contorll.setVisibility(View.VISIBLE);
        });
    }

    public void initView() {
        search = findViewById(R.id.search);
        company_name = findViewById(R.id.company_name);
        department_name = findViewById(R.id.department_name);
        add_member = findViewById(R.id.add_member);
        contorll = findViewById(R.id.contorll);
        department_updatename = findViewById(R.id.department_updatename);
        department_delete = findViewById(R.id.department_delete);
        rcv = findViewById(R.id.rcv);

        company_name.setText(structBeanNetInfo.getCompanyName());
        department_name.setText(structBeanNetInfo.getDepartments().get(departpostion).getDepartName());

        datas = new ArrayList<>();
        datas.addAll(structBeanNetInfo.getDepartments().get(departpostion).getEmployees());
        adapter = new EmployeesAdapter(this, datas);
        adapter.setStructBeanNetInfo(structBeanNetInfo);
        adapter.setDepartpostion(departpostion);
        rcv.setLayoutManager(new LinearLayoutManager(this));
        rcv.setAdapter(adapter);

        contorll.setOnClickListener(v -> {
            contorll.setVisibility(View.GONE);
        });
        department_updatename.setOnClickListener(v -> {
            if (coreManager.getSelf().getUserId().equals(String.valueOf(structBeanNetInfo.getCreateUserId()))) {
                // 添加部门
                Intent intent = new Intent(DepartmentDetailActivity.this, ModifyDepartmentName.class);
                intent.putExtra("departmentId", structBeanNetInfo.getDepartments().get(departpostion).getId());
                intent.putExtra("departmentName", structBeanNetInfo.getDepartments().get(departpostion).getDepartName());
                intent.putExtra("departpostion", departpostion);
                Bundle bundle = new Bundle();
                bundle.putSerializable("data", structBeanNetInfo);
                intent.putExtras(bundle);
                startActivity(intent);
                contorll.setVisibility(View.GONE);
            } else {
                Toast.makeText(this, "您没有权限，进行此操作！", Toast.LENGTH_SHORT).show();
            }
        });
        department_delete.setOnClickListener(v -> {
            if (coreManager.getSelf().getUserId().equals(String.valueOf(structBeanNetInfo.getCreateUserId()))) {
                deleteDepartment(structBeanNetInfo.getDepartments().get(departpostion).getId());
                contorll.setVisibility(View.GONE);
            } else {
                Toast.makeText(this, "您没有权限，进行此操作！", Toast.LENGTH_SHORT).show();
            }

        });
        add_member.setOnClickListener(v -> {
            // 添加成员
            Intent intent = new Intent(DepartmentDetailActivity.this, AddEmployee.class);
            intent.putExtra("departmentPosition", departpostion);
            Bundle bundle = new Bundle();
            bundle.putSerializable("data", structBeanNetInfo);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.e("ssss", s.toString());
                if (TextUtils.isEmpty(s)) {
                    datas.clear();
                    datas.addAll(structBeanNetInfo.getDepartments().get(departpostion).getEmployees());
                    adapter.notifyDataSetChanged();
                } else {

                    adapter.getFilter().setResultListener(new SearchFilter.ResultListener<StructBeanNetInfo.DepartmentsBean.EmployeesBean>() {
                        @Override
                        public void result(List<StructBeanNetInfo.DepartmentsBean.EmployeesBean> results) {

                            datas.clear();
                            datas.addAll(results);
                            adapter.notifyDataSetChanged();
                        }
                    });
                    adapter.getFilter().filter(s);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void updateDepartment(EventBusCommpanyContorl event) {
        /*
         * 修改部门名称
         * */
        if (event.getContorlType() == EventBusCommpanyContorl.DEPARTMENT_UPDATENAME) {
            tvTitle.setText(event.getData().getDepartments().get(departpostion).getDepartName());
        }
        /*
         * 修改成员职称
         * */
        if (event.getContorlType() == EventBusCommpanyContorl.POSITION_NAME_UPDATE) {

            datas.get(event.getmUserPostion()).setPosition(event.getData().getDepartments().get(event.getmDepartPostion())
                    .getEmployees().get(event.getmUserPostion()).getPosition());
            adapter.notifyItemChanged(event.getmUserPostion());
        }
        /*
         * 跟换部门
         * */
        if (event.getContorlType() == EventBusCommpanyContorl.DEPARTMENT_CHANG & event.isRefresh()) {
            structBeanNetInfo = null;
            structBeanNetInfo = event.getData();
            datas.clear();
            datas.addAll(structBeanNetInfo.getDepartments().get(departpostion).getEmployees());
            adapter.notifyDataSetChanged();
        }
        /*
         * 删除员工
         * */
        if (event.getContorlType() == EventBusCommpanyContorl.DELETE_EMPLOYEES & event.isRefresh()) {
            structBeanNetInfo = null;
            structBeanNetInfo = event.getData();
            datas.clear();
            datas.addAll(structBeanNetInfo.getDepartments().get(departpostion).getEmployees());
            adapter.notifyDataSetChanged();
        }
        /*
         * 添加员工
         * */
        if (event.getContorlType() == EventBusCommpanyContorl.ADD_EMPLOYEES & event.isRefresh()) {
            structBeanNetInfo = null;
            structBeanNetInfo = event.getData();
            datas.clear();
            datas.addAll(structBeanNetInfo.getDepartments().get(departpostion).getEmployees());
            adapter.notifyDataSetChanged();
        }
    }

    // 删除部门
    private void deleteDepartment(String departmentId) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("departmentId", departmentId);

        HttpUtils.get().url(coreManager.getConfig().DELETE_DEPARTMENT)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result.getResultCode() == 1) {
                            Toast.makeText(DepartmentDetailActivity.this, R.string.del_d_succ, Toast.LENGTH_SHORT).show();
                            EventBusCommpanyContorl contorl = EventBusCommpanyContorl.getInstance(structBeanNetInfo,
                                    EventBusCommpanyContorl.DEPARTMENT_DELETER);
                            Log.e("ccccc", departpostion + "");
                            contorl.setmDepartPostion(departpostion);
                            EventBus.getDefault().post(contorl);
                            finish();
                        } else {
                            // 删除部门失败
                            ToastUtil.showToast(DepartmentDetailActivity.this, R.string.del_d_fail);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(DepartmentDetailActivity.this);
                    }
                });
    }
}
