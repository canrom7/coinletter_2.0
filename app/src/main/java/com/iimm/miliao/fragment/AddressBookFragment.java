package com.iimm.miliao.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.adapter.AddressBookAdapter;
import com.iimm.miliao.bean.AttentionUser;
import com.iimm.miliao.bean.EventBusMsg;
import com.iimm.miliao.bean.EventMessage;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.broadcast.CardcastUiUpdateUtil;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.NewFriendDao;
import com.iimm.miliao.db.dao.OnCompleteListener2;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.sortlist.BaseSortModel;
import com.iimm.miliao.sortlist.SideBar;
import com.iimm.miliao.sortlist.SortHelper;
import com.iimm.miliao.ui.MainActivity;
import com.iimm.miliao.ui.company.ManagerCompany;
import com.iimm.miliao.ui.contacts.FriendAndGroupActivity;
import com.iimm.miliao.ui.contacts.label.LabelActivity;
import com.iimm.miliao.ui.message.ChatActivity;
import com.iimm.miliao.ui.nearby.UserAddActivity;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.view.NoDoubleClickListener;
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

/**
 * MrLiu253@163.com
 *
 * @time 2019-10-19
 */
public class AddressBookFragment extends AddressBookSearchFragment {

    private AddressBookAdapter mAdapter;
    private TextView mTvDialog;
    private SideBar mSideBar;
    private String mLoginUserId;
    private View mHeadView;
    private ImageView ivRightBtn;
    private TextView mNewFriendV;
    private View mGroupV;
    private SmartRefreshLayout mSmartRefreshLayout;

    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(CardcastUiUpdateUtil.ACTION_UPDATE_UI)) {
                loadData();
            }
            if (action.equals(MsgBroadcast.ACTION_MSG_NUM_UPDATE_NEW_FRIEND)) {// 更新消息数量
                Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, Constants.ID_NEW_FRIEND_MESSAGE);
                MainActivity activity = (MainActivity) getActivity();
                if (friend != null && friend.getUnReadNum() > 0) {
                    activity.updateNewFriendMsgNum(friend.getUnReadNum());// 更新底部Tab栏通讯录角标
                    mNewFriendV.setVisibility(View.VISIBLE);
                    mNewFriendV.setText(String.format("%d", friend.getUnReadNum()));
                } else {
                    activity.updateNewFriendMsgNum(0);// 更新底部Tab栏通讯录角标
                    mNewFriendV.setVisibility(View.GONE);
                }
            }
        }
    };
    private EditText mEditText;
    private TextView mTvCancel;

    public AddressBookFragment() {
        mSortFriends = new ArrayList<BaseSortModel<Friend>>();
        searchFriends = new ArrayList<>();
    }

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_address_book;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        EventBus.getDefault().register(this);
        mLoginUserId = coreManager.getSelf().getUserId();
        mLvContent = findViewById(R.id.address_book_lv);
        mTvDialog = findViewById(R.id.text_dialog);
        mSideBar = findViewById(R.id.sidebar);
        mRvSearchList = findViewById(R.id.rv_search_list);
        mRvSearchList.setVisibility(View.GONE);
        mEditText = findViewById(R.id.search_edit);
        mEditText.setHint(R.string.search_for_friends);
        mTvCancel = findViewById(R.id.tv_cancel);
        initView();
        upDataFriend();

        int mNewContactsNumber = PreferenceUtils.getInt(getActivity(), Constants.NEW_CONTACTS_NUMBER + mLoginUserId, 0);
        MainActivity activity = (MainActivity) getActivity();
        activity.updateNewFriendMsgNum(mNewContactsNumber);// 更新底部Tab栏通讯录角标
        if (mNewContactsNumber > 0) {
            mNewFriendV.setVisibility(View.VISIBLE);
            mNewFriendV.setText(String.format("%d", mNewContactsNumber));
        } else {
            mNewFriendV.setVisibility(View.GONE);
        }
        mGroupV.setVisibility(View.GONE);
    }

    @Override
    protected void tvCancelOnClick() {
        mSideBar.setVisibility(View.VISIBLE);
    }

    /**
     * 从服务端获取好友列表，更新数据库
     */
    private void upDataFriend() {
        // 使用这个对话框阻止其他操作，以免主线程读写数据库被阻塞anr,
        DialogHelper.showDefaulteMessageProgressDialog(getActivity());
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        HttpUtils.get().url(coreManager.getConfig().FRIENDS_ATTENTION_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<AttentionUser>(AttentionUser.class) {
                    @Override
                    public void onResponse(ArrayResult<AttentionUser> result) {
                        mSmartRefreshLayout.finishRefresh();
                        Log.e("ssssss", TimeUtils.getCurrentTime());
                        if (result.getResultCode() == 1) {
                            AsyncUtils.doAsync(AddressBookFragment.this, e -> {
                                Reporter.post("保存好友失败，", e);
                                AsyncUtils.runOnUiThread(requireContext(), ctx -> {
                                    DialogHelper.dismissProgressDialog();
                                    ToastUtil.showToast(ctx, R.string.data_exception);
                                });
                            }, c -> {
                                FriendDao.getInstance().addAttentionUsers(coreManager.getSelf().getUserId(), result.getData(),
                                        new OnCompleteListener2() {

                                            @Override
                                            public void onLoading(int progressRate, int sum) {
                                            }

                                            @Override
                                            public void onCompleted() {
                                                Log.e("ssssss", TimeUtils.getCurrentTime());
                                                c.uiThread(r -> {
                                                    r.loadData();
                                                });
                                            }
                                        });
                            });
                        } else {
                            DialogHelper.dismissProgressDialog();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        mSmartRefreshLayout.finishRefresh();
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(getActivity());
                    }
                });
    }

    @Override
    protected void loadData() {
        if (!DialogHelper.isShowing()) {
            DialogHelper.showDefaulteMessageProgressDialog(getActivity());
        }
        AsyncUtils.doAsync(this, e -> {
            Reporter.post("加载数据失败，", e);
            AsyncUtils.runOnUiThread(requireContext(), ctx -> {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showToast(ctx, R.string.data_exception);
            });
        }, c -> {
            final List<Friend> friends = FriendDao.getInstance().getAllFriends(mLoginUserId);
            Map<String, Integer> existMap = new HashMap<>();
            List<BaseSortModel<Friend>> sortedList = SortHelper.toSortedModelList(friends, existMap, Friend::getShowName);
            Log.e("ssssss", TimeUtils.getCurrentTime() + "");
            c.uiThread(r -> {
                DialogHelper.dismissProgressDialog();
                mSideBar.setExistMap(existMap);
                mSortFriends = sortedList;
                mAdapter.setData(sortedList);
            });
        });
    }

    private void initView() {
        mSmartRefreshLayout = findViewById(R.id.address_book_srl);
        mSmartRefreshLayout.setRefreshHeader(new ClassicsHeader(getContext()));
        ivRightBtn = findViewById(R.id.iv_title_right);
        ivRightBtn.setVisibility(View.VISIBLE);
        ivRightBtn.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                Intent intent = new Intent(AddressBookFragment.this.getActivity(), UserAddActivity.class);
                AddressBookFragment.this.startActivity(intent);
            }
        });
        if (coreManager.getConfig().isHideSearchFriend || (coreManager.getConfig().ordinaryUserCannotSearchFriend && (coreManager.getSelf().getRole() == null || coreManager.getSelf().getRole().size() == 0))) {
            ivRightBtn.setVisibility(View.GONE);
        } else {
            ivRightBtn.setVisibility(View.VISIBLE);
        }
        LayoutInflater inflater = LayoutInflater.from(getContext());
        if (mHeadView == null) {
            mHeadView = inflater.inflate(R.layout.fragment_address_book_head, null);
        }
        mNewFriendV = mHeadView.findViewById(R.id.new_friend_v);
        mGroupV = mHeadView.findViewById(R.id.group_v);
        ConstraintLayout llMyColleague = mHeadView.findViewById(R.id.ll_my_colleague);
        ConstraintLayout llNewFriend = mHeadView.findViewById(R.id.ll_new_friend);
        ConstraintLayout llGroup = mHeadView.findViewById(R.id.ll_group);
        ConstraintLayout llTag = mHeadView.findViewById(R.id.ll_tag);
        initSearchModule(mEditText, mTvCancel);
        llNewFriend.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                Intent intent = new Intent(AddressBookFragment.this.getActivity(), FriendAndGroupActivity.class);
                intent.putExtra("type", 2);
                AddressBookFragment.this.getActivity().startActivity(intent);

                PreferenceUtils.putInt(AddressBookFragment.this.getActivity(), Constants.NEW_CONTACTS_NUMBER + mLoginUserId, 0);
                Friend mNewFriend = FriendDao.getInstance().getFriend(mLoginUserId, Constants.ID_NEW_FRIEND_MESSAGE);
                if (mNewFriend != null) {
                    mNewFriend.setUnReadNum(0);
                    mNewFriendV.setVisibility(View.GONE);
                    MainActivity activity = (MainActivity) AddressBookFragment.this.getActivity();
                    if (activity != null) {
                        activity.updateNewFriendMsgNum(0);// 更新底部Tab栏通讯录角标
                    }
                }
            }
        });
        llGroup.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                Intent intent = new Intent(AddressBookFragment.this.getActivity(), FriendAndGroupActivity.class);
                intent.putExtra("type", 1);
                AddressBookFragment.this.getActivity().startActivity(intent);
            }
        });
        llTag.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                LabelActivity.start(AddressBookFragment.this.requireContext());
            }
        });
        llMyColleague.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                ManagerCompany.start(AddressBookFragment.this.requireContext());
            }
        });
        mLvContent.addHeaderView(mHeadView);
        mAdapter = new AddressBookAdapter(getActivity(), mSortFriends,false);
        mLvContent.setAdapter(mAdapter);
        mLvContent.setOnItemClickListener((parent, view, position, id) -> {
            if (id < 0) {
                return;
            }
            Friend friend = mSortFriends.get((int) id).getBean();
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra(ChatActivity.FRIEND, friend);
            intent.putExtra("isserch", false);
            startActivity(intent);
        });
        mLvContent.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        if (mInputManager != null) {
                            mInputManager.hideSoftInputFromWindow(mEditText.getApplicationWindowToken(), 0);
                        }
                        break;
                }

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mTvDialog = (TextView) findViewById(R.id.text_dialog);
        mSideBar.setTextView(mTvDialog);
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = mAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mLvContent.setSelection(position);
                }
            }
        });
        mRvSearchList.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                mTvCancel.setVisibility(View.GONE);
                mEditText.setText("");
                mEditText.clearFocus();
                if (mInputManager != null) {
                    mInputManager.hideSoftInputFromWindow(mEditText.getApplicationWindowToken(), 0);
                }
            }
        });
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                upDataFriend();
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CardcastUiUpdateUtil.ACTION_UPDATE_UI);
        intentFilter.addAction(MsgBroadcast.ACTION_MSG_NUM_UPDATE_NEW_FRIEND);
        getActivity().registerReceiver(mUpdateReceiver, intentFilter);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void processEventBusMsg(EventBusMsg eventBusMsg) {
        if (eventBusMsg.getMessageType() == Constants.EVENTBUS_FRIEND_DOWNLOAD_REFRESH_UI) {
            final List<Friend> friends = FriendDao.getInstance().getAllFriends(mLoginUserId);
            Map<String, Integer> existMap = new HashMap<>();
            List<BaseSortModel<Friend>> sortedList = SortHelper.toSortedModelList(friends, existMap, Friend::getShowName);
            if (mSideBar != null) {
                mSideBar.setExistMap(existMap);
            }
            if (mAdapter != null) {
                mSortFriends = sortedList;
                mAdapter.setData(sortedList);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void processEventBusMsg(EventMessage eventMessage) {
        PreferenceUtils.putInt(getActivity(), Constants.NEW_CONTACTS_NUMBER + mLoginUserId, 0);
        Friend mNewFriend = FriendDao.getInstance().getFriend(mLoginUserId, Constants.ID_NEW_FRIEND_MESSAGE);
        if (mNewFriend != null) {
            mNewFriend.setUnReadNum(0);
            mNewFriendV.setVisibility(View.GONE);
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                activity.updateNewFriendMsgNum(0);// 更新底部Tab栏通讯录角标
            }
        }
        FriendDao.getInstance().markUserMessageRead(mLoginUserId, Constants.ID_NEW_FRIEND_MESSAGE);
        // 将所有消息都变为已读
        NewFriendDao.getInstance().markNewFriendRead(mLoginUserId);
        NewFriendDao.getInstance().resetAllNewFriendUnRead(mLoginUserId);
    }

    @Override
    protected void searchEditFocusChange(boolean hasFocus) {
        //mSideBar.setVisibility(View.GONE);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mUpdateReceiver);
        EventBus.getDefault().unregister(this);
    }
}
