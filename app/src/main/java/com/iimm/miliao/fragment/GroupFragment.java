package com.iimm.miliao.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.adapter.GroupItemAdapter;
import com.iimm.miliao.bean.EventBusMsg;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.message.MucRoom;
import com.iimm.miliao.broadcast.CardcastUiUpdateUtil;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.broadcast.MucgroupUpdateUtil;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.OnCompleteUpdateListener;
import com.iimm.miliao.sortlist.BaseComparator;
import com.iimm.miliao.sortlist.BaseSortModel;
import com.iimm.miliao.sortlist.SideBar;
import com.iimm.miliao.sortlist.SortHelper;
import com.iimm.miliao.ui.message.MucChatActivity;
import com.iimm.miliao.util.AppExecutors;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.xmpp.util.ImHelper;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

public class GroupFragment extends GroupSearchFragment implements GroupItemAdapter.GroupItemClickListener {
    private String TAG = "GroupFragment";
    private List<BaseSortModel<Friend>> mSortFriends = new ArrayList<>();
    private BaseComparator<Friend> mBaseComparator;
    private String mLoginUserId;
    private TextView mTvDialog;
    private GroupItemAdapter mGroupItemAdapter;
    private SideBar mSideBar;
    private EditText mEditText;
    private TextView mTvCancel;
    private RecyclerView mRecyclerView;
    private Handler mHandler = new Handler();
    private SmartRefreshLayout mSmartRefreshLayout;
    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MucgroupUpdateUtil.ACTION_UPDATE)) {
                Log.i(TAG, "onReceive: xcccccccc");
                loadData();
            }
        }
    };

    public GroupFragment() {
        mBaseComparator = new BaseComparator<>();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
        mGroupItemAdapter.notifyDataSetChanged();
    }

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_group;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        super.onActivityCreated(savedInstanceState, createView);
        EventBus.getDefault().register(this);
        mLoginUserId = coreManager.getSelf().getUserId();
        if (createView) {
            initView();
        }
    }

    private void initView() {
        mRecyclerView = findViewById(R.id.recycler_group);
        mSmartRefreshLayout = findViewById(R.id.refresh_group_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        mGroupItemAdapter = new GroupItemAdapter(getActivity(), mSortFriends);
        mGroupItemAdapter.setGroupItemClickListener(this);
        mRecyclerView.setAdapter(mGroupItemAdapter);
        mTvDialog = findViewById(R.id.text_dialog);
        mSideBar = findViewById(R.id.sidebar);
        mRvSearchList = findViewById(R.id.rv_search_list);
        mRvSearchList.setVisibility(View.GONE);
        mEditText = findViewById(R.id.search_edit);
        mEditText.setHint(R.string.search_for_friends);
        mTvCancel = findViewById(R.id.tv_cancel);
        View view = findViewById(R.id.include2);
        view.setBackgroundColor(getResources().getColor(R.color.white));
        initSearchModule(mEditText, mTvCancel);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mInputManager.hideSoftInputFromWindow(mEditText.getApplicationWindowToken(), 0);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                updateRoom();
            }
        });
        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mTvDialog = (TextView) findViewById(R.id.text_dialog);
        mSideBar.setTextView(mTvDialog);
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = mGroupItemAdapter.getPositionForSection(s.charAt(0));
                mRecyclerView.scrollToPosition(position);
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CardcastUiUpdateUtil.ACTION_UPDATE_UI);
        getActivity().registerReceiver(mUpdateReceiver, intentFilter);
    }

    @Override
    protected void tvCancelOnClick() {
        mSideBar.setVisibility(View.VISIBLE);
    }


    @Override
    protected void loadData() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<Friend> friends = FriendDao.getInstance().getAllRooms(mLoginUserId);
                Map<String, Integer> existMap = new HashMap<>();
                List<BaseSortModel<Friend>> sortedList = SortHelper.toSortedModelList(friends, existMap, Friend::getShowName);
                MyApplication.applicationHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSideBar.setExistMap(existMap);
                        mSortFriends.clear();
                        mSortFriends.addAll(sortedList);
                        mGroupItemAdapter.notifyDataSetChanged();
                    }
                }, 300);
                mSmartRefreshLayout.finishRefresh();
            }
        });
    }

    @Override
    protected void searchEditFocusChange(boolean hasFocus) {
        //mSideBar.setVisibility(View.GONE);
    }


    /**
     * 下载我的群组
     */
    private void updateRoom() {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("type", "0");
        params.put("pageIndex", "0");
        params.put("pageSize", "20000");// 给一个尽量大的值

        HttpUtils.get().url(coreManager.getConfig().ROOM_LIST_HIS)
                .params(params)
                .build()
                .execute(new ListCallback<MucRoom>(MucRoom.class) {
                    @Override
                    public void onResponse(ArrayResult<MucRoom> result) {
                        if (result.getResultCode() == 1) {
                            FriendDao.getInstance().reset(mHandler, mLoginUserId, result.getData(),
                                    new OnCompleteUpdateListener() {
                                        @Override
                                        public void onLoading(int progressRate, int sum) {

                                        }

                                        @Override
                                        public void onCompleted() {// 下载完成
                                            go();
                                        }

                                        @Override
                                        public void update() {
                                            try {
                                                if (mGroupItemAdapter != null) {
                                                    mGroupItemAdapter.notifyDataSetChanged();
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(getActivity());
                    }
                });
    }

    private void go() {
        if (ImHelper.checkXmppAuthenticated()) {
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    // 1.调用smack内join方法加入群组
                    List<Friend> mFriends = FriendDao.getInstance().getAllRooms(mLoginUserId);
                    for (int i = 0; i < mFriends.size(); i++) {// 已加入的群组不会重复加入，方法内已去重
                        ImHelper.joinMucChat(mFriends.get(i).getUserId(),
                                mFriends.get(i).getTimeSend());
                    }
                    // 2.更新我的群组列表
                    loadData();
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void processEventBusMsg(EventBusMsg eventBusMsg) {
        if (eventBusMsg.getMessageType() == Constants.EVENTBUS_ROOM_MEMBER_DOWNLOAD_REFRESH_UI) {
            MyApplication.applicationHandler.post(new Runnable() {
                @Override
                public void run() {
                    loadData();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mUpdateReceiver);
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onItemClick(int position, RecyclerView.ViewHolder viewHolder) {
        Friend friend = mSortFriends.get(position).getBean();
        Intent intent = new Intent(getActivity(), MucChatActivity.class);
        intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
        intent.putExtra(AppConstant.EXTRA_NICK_NAME, friend.getNickName());
        intent.putExtra(AppConstant.EXTRA_IS_GROUP_CHAT, true);
        startActivity(intent);
        if (friend.getUnReadNum() > 0) {// 如该群组未读消息数量大于1, 刷新MessageFragment
            MsgBroadcast.broadcastMsgNumReset(getActivity());
            MsgBroadcast.broadcastMsgUiUpdate(getActivity());
        }
    }
}
