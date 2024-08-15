package com.iimm.miliao.ui.message;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.adapter.FriendWithSelectAdapter;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.message.MucRoom;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.RoomMemberDao;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.sortlist.BaseSortModel;
import com.iimm.miliao.sortlist.SideBar;
import com.iimm.miliao.sortlist.SortHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * 转发 选择 群组
 */
public class SelectNewGroupInstantActivity extends BaseActivity {
    private PullToRefreshListView mPullToRefreshListView;
    private FriendWithSelectAdapter mAdapter;
    private TextView mTextDialog;
    private SideBar mSideBar;
    private List<BaseSortModel<Friend>> mSortFriends;
    private String mLoginUserId;
    private boolean isMoreSelected;// 是否为多选转发
    private boolean isSingleOrMerge;// 逐条还是合并转发
    private String toUserId;
    private String messageId;

    private InstantMessageConfirm menuWindow;
    private String[] mGroupIds;
    private Map<String, String> mGroupIdsMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newchat_person_selected);
        isMoreSelected = getIntent().getBooleanExtra(Constants.IS_MORE_SELECTED_INSTANT, false);
        isSingleOrMerge = getIntent().getBooleanExtra(Constants.IS_SINGLE_OR_MERGE, false);
        // 在ChatContentView内长按转发才需要以下参数
        toUserId = getIntent().getStringExtra("fromUserId");
        messageId = getIntent().getStringExtra("messageId");
        mGroupIds = getIntent().getStringArrayExtra("groupIds");
        mGroupIdsMap = new HashMap<>();
        if (mGroupIds != null) {
            for (int i = 0; i < mGroupIds.length; i++) {
                mGroupIdsMap.put(mGroupIds[i], mGroupIds[i]);
            }
        }
        Log.i(TAG, JSON.toJSONString(mGroupIds));

        mSortFriends = new ArrayList<BaseSortModel<Friend>>();
        mLoginUserId = coreManager.getSelf().getUserId();

        initActionBar();
        initView();
        loadData();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.select_group_chat_instant));
        TextView tvTitleRight = findViewById(R.id.tv_title_right);
        tvTitleRight.setVisibility(View.VISIBLE);
        tvTitleRight.setText(R.string.confirm);
        tvTitleRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                List<BaseSortModel<Friend>> baseSortModels = new ArrayList<>();
                for (BaseSortModel<Friend> friendBaseSortModel : mSortFriends) {
                    if (friendBaseSortModel.bean.isSelect()) {
                        baseSortModels.add(friendBaseSortModel);
                    }
                }
                String[] s = new String[baseSortModels.size()];
                for (int i = 0; i < s.length; i++) {
                    s[i] = baseSortModels.get(i).bean.getUserId();
                }
                Intent intent = new Intent();
                intent.putExtra("groupIds", s);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    private void initView() {
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
        mAdapter = new FriendWithSelectAdapter(SelectNewGroupInstantActivity.this, mSortFriends);
        mPullToRefreshListView.setMode(Mode.PULL_FROM_START);
        mPullToRefreshListView.getRefreshableView().setAdapter(mAdapter);
        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                loadData();
            }
        });

        mPullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean isSelect = !mSortFriends.get((int) id).getBean().isSelect();
                Friend bean = mSortFriends.get((int) id).getBean();
                if (isSelect) {
                    String userId = bean.getUserId();
                    mGroupIdsMap.put(userId, userId);
                } else {
                    String userId = bean.getUserId();
                    mGroupIdsMap.remove(userId);
                }
                bean.setSelect(isSelect);
                mAdapter.notifyDataSetChanged();

//                Friend friend = mSortFriends.get((int) id).getBean();
//                showPopuWindow(view, friend);
            }
        });

        mTextDialog = (TextView) findViewById(R.id.text_dialog);
        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mSideBar.setTextView(mTextDialog);

        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = mAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mPullToRefreshListView.getRefreshableView().setSelection(position);
                }
            }
        });
    }

    private void showPopuWindow(View view, Friend friend) {
        menuWindow = new InstantMessageConfirm(SelectNewGroupInstantActivity.this, new ClickListener(friend), friend);
        // 显示窗口
        menuWindow.showAtLocation(view, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void loadData() {
        AsyncUtils.doAsync(this, e -> {
            Reporter.post("加载数据失败，", e);
            AsyncUtils.runOnUiThread(this, ctx -> {
                ToastUtil.showToast(ctx, R.string.data_exception);
            });
        }, c -> {
            long startTime = System.currentTimeMillis();
            final List<Friend> friends = FriendDao.getInstance().getAllRooms(mLoginUserId);
            Map<String, Integer> existMap = new HashMap<>();
            List<BaseSortModel<Friend>> sortedList = SortHelper.toSortedModelList(friends, existMap, Friend::getShowName);
            if (mGroupIds != null) {
                for (int i = 0; i < sortedList.size(); i++) {
                    if (mGroupIdsMap.containsKey(sortedList.get(i).getBean().getUserId())) {
                        sortedList.get(i).getBean().setSelect(true);
                    }
                }
            }
            long delayTime = 200 - (startTime - System.currentTimeMillis());// 保证至少200ms的刷新过程
            if (delayTime < 0) {
                delayTime = 0;
            }
            c.postDelayed(r -> {
                mSideBar.setExistMap(existMap);
                mSortFriends = sortedList;
                mAdapter.setData(sortedList);
                mPullToRefreshListView.onRefreshComplete();
            }, delayTime);
        });
    }

    private void forwardingStep(Friend friend) {
        if (isMoreSelected) {// 多选转发 通知多选页面(即多选消息的单聊 || 群聊页面，在该页面获取选中的消息在发送出去)
            EventBus.getDefault().post(new EventMoreSelected(friend.getUserId(), isSingleOrMerge, true));
            finish();
        } else {
            Intent intent = new Intent(SelectNewGroupInstantActivity.this, MucChatActivity.class);
            intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
            intent.putExtra(AppConstant.EXTRA_NICK_NAME, friend.getNickName());
            intent.putExtra(AppConstant.EXTRA_IS_GROUP_CHAT, true);
            intent.putExtra("fromUserId", toUserId);
            intent.putExtra("messageId", messageId);
            startActivity(intent);
            finish();
        }
    }

    /**
     * 获取自己在该群组的信息(职位、昵称、禁言时间等)以及群属性
     */
    private void isSupportForwarded(final Friend friend) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", friend.getRoomId());

        HttpUtils.get().url(coreManager.getConfig().ROOM_GET_ROOM)
                .params(params)
                .build()
                .execute(new BaseCallback<MucRoom>(MucRoom.class) {

                             @Override
                             public void onResponse(ObjectResult<MucRoom> result) {// 数据结果与room/get接口一样，只是服务端没有返回群成员列表的数据
                                 if (result.getResultCode() == 1 && result.getData() != null) {
                                     final MucRoom mucRoom = result.getData();
                                     if (mucRoom.getMember() == null) {// 被踢出该群组
                                         FriendDao.getInstance().updateFriendGroupStatus(mLoginUserId, mucRoom.getJid(), 1);// 更新本地群组状态
                                         DialogHelper.tip(SelectNewGroupInstantActivity.this, getString(R.string.tip_forward_kick));
                                     } else {// 正常状态
                                         if (mucRoom.getS() == -1) {// 该群组已被锁定
                                             FriendDao.getInstance().updateFriendGroupStatus(mLoginUserId, mucRoom.getJid(), 3);// 更新本地群组状态
                                             DialogHelper.tip(SelectNewGroupInstantActivity.this, getString(R.string.tip_group_disable_by_service));
                                             return;
                                         }
                                         int role = mucRoom.getMember().getRole();
                                         // 更新禁言状态
                                         FriendDao.getInstance().updateRoomTalkTime(mLoginUserId, mucRoom.getJid(), mucRoom.getMember().getTalkTime());

                                         // 更新部分群属性
                                         MyApplication.getInstance().saveGroupPartStatus(mucRoom.getJid(), mucRoom.getShowRead(),
                                                 mucRoom.getAllowSendCard(), mucRoom.getAllowConference(),
                                                 mucRoom.getAllowSpeakCourse(), mucRoom.getTalkTime());

                                         // 更新个人职位
                                         RoomMemberDao.getInstance().updateRoomMemberRole(mucRoom.getId(), mLoginUserId, role);

                                         if (role == 1 || role == 2) {// 群组或管理员 直接转发出去
                                             forwardingStep(friend);
                                         } else {
                                             if (mucRoom.getTalkTime() > 0) {// 全体禁言
                                                 DialogHelper.tip(SelectNewGroupInstantActivity.this, getString(R.string.tip_now_ban_all));
                                             } else if (mucRoom.getMember().getTalkTime() > System.currentTimeMillis() / 1000) {// 禁言
                                                 DialogHelper.tip(SelectNewGroupInstantActivity.this, getString(R.string.tip_forward_ban));
                                             } else {
                                                 forwardingStep(friend);
                                             }
                                         }
                                     }
                                 } else {// 群组已解散
                                     FriendDao.getInstance().updateFriendGroupStatus(mLoginUserId, friend.getUserId(), 2);// 更新本地群组状态
                                     DialogHelper.tip(SelectNewGroupInstantActivity.this, getString(R.string.tip_forward_disbanded));
                                 }
                             }

                             @Override
                             public void onError(Call call, Exception e) {
                                 ToastUtil.showNetError(mContext);
                             }
                         }
                );
    }

    /**
     * 事件的监听
     */
    class ClickListener implements OnClickListener {
        private Friend friend;

        public ClickListener(Friend friend) {
            this.friend = friend;
        }

        @Override
        public void onClick(View v) {
            menuWindow.dismiss();
            switch (v.getId()) {
                case R.id.btn_send:// 发送
                    isSupportForwarded(friend);
                    break;
                case R.id.btn_cancle:// 取消
                    break;
                default:
                    break;
            }
        }
    }
}
