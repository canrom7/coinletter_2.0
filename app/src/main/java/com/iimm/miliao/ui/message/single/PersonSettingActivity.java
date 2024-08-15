package com.iimm.miliao.ui.message.single;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.example.qrcode.Constant;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.bean.EventBusMsg;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.Label;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.LabelDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.pay.TransferRecordActivity;
import com.iimm.miliao.ui.MainActivity;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.groupchat.SelectContactsActivity;
import com.iimm.miliao.ui.message.search.SearchChatHistoryActivity;
import com.iimm.miliao.ui.other.BasicInfoActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.LogUtils;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ToolUtils;
import com.iimm.miliao.util.UiUtils;
import com.iimm.miliao.view.MsgSaveDaysDialog;
import com.iimm.miliao.view.SelectionFrame;
import com.iimm.miliao.xmpp.util.ImHelper;
import com.suke.widget.SwitchButton;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * Created by Administrator on 2018/4/18 0018.
 * 聊天设置界面，好友资料
 */
public class PersonSettingActivity extends BaseActivity implements View.OnClickListener {
    private String TAG = "PersonSettingActivity";
    private ImageView mFriendAvatarIv;
    private TextView mFriendNameTv;
    private TextView mRemarkNameTv;
    private TextView mLabelNameTv;
    private SwitchButton mIsReadFireSb;
    private SwitchButton mTopSb;
    private SwitchButton mIsDisturbSb;
    private TextView mMsgSaveDays;

    private String mLoginUserId;
    private String mFriendId;
    private View sb_read_fire_rl;

    MsgSaveDaysDialog.OnMsgSaveDaysDialogClickListener onMsgSaveDaysDialogClickListener = new MsgSaveDaysDialog.OnMsgSaveDaysDialogClickListener() {
        @Override
        public void tv1Click() {
            updateChatRecordTimeOut(-1);
        }

        @Override
        public void tv2Click() {
            updateChatRecordTimeOut(0.04);
            // updateChatRecordTimeOut(0.00347); // 五分钟过期
        }

        @Override
        public void tv3Click() {
            updateChatRecordTimeOut(1);
        }

        @Override
        public void tv4Click() {
            updateChatRecordTimeOut(7);
        }

        @Override
        public void tv5Click() {
            updateChatRecordTimeOut(30);
        }

        @Override
        public void tv6Click() {
            updateChatRecordTimeOut(90);
        }

        @Override
        public void tv7Click() {
            updateChatRecordTimeOut(365);
        }
    };
    private Friend mFriend;
    private String mFriendName;
    private RefreshBroadcastReceiver receiver = new RefreshBroadcastReceiver();
    private SeekBar mSbReadFireTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_setting);
        EventBus.getDefault().register(this);
        mLoginUserId = coreManager.getSelf().getUserId();
        mFriendId = getIntent().getStringExtra("ChatObjectId");
        mFriend = FriendDao.getInstance().getFriend(mLoginUserId, mFriendId);

        if (mFriend == null) {
            LogUtils.log(getIntent());
            Reporter.unreachable();
            ToastUtil.showToast(this, R.string.tip_friend_not_found);
            finish();
            return;
        }

        initActionBar();
        initView();
        registerReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFriend = FriendDao.getInstance().getFriend(mLoginUserId, mFriendId);// Friend也更新下
        if (mFriend == null) {
            Toast.makeText(this, R.string.tip_friend_removed, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            initFriendInfoView();
        }
    }

    public void initFriendInfoView() {
        mFriendName = TextUtils.isEmpty(mFriend.getRemarkName()) ? mFriend.getNickName() : mFriend.getRemarkName();
        mFriendNameTv.setText(mFriendName);
        if (mFriend.getRemarkName() != null) {
            mRemarkNameTv.setText(mFriend.getRemarkName());
        }
        List<Label> friendLabelList = LabelDao.getInstance().getFriendLabelList(mLoginUserId, mFriendId);
        String labelNames = "";
        if (friendLabelList != null && friendLabelList.size() > 0) {
            for (int i = 0; i < friendLabelList.size(); i++) {
                if (i == friendLabelList.size() - 1) {
                    labelNames += friendLabelList.get(i).getGroupName();
                } else {
                    labelNames += friendLabelList.get(i).getGroupName() + "，";
                }
            }
        }
        mLabelNameTv.setText(labelNames);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void processEventBusMsg(EventBusMsg eventBusMsg) {
        if (eventBusMsg.getMessageType() == Constants.EVENTBUS_MSG_FRIEND_INFO_UPDATE_UI) {
            String friendId = (String) eventBusMsg.getObject();
            if (!TextUtils.isEmpty(friendId) && friendId.equals(mFriend.getUserId())) {
                mFriend = FriendDao.getInstance().getFriend(mLoginUserId, friendId);
                initFriendInfoView();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            // 无论如何不应该在destroy崩溃，
        }
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(this);
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.chat_settings));
    }

    private void initView() {
        mFriendAvatarIv = (ImageView) findViewById(R.id.person_avatar);
        AvatarHelper.getInstance().displayAvatar(mFriendId, mFriendAvatarIv, true);
        mFriendNameTv = (TextView) findViewById(R.id.name);
        mRemarkNameTv = (TextView) findViewById(R.id.remark_name);
        mLabelNameTv = (TextView) findViewById(R.id.label_name);
        TextView mNoDisturbTv = (TextView) findViewById(R.id.no_disturb_tv);
        mNoDisturbTv.setText(InternationalizationHelper.getString("JX_MessageFree"));
        // 阅后即焚 && 置顶 && 消息免打扰
        mIsReadFireSb = (SwitchButton) findViewById(R.id.sb_read_fire);
        TextView tvFireTimeHint = findViewById(R.id.tv_fire_time_hint);
        mSbReadFireTime = findViewById(R.id.sb_read_fire_time);

        int isReadDel = PreferenceUtils.getInt(mContext, Constants.MESSAGE_READ_FIRE + mFriendId + mLoginUserId, 0);
        tvFireTimeHint.setText(String.format(getResources().getString(R.string.read_fire_time_hint), handlerMsg(isReadDel)));
        if (isReadDel != 0) { //不是零 就是开着的
            tvFireTimeHint.setVisibility(View.VISIBLE);
            mSbReadFireTime.setVisibility(View.VISIBLE);
        } else {
            tvFireTimeHint.setVisibility(View.GONE);
            mSbReadFireTime.setVisibility(View.GONE);
        }
        mIsReadFireSb.setChecked(isReadDel != 0);
        mIsReadFireSb.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                saveProgress(mSbReadFireTime.getProgress(), isChecked);
                if (isChecked) {
                    tvFireTimeHint.setVisibility(View.VISIBLE);
                    mSbReadFireTime.setVisibility(View.VISIBLE);
                    ToastUtil.showToast(PersonSettingActivity.this, R.string.tip_status_burn);
                } else {
                    tvFireTimeHint.setVisibility(View.GONE);
                    mSbReadFireTime.setVisibility(View.GONE);
                }
                int result = PreferenceUtils.getInt(mContext, Constants.MESSAGE_READ_FIRE + mFriendId + mLoginUserId);
                Map<String, Object> map = new ArrayMap<>();
                map.put("isReadDel", result + "");
                JSONObject json = new JSONObject(map);
                ImHelper.syncFriendMsgDeadDelToOtherMachine(json.toJSONString(), mFriendId);
            }
        });
        mSbReadFireTime.setProgress((int) (isReadDel / 11.0 * 100));
        tvFireTimeHint.setText(String.format(getResources().getString(R.string.read_fire_time_hint), handlerMsg(isReadDel)));
        mSbReadFireTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int result = handlerFireTimeProgress(progress);
                tvFireTimeHint.setText(String.format(getResources().getString(R.string.read_fire_time_hint), handlerMsg(result)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                saveProgress(progress, true);
            }
        });
        mTopSb = (SwitchButton) findViewById(R.id.sb_top_chat);
        mTopSb.setChecked(mFriend.getTopTime() != 0);// TopTime不为0，当前状态为置顶
        mTopSb.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked) {
                    FriendDao.getInstance().updateTopFriend(mFriendId, mFriend.getTimeSend());
                } else {
                    FriendDao.getInstance().resetTopFriend(mFriendId);
                }
            }
        });

        mIsDisturbSb = (SwitchButton) findViewById(R.id.sb_no_disturb);
        mIsDisturbSb.setChecked(mFriend.getOfflineNoPushMsg() == 1);
        mIsDisturbSb.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                updateDisturbStatus(isChecked);
            }
        });

        mMsgSaveDays = (TextView) findViewById(R.id.msg_save_days_tv);
        mMsgSaveDays.setText(conversion(mFriend.getChatRecordTimeOut()));

        findViewById(R.id.person_avatar).setOnClickListener(this);
        if (coreManager.getLimit().cannotCreateGroup()) {
            findViewById(R.id.add_contacts).setVisibility(View.GONE);
        } else {
            findViewById(R.id.add_contacts).setOnClickListener(this);
        }
        findViewById(R.id.chat_history_search).setOnClickListener(this);
        findViewById(R.id.remark_rl).setOnClickListener(this);
        findViewById(R.id.label_rl).setOnClickListener(this);
        findViewById(R.id.msg_save_days_rl).setOnClickListener(this);
        findViewById(R.id.set_background_rl).setOnClickListener(this);
        findViewById(R.id.chat_history_empty).setOnClickListener(this);
        findViewById(R.id.sync_chat_history_empty).setOnClickListener(this);
        findViewById(R.id.rl_transfer).setOnClickListener(this);
        sb_read_fire_rl = findViewById(R.id.sb_read_fire_rl);
        if (coreManager.getConfig().isDelAfterReading == 0) {
            if (sb_read_fire_rl != null) {
                sb_read_fire_rl.setVisibility(View.VISIBLE);
            }
        } else {
            if (sb_read_fire_rl != null) {
                sb_read_fire_rl.setVisibility(View.GONE);
            }
            PreferenceUtils.putInt(mContext, Constants.MESSAGE_READ_FIRE + mFriendId + mLoginUserId, 0);
        }
    }


    /**
     * 存储 阅后既焚 进度
     *
     * @param progress
     * @param isChecked
     */
    private void saveProgress(int progress, boolean isChecked) {
        if (progress == 0) {
            progress = 5;
        }
        int result = handlerFireTimeProgress(progress);
        if (result == 0) {
            result = 1;
        }
        PreferenceUtils.putInt(mContext, Constants.MESSAGE_READ_FIRE + mFriendId + mLoginUserId, isChecked ? result : 0);
    }

    //(时间选项备注： 5秒  10秒   30秒   1分钟 5分钟  30分钟  1小时  6小时   12小时   1天
    public static String handlerMsg(int result) {
        switch (result) {
            case 1:
                return "5秒";
            case 2:
                return "10秒";
            case 3:
                return "30秒";
            case 4:
                return "1分钟";
            case 5:
                return "5分钟";
            case 6:
                return "30分钟";
            case 7:
                return "1小时";
            case 8:
                return "6小时";
            case 9:
                return "12小时";
            case 10:
                return "1天";
            case 11:
                return "1星期";
            default:
                return "5秒";
        }
    }

    private int handlerFireTimeProgress(int progress) {
        int result = (int) (progress / 100.0 * 11);
        Log.i(TAG, "滑动的坐标：" + result);
        return result;
    }

    @Override
    public void onClick(View v) {
        if (!UiUtils.isNormalClick(v)) {
            return;
        }
        switch (v.getId()) {
            case R.id.iv_title_left:
                finish();
                break;
            case R.id.person_avatar:
                Intent intentBasic = new Intent(this, BasicInfoActivity.class);
                intentBasic.putExtra(AppConstant.EXTRA_USER_ID, mFriendId);
                startActivity(intentBasic);
                break;
            case R.id.add_contacts:
                Intent intentAdd = new Intent(this, SelectContactsActivity.class);
                intentAdd.putExtra("QuicklyCreateGroup", true);
                intentAdd.putExtra("ChatObjectId", mFriendId);
                intentAdd.putExtra("ChatObjectName", mFriendName);
                startActivity(intentAdd);
                break;
            case R.id.chat_history_search:
                Intent intentChat = new Intent(this, SearchChatHistoryActivity.class);
                intentChat.putExtra("isSearchSingle", true);
                intentChat.putExtra(AppConstant.EXTRA_USER_ID, mFriendId);
                startActivity(intentChat);
                break;
            case R.id.remark_rl:
                SetRemarkActivity.start(this, mFriendId);
                break;
            case R.id.label_rl:
                Intent intentLabel = new Intent(this, SetLabelActivity.class);
                intentLabel.putExtra(AppConstant.EXTRA_USER_ID, mFriendId);
                startActivity(intentLabel);
                break;
            case R.id.msg_save_days_rl:
                MsgSaveDaysDialog msgSaveDaysDialog = new MsgSaveDaysDialog(this, onMsgSaveDaysDialogClickListener);
                msgSaveDaysDialog.show();
                break;
            case R.id.set_background_rl:
                Intent intentBackground = new Intent(this, SetChatBackActivity.class);
                intentBackground.putExtra(AppConstant.EXTRA_USER_ID, mFriendId);
                startActivity(intentBackground);
                break;
            case R.id.chat_history_empty:
                clean(false);
                break;
            case R.id.sync_chat_history_empty:
                clean(true);
                break;
            case R.id.rl_transfer:
                Intent intentTransfer = new Intent(this, TransferRecordActivity.class);
                intentTransfer.putExtra(Constant.TRANSFE_RRECORD, mFriendId);
                startActivity(intentTransfer);
                break;
        }
    }

    private void clean(boolean isSync) {
        String tittle = isSync ? getString(R.string.sync_chat_history_clean) : getString(R.string.clean_chat_history);
        String tip = isSync ? getString(R.string.tip_sync_chat_history_clean) : getString(R.string.tip_confirm_clean_history);

        SelectionFrame selectionFrame = new SelectionFrame(mContext);
        selectionFrame.setSomething(tittle, tip, new SelectionFrame.OnSelectionFrameClickListener() {
            @Override
            public void cancelClick() {

            }

            @Override
            public void confirmClick() {
                if (isSync) {
                    // 发送一条双向清除的消息给对方，对方收到消息后也将本地消息删除
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setFromUserId(mLoginUserId);
                    chatMessage.setFromUserName(coreManager.getSelf().getNickName());
                    chatMessage.setToUserId(mFriendId);
                    chatMessage.setType(Constants.TYPE_SYNC_CLEAN_CHAT_HISTORY);
                    chatMessage.setPacketId(ToolUtils.getUUID());
                    chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
                    ImHelper.sendChatMessage(mFriendId, chatMessage);
                    ImHelper.syncFriendOtherInfoToOtherMachine(mFriendId);
                }
                emptyServerMessage();

                FriendDao.getInstance().resetFriendMessage(mLoginUserId, mFriendId);
                ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, mFriendId);
                sendBroadcast(new Intent(Constants.CHAT_HISTORY_EMPTY));// 清空聊天界面
                MsgBroadcast.broadcastMsgUiUpdate(mContext);
                Toast.makeText(PersonSettingActivity.this, getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
            }
        });
        selectionFrame.show();
    }

    // 更新消息免打扰状态
    private void updateDisturbStatus(final boolean isChecked) {
        final String offlineNoPushMsg = isChecked ? "1" : "0";
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", mLoginUserId);
        params.put("toUserId", mFriendId);
        params.put("offlineNoPushMsg", offlineNoPushMsg);
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().FRIENDS_NOPULL_MSG)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            FriendDao.getInstance().updateOfflineNoPushMsgStatus(mFriendId, isChecked ? 1 : 0);
                            ImHelper.syncFriendOtherInfoToOtherMachine(mFriendId);
                        } else {
                            Toast.makeText(PersonSettingActivity.this, R.string.tip_edit_failed, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(PersonSettingActivity.this);
                    }
                });
    }

    // 更新消息保存天数
    private void updateChatRecordTimeOut(final double outTime) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("toUserId", mFriendId);
        params.put("chatRecordTimeOut", String.valueOf(outTime));

        HttpUtils.get().url(coreManager.getConfig().FRIENDS_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            Toast.makeText(PersonSettingActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT).show();
                            mMsgSaveDays.setText(conversion(outTime));
                            FriendDao.getInstance().updateChatRecordTimeOut(mFriendId, outTime);
                            sendBroadcast(new Intent(com.iimm.miliao.broadcast.OtherBroadcast.NAME_CHANGE));// 刷新聊天界面
                            ImHelper.syncFriendOtherInfoToOtherMachine(mFriendId);
                        } else {
                            Toast.makeText(PersonSettingActivity.this, result.getResultMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    // 服务器上与该人的聊天记录也需要删除
    private void emptyServerMessage() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("type", String.valueOf(0));// 0 清空单人 1 清空所有
        params.put("toUserId", mFriendId);

        HttpUtils.get().url(coreManager.getConfig().EMPTY_SERVER_MESSAGE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    private String conversion(double outTime) {
        String outTimeStr;
        if (outTime == -1 || outTime == 0) {
            outTimeStr = getString(R.string.permanent);
        } else if (outTime == -2) {
            outTimeStr = getString(R.string.no_sync);
        } else if (outTime == 0.04) {
            outTimeStr = getString(R.string.one_hour);
        } else if (outTime == 1) {
            outTimeStr = getString(R.string.one_day);
        } else if (outTime == 7) {
            outTimeStr = getString(R.string.one_week);
        } else if (outTime == 30) {
            outTimeStr = getString(R.string.one_month);
        } else if (outTime == 90) {
            outTimeStr = getString(R.string.one_season);
        } else {
            outTimeStr = getString(R.string.one_year);
        }
        return outTimeStr;
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(com.iimm.miliao.broadcast.OtherBroadcast.QC_FINISH);
        registerReceiver(receiver, intentFilter);
    }

    public class RefreshBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(com.iimm.miliao.broadcast.OtherBroadcast.QC_FINISH)) {
                // 快速创建群组 || 更换聊天背景 成功，接收到该广播结束当前界面
                finish();
            }
        }
    }
}
