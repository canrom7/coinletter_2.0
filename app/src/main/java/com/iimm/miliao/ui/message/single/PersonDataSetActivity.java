package com.iimm.miliao.ui.message.single;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.Label;
import com.iimm.miliao.bean.Report;
import com.iimm.miliao.bean.User;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.bean.message.NewFriendMessage;
import com.iimm.miliao.broadcast.CardcastUiUpdateUtil;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.LabelDao;
import com.iimm.miliao.db.dao.NewFriendDao;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.helper.FriendHelper;
import com.iimm.miliao.ui.MainActivity;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.UiUtils;
import com.iimm.miliao.view.ReportDialog;
import com.iimm.miliao.view.SelectionFrame;
import com.iimm.miliao.xmpp.ListenerManager;
import com.iimm.miliao.xmpp.listener.NewFriendListener;
import com.iimm.miliao.xmpp.util.ImHelper;
import com.suke.widget.SwitchButton;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by  on 2019/6/27.
 * 资料设置(从用户资料跳转过来)
 */

public class PersonDataSetActivity extends BaseActivity implements View.OnClickListener, NewFriendListener {

    private RelativeLayout mLabels;
    private RelativeLayout mBlacklist;
    private RelativeLayout mReport;
    private boolean isMyInfo = false;
    private User mUser;
    private String mUserId;
    private String mLoginUserId;
    private Friend mFriend;
    private static final int REQUEST_CODE_SET_REMARK = 075;
    private TextView tv_lable_basic;
    private TextView tv_setting_name;
    private TextView mBlacktv;
    private ConstraintLayout cardView;
    private View add_blacklist_relat_v;
    private SwitchButton mSwitchButton;
    private String deletehaoyou = null;
    private String addblackid = null;
    private String removeblack = null;
    private boolean mIsBlacklist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_set);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());

        initView();

        if (getIntent() != null) {
            mUser = getIntent().getParcelableExtra("user");
            mUserId = getIntent().getStringExtra("userId");
        }

        mLoginUserId = coreManager.getSelf().getUserId();
        if (TextUtils.isEmpty(mUserId)) {
            mUserId = mLoginUserId;
        }
        mFriend = FriendDao.getInstance().getFriend(mLoginUserId, mUserId);

        if (mLoginUserId.equals(mUserId)) { // 显示自己资料
            isMyInfo = true;
            loadMyInfoFromDb();
        } else { // 显示其他用户的资料
            isMyInfo = false;
            loadOthersInfoFromNet();
        }

        ListenerManager.getInstance().addNewFriendListener(this);
    }

    private void initView() {
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText("资料设置");
        mLabels = findViewById(R.id.set_notes_labels_relat);

        mBlacklist = findViewById(R.id.add_blacklist_relat);
        tv_lable_basic = findViewById(R.id.set_notes_labels_relat_conent);
        tv_setting_name = findViewById(R.id.set_notes_labels_relat_tv);
        mReport = findViewById(R.id.report_relat);
        cardView = findViewById(R.id.delete_cd);
        add_blacklist_relat_v = findViewById(R.id.add_blacklist_relat_v);
        mSwitchButton = findViewById(R.id.isAdd_blacklist);
        mBlacktv = findViewById(R.id.blacklist_tv);
        ((TextView) findViewById(R.id.set_notes_labels_relat_tv)).setText(InternationalizationHelper.getString("JXUserInfoVC_SetName"));
//        ((TextView) findViewById(R.id.delete_tv)).setText(InternationalizationHelper.getString("JXUserInfoVC_DeleteFirend"));


        mSwitchButton.setOnCheckedChangeListener(onCheckedChangeListener);
        mLabels.setOnClickListener(this);
        mReport.setOnClickListener(this);
        cardView.setOnClickListener(this);
        findViewById(R.id.tv_view).setOnClickListener(this);
    }

    SwitchButton.OnCheckedChangeListener onCheckedChangeListener = new SwitchButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(SwitchButton view, boolean isChecked) {
            switch (view.getId()) {
                case R.id.isAdd_blacklist://加入黑名单和删除黑名单

//                    if (isChecked) {
//                        //加入黑名单
//                        showBlacklistDialog(mFriend);
//                    } else {
//                        removeBlacklist(mFriend);
//                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (!UiUtils.isNormalClick(v)) {
            return;
        }
        switch (v.getId()) {
            case R.id.set_notes_labels_relat://设置备注和标签
                String name = "";
                String desc = "";
                if (mUser != null && mUser.getFriends() != null) {
                    name = mUser.getFriends().getRemarkName();
                    desc = mUser.getFriends().getDescribe();
                }
                SetRemarkActivity.startForResult(PersonDataSetActivity.this, mUserId, name, desc, REQUEST_CODE_SET_REMARK);
                break;
            case R.id.report_relat://举报
                ReportDialog mReportDialog = new ReportDialog(PersonDataSetActivity.this, false, new ReportDialog.OnReportListItemClickListener() {
                    @Override
                    public void onReportItemClick(Report report) {
                        report(mFriend, report);
                    }
                });
                mReportDialog.show();
                break;
            case R.id.delete_cd://删除
                showDeleteAllDialog(mFriend);
                break;
            case R.id.tv_view:
                if (mIsBlacklist) {
                    removeBlacklist(mFriend);
                } else {
                    //加入黑名单
                    showBlacklistDialog(mFriend);
                }
                break;
            default:
                break;

        }
    }

    // 加入、移除黑名单
    private void showBlacklistDialog(final Friend friend) {
/*
        if (friend.getStatus() == Constants.STATUS_BLACKLIST) {
            removeBlacklist(friend);
        } else if (friend.getStatus() == Constants.STATUS_ATTENTION || friend.getStatus() == Constants.STATUS_FRIEND) {
            addBlacklist(friend);
        }
*/
        SelectionFrame mSF = new SelectionFrame(this);
        mSF.setSomething(getString(R.string.add_black_list), getString(R.string.sure_add_friend_blacklist), new SelectionFrame.OnSelectionFrameClickListener() {
            @Override
            public void cancelClick() {

            }

            @Override
            public void confirmClick() {
                addBlacklist(friend);
            }
        });
        mSF.show();
    }

    //移除黑名单
    private void removeBlacklist(final Friend friend) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("toUserId", mUser.getUserId());
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().FRIENDS_BLACKLIST_DELETE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            NewFriendMessage message = NewFriendMessage.createWillSendMessage(
                                    coreManager.getSelf(), Constants.TYPE_REFUSED, null, friend);
                            ImHelper.sendNewFriendMessage(friend.getUserId(), message);// 移除黑名单

                            // 记录移除黑名单消息packet，如收到消息回执，在做后续操作
                            removeblack = message.getPacketId();
                            mSwitchButton.setChecked(false);
                            mIsBlacklist = false;
                        } else if (!TextUtils.isEmpty(result.getResultMsg())) {
                            ToastUtil.showToast(mContext, result.getResultMsg());
                        } else {
                            ToastUtil.showToast(mContext, R.string.tip_server_error);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(mContext, R.string.tip_remove_black_failed);
                    }
                });
    }

    private void addBlacklist(final Friend friend) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("toUserId", friend.getUserId());
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().FRIENDS_BLACKLIST_ADD)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            if (friend.getStatus() == Constants.STATUS_FRIEND) {
                                NewFriendMessage message = NewFriendMessage.createWillSendMessage(
                                        coreManager.getSelf(), Constants.TYPE_BLACK, null, friend);
                                ImHelper.sendNewFriendMessage(friend.getUserId(), message);// 加入黑名单

                                // 记录加入黑名单消息packet，如收到消息回执，在做后续操作
                                addblackid = message.getPacketId();
                                mSwitchButton.setChecked(true);
                                mIsBlacklist = true;
                            }
                        } else if (!TextUtils.isEmpty(result.getResultMsg())) {
                            ToastUtil.showToast(mContext, result.getResultMsg());
                        } else {
                            ToastUtil.showToast(mContext, R.string.tip_server_error);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(mContext, getString(R.string.add_blacklist_fail));
                    }
                });
    }

    /**
     * 举报
     *
     * @param friend
     * @param report
     */
    private void report(Friend friend, Report report) {
        if (friend == null) {
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("toUserId", friend.getUserId());
        params.put("reason", String.valueOf(report.getReportId()));
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().USER_REPORT)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            ToastUtil.showToast(PersonDataSetActivity.this, R.string.report_success);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                    }
                });
    }

    //彻底删除
    private void showDeleteAllDialog(final Friend friend) {
        if (friend.getStatus() == Constants.STATUS_UNKNOW) {// 陌生人
            return;
        }
        SelectionFrame mSF = new SelectionFrame(this);
        mSF.setSomething(getString(R.string.delete_friend), getString(R.string.sure_delete_friend), new SelectionFrame.OnSelectionFrameClickListener() {
            @Override
            public void cancelClick() {

            }

            @Override
            public void confirmClick() {
                deleteFriend(friend, 1);
            }
        });
        mSF.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_SET_REMARK) {
            setResult(RESULT_OK);
            loadOthersInfoFromNet();
        }
    }

    // 加载自己的信息
    private void loadMyInfoFromDb() {
        mUser = coreManager.getSelf();
        updateUI();
    }

    // 加载好友的信息
    private void loadOthersInfoFromNet() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", mUserId);

        HttpUtils.get().url(coreManager.getConfig().USER_GET_URL)
                .params(params)
                .build()
                .execute(new BaseCallback<User>(User.class) {

                    @Override
                    public void onResponse(ObjectResult<User> result) {
                        if (Result.checkSuccess(PersonDataSetActivity.this, result)) {
                            mUser = result.getData();
                            if (mUser.getUserType() != 2) {// 公众号不做该操作 否则会将公众号的status变为好友
                                // 服务器的状态 与本地状态对比
                                if (FriendHelper.updateFriendRelationship(mLoginUserId, mUser)) {
                                    CardcastUiUpdateUtil.broadcastUpdateUi(mContext);
                                }
                            }
                            updateUI();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(mContext);
                    }
                });
    }

    private void updateUI() {
        if (mUser == null) {
            return;
        }
        if (isFinishing()) {
            return;
        }

        if (mFriend != null) {// 该人为我的好友，先赋值，接口成功后在更新ui
            List<Label> friendLabelList = LabelDao.getInstance().getFriendLabelList(mLoginUserId, mUserId);
            String labelNames = "";
            if (friendLabelList != null && friendLabelList.size() > 0) {
                for (int i = 0; i < friendLabelList.size(); i++) {
                    if (i == friendLabelList.size() - 1) {
                        labelNames += friendLabelList.get(i).getGroupName();
                    } else {
                        labelNames += friendLabelList.get(i).getGroupName() + "，";
                    }
                }
                tv_lable_basic.setText(labelNames);  //标签有则显示
                tv_setting_name.setText(getResources().getString(R.string.tag));
            } else {
                // 没有则判断描述 描述也为空则显示标签一栏
                if (TextUtils.isEmpty(mFriend.getDescribe())) {
                    tv_setting_name.setText(getResources().getString(R.string.setting_nickname));
                    tv_lable_basic.setText("");
                } else {
                    // 否则不出现这一栏
                    findViewById(R.id.set_notes_labels_relat).setVisibility(View.GONE);
                }
            }
        }


        if (mUser.getFriends() != null) {
            if (!TextUtils.isEmpty(mUser.getFriends().getRemarkName())) {
                tv_setting_name.setText(getString(R.string.tag));// 有备注 || 标签 显示“标签”
            }
            if (mFriend != null) {
                FriendDao.getInstance().updateFriendPartStatus(mFriend.getUserId(), mUser);

                if (!TextUtils.equals(mFriend.getRemarkName(), mUser.getFriends().getRemarkName())
                        || !TextUtils.equals(mFriend.getDescribe(), mUser.getFriends().getDescribe())) {
                    // 本地备注或者描述与服务器数据不匹配时更新数据库，
                    // mUser是接口数据，
                    mFriend.setRemarkName(mUser.getFriends().getRemarkName());
                    mFriend.setDescribe(mUser.getFriends().getDescribe());
                    FriendDao.getInstance().updateRemarkNameAndDescribe(coreManager.getSelf().getUserId(),
                            mUserId, mUser.getFriends().getRemarkName(),
                            mUser.getFriends().getDescribe());
                    // 刷新消息、通讯录、聊天页面
                    MsgBroadcast.broadcastMsgUiUpdate(mContext);
                    CardcastUiUpdateUtil.broadcastUpdateUi(mContext);
                    sendBroadcast(new Intent(com.iimm.miliao.broadcast.OtherBroadcast.NAME_CHANGE));
                }
            }
        }

        List<Label> friendLabelList = LabelDao.getInstance().getFriendLabelList(mLoginUserId, mUserId);
        String labelNames = "";
        if (friendLabelList != null && friendLabelList.size() > 0) {
            for (int i = 0; i < friendLabelList.size(); i++) {
                if (i == friendLabelList.size() - 1) {
                    labelNames += friendLabelList.get(i).getGroupName();
                } else {
                    labelNames += friendLabelList.get(i).getGroupName() + "，";
                }
            }
            tv_setting_name.setText(getString(R.string.tag));// 有备注 || 标签 显示“标签”
            tv_lable_basic.setText(labelNames);
        }

        if (isMyInfo) {
            findViewById(R.id.set_notes_labels_relat).setVisibility(View.GONE);
        } else {
            if (mUser.getFriends() == null) {// 陌生人
                mBlacklist.setVisibility(View.GONE);
                cardView.setVisibility(View.GONE);
                add_blacklist_relat_v.setVisibility(View.GONE);
            } else if (mUser.getFriends().getBlacklist() == 1) {  //  需显示移除黑名单
                mBlacktv.setText("移除黑名单");
                mSwitchButton.setChecked(true);
                mIsBlacklist = true;
            } else if (mUser.getFriends().getIsBeenBlack() == 1) {//  需显示加入黑名单
                mSwitchButton.setChecked(false);
                mIsBlacklist = false;
                mBlacktv.setText("加入黑名单");
            }
        }
    }

    private void deleteFriend(final Friend friend, final int type) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("toUserId", friend.getUserId());
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().FRIENDS_DELETE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            NewFriendMessage message = NewFriendMessage.createWillSendMessage(
                                    coreManager.getSelf(), Constants.TYPE_DELALL, null, friend);
                            ImHelper.sendNewFriendMessage(mUser.getUserId(), message); // 删除好友
                            ImHelper.syncMyFriendToOtherMachine();
                            // 记录删除好友消息packet，如收到消息回执，在做后续操作
                            deletehaoyou = message.getPacketId();

                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(mContext, R.string.tip_remove_friend_failed);
                    }
                });
    }


    @Override
    public void onNewFriendSendStateChange(String toUserId, NewFriendMessage message, int messageState) {
        if (messageState == Constants.MESSAGE_SEND_SUCCESS) {
            msgSendSuccess(message, message.getPacketId());
        } else if (messageState == Constants.MESSAGE_SEND_FAILED) {
            msgSendFailed(message.getPacketId());
        }
    }

    @Override
    public boolean onNewFriend(NewFriendMessage message) {
        return false;
    }

    // xmpp消息发送成功最终回调到这，
    // 在这里调整ui,
    // 还有存本地数据库，
    public void msgSendSuccess(NewFriendMessage message, String packet) {
        if (addblackid != null && addblackid.equals(packet)) {
            ToastUtil.showToast(getApplicationContext(), getString(R.string.add_blacklist_succ));
            // 更新当前持有的Friend对象，
            mFriend.setStatus(Constants.STATUS_BLACKLIST);
            FriendDao.getInstance().updateFriendStatus(message.getOwnerId(), message.getUserId(), mFriend.getStatus());
            FriendHelper.addBlacklistExtraOperation(message.getOwnerId(), message.getUserId());

            ChatMessage addBlackChatMessage = new ChatMessage();
            addBlackChatMessage.setContent(InternationalizationHelper.getString("JXFriendObject_AddedBlackList") + " " + mUser.getNickName());
            addBlackChatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
            FriendDao.getInstance().updateLastChatMessage(mLoginUserId, Constants.ID_NEW_FRIEND_MESSAGE, addBlackChatMessage);

            NewFriendDao.getInstance().createOrUpdateNewFriend(message);
            NewFriendDao.getInstance().changeNewFriendState(mUser.getUserId(), Constants.STATUS_18);
            ListenerManager.getInstance().notifyNewFriend(mLoginUserId, message, true);

            CardcastUiUpdateUtil.broadcastUpdateUi(mContext);

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (removeblack != null && removeblack.equals(packet)) {
            ToastUtil.showToast(getApplicationContext(), InternationalizationHelper.getString("REMOVE_BLACKLIST"));

            // 更新当前持有的Friend对象，
            if (mFriend != null) {
                mFriend.setStatus(Constants.STATUS_FRIEND);
            }
            mBlacktv.setText("加入黑名单");

            NewFriendDao.getInstance().ascensionNewFriend(message, Constants.STATUS_FRIEND);
            FriendHelper.beAddFriendExtraOperation(message.getOwnerId(), message.getUserId());

            ChatMessage removeChatMessage = new ChatMessage();
            removeChatMessage.setContent(coreManager.getSelf().getNickName() + InternationalizationHelper.getString("REMOVE"));
            removeChatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
            FriendDao.getInstance().updateLastChatMessage(mLoginUserId, Constants.ID_NEW_FRIEND_MESSAGE, removeChatMessage);

            NewFriendDao.getInstance().createOrUpdateNewFriend(message);
            NewFriendDao.getInstance().changeNewFriendState(message.getUserId(), Constants.STATUS_24);
            ListenerManager.getInstance().notifyNewFriend(mLoginUserId, message, true);

            CardcastUiUpdateUtil.broadcastUpdateUi(mContext);

            loadOthersInfoFromNet();
        } else if (deletehaoyou != null && deletehaoyou.equals(packet)) {
            ToastUtil.showToast(getApplicationContext(), InternationalizationHelper.getString("JXAlert_DeleteOK"));

            FriendHelper.removeAttentionOrFriend(mLoginUserId, message.getUserId());

            ChatMessage deleteChatMessage = new ChatMessage();
            deleteChatMessage.setContent(InternationalizationHelper.getString("JXAlert_DeleteFirend") + " " + mUser.getNickName());
            deleteChatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
            FriendDao.getInstance().updateLastChatMessage(mLoginUserId, Constants.ID_NEW_FRIEND_MESSAGE, deleteChatMessage);

            NewFriendDao.getInstance().createOrUpdateNewFriend(message);
            NewFriendDao.getInstance().changeNewFriendState(mUser.getUserId(), Constants.STATUS_16);
            ListenerManager.getInstance().notifyNewFriend(mLoginUserId, message, true);

            CardcastUiUpdateUtil.broadcastUpdateUi(mContext);

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void msgSendFailed(String packet) {
        DialogHelper.dismissProgressDialog();
        if (packet.equals(addblackid)) {
            ToastUtil.showToast(this, R.string.tip_put_black_failed);
        } else if (packet.equals(removeblack)) {
            ToastUtil.showToast(this, R.string.tip_remove_black_failed);
        } else if (packet.equals(deletehaoyou)) {
            ToastUtil.showToast(this, R.string.tip_remove_friend_failed);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ListenerManager.getInstance().removeNewFriendListener(this);
    }

}
