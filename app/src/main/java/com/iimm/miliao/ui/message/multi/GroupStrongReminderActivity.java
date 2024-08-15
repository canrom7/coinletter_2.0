package com.iimm.miliao.ui.message.multi;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.message.MucChatActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.view.HeadView;


/**
 * 群的强提醒界面
 * Created by rwq on 2019/5/16.
 */

public class GroupStrongReminderActivity extends BaseActivity {
    private MediaPlayer media;
    private TextView tvName;
    private LinearLayout llRoot;
    private TextView tvContent;
    private LinearLayout llIgnore;
    private LinearLayout llComeIn;
    private TextView tvHowCloseHint;
    private HeadView civHeadImg;
    private TextView tvGroupName;
    private String groupId;
    private String groupName;
    private Vibrator mVibrator;
    private String currentChatWithId = "Empty";  //当前聊天的ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_strong_reminder);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        initView();
        event();
        handleViewBlur();
        playSoundAndShock();
    }


    private void handleViewBlur() {


    }

    /**
     * 播放声音震动
     */
    private void playSoundAndShock() {
        if (media == null) {
            media = MediaPlayer.create(this, R.raw.group_strong_reminder);
        }
        media.setLooping(true);
        media.start();
        //开始震动
        mVibrator = (Vibrator) MyApplication.getContext().getSystemService(VIBRATOR_SERVICE);
        long[] pattern = {100, 400, 100, 400};
        mVibrator.vibrate(pattern, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (media != null && !media.isPlaying()) {
            media.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (media != null) {
            media.stop();
        }
        mVibrator.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (media != null) {
            media.stop();
            media.release();
            media = null;
        }
        mVibrator.cancel();
    }

    private void event() {
        llComeIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comeIn();
            }
        });
        llIgnore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ignore();
            }
        });
        tvHowCloseHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.showToast(GroupStrongReminderActivity.this, R.string.how_close_group_reminder_content);
            }
        });
    }

    /**
     * 进入群聊
     */
    private void comeIn() {
        if (TextUtils.isEmpty(groupId) || TextUtils.isEmpty(groupName)) {
            ToastUtil.showToast(this, R.string.failed_into_the_group);
        } else {
            if (!currentChatWithId.equals(groupId)) {  //只有当前聊天的对象不是 当前群组才进行 跳转 否则直接关闭
                Intent intent = new Intent(GroupStrongReminderActivity.this, MucChatActivity.class);
                intent.putExtra(AppConstant.EXTRA_USER_ID, groupId);
                intent.putExtra(AppConstant.EXTRA_NICK_NAME, groupName);
                intent.putExtra(AppConstant.EXTRA_IS_GROUP_CHAT, true);
                startActivity(intent);
                FriendDao.getInstance().markUserMessageRead(coreManager.getSelf().getUserId(), groupId); //更新 数据库 当前群未读消息数未0
                Intent boardCastIntent = new Intent();
                boardCastIntent.setAction(MsgBroadcast.ACTION_MSG_NUM_RESET);
                sendBroadcast(boardCastIntent);   //发送广播 通知 MainActivity 进行消息数更新
            }
            finish();
        }
    }

    /**
     * 忽略请求
     */
    private void ignore() {
        finish();
    }

    private void initView() {
        tvName = findViewById(R.id.tv_name);
        llRoot = findViewById(R.id.ll_root);
        tvContent = findViewById(R.id.tv_content);
        llIgnore = findViewById(R.id.ll_ignore);
        llComeIn = findViewById(R.id.ll_come_in);
        tvHowCloseHint = findViewById(R.id.tv_how_close_hint);
        civHeadImg = findViewById(R.id.civ_head_img);
        if (Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR) {
            civHeadImg.setRound(true);
        } else {
            civHeadImg.setRound(false);
        }
        tvGroupName = findViewById(R.id.tv_group_name);
        Intent intent = getIntent();
        if (intent != null) {
            groupId = intent.getStringExtra("groupId");
            groupName = intent.getStringExtra("groupName");
            currentChatWithId = intent.getStringExtra("currentChatWithId");

            String headUrl = intent.getStringExtra("sendUserHeadImg");
            if (!TextUtils.isEmpty(headUrl)) {
                Glide.with(this).load(headUrl).into(civHeadImg.getHeadImage());
            }
            String sendUserName = intent.getStringExtra("sendUserName");
            if (!TextUtils.isEmpty(sendUserName)) {
                tvName.setText(sendUserName);
            }
            String content = intent.getStringExtra("content");
            if (!TextUtils.isEmpty(content)) {
                tvContent.setText(content);
                tvContent.setMovementMethod(ScrollingMovementMethod.getInstance());
            }
            String groupName = intent.getStringExtra("groupName");
            if (!TextUtils.isEmpty(groupName)) {
                tvGroupName.setText(getResources().getString(R.string.from) + ":" + groupName);
            }
        }
    }


    // Remix Blur
    private void blur(Bitmap overlay, float radius, View view) {
        RenderScript rs = RenderScript.create(this);
        Allocation overlayAlloc = Allocation.createFromBitmap(rs, overlay);
        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, overlayAlloc.getElement());
        blur.setInput(overlayAlloc);
        blur.setRadius(radius);
        blur.forEach(overlayAlloc);
        overlayAlloc.copyTo(overlay);
        view.setBackground(new BitmapDrawable(getResources(), overlay));
        rs.destroy();
    }


}
