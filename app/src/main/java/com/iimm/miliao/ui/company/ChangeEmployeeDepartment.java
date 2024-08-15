package com.iimm.miliao.ui.company;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.company.StructBeanNetInfo;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.EventBusCommpanyContorl;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ViewHolder;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;


public class ChangeEmployeeDepartment extends BaseActivity {
    StructBeanNetInfo structBeanNetInfo;
    private ListView mListView;
    private ListView mPullToRefreshListView;
    private EmpDepAdapter mAdapter;
    private Button sure;
    // 该公司下所有部门(除原部门)
    private List<StructBeanNetInfo.DepartmentsBean> datas;
    private String userid;
    private String departmentid;
    private int oldPostion = -1;
    private int departmentposition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_employee_department);
        userid = getIntent().getStringExtra("userid");
        departmentposition = getIntent().getIntExtra("departmentposition", 0);
        structBeanNetInfo = (StructBeanNetInfo) getIntent().getExtras().getSerializable("data");
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.change_departement);
        initView();
    }

    private void initView() {
        mPullToRefreshListView = (ListView) findViewById(R.id.pull_refresh_list);
        sure = findViewById(R.id.sure);
        datas = new ArrayList<>();
        datas.addAll(structBeanNetInfo.getDepartments());
        datas.remove(0);
        datas.get(departmentposition - 1).setSelector(true);
        oldPostion = departmentposition - 1;

        mAdapter = new EmpDepAdapter(datas, this);
        mPullToRefreshListView.setAdapter(mAdapter);
        mPullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (oldPostion != i) {
                    datas.get(i).setSelector(true);
                    if (oldPostion != -1) {
                        datas.get(oldPostion).setSelector(false);
                    }
                    oldPostion = i;
                    mAdapter.notifyDataSetChanged();
                } else {
                    return;
                }


            }
        });
        sure.setOnClickListener(v -> {
            if (oldPostion != -1) {
                if (oldPostion != departmentposition - 1) {
                    changeEmployeeDepartment(userid, datas.get(oldPostion).getId());
                } else {
                    ToastUtil.showToast(this, "选择部门与原部门相同！");
                }
            } else {
                ToastUtil.showToast(this, "请选择部门！" );
            }
        });
    }

    private void changeEmployeeDepartment(String userId, String newDepartmentId) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", userId);
        params.put("companyId", structBeanNetInfo.getId());
        params.put("newDepartmentId", newDepartmentId);

        HttpUtils.get().url(coreManager.getConfig().MODIFY_EMPLOYEE_DEPARTMENT)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result.getResultCode() == 1) {
                            Toast.makeText(ChangeEmployeeDepartment.this, R.string.change_departement_succ, Toast.LENGTH_SHORT).show();
                            // 更换部门成功,跳转至公司管理页面
                            EventBusCommpanyContorl instance = EventBusCommpanyContorl.getInstance(structBeanNetInfo,
                                    EventBusCommpanyContorl.DEPARTMENT_CHANG);
                            instance.setmDepartPostion(oldPostion + 1);
                            EventBus.getDefault().post(instance);//数据有更新
                            finish();
                        } else {
                            // 更换部门失败
                            Toast.makeText(ChangeEmployeeDepartment.this, R.string.change_departement_fail, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(ChangeEmployeeDepartment.this);
                    }
                });
    }

    private class EmpDepAdapter extends BaseAdapter {

        private List<StructBeanNetInfo.DepartmentsBean> mIdentity;
        private Context mContext;

        public EmpDepAdapter(List<StructBeanNetInfo.DepartmentsBean> identity, Context context) {
            mIdentity = identity;
            mContext = context;
        }

        @Override
        public int getCount() {
            return mIdentity.size();
        }

        @Override
        public Object getItem(int i) {
            return mIdentity.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.a_item_for_change_department, viewGroup, false);
            }
            TextView departmentTv = ViewHolder.get(view, R.id.name);
            Button img = ViewHolder.get(view, R.id.selector);
            departmentTv.setText(mIdentity.get(i).getDepartName() + "(" + mIdentity.get(i).getEmployees().size() + ")");
            if (mIdentity.get(i).isSelector()) {
                img.setEnabled(true);
            } else {
                img.setEnabled(false);
            }

            return view;
        }
    }
}
