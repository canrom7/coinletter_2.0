package com.iimm.miliao.ui.message;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.adapter.FriendWithSelectAdapter;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.sortlist.BaseComparator;
import com.iimm.miliao.sortlist.BaseSortModel;
import com.iimm.miliao.sortlist.SideBar;
import com.iimm.miliao.sortlist.SortHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * 转发 选择 好友
 */
public class SelectNewContactsActivity extends BaseActivity implements OnClickListener {
    private static final int REQUEST_CODE_SELECT_NEW_GROUP = 1002;
    private PullToRefreshListView mPullToRefreshListView;
    private FriendWithSelectAdapter mAdapter;
    private TextView mTextDialog;
    private SideBar mSideBar;
    private List<BaseSortModel<Friend>> mSortFriends;
    private BaseComparator<Friend> mBaseComparator;
    private String mLoginUserId;

    private Handler mHandler = new Handler();

    private boolean isMoreSelected;// 是否为多选转发
    private boolean isSingleOrMerge;// 逐条还是合并转发
    private String toUserId;
    private String messageId;

    private InstantMessageConfirm menuWindow;
    private String[] GroupFriendIds = new String[]{};
    private Map<String, String> mFriendIdsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newchat_person_selected);
        isMoreSelected = getIntent().getBooleanExtra(Constants.IS_MORE_SELECTED_INSTANT, false);
        isSingleOrMerge = getIntent().getBooleanExtra(Constants.IS_SINGLE_OR_MERGE, false);
        // 在ChatContentView内长按转发才需要以下参数
        toUserId = getIntent().getStringExtra("fromUserId");
        messageId = getIntent().getStringExtra("messageId");
        String[] friendIds = getIntent().getStringArrayExtra("friendIds");
        String[] groupIds = getIntent().getStringArrayExtra("groupIds");
        if (friendIds != null) {
            mFriendIdsMap = new HashMap<>();
            for (int i = 0; i < friendIds.length; i++) {
                mFriendIdsMap.put(friendIds[i], friendIds[i]);
            }
            Log.i(TAG, "已经选择的：" + JSON.toJSONString(mFriendIdsMap));
        }
        if (groupIds != null) {
            this.GroupFriendIds = groupIds;
        }
        mSortFriends = new ArrayList<BaseSortModel<Friend>>();
        mBaseComparator = new BaseComparator<Friend>();
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
        tvTitle.setText(InternationalizationHelper.getString("SELECT_CONTANTS"));
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
                intent.putExtra("friendIds", s);
                intent.putExtra("groupIds", GroupFriendIds);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    private void initView() {
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
        View headView = View.inflate(this, R.layout.item_headview_creategroup_chat, null);
        mPullToRefreshListView.getRefreshableView().addHeaderView(headView);
        headView.setOnClickListener(this);
        mPullToRefreshListView.setMode(Mode.PULL_FROM_START);
        mAdapter = new FriendWithSelectAdapter(this, mSortFriends);
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
                    mFriendIdsMap.put(userId, userId);
                } else {
                    String userId = bean.getUserId();
                    mFriendIdsMap.remove(userId);
                }
                mSortFriends.get((int) id).getBean().setSelect(!mSortFriends.get((int) id).getBean().isSelect());
                mAdapter.notifyDataSetChanged();

//                Friend friend = mSortFriends.get((int) id).getBean().getFriend();
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
        menuWindow = new InstantMessageConfirm(SelectNewContactsActivity.this, new ClickListener(friend),
                friend);
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
            final List<Friend> friends = FriendDao.getInstance().getAllFriends(mLoginUserId);
            Map<String, Integer> existMap = new HashMap<>();
            List<BaseSortModel<Friend>> sortedList = SortHelper.toSortedModelList(friends, existMap, Friend::getShowName);
            if (mFriendIdsMap != null) {
                for (int i = 0; i < sortedList.size(); i++) {
                    if (mFriendIdsMap.containsKey(sortedList.get(i).getBean().getUserId())) {
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_headview_instant_group:
                Intent intent = new Intent(SelectNewContactsActivity.this, SelectNewGroupInstantActivity.class);
                intent.putExtra(Constants.IS_MORE_SELECTED_INSTANT, isMoreSelected);
                intent.putExtra(Constants.IS_SINGLE_OR_MERGE, isSingleOrMerge);
                intent.putExtra("groupIds", GroupFriendIds);
                intent.putExtra("fromUserId", toUserId);
                intent.putExtra("messageId", messageId);
                startActivityForResult(intent, REQUEST_CODE_SELECT_NEW_GROUP);
                break;
            default:
                break;
        }
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
                case R.id.btn_send:
                    if (isMoreSelected) {// 多选转发 通知多选页面(即多选消息的单聊 || 群聊页面，在该页面获取选中的消息在发送出去)
                        EventBus.getDefault().post(new EventMoreSelected(friend.getUserId(), isSingleOrMerge, false));
                        finish();
                    } else {
                        Intent intent = new Intent(SelectNewContactsActivity.this, ChatActivity.class);
                        intent.putExtra(ChatActivity.FRIEND, friend);
                        intent.putExtra("fromUserId", toUserId);
                        intent.putExtra("messageId", messageId);
                        startActivity(intent);
                        finish();
                    }
                    break;
                case R.id.btn_cancle:// 取消
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SELECT_NEW_GROUP:
                    if (data != null) {
                        String[] friendIds = data.getStringArrayExtra("groupIds");
                        this.GroupFriendIds = friendIds;
                        Log.i(TAG, JSON.toJSONString(GroupFriendIds));

                    }
                    break;
            }
        }
    }
}
