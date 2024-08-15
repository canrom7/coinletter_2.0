package com.iimm.miliao.ui.emoji;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.adapter.CustomBqManageAdapter;
import com.iimm.miliao.bean.collection.Collectiion;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by  on 2019/3/1.
 */

public class CustomBqManageActivity extends BaseActivity implements CustomBqManageAdapter.CustomBqManageListener {
    TextView ok;
    RecyclerView rvContent;
    private List<Collectiion> customBqBeans;
    private CustomBqManageAdapter adapter;
    private boolean isRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());
        initActionBar();
        initViews(savedInstanceState);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        TextView tvCenterTitle = findViewById(R.id.tv_title_center);
        tvCenterTitle.setText(getTitleText());
        ok = findViewById(R.id.tv_title_right);
        ok.setVisibility(View.VISIBLE);
        ok.setText(R.string.delete);
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
    }


    protected String getTitleText() {
        return getResources().getString(R.string.custom_emoji_management);
    }

    protected void initViews(Bundle savedInstanceState) {
        rvContent = findViewById(R.id.rv_content);
        customBqBeans = new ArrayList<>();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 5);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvContent.setLayoutManager(gridLayoutManager);
        adapter = new CustomBqManageAdapter(customBqBeans, this);
        adapter.setListener(this);
        rvContent.setAdapter(adapter);
        event();
        getData();
    }

    private void getData() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(this).accessToken);
        params.put("userId", CoreManager.requireSelf(this).getUserId());
        HttpUtils.get().url(CoreManager.requireConfig(this).Collection_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<Collectiion>(Collectiion.class) {
                    @Override
                    public void onResponse(ArrayResult<Collectiion> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            customBqBeans.clear();
                            customBqBeans.addAll(result.getData());
                            adapter.notifyDataSetChanged();
                        } else {
                            ToastUtil.showToast(MyApplication.getContext(), result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(MyApplication.getContext());
                    }
                });
    }

    private void event() {
        ok.setVisibility(View.VISIBLE);
        ok.setText(R.string.delete);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRefresh = true;
                if (adapter != null) {
                    List<Collectiion> selectP = adapter.getSelectPosition();
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < selectP.size(); i++) {
                        builder.append(selectP.get(i).getEmojiId() + ",");
                    }
                    if (selectP.size() > 0) {
                        deleteMyCustomBq(builder, selectP);
                    }
                }
//                isEidt = !isEidt;
//                adapter.setEdit(isEidt);
//                if (isEidt) {
//                    ok.setText(R.string.delete);
//                } else {
//                    ok.setText(R.string.finishing);
//                }
            }
        });

    }

    private void deleteMyCustomBq(StringBuilder builder, final List<Collectiion> selectP) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("emojiId", builder.toString());

        HttpUtils.get().url(coreManager.getConfig().Collection_REMOVE)
                .params(params)
                .build()
                .execute(new BaseCallback<Collectiion>(Collectiion.class) {

                    @Override
                    public void onResponse(ObjectResult<Collectiion> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            customBqBeans.removeAll(selectP);
                            adapter.clearSelect();
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    public int getContentViewId() {
        return R.layout.activity_custom_bq_manage;
    }

    @Override
    public void switchSelect(View v, int position) {
        Collectiion customBqBean = customBqBeans.get(position);
        customBqBean.setSelect(customBqBean.isSelect() ? false : true);
        if (customBqBean.isSelect()) {
            adapter.addDelete(customBqBean);
        } else {
            adapter.removeDelete(customBqBean);
        }
        adapter.notifyItemChanged(position);
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("isRefresh", isRefresh);
        setResult(Activity.RESULT_OK, intent);
        super.finish();
    }
}
