package com.iimm.miliao.ui.contacts;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.ui.base.BaseActivity;

import fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import fm.jiecao.jcvideoplayer_lib.JVCideoPlayerStandardSecond;

/**
 * MrLiu253@163.com
 * 新朋友和群组
 *
 * @time 2019-10-19
 */
public class FriendAndGroupActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_and_group);
        getSupportActionBar().hide();
        int type = getIntent().getIntExtra("type", 0);
        View groupFl = findViewById(R.id.friend_and_group_group);
        View friendFl = findViewById(R.id.friend_and_group_friend);
        TextView title = findViewById(R.id.tv_title_center);
        if (type == 1) {
            groupFl.setVisibility(View.VISIBLE);
            friendFl.setVisibility(View.GONE);
            title.setText(R.string.group);
        }
        if (type == 2) {
            groupFl.setVisibility(View.GONE);
            friendFl.setVisibility(View.VISIBLE);
            title.setText(R.string.new_friend);
        }
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        // 点返回键退出全屏视频，
        // 如果DiscoverFragment用在其他activity, 也要加上，
        if (JVCideoPlayerStandardSecond.backPress()) {
            JCMediaManager.instance().recoverMediaPlayer();
        } else {
            super.onBackPressed();
        }
    }
}
