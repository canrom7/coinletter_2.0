package com.iimm.miliao.ui.emoji;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.iimm.miliao.R;
import com.iimm.miliao.adapter.BqShopAdapter;
import com.iimm.miliao.bean.BqBao;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;


/**
 * Created by  on 2019/2/28.
 */

public class BqShopActivity extends BaseActivity implements BqShopAdapter.BqShopListener {
    public static final int BQ_CODE_REQUEST = 1000;
    private static final int BQ_MANAGE_REQUEST = 1002;
    RecyclerView rvContent;
    SmartRefreshLayout srlRefresh;
    private int currentPage = 0;
    private List<BqBao> bqBaoList;
    private BqShopAdapter bqShopAdapter;
    private final int REFRESH = 0;
    private final int LOAD_MORE = 1;
    private int currentType = REFRESH;

    private boolean isRefresh = false;
    private static final String pageSize = "15";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bq_shop);
        initActionBar();
        initViews(savedInstanceState);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        TextView tvTitleContent = findViewById(R.id.tv_title_center);
        tvTitleContent.setText(getTitleText());
        ImageView ivTitleRight = findViewById(R.id.iv_title_right);
        ivTitleRight.setImageResource(R.mipmap.bq_setting);
        ivTitleRight.setVisibility(View.VISIBLE);
        ivTitleRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转表情管理
                Intent intent = new Intent(BqShopActivity.this, BqManageActivity.class);
                startActivityForResult(intent, BQ_MANAGE_REQUEST);
            }
        });
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
    }

    protected String getTitleText() {
        return getResources().getString(R.string.expression_store);
    }


    protected void initViews(Bundle savedInstanceState) {
        rvContent = findViewById(R.id.rv_content);
        srlRefresh = findViewById(R.id.srl_refresh);
        bqBaoList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvContent.setLayoutManager(linearLayoutManager);
        bqShopAdapter = new BqShopAdapter(bqBaoList, this);
        bqShopAdapter.setListener(this);
        rvContent.setAdapter(bqShopAdapter);
        event();
        srlRefresh.autoRefresh(500);
    }

    private void initData() {
        Map<String, String> par = new HashMap<>();
        par.put("access_token", coreManager.getSelfStatus().accessToken);
        par.put("pageIndex", currentPage + "");
        par.put("pageSize", pageSize);
        HttpUtils.get().url(coreManager.getConfig().LOAD_BQ_SHOP_LIST)
                .params(par)
                .build().execute(new BaseCallback<String>(String.class) {
            @Override
            public void onResponse(ObjectResult<String> result) {
                List<BqBao> bqBaoList = JSON.parseArray(result.getData(), BqBao.class);
                if (currentPage == 0) {
                    BqShopActivity.this.bqBaoList.clear();
                }
                BqShopActivity.this.bqBaoList.addAll(bqBaoList);
                bqShopAdapter.notifyDataSetChanged();
                if (bqBaoList.size() < 15) {
                    srlRefresh.setEnableLoadMore(false);
                } else {
                    srlRefresh.setEnableLoadMore(true);
                }
                finishRefreshAndLoadMore();
            }

            @Override
            public void onError(Call call, Exception ex) {
                ToastUtil.showToast(BqShopActivity.this, getResources().getString(R.string.the_request_failed));
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

    private void event() {
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


    public static void start(BaseActivity activity, int bqCode) {
        Intent intent = new Intent(activity, BqShopActivity.class);
        activity.startActivityForResult(intent, bqCode);
    }


    @Override
    public void onAddBqClick(final View v, final int position) {
        BqShopActivity.this.isRefresh = true;
        final BqBao bqBao = bqBaoList.get(position);
        v.setClickable(false);
        ((Button) v).setText(R.string.adding);
        Map<String, String> par = new HashMap<>();
        par.put("access_token", coreManager.getSelfStatus().accessToken);
        par.put("customEmoId", bqBao.getEmoPackId());
        HttpUtils.get().url(coreManager.getConfig().ADD_BQ).params(par).build().execute(new BaseCallback<String>(String.class) {
            @Override
            public void onResponse(ObjectResult<String> result) {
                bqBao.setEmoDownStatus(1);
                bqShopAdapter.notifyItemChanged(position);
            }

            @Override
            public void onError(Call call, Exception e) {
                ((Button) v).setText(R.string.add);
                ToastUtil.showToast(BqShopActivity.this, getResources().getString(R.string.add_failure));
                v.setClickable(true);
            }
        });
    }

    @Override
    public void onItemClick(View v, int position) {
        BqBao bqBao = bqBaoList.get(position);
        Intent intent = new Intent(BqShopActivity.this, BqDetailActivity.class);
        intent.putExtra("bqId", bqBao.getEmoPackId());
        intent.putExtra("bqContent", JSON.toJSON(bqBao.getImEmojiStoreListInfo()).toString());
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case BQ_MANAGE_REQUEST:
                    if (data != null) {
                        boolean isRefresh = data.getBooleanExtra("isRefresh", false);
                        if (isRefresh) {
                            srlRefresh.autoRefresh(500);
                            BqShopActivity.this.isRefresh = true;
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("isRefresh", isRefresh);
        setResult(Activity.RESULT_OK, intent);
        super.finish();
    }
}
