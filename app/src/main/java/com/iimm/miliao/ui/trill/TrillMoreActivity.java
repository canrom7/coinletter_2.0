package com.iimm.miliao.ui.trill;

import android.os.Bundle;
import android.support.v7.widget.StaggeredGridLayoutManager;

import com.iimm.miliao.R;

/**
 * 用户信息短视频
 */
public class TrillMoreActivity extends BaseSlideActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trill_user);


    }

    @Override
    public void intentLeft() {

    }


    public void chageManager() {

        StaggeredGridLayoutManager recyclerViewLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        //        mPager.setLayoutManager(recyclerViewLayoutManager);
    }


    @Override
    public void onBackPressed() {
        //        super.onBackPressed();


    }
}
