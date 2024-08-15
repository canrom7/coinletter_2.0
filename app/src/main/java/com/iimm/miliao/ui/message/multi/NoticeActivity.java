package com.iimm.miliao.ui.message.multi;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.message.MucRoom;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.DisplayUtil;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * MrLiu253@163.com
 *
 * @time 2019-06-28
 */
public class NoticeActivity extends BaseActivity {

    private EditText mAnnouncement;
    private int mRole;
    private String mRoomId;
    private ImageView ivCheckIcon;
    private TextView tvCheckHint;
    private boolean showRemind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice);
        getSupportActionBar().hide();


        mRole = getIntent().getIntExtra("mRole", 3);
        mRoomId = getIntent().getStringExtra("mRoomId");
        initView();
    }

    private void initView() {
        mAnnouncement = findViewById(R.id.input_announcement);
        TextView mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText("群公告/群说明");
        TextView tvRight = (TextView) findViewById(R.id.tv_title_right);
        DisplayUtil.setRightOkStyle(this, tvRight, R.string.confirm);
        tvRight.setOnClickListener(v -> {
            if (mRole == 1 || mRole == 2) {
                String str = mAnnouncement.getText().toString();
                if (TextUtils.isEmpty(str)) {
                    return;
                }
                updateNotice(str);
            } else {
                ToastUtil.showToast(NoticeActivity.this, R.string.tip_cannot_public_bulletin);
            }
        });
        findViewById(R.id.iv_title_left).setOnClickListener(view -> finish());

        ivCheckIcon = findViewById(R.id.iv_check_icon);
        tvCheckHint = findViewById(R.id.tv_check_hint);
        ivCheckIcon.setSelected(false);
        findViewById(R.id.ll_strong_remind).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showRemind) {
                    ivCheckIcon.setSelected(false);
                    showRemind = false;
                } else {
                    ivCheckIcon.setSelected(true);
                    showRemind = true;
                }
            }
        });


    }

    private void updateNotice(final String text) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoomId);
        params.put("notice", text);
        params.put("allowForceNotice", showRemind ? "1" : "0");
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            MucRoom.Notice notice = new MucRoom.Notice();
                            notice.setUserId(coreManager.getSelf().getUserId());
                            notice.setNickname(coreManager.getSelf().getNickName());
                            notice.setTime(TimeUtils.time_current_time());
                            notice.setNoticeType(showRemind ? 1 : 0);
                            notice.setText(text);
                            if (!TextUtils.isEmpty(result.getResultMsg())) {
                                // 按理说公告发布成功后，服务端需要返回data过来，因为删除公告需要公告id，但服务端比较懒，直接将公告id返回到了resultMsg字段内，我们就这样取值吧
                                notice.setId(result.getResultMsg());
                            }

                            Intent intent = new Intent();
                            intent.putExtra("data", notice);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }
}
