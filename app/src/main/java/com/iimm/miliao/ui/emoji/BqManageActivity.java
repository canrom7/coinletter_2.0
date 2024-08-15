package com.iimm.miliao.ui.emoji;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.iimm.miliao.R;
import com.iimm.miliao.adapter.BqManageAdapter;
import com.iimm.miliao.bean.BqBao;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 表情管理
 * Created by  on 2019/2/28.
 */
public class BqManageActivity extends BaseActivity implements BqManageAdapter.BqManageListener {
    private static final String TYPE = "1";
    RecyclerView rvContent;
    SmartRefreshLayout srlRefresh;
    private List<BqBao> datas;

    private final int REFRESH = 0;
    private final int LOAD_MORE = 1;
    private int currentType = REFRESH;
    private int currentPage = 0;
    private BqManageAdapter adapter;
    private boolean isRefresh = false;
    private String pageSize = "15";


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
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
    }


    protected String getTitleText() {
        return getResources().getString(R.string.expression_management);
    }


    protected void initViews(Bundle savedInstanceState) {
        rvContent = findViewById(R.id.rv_content);
        srlRefresh = findViewById(R.id.srl_refresh);
        datas = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvContent.setLayoutManager(linearLayoutManager);
        adapter = new BqManageAdapter(datas, this);
        adapter.setListener(this);
        rvContent.setAdapter(adapter);
        srlRefresh.autoRefresh(500);
        srlRefresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                currentType = REFRESH;
                currentPage = 0;
                initData();
            }
        });
        srlRefresh.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                currentType = LOAD_MORE;
                currentPage++;
                initData();
            }
        });
    }

    private void initData() {
        Map<String, String> par = new HashMap<>();
        par.put("access_token", coreManager.getSelfStatus().accessToken);
        par.put("pageIndex", currentPage + "");
        par.put("pageSize", pageSize);
        HttpUtils.get().url(coreManager.getConfig().LOAD_MY_BQ)
                .params(par)
                .build()
                .execute(new BaseCallback<String>(String.class) {
                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        try {
                            JSONArray js = new JSONArray(result.getData());
                            if (js.length() > 0) {
                                JSONObject info = js.getJSONObject(0);
                                JSONArray imEmojiStoe = info.getJSONArray("imEmojiStore");
                                String id = info.getString("id");
                                String userId = info.getString("userId");
                                List<BqBao> bqBaoList = new ArrayList<>();
                                for (int i = 0; i < imEmojiStoe.length(); i++) {
                                    BqBao bqBao = JSON.parseObject(imEmojiStoe.getJSONObject(i).toString(), BqBao.class);
                                    bqBao.setId(id);
                                    bqBaoList.add(bqBao);
                                }
                                if (currentPage == 0) {
                                    BqManageActivity.this.datas.clear();
                                }
                                BqManageActivity.this.datas.addAll(bqBaoList);
                                adapter.notifyDataSetChanged();
                                if (bqBaoList.size() < 15) {
                                    srlRefresh.setEnableLoadMore(false);
                                } else {
                                    srlRefresh.setEnableLoadMore(true);
                                }
                            }
                            finishRefreshAndLoadMore();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        finishRefreshAndLoadMore();
                    }
                });
    }

    private void finishRefreshAndLoadMore() {
        if (currentType == REFRESH) {
            srlRefresh.finishRefresh();
        } else {
            srlRefresh.finishLoadMore();
        }
    }


    public int getContentViewId() {
        return R.layout.activity_bq_manage;
    }


    @Override
    public void deleteBqClick(final View v, final int position) {
        isRefresh = true;
        v.setClickable(false);
        Map<String, String> par = new HashMap<>();
        par.put("access_token", coreManager.getSelfStatus().accessToken);
        par.put("customEmoId", datas.get(position).getEmoPackId());
        HttpUtils.get().url(coreManager.getConfig().REMOVE_MY_BQ).params(par).build().execute(new BaseCallback<String>(String.class) {
            @Override
            public void onResponse(ObjectResult<String> result) {
                v.setClickable(true);
                datas.remove(position);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Call call, Exception e) {
                ToastUtil.showToast(BqManageActivity.this, getResources().getString(R.string.the_request_failed));
                v.setClickable(true);
            }
        });
    }


    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("isRefresh", isRefresh);
        setResult(Activity.RESULT_OK, intent);
        super.finish();
    }
}
