package com.iimm.miliao.ui.me;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.nearby.NearbyGridFragment;
import com.iimm.miliao.ui.nearby.NearbyMapFragment;
import com.iimm.miliao.ui.nearby.UserAddActivity;
import com.iimm.miliao.util.DisplayUtil;
import com.iimm.miliao.util.MyFragmentManager;
import com.iimm.miliao.view.NearSeachDialog;
import com.iimm.miliao.view.TabView;

/**
 * 附近的人
 */

public class NearPersonActivity extends BaseActivity {
    private TabView tabView;
    private MyFragmentManager mMyFragmentManager;
    // 列表
    private NearbyGridFragment mGridFragment;
    // 地图
    private NearbyMapFragment mMapFragment;
    private NearSeachDialog nearSeachDialog;
    NearSeachDialog.OnNearSeachDialogClickListener onNearSeachDialogClickListener = new NearSeachDialog.OnNearSeachDialogClickListener() {

        @Override
        public void tv1Click() {
            mGridFragment.refreshData("");
            mMapFragment.refreshData("");
        }

        @Override
        public void tv2Click() {
            mGridFragment.refreshData("1");
            mMapFragment.refreshData("1");

        }

        @Override
        public void tv3Click() {
            mGridFragment.refreshData("0");
            mMapFragment.refreshData("0");
        }

        @Override
        public void tv4Click() {
            nearSeachDialog.dismiss();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardcast);

        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(InternationalizationHelper.getString("JXNearVC_NearPer"));
        ImageView ivRight = (ImageView) findViewById(R.id.iv_title_right);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) ivRight.getLayoutParams();
        lp.setMargins(0, DisplayUtil.dip2px(mContext, 2), DisplayUtil.dip2px(mContext, 55), 0);
        ivRight.setImageResource(R.drawable.search_near);
        ivRight.setOnClickListener((view) -> {
            Intent intent = new Intent(NearPersonActivity.this, UserAddActivity.class);
            startActivity(intent);
        });

        TextView tv_title_right = (TextView) findViewById(R.id.tv_title_right);
        tv_title_right.setText(getString(R.string.screening_condit));
        tv_title_right.setOnClickListener((view) -> {
            nearSeachDialog = new NearSeachDialog(NearPersonActivity.this, onNearSeachDialogClickListener);
            nearSeachDialog.show();
        });

        tabView = new TabView(this);
        tabView.getAttention_each_tv().setText(InternationalizationHelper.getString("JXNearVC_NearPer"));
        tabView.getAttention_single_tv().setText(InternationalizationHelper.getString("MAP"));
        ((LinearLayout) findViewById(R.id.ll_content)).addView(tabView.getView(), 0);

        mGridFragment = new NearbyGridFragment();
        mMapFragment = new NearbyMapFragment();
        mMyFragmentManager = new MyFragmentManager(this, R.id.fl_fragments);
        mMyFragmentManager.add(mGridFragment, mMapFragment);
        tabView.setOnTabSelectedLisenter(index -> mMyFragmentManager.show(index));
        tabView.callOnSelect(1);
    }

}
