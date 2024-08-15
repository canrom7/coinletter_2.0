package com.iimm.miliao.ui.me;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.TextView;

import com.daimajia.swipe.util.Attributes;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.BlanceInfo;
import com.iimm.miliao.bean.DynamicWithdrawalBean;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.me.redpacket.AddWithdrawActivity;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

/**
 * 提现方式
 */
public class SelectorBlanceActivity extends BaseActivity {
    SmartRefreshLayout smartRefreshLayout;
    RecyclerView recyclerView;
    BlanceAdapter blanceAdapter;
    List<BlanceInfo.MethodBean> list;
    private String mID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectorblance);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> {
            Intent intent = new Intent();
            setResult(Activity.RESULT_OK, intent);
            finish();
        });
        mID = getIntent().getStringExtra("id");
        initView();
    }

    public void initView() {
        TextView sTitle = findViewById(R.id.tv_title_center);
        sTitle.setText("提现方式");
        list = new ArrayList<>();
        smartRefreshLayout = findViewById(R.id.refresh);
        recyclerView = findViewById(R.id.rcv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        blanceAdapter = new BlanceAdapter(this, list);
        blanceAdapter.setMode(Attributes.Mode.Single);
        blanceAdapter.setOnClickeListener(new BlanceAdapter.OnClickeListener() {
            @Override
            public void onClick(int postion) {
                Intent intent = new Intent();
                intent.putExtra("datas", list.get(postion));
                setResult(Activity.RESULT_OK, intent);
                finish();
            }

            @Override
            public void delete(int postion) {
                if (list.get(postion).getType() == 1) {
                    detele(list.get(postion).getType(), list.get(postion).getAlipayId(), postion);
                } else if (list.get(postion).getType() == 5) {
                    detele(list.get(postion).getType(), list.get(postion).getBankId(), postion);
                } else {
                    detele(list.get(postion).getType(), list.get(postion).getOtherId(), postion);
                }

            }

            @Override
            public void AddAlipay(int postion) {
                Intent intent = new Intent(SelectorBlanceActivity.this, AddWithdrawActivity.class);
                intent.putExtra("type", list.get(postion).getWithdrawWaySort());
                intent.putExtra("methodBean", list.get(postion));
                startActivityForResult(intent, 1001);
            }

            @Override
            public void AddBank(int postion) {
                Intent intent = new Intent(SelectorBlanceActivity.this, AddWithdrawActivity.class);
                intent.putExtra("type", list.get(postion).getWithdrawWaySort());
                intent.putExtra("methodBean", list.get(postion));
                startActivityForResult(intent, 1001);
            }

            @Override
            public void AddDynamic(int postion) {
                Intent intent = new Intent(SelectorBlanceActivity.this, AddWithdrawActivity.class);
                intent.putExtra("type", list.get(postion).getWithdrawWaySort());
                intent.putExtra("methodBean", list.get(postion));
                startActivityForResult(intent, 1001);
            }
        });
        recyclerView.setAdapter(blanceAdapter);

        getDynamicData();
    }

    private void getDynamicData() {
        HttpUtils.get().url(coreManager.getConfig().GET_WITHDRAW_WAY)
                .params("access_token", coreManager.getSelfStatus().accessToken)
                .build()
                .execute(new ListCallback<DynamicWithdrawalBean>(DynamicWithdrawalBean.class) {

                    @Override
                    public void onResponse(ArrayResult<DynamicWithdrawalBean> result) {
                        if (result != null && result.getData() != null) {
                            list.clear();
                            for (DynamicWithdrawalBean data : result.getData()) {
                                list.add(new BlanceInfo.MethodBean(-1, data.getWithdrawWayName(), data.getWithdrawWaySort(), data.getWithdrawKeyDetails()));
                            }
                        }
                        initData();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        initData();
                    }
                });
    }

    private void initData() {
        HttpUtils.get().url(coreManager.getConfig().LIST_BLANCE)
                .params("access_token", coreManager.getSelfStatus().accessToken)
                .build()
                .execute(new BaseCallback<BlanceInfo>(BlanceInfo.class) {
                    @Override
                    public void onResponse(ObjectResult<BlanceInfo> result) {
                        if (result.getData() != null) {
                            list.addAll(result.getData().getAlipayMethod());
                            list.addAll(result.getData().getBankCardMethod());
                            list.addAll(result.getData().getOtherMethod());
                            if (!TextUtils.isEmpty(mID)) {
                                for (BlanceInfo.MethodBean mList : list) {
                                    if (TextUtils.equals(mID, mList.getAlipayId()) || TextUtils.equals(mID, mList.getBankId()) || TextUtils.equals(mID, mList.getOtherId())) {
                                        mList.setSelect(true);
                                    }
                                }
                            }
                        }
                        blanceAdapter.setData(list);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(SelectorBlanceActivity.this);
                    }
                });
    }

    private void detele(int type, String id, int position) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HttpUtils.get().url(coreManager.getConfig().DELETE_BLANCE)
                .params("access_token", coreManager.getSelfStatus().accessToken)
                .params("type", String.valueOf(type))
                .params("id", id)
                .build()
                .execute(new BaseCallback<BlanceInfo>(BlanceInfo.class) {
                    @Override
                    public void onResponse(ObjectResult<BlanceInfo> result) {
                        DialogHelper.dismissProgressDialog();
                        blanceAdapter.removeList(position);
                        ToastUtil.showToast(SelectorBlanceActivity.this, "删除成功");
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(SelectorBlanceActivity.this);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            getDynamicData();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
