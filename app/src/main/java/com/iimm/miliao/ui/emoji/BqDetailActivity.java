package com.iimm.miliao.ui.emoji;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.iimm.miliao.R;
import com.iimm.miliao.adapter.BqDetailAdapter;
import com.iimm.miliao.bean.ImEmojiStore;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by  on 2019/3/1.
 */
public class BqDetailActivity extends BaseActivity {
    private String bqId;
    private String bqContent;
    private List<ImEmojiStore> datas;
    private BqDetailAdapter adapter;
    private RecyclerView rvContent;


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
        return getResources().getString(R.string.expressions_for_details);
    }


    protected void initViews(Bundle savedInstanceState) {
        rvContent = findViewById(R.id.rv_content);
        datas = new ArrayList<>();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        rvContent.setLayoutManager(gridLayoutManager);
        rvContent.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                Rect rect = new Rect();
                rect.top = 1;
                rect.bottom = 1;
                rect.left = 1;
                rect.right = 1;
                super.getItemOffsets(rect, view, parent, state);
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDraw(c, parent, state);
                c.drawColor(getResources().getColor(R.color.background));
            }
        });
        adapter = new BqDetailAdapter(datas, this);
        rvContent.setAdapter(adapter);
        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            bqId = intent.getStringExtra("bqId");
            bqContent = intent.getStringExtra("bqContent");
        }
        if (bqContent != null) {
            List<ImEmojiStore> listBeans = JSON.parseArray(bqContent, ImEmojiStore.class);
            if (listBeans.size() != 0) {
                datas.clear();
                datas.addAll(listBeans);
                adapter.notifyDataSetChanged();
            }
        } else {
            ToastUtil.showToast(BqDetailActivity.this, getResources().getString(R.string.failed_to_get_emoji));
        }
    }

    public int getContentViewId() {
        return R.layout.activity_bq_detail;
    }

}
