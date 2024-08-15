package com.iimm.miliao.ui.message.multi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.ToastUtil;
import com.suke.widget.SwitchButton;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * 群管理
 */
public class GroupManager extends BaseActivity {
    /**
     * 更新群组 是否显示已读人数、私密群组、是否开启进群验证、是否对普通成员开放群成员列表、群成员是否可在群组内发送名片
     */
    String authority;
    private String mRoomId;
    private String mRoomJid;
    SwitchButton.OnCheckedChangeListener mOnCheckedChangeListener = new SwitchButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(SwitchButton view, boolean isChecked) {
            switch (view.getId()) {
                case R.id.sb_read:
                    updateGroupHostAuthority(0, isChecked);
                    break;
                case R.id.sb_look:
                    updateGroupHostAuthority(1, isChecked);
                    break;
                case R.id.sb_verify:
                    updateGroupHostAuthority(2, isChecked);
                    break;
                case R.id.sb_show_member:
                    updateGroupHostAuthority(3, isChecked);
                    break;
                case R.id.sb_allow_chat:
                    updateGroupHostAuthority(4, isChecked);
                    break;
                case R.id.sb_allow_invite:
                    updateGroupHostAuthority(5, isChecked);
                    break;
                case R.id.sb_allow_upload:
                    updateGroupHostAuthority(6, isChecked);
                    break;
                case R.id.sb_allow_conference:
                    updateGroupHostAuthority(7, isChecked);
                    break;
                case R.id.sb_allow_send_course:
                    updateGroupHostAuthority(8, isChecked);
                    break;
                case R.id.sb_notify:
                    updateGroupHostAuthority(9, isChecked);
                    break;
                case R.id.sb_allow_strong_remind:
                    updateGroupHostAuthority(10, isChecked);
                    break;
                case R.id.sb_sign:

                    updateGroupHostAuthority(11, isChecked);
                    break;
            }
        }
    };
    private int[] status_lists;
    private SwitchButton mSbRead;
    private SwitchButton mSbLook;
    private SwitchButton mSbVerify;
    private SwitchButton mSbShowMember;
    private SwitchButton mSbAllowChat;
    private SwitchButton mSbAllowInvite;
    private SwitchButton mSbAllowUpload;
    private SwitchButton mSbAllowConference;
    private SwitchButton mSbAllowSendCourse;
    private SwitchButton mSbNotify;
    private SwitchButton mSbAllowStrongRemind;
    private SwitchButton mSign;
    View sign_history_view;
    RelativeLayout sign_history;
    LinearLayout group_sign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_manager);
        mRoomId = getIntent().getStringExtra("roomId");
        mRoomJid = getIntent().getStringExtra("roomJid");
        status_lists = getIntent().getIntArrayExtra("GROUP_STATUS_LIST");
        initAction();
        initView();
    }

    private void initAction() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.group_management));
    }

    private void initView() {
        group_sign = (LinearLayout) findViewById(R.id.group_sign);
        mSbRead = (SwitchButton) findViewById(R.id.sb_read);
        mSbLook = (SwitchButton) findViewById(R.id.sb_look);
        mSbVerify = (SwitchButton) findViewById(R.id.sb_verify);
        mSbShowMember = (SwitchButton) findViewById(R.id.sb_show_member);
        mSbAllowChat = (SwitchButton) findViewById(R.id.sb_allow_chat);
        mSbAllowInvite = (SwitchButton) findViewById(R.id.sb_allow_invite);
        mSbAllowUpload = (SwitchButton) findViewById(R.id.sb_allow_upload);
        mSbAllowConference = (SwitchButton) findViewById(R.id.sb_allow_conference);
        mSbAllowSendCourse = (SwitchButton) findViewById(R.id.sb_allow_send_course);
        mSbNotify = (SwitchButton) findViewById(R.id.sb_notify);
        sign_history_view = findViewById(R.id.sign_history_view);
        sign_history = findViewById(R.id.sign_history);
        mSbAllowStrongRemind = findViewById(R.id.sb_allow_strong_remind);
        mSign = findViewById(R.id.sb_sign);
        mSbRead.setChecked(status_lists[0] == 1);
        mSbLook.setChecked(status_lists[1] == 1);
        mSbVerify.setChecked(status_lists[2] == 1);
        mSbShowMember.setChecked(status_lists[3] == 1);
        mSbAllowChat.setChecked(status_lists[4] == 1);
        mSbAllowInvite.setChecked(status_lists[5] == 1);
        mSbAllowUpload.setChecked(status_lists[6] == 1);
        mSbAllowConference.setChecked(status_lists[7] == 1);
        mSbAllowSendCourse.setChecked(status_lists[8] == 1);
        mSbNotify.setChecked(status_lists[9] == 1);
        mSbAllowStrongRemind.setChecked(status_lists[10] == 1);
        mSign.setChecked(status_lists[11] == 1);
        if (status_lists[11] == 1) {
            sign_history.setVisibility(View.VISIBLE);
            sign_history_view.setVisibility(View.VISIBLE);
        } else {
            sign_history.setVisibility(View.GONE);
            sign_history_view.setVisibility(View.GONE);
        }
        mSbRead.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbLook.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbVerify.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbShowMember.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbAllowChat.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbAllowInvite.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbAllowUpload.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbAllowConference.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbAllowSendCourse.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbNotify.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbAllowStrongRemind.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSign.setOnCheckedChangeListener(mOnCheckedChangeListener);

        // 设置 &&  取消 管理员，隐身人，监控人的按钮设置监听，
        @SuppressLint("UseSparseArrays")
        Map<Integer, Integer> setRoleIdMap = new HashMap<>();
        setRoleIdMap.put(R.id.set_manager_rl, Constants.ROLE_MANAGER);
        setRoleIdMap.put(R.id.set_invisible_rl, Constants.ROLE_INVISIBLE);
        setRoleIdMap.put(R.id.set_guardian_rl, Constants.ROLE_GUARDIAN);
        for (Integer id : setRoleIdMap.keySet()) {
            findViewById(id).setOnClickListener(v -> {
                SetManagerActivity.start(this, mRoomId, mRoomJid, setRoleIdMap.get(id));
            });
        }

        findViewById(R.id.set_remarks_rl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupManager.this, GroupMoreFeaturesActivity.class);
                intent.putExtra("roomId", mRoomId);
                intent.putExtra("isSetRemark", true);
                startActivity(intent);
            }
        });

        findViewById(R.id.transfer_group_rl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupManager.this, GroupTransferActivity.class);
                intent.putExtra("roomId", mRoomId);
                intent.putExtra("roomJid", mRoomJid);
                startActivity(intent);
                finish();
            }
        });
        sign_history.setOnClickListener(v -> {
            Intent intent = new Intent(GroupManager.this, GroupSignRecordActivity.class);
            intent.putExtra("roomJid", mRoomJid);
            startActivity(intent);
        });
        if (Constants.SUPPORT_GROUP_SIGN) {
            group_sign.setVisibility(View.VISIBLE);
        } else {
            group_sign.setVisibility(View.GONE);
        }

    }

    private void updateGroupHostAuthority(final int type, final boolean isChecked) {
        authority = isChecked ? "1" : "0";
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoomId);
        if (type == 0) {
            params.put("showRead", authority);
        } else if (type == 1) {
            params.put("isLook", authority);
        } else if (type == 2) {
            params.put("isNeedVerify", authority);
        } else if (type == 3) {
            params.put("showMember", authority);
        } else if (type == 4) {
            params.put("allowSendCard", authority);
        } else if (type == 5) {
            params.put("allowInviteFriend", authority);
        } else if (type == 6) {
            params.put("allowUploadFile", authority);
        } else if (type == 7) {
            params.put("allowConference", authority);
        } else if (type == 8) {
            params.put("allowSpeakCourse", authority);
        } else if (type == 9) {
            params.put("isAttritionNotice", authority);
        } else if (type == 10) {
            params.put("allowForceNotice", authority);
        } else if (type == 11) {
            params.put("isShowSignIn", authority);
        }
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            EventBus.getDefault().post(new EventGroupStatus(type, Integer.valueOf(authority)));// 更新群组信息页面
                            String str;
                            if (isChecked) {
                                str = getString(R.string.is_open);
                            } else {
                                str = getString(R.string.is_close);
                            }
                            if (type == 0) {
                                PreferenceUtils.putBoolean(mContext, Constants.IS_SHOW_READ + mRoomJid, isChecked);
                                MsgBroadcast.broadcastMsgRoomUpdate(mContext);// 服务端不会给调用接口者推送对应的XMPP协议，所以需要通知聊天界面刷新
                            } else if (type == 4) {
                                PreferenceUtils.putBoolean(mContext, Constants.IS_SEND_CARD + mRoomJid, isChecked);
                            } else if (type == 7) {
                                PreferenceUtils.putBoolean(mContext, Constants.IS_ALLOW_NORMAL_CONFERENCE + mRoomJid, isChecked);
                            } else if (type == 8) {
                                PreferenceUtils.putBoolean(mContext, Constants.IS_ALLOW_NORMAL_SEND_COURSE + mRoomJid, isChecked);
                            } else if (type == 10) {
                                PreferenceUtils.putBoolean(mContext, Constants.IS_ALLOW_SEND_STRONG_REMIND + mRoomJid, isChecked);
                            } else if (type == 11) {
                                PreferenceUtils.putBoolean(mContext, Constants.IS_GROUP_SIGN + mRoomJid, isChecked);
                                if (isChecked) {
                                    sign_history.setVisibility(View.VISIBLE);
                                    sign_history_view.setVisibility(View.VISIBLE);
                                } else {
                                    sign_history.setVisibility(View.GONE);
                                    sign_history_view.setVisibility(View.GONE);
                                }
                            }
                            tip(str);
                        } else {
                            ToastUtil.showErrorData(mContext);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(mContext);
                    }
                });
    }

    public void tip(String tip) {
//        TipDialog tipDialog = new TipDialog(mContext);
//        tipDialog.setTip(tip);
//        tipDialog.show();
        ToastUtil.showToast(getBaseContext(), tip);
    }
}
