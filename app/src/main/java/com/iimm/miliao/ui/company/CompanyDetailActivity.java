package com.iimm.miliao.ui.company;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iimm.miliao.AppConstant;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.adapter.DepartmentAdapter;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.company.StructBeanNetInfo;
import com.iimm.miliao.bean.message.MucRoom;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.OnCompleteListener2;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.message.MucChatActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.EventBusCommpanyContorl;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.view.TipDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import fm.jiecao.jcvideoplayer_lib.MessageEvent;
import okhttp3.Call;

/*
 * 公司详情界面
 * */
public class CompanyDetailActivity extends BaseActivity implements View.OnClickListener {

    RecyclerView departmentlist;
    List<StructBeanNetInfo.DepartmentsBean> data;
    StructBeanNetInfo structBeanNetInfo;
    DepartmentAdapter departmentAdapter;
    RelativeLayout contorll;
    TextView department_add;
    TextView companyname_update;
    TextView exit_company;
    private int companyPostion = 0;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_companydetail);
        structBeanNetInfo = (StructBeanNetInfo) getIntent().getExtras().getSerializable("data");
        companyPostion = getIntent().getIntExtra("postion", 0);
        initActionBar();
        initView();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(structBeanNetInfo.getCompanyName());

        ImageView right = findViewById(R.id.iv_title_right);
        right.setImageResource(R.drawable.ic_app_add);
        right.setVisibility(View.VISIBLE);
        right.setOnClickListener(v -> {
            contorll.setVisibility(View.VISIBLE);
        });
    }

    private void initView() {
        EventBus.getDefault().register(this);
        contorll = findViewById(R.id.contorll);
        department_add = findViewById(R.id.department_add);
        companyname_update = findViewById(R.id.companyname_update);
        exit_company = findViewById(R.id.exit_company);
        departmentlist = findViewById(R.id.departmentlist);
        data = new ArrayList<>();
        data.addAll(structBeanNetInfo.getDepartments());
        data.remove(0);
        departmentAdapter = new DepartmentAdapter(data, this);
        departmentAdapter.setStructBeanNetInfo(structBeanNetInfo);
        departmentAdapter.setCompanypostion(companyPostion);
        departmentAdapter.setOnclickItemListener(new DepartmentAdapter.OnclickItemListener() {
            @Override
            public void onGroupItemClick(int i) {

                if (!TextUtils.isEmpty(data.get(i).getRoomJid())) {
                    Friend mFriend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), data.get(i).getRoomJid());
                    if (mFriend != null) {
                        Intent intent = new Intent(CompanyDetailActivity.this, MucChatActivity.class);
                        intent.putExtra(AppConstant.EXTRA_USER_ID, data.get(i).getRoomJid());
                        intent.putExtra(AppConstant.EXTRA_NICK_NAME, data.get(i).getDepartName());
                        intent.putExtra(AppConstant.EXTRA_IS_GROUP_CHAT, true);
                        startActivity(intent);
                    } else {
                        DialogHelper.showMessageProgressDialog(CompanyDetailActivity.this, "请稍等...");
                        loadMembers(data.get(i).getRoomId(), data.get(i).getRoomJid(), data.get(i).getDepartName());
                    }
                } else {
                    Toast.makeText(CompanyDetailActivity.this, "请先添加部门成员！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        departmentlist.setLayoutManager(new LinearLayoutManager(this));
        departmentlist.setAdapter(departmentAdapter);
        department_add.setOnClickListener(this);
        exit_company.setOnClickListener(this);
        companyname_update.setOnClickListener(this);

        contorll.setOnClickListener(v -> {
            contorll.setVisibility(View.GONE);
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.department_add:
                if (coreManager.getSelf().getUserId().equals(String.valueOf(structBeanNetInfo.getCreateUserId()))) {

                    // 添加部门
                    Intent intent = new Intent(CompanyDetailActivity.this, CreateDepartment.class);
                    intent.putExtra("companyId", structBeanNetInfo.getId());
                    intent.putExtra("rootDepartmentId", structBeanNetInfo.getId());
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("data", structBeanNetInfo);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "您没有权限，进行此操作！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.exit_company:
                // 退出公司
                String str = "";
                if (coreManager.getSelf().getUserId().equals(String.valueOf(structBeanNetInfo.getCreateUserId()))) {
                    str = "确定解散该团队吗？";
                } else {
                    str = getString(R.string.sure_exit_company);
                }
                TipDialog tipDialog = new TipDialog(mContext);
                tipDialog.setmConfirmOnClickListener(str, new TipDialog.ConfirmOnClickListener() {
                    @Override
                    public void confirm() {
                        if (coreManager.getSelf().getUserId().equals(String.valueOf(structBeanNetInfo.getCreateUserId()))) {
                            deleteCompany(structBeanNetInfo.getId(), coreManager.getSelf().getUserId());
                        } else {
                            exitCompany(structBeanNetInfo.getId(), coreManager.getSelf().getUserId());
                        }

                    }
                });
                tipDialog.show();
                break;
            case R.id.companyname_update:
                if (coreManager.getSelf().getUserId().equals(String.valueOf(structBeanNetInfo.getCreateUserId()))) {
                    // 添加部门
                    Intent intent = new Intent(CompanyDetailActivity.this, ModifyCompanyName.class);
                    intent.putExtra("companyId", structBeanNetInfo.getId());
                    intent.putExtra("companyName", structBeanNetInfo.getCompanyName());
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("data", structBeanNetInfo);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "您没有权限，进行此操作！", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        contorll.setVisibility(View.GONE);
    }

    // 退出公司
    private void exitCompany(String companyId, String userId) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("companyId", companyId);
        params.put("userId", userId);

        HttpUtils.get().url(coreManager.getConfig().EXIT_COMPANY)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result.getResultCode() == 1) {
                            Toast.makeText(CompanyDetailActivity.this, R.string.exi_c_succ, Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().post(new MessageEvent("Update"));// 数据有更新
                            finish();
                        } else {
                            // 退出公司失败
                            Toast.makeText(CompanyDetailActivity.this, R.string.exi_c_fail, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(CompanyDetailActivity.this);
                    }
                });
    }

    // 删除公司
    private void deleteCompany(String companyId, String userId) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("companyId", companyId);
        params.put("userId", userId);

        HttpUtils.get().url(coreManager.getConfig().DELETE_COMPANY)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result.getResultCode() == 1) {
                            Toast.makeText(CompanyDetailActivity.this, R.string.del_c_succ, Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().post(new MessageEvent("Update"));// 数据有更新
                            finish();
                        } else {
                            // 删除公司失败
                            Toast.makeText(CompanyDetailActivity.this, R.string.del_c_fail, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(CompanyDetailActivity.this);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /*
     * 接收数据改变
     * */
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void updateDepartment(EventBusCommpanyContorl event) {
        /*
         * 新建部门
         * */
        if (event.getContorlType() == EventBusCommpanyContorl.DEPARTMENT_ADD & event.isRefresh()) {
            data.clear();
            data.addAll(event.getData().getDepartments());
            data.remove(0);
            departmentAdapter.setStructBeanNetInfo(event.getData());
            departmentAdapter.notifyDataSetChanged();
        }
        /*
         * 修改部门名称
         * */
        if (event.getContorlType() == EventBusCommpanyContorl.DEPARTMENT_UPDATENAME) {
            data.get(event.getmDepartPostion() - 1).setDepartName(event.getDepartMentname());
            departmentAdapter.notifyItemChanged(event.getmDepartPostion() - 1);
        }
        /*
         * 修改公司名字
         * */
        if (event.getContorlType() == EventBusCommpanyContorl.COMPANYNAME_UPDATE) {
            tvTitle.setText(event.getData().getCompanyName());
        }
        /*
         * 删除部门
         * */
        if (event.getContorlType() == EventBusCommpanyContorl.DEPARTMENT_DELETER & event.isRefresh()) {

            structBeanNetInfo = null;
            structBeanNetInfo = event.getData();
            departmentAdapter.setStructBeanNetInfo(structBeanNetInfo);
            data.clear();
            data.addAll(structBeanNetInfo.getDepartments());
            data.remove(0);
            departmentAdapter.notifyDataSetChanged();
        }
        /*
         * 修改成员职位名称
         * */
        if (event.getContorlType() == EventBusCommpanyContorl.POSITION_NAME_UPDATE) {
            structBeanNetInfo = null;
            structBeanNetInfo = event.getData();
            departmentAdapter.setStructBeanNetInfo(structBeanNetInfo);
        }
        /*
         * 跟换部门
         * */
        if (event.getContorlType() == EventBusCommpanyContorl.DEPARTMENT_CHANG & event.isRefresh()) {
            structBeanNetInfo = null;
            structBeanNetInfo = event.getData();
            departmentAdapter.setStructBeanNetInfo(structBeanNetInfo);
            data.clear();
            data.addAll(structBeanNetInfo.getDepartments());
            data.remove(0);
            departmentAdapter.notifyDataSetChanged();
        }
        /*
         * 删除员工
         * */
        if (event.getContorlType() == EventBusCommpanyContorl.DELETE_EMPLOYEES & event.isRefresh()) {
            structBeanNetInfo = null;
            structBeanNetInfo = event.getData();
            departmentAdapter.setStructBeanNetInfo(structBeanNetInfo);
            data.clear();
            data.addAll(structBeanNetInfo.getDepartments());
            data.remove(0);
            departmentAdapter.notifyDataSetChanged();
        }
        /*
         * 添加员工
         * */
        if (event.getContorlType() == EventBusCommpanyContorl.ADD_EMPLOYEES & event.isRefresh()) {
            structBeanNetInfo = null;
            structBeanNetInfo = event.getData();
            departmentAdapter.setStructBeanNetInfo(structBeanNetInfo);
            data.clear();
            data.addAll(structBeanNetInfo.getDepartments());
            data.remove(0);
            departmentAdapter.notifyDataSetChanged();
        }
    }

    private void loadMembers(String roomId, String roomJid, String name) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", roomId);
        params.put("pageSize", Constants.MUC_MEMBER_SIZE);
        HttpUtils.get().url(coreManager.getConfig().ROOM_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<MucRoom>(MucRoom.class) {

                             @Override
                             public void onResponse(ObjectResult<MucRoom> result) {
                                 if (result.getResultCode() == 1 && result.getData() != null) {
                                     final MucRoom mucRoom = result.getData();
                                     List<MucRoom> rooms = new ArrayList<>();
                                     rooms.add(mucRoom);
                                     FriendDao.getInstance().addRooms(MyApplication.applicationHandler, coreManager.getSelf().getUserId(), rooms, new OnCompleteListener2() {
                                         @Override
                                         public void onLoading(int progressRate, int sum) {

                                         }

                                         @Override
                                         public void onCompleted() {
                                             DialogHelper.dismissProgressDialog();
                                             Intent intent = new Intent(CompanyDetailActivity.this, MucChatActivity.class);
                                             intent.putExtra(AppConstant.EXTRA_USER_ID, roomJid);
                                             intent.putExtra(AppConstant.EXTRA_NICK_NAME, name);
                                             intent.putExtra(AppConstant.EXTRA_IS_GROUP_CHAT, true);
                                             startActivity(intent);
                                         }
                                     });

                                 } else {
                                     DialogHelper.dismissProgressDialog();
                                     ToastUtil.showToast(CompanyDetailActivity.this, result.getResultMsg());
                                 }
                             }

                             @Override
                             public void onError(Call call, Exception e) {
                                 DialogHelper.dismissProgressDialog();
                                 ToastUtil.showNetError(mContext);
                             }
                         }
                );
    }
}
