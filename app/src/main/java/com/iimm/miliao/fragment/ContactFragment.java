package com.iimm.miliao.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.adapter.FriendSortAdapter;
import com.iimm.miliao.bean.AttentionUser;
import com.iimm.miliao.bean.EventBusMsg;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.broadcast.CardcastUiUpdateUtil;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.OnCompleteListener2;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.sortlist.BaseSortModel;
import com.iimm.miliao.sortlist.SideBar;
import com.iimm.miliao.sortlist.SortHelper;
import com.iimm.miliao.ui.company.ManagerCompany;
import com.iimm.miliao.ui.contacts.BlackActivity;
import com.iimm.miliao.ui.contacts.DeviceActivity;
import com.iimm.miliao.ui.contacts.label.LabelActivity;
import com.iimm.miliao.ui.message.ChatActivity;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.ToastUtil;
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


public class ContactFragment extends SearchFragment {
    private TextView mTvDialog;
    private SideBar mSideBar;
    private String mLoginUserId;
    private View mHeadView;
    private FriendSortAdapter mAdapter;
    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(CardcastUiUpdateUtil.ACTION_UPDATE_UI)) {
                loadData();
            }
        }
    };
    private EditText mEditText;
    private TextView mTvCancel;


    public ContactFragment() {
        mSortFriends = new ArrayList<BaseSortModel<Friend>>();
        searchFriends = new ArrayList<>();
    }

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_contact;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        super.onActivityCreated(savedInstanceState, createView);
        EventBus.getDefault().register(this);
        mLoginUserId = coreManager.getSelf().getUserId();
        mLvContent = findViewById(R.id.pull_refresh_list);
        mTvDialog = findViewById(R.id.text_dialog);
        mSideBar = findViewById(R.id.sidebar);
        mRvSearchList = findViewById(R.id.rv_search_list);
        mRvSearchList.setVisibility(View.GONE);
        mEditText = findViewById(R.id.search_edit);
        mEditText.setHint(R.string.search_for_friends);
        mTvCancel = findViewById(R.id.tv_cancel);
        View view = findViewById(R.id.include2);
        view.setBackgroundColor(getResources().getColor(R.color.white));

        initView();
        loadData();
    }

    @Override
    protected void tvCancelOnClick() {
        mSideBar.setVisibility(View.VISIBLE);
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        if (mHeadView == null) {
            mHeadView = inflater.inflate(R.layout.fragment_contact_head, null);
        }
//        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ScreenUtil.getScreenWidth(getContext()), DisplayUtil.dip2px(getContext(),121));
//        mHeadView.setLayoutParams(layoutParams);
        ImageView ivMyColleagueIcon = mHeadView.findViewById(R.id.iv_my_colleague_icon);
        ImageView ivMyDeviceIcon = mHeadView.findViewById(R.id.iv_my_device_icon);
        ImageView ivMyBlackListIcon = mHeadView.findViewById(R.id.iv_my_black_list_icon);
        ImageView ivMyTagIcon = mHeadView.findViewById(R.id.iv_my_tag_icon);
        AvatarHelper.setIcon(ivMyColleagueIcon, R.mipmap.my_colleague, R.mipmap.my_colleague_square);
        AvatarHelper.setIcon(ivMyDeviceIcon, R.mipmap.my_device, R.mipmap.my_device_square);
        AvatarHelper.setIcon(ivMyBlackListIcon, R.mipmap.black_list, R.mipmap.black_list_square);
        AvatarHelper.setIcon(ivMyTagIcon, R.mipmap.tag, R.mipmap.tag_square);
        LinearLayout llMyColleague = mHeadView.findViewById(R.id.ll_my_colleague);
        LinearLayout llMyDevice = mHeadView.findViewById(R.id.ll_my_device);
        LinearLayout llBlackList = mHeadView.findViewById(R.id.ll_black_book);
        LinearLayout llTag = mHeadView.findViewById(R.id.ll_tag);
        initSearchModule(mEditText, mTvCancel);
        llMyColleague.setOnClickListener(v -> {
            ManagerCompany.start(requireContext());
        });
        llMyDevice.setOnClickListener(v -> {
            if (MyApplication.IS_SUPPORT_MULTI_LOGIN) {
                Intent intentDevice = new Intent(getActivity(), DeviceActivity.class);
                getActivity().startActivity(intentDevice);
            } else {
                ToastUtil.showToast(getContext(), R.string.tip_disable_multi_login);
            }
        });
        llBlackList.setOnClickListener(v -> {
            Intent intentBlack = new Intent(getActivity(), BlackActivity.class);
            getActivity().startActivity(intentBlack);
        });
        llTag.setOnClickListener(v -> {
            LabelActivity.start(requireContext());
        });
        mLvContent.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mAdapter = new FriendSortAdapter(getActivity(), mSortFriends);
        mLvContent.getRefreshableView().setAdapter(mAdapter);
        mLvContent.getRefreshableView().addHeaderView(mHeadView);
        mLvContent.setOnRefreshListener(refreshView -> upDataFriend());
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
                        mInputManager.hideSoftInputFromWindow(mEditText.getApplicationWindowToken(), 0);
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
                    mLvContent.getRefreshableView().setSelection(position);
                }
            }
        });
        mRvSearchList.setOnClickListener(v -> {
            mTvCancel.setVisibility(View.GONE);
            mEditText.setText("");
            mEditText.clearFocus();
            if (mInputManager != null) {
                mInputManager.hideSoftInputFromWindow(mEditText.getApplicationWindowToken(), 0);
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CardcastUiUpdateUtil.ACTION_UPDATE_UI);
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

    /**
     * 从服务端获取好友列表，更新数据库
     */
    private void upDataFriend() {
        // 这鬼库马上停止刷新会停不了，只能post一下，
        mLvContent.post(() -> {
            mLvContent.onRefreshComplete();
        });
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
                        if (result.getResultCode() == 1) {
                            AsyncUtils.doAsync(ContactFragment.this, e -> {
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
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(getActivity());
                    }
                });
    }

    @Override
    public void loadData() {
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
            c.uiThread(r -> {
                DialogHelper.dismissProgressDialog();
                mSideBar.setExistMap(existMap);
                mSortFriends = sortedList;
                mAdapter.setData(sortedList);
                mLvContent.onRefreshComplete();
            });
        });
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
