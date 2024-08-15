package com.iimm.miliao.fragment;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.BuildConfig;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.ad.BaiduRewardVideoActivity;
import com.iimm.miliao.ad.CjsRewardVideoActivity;
import com.iimm.miliao.ad.GdtRewardVideoActivity;
import com.iimm.miliao.ad.SigmobRewardVideoActivity;
import com.iimm.miliao.adapter.MessageListAdapter;
import com.iimm.miliao.bean.AdvertisingList;
import com.iimm.miliao.bean.EventBusMsg;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.PrivacySetting;
import com.iimm.miliao.bean.RoomMember;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.bean.message.NewFriendMessage;
import com.iimm.miliao.broadcast.CardcastUiUpdateUtil;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.NewFriendDao;
import com.iimm.miliao.db.dao.RoomMemberDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.helper.FriendHelper;
import com.iimm.miliao.helper.PrivacySettingHelper;
import com.iimm.miliao.pay.PayActivity;
import com.iimm.miliao.pay.ReceiptActivity;
import com.iimm.miliao.ui.MainActivity;
import com.iimm.miliao.ui.base.EasyFragment;
import com.iimm.miliao.ui.groupchat.FaceToFaceGroup;
import com.iimm.miliao.ui.groupchat.SelectContactsActivity;
import com.iimm.miliao.ui.me.NearPersonActivity;
import com.iimm.miliao.ui.message.ChatActivity;
import com.iimm.miliao.ui.message.MucChatActivity;
import com.iimm.miliao.ui.message.multi.RoomInfoActivity;
import com.iimm.miliao.ui.nearby.PublicNumberSearchActivity;
import com.iimm.miliao.ui.nearby.UserAddActivity;
import com.iimm.miliao.ui.other.BasicInfoActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.HttpUtil;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ToolUtils;
import com.iimm.miliao.util.ViewHolder;
import com.iimm.miliao.view.HeadView;
import com.iimm.miliao.view.MessagePopupWindow;
import com.iimm.miliao.view.QuickReplyDialog;
import com.iimm.miliao.view.SelectionFrame;
import com.iimm.miliao.xmpp.ListenerManager;
import com.iimm.miliao.xmpp.XmppConnectionImpl;
import com.iimm.miliao.xmpp.listener.ChatMessageListener;
import com.iimm.miliao.xmpp.util.ImHelper;
import com.roamer.slidelistview.SlideBaseAdapter;
import com.roamer.slidelistview.SlideListView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.JsonCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * 消息界面
 */
public class MessageFragment extends EasyFragment implements ChatMessageListener, MessageListAdapter.MessageItemClickListener {
    private static String TAG = "MessageFragment";
    private TextView mTvTitle;
    private EditText mEditText;
    private boolean search;
    private LinearLayout mNetErrorLl;
    private SmartRefreshLayout mSmartRefreshLayout;
    private RecyclerView mRecyclerView;
    private MessageListAdapter mAdapter;
    private List<Friend> mFriendList;
    private String mLoginUserId;
    private MessagePopupWindow mMessagePopupWindow;
    // 上次刷新时间，限制过快刷新，
    private long refreshTime;
    private boolean timerRunning;
    private boolean isNewMessage = false;
    private InputMethodManager mInputManager;

    //新加
    private SlideListView mNoticeAccountList;
    private NoticeAdapter mNoticeAdapter;
    private List<Friend> mNoticeFriendList;
    private List<Friend> mKefuFriendList;

    private long mOldTime;
    private boolean isLoadAdFirst = true;
    private long mDelayMilliseconds = 1000;
    // 刷新的定时器，限制过快刷新，
    private CountDownTimer timer = new CountDownTimer(1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            Log.e(TAG, "计时结束，更新消息页面");
            timerRunning = false;
            refreshTime = System.currentTimeMillis();
            isNewMessage = true;
            refresh();
        }
    };
    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            if (action.equals(MsgBroadcast.ACTION_MSG_UI_UPDATE)) {// 刷新页面
                long lastRefreshTime = refreshTime;
                long delta = System.currentTimeMillis() - lastRefreshTime;
                if (delta > TimeUnit.SECONDS.toMillis(1)) {
                    refreshTime = System.currentTimeMillis();
                    isNewMessage = true;
                    refresh();
                } else if (!timerRunning) {
                    timerRunning = true;
                    timer.start();
                }
            } else if (action.equals(Constants.NOTIFY_MSG_SUBSCRIPT)) {
                Friend friend = (Friend) intent.getSerializableExtra(AppConstant.EXTRA_FRIEND);
                if (friend != null) {
                    clearMessageNum(friend);
                }
            } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {// 网络发生改变
                if (!HttpUtil.isGprsOrWifiConnected(getActivity())) {
                    mNetErrorLl.setVisibility(View.VISIBLE);
                    findViewById(R.id.pb_title_center).setVisibility(View.VISIBLE);
                    mTvTitle.setText(InternationalizationHelper.getString("JXMsgViewController_OffLine"));
                    XMPPTCPConnection xmpptcpConnection = XmppConnectionImpl.getInstance().getXMPPConnection();
                    if (xmpptcpConnection != null && xmpptcpConnection.isConnected()) {
                        xmpptcpConnection.disconnect();
                    }
                } else {
                    mNetErrorLl.setVisibility(View.GONE);
                }
            } else if (action.equals(Constants.NOT_AUTHORIZED)) {
                mTvTitle.setText(getString(R.string.password_error));
            }
        }
    };
    private TextView mCancel;
    private boolean isScroll;


    private void getUserSignInInfo() {
        if (coreManager == null || coreManager.getSelfStatus() == null) {
            return;
        }
//
//        Map<String, String> params = new HashMap<>();
//        params.put("access_token", coreManager.getSelfStatus().accessToken);
//        params.put("userId", mLoginUserId);
//        HttpUtils.get().url(coreManager.getConfig().GET_USER_SIGN_INFO)
//                .params(params)
//                .build()
//                .execute(new BaseCallback<SignInfoEntity>(SignInfoEntity.class) { //获取通证成功
//                    @Override
//                    public void onResponse(ObjectResult<SignInfoEntity> result) {
//                        if (result.getResultCode() == Result.CODE_SUCCESS && result.getData() != null) {
//                            SignInfoEntity infoBean = result.getData();
//                            if (Integer.parseInt(infoBean.getSignStatus()) != SIGN_IN_ALREADY) {
//                                View view = View.inflate(getActivity(), R.layout.view_sign, null);
//
//
//                                AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.dialogNoBg)
//                                        .setView(view)
//                                        .setCancelable(true)
//                                        .show();
//                                view.setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        startActivity(new Intent(getActivity(), SignInRedActivity.class));
//                                        dialog.dismiss();
//                                    }
//                                });
//                                view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View mView) {
//                                        dialog.dismiss();
//                                    }
//                                });
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onError(Call call, Exception e) {
//                    }
//                });
    }

    /**
     * 刷新
     */
    private void refresh() {
        if (isScroll) {
            return;
        }
        isNewMessage = false;
        if (!TextUtils.isEmpty(mEditText.getText().toString().trim())) {
            mEditText.setText("");// 内部调用了loadData
        } else {
            loadDatas();
        }
    }

    class NoticeAdapter extends SlideBaseAdapter {
        NoticeAdapter(Context context) {
            super(context);
        }

        @Override
        public int getFrontViewId(int position) {
            return R.layout.item_notice_account;
        }

        @Override
        public int getLeftBackViewId(int position) {
            return 0;
        }

        @Override
        public int getRightBackViewId(int position) {
            return R.layout.item_notice_right;
        }

        @Override
        public int getCount() {
            return mKefuFriendList.size();
        }

        @Override
        public Friend getItem(int position) {
            return mKefuFriendList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = createConvertView(position);
            }
            HeadView mNoticeAccountIv = ViewHolder.get(convertView, R.id.notice_iv);
            TextView mNoticeAccountTv = ViewHolder.get(convertView, R.id.notice_tv);
            Friend friend = getItem(position);
            if (friend != null) {
                AvatarHelper.getInstance().displayAvatar(friend.getUserId(), mNoticeAccountIv);
                mNoticeAccountTv.setText(!TextUtils.isEmpty(friend.getRemarkName()) ? friend.getRemarkName() : friend.getNickName());
            }

            TextView delete_tv = ViewHolder.get(convertView, R.id.delete_tv);

            delete_tv.setOnClickListener(v -> {
                showDeleteAllDialog(position);
            });
            return convertView;
        }
    }

    // 删除公众号，
    private void showDeleteAllDialog(final int position) {
        Friend friend = mKefuFriendList.get(position);
        if (friend.getStatus() == Constants.STATUS_UNKNOW) {// 陌生人
            return;
        }
        if (friend.getUserId().equals(Constants.ID_SYSTEM_MESSAGE)
                || friend.getUserId().equals(Constants.ID_SK_PAY)) {// 10000 与1100 号不能删除，
            ToastUtil.showToast(getContext(), getString(R.string.tip_not_allow_delete));
            return;
        }
        SelectionFrame mSF = new SelectionFrame(getContext());
        mSF.setSomething(getString(R.string.delete_public_number), getString(R.string.ask_delete_public_number), new SelectionFrame.OnSelectionFrameClickListener() {
            @Override
            public void cancelClick() {

            }

            @Override
            public void confirmClick() {
                deleteFriend(position, 1);
            }
        });
        mSF.show();
    }

    private void deleteFriend(final int position, final int type) {
        Friend friend = mKefuFriendList.get(position);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("toUserId", friend.getUserId());
        DialogHelper.showDefaulteMessageProgressDialog(getActivity());

        HttpUtils.get().url(coreManager.getConfig().FRIENDS_ATTENTION_DELETE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            NewFriendMessage message = NewFriendMessage.createWillSendMessage(
                                    coreManager.getSelf(), Constants.TYPE_DELALL, null, friend);
                            ImHelper.sendNewFriendMessage(coreManager.getSelf().getUserId(), message); // 删除好友
                            // 老大说不要提示，行消失就可以，
                            /*
                            ToastUtil.showToast(getApplicationContext(), R.string.tip_delete_public_number_success);
                             */

                            FriendHelper.removeAttentionOrFriend(coreManager.getSelf().getUserId(), message.getUserId());

                            ChatMessage deleteChatMessage = new ChatMessage();
                            deleteChatMessage.setContent(getString(R.string.has_delete_public_number_place_holder, coreManager.getSelf().getNickName()));
                            deleteChatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
                            FriendDao.getInstance().updateLastChatMessage(coreManager.getSelf().getUserId(), Constants.ID_NEW_FRIEND_MESSAGE, deleteChatMessage);

                            NewFriendDao.getInstance().createOrUpdateNewFriend(message);
                            NewFriendDao.getInstance().changeNewFriendState(coreManager.getSelf().getUserId(), Constants.STATUS_16);
                            ListenerManager.getInstance().notifyNewFriend(coreManager.getSelf().getUserId(), message, true);

                            CardcastUiUpdateUtil.broadcastUpdateUi(getActivity());
                            mKefuFriendList.remove(position);
                            mNoticeAdapter.notifyDataSetChanged();
                        } else if (!TextUtils.isEmpty(result.getResultMsg())) {
                            ToastUtil.showToast(getActivity(), result.getResultMsg());
                        } else {
                            ToastUtil.showToast(getActivity(), R.string.tip_server_error);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(getActivity());
                    }
                });
    }

    private void initKefuView() {

        mNoticeFriendList = FriendDao.getInstance().getAllSystems(coreManager.getSelf().getUserId());

        if (mNoticeFriendList == null) {
            mNoticeFriendList = new ArrayList<>();

        }
        mKefuFriendList = new ArrayList<>();
        mKefuFriendList.clear();
//        for(int k=0;k<mNoticeFriendList.size();k++){
//            if(mNoticeFriendList.get(k).getUserId().equals(Constants.ID_SYSTEM_MESSAGE)){
//                mKefuFriendList.add(mNoticeFriendList.get(k));
//            }
//
//        }
        mNoticeAccountList = findViewById(R.id.notice_account_lv);
        mNoticeAdapter = new NoticeAdapter(getActivity());
        mNoticeAccountList.setAdapter(mNoticeAdapter);

        mNoticeAccountList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                long nowTime = SystemClock.elapsedRealtime();
                long intervalTime = nowTime - mOldTime;
                if (mOldTime == 0 || intervalTime >= mDelayMilliseconds) {
                    mOldTime = nowTime;

                    Friend mFriend = mKefuFriendList.get(position);
                    if (mFriend != null) {
                        if (mFriend.getUserId().equals(Constants.ID_SK_PAY)) {
                            startActivity(new Intent(getActivity(), PayActivity.class));
                        } else {
                            Intent intent = new Intent(getActivity(), ChatActivity.class);
                            intent.putExtra(ChatActivity.FRIEND, mFriend);
                            startActivity(intent);
                        }
                    }
                }

            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ");
        EventBus.getDefault().register(this);
    }

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_message_new;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        mInputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        initActionBar();
        // 不能用createView判断不初始化，因为Fragment复用时老activity可能被销毁了，
        initView();
        //新加修改
        initKefuView();
        loadDatas();
        mSmartRefreshLayout.autoLoadMore();
    }

    private void initActionBar() {
        findViewById(R.id.iv_title_left).setVisibility(View.GONE);
        mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText(InternationalizationHelper.getString("JXMsgViewController_OffLine"));
        appendClick(mTvTitle);
        ImageView mIvTitleRight = (ImageView) findViewById(R.id.iv_title_right);
        mIvTitleRight.setImageResource(R.drawable.chat_more);
        appendClick(mIvTitleRight);
    }

    private void initView() {
        mLoginUserId = coreManager.getSelf().getUserId();
        mFriendList = new ArrayList<>();

        mCancel = findViewById(R.id.tv_cancel);
        mEditText = findViewById(R.id.search_edit);
        mEditText.setHint(InternationalizationHelper.getString("JX_SearchChatLog"));
        mNetErrorLl = (LinearLayout) findViewById(R.id.net_error_ll);
        mNetErrorLl.setOnClickListener(this);

        mSmartRefreshLayout = findViewById(R.id.message_srl);
        ((ClassicsHeader) mSmartRefreshLayout.getRefreshHeader()).setEnableLastTime(false);
        mRecyclerView = findViewById(R.id.message_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new MessageListAdapter(getActivity(), mFriendList, this);
        mRecyclerView.setAdapter(mAdapter);
        initClick();
        ListenerManager.getInstance().addChatMessageListener(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MsgBroadcast.ACTION_MSG_UI_UPDATE);// 刷新页面Ui
        intentFilter.addAction(Constants.NOTIFY_MSG_SUBSCRIPT);// 刷新"消息"角标
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);// 网络发生改变
        intentFilter.addAction(Constants.NOT_AUTHORIZED);// XMPP密码错误
        getActivity().registerReceiver(mUpdateReceiver, intentFilter);

    }

    private void initClick() {

        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                loadDatas();
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mAdapter.closeAllItems();
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    isScroll = false;
                    if (isNewMessage) {
                        refresh();
                    }
                    try {
                        if (getContext() != null) {
                            Glide.with(getContext()).resumeRequests();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    isScroll = true;
                    try {
                        if (getContext() != null) {
                            Glide.with(getContext()).pauseRequests();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString().trim();
                if (!TextUtils.isEmpty(str)) {
                    queryChatMessage(str);
                } else {
                    loadDatas();
                }
            }
        });
        mEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mEditText.setTextColor(getResources().getColor(R.color.search_color_focus));
                mCancel.setVisibility(View.VISIBLE);
            } else {
                mEditText.setTextColor(getResources().getColor(R.color.search_color_cancle));
            }
        });
        mCancel.setOnClickListener(v -> {
            if (mInputManager != null) {
                mCancel.setVisibility(View.GONE);
                mEditText.setText(null);
                mInputManager.hideSoftInputFromWindow(mEditText.getApplicationWindowToken(), 0);
                mEditText.clearFocus();
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                getUserSignInInfo();
                getADReward();
            }
        }, 1500);

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getUserSignInInfo();
            getADReward();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            getUserSignInInfo();
            getADReward();
        }
    }

    private void getADReward() {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("isFirst", isLoadAdFirst ? "1" : "0");
        isLoadAdFirst = false;
        HttpUtils.get().url(coreManager.getConfig().ADVERTISINGDISTRIBUTED)
                .params(params)
                .build()
                .execute(new JsonCallback() {
                    @Override
                    public void onResponse(String result) {
                        if (result != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(result);
                                JSONObject jsonObject1 = jsonObject.optJSONObject("data");
                                String id = jsonObject1.optString("id");
                                if (!TextUtils.isEmpty(id)) {
                                    if ("1".equals(id)) {
                                        if (BuildConfig.DEBUG) {
                                            ToastUtil.showToast("加载优量汇激励");
                                        }
                                    } else if ("2".equals(id)) {
                                        if (BuildConfig.DEBUG) {
                                            ToastUtil.showToast("加载百度激励");
                                        }
                                    } else if ("3".equals(id)) {
                                        if (BuildConfig.DEBUG) {
                                            ToastUtil.showToast("加载快手激励");
                                        }
                                    } else if ("4".equals(id)) {
                                        if (BuildConfig.DEBUG) {
                                            ToastUtil.showToast("加载穿山甲激励");
                                        }
                                    } else if ("5".equals(id)) {
                                        if (BuildConfig.DEBUG) {
                                            ToastUtil.showToast("加载sigmob激励");
                                        }
                                    }
                                    View view = View.inflate(getActivity(), R.layout.view_red, null);
                                    AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.dialogNoBg)
                                            .setView(view)
                                            .setCancelable(false)
                                            .show();
                                    view.setOnClickListener(v -> {
                                        showRewardAd(id);
                                        dialog.dismiss();
                                    });
                                    view.findViewById(R.id.close).setOnClickListener(mView -> dialog.dismiss());
                                    SPUtils.getInstance().put("splashKey", jsonObject.optString("data"));
                                }
                                Log.e("666666", jsonObject.optString("data"));
                            } catch (Exception e) {
                                Log.e("666666", e.getMessage());
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onError(Call call, Exception e) {
//                        SPUtils.getInstance().put("splashKey", "");
                    }
                });
    }

    private void getADIncome() {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        HttpUtils.get().url(coreManager.getConfig().ADVERTISINGINCOME)
                .params(params)
                .build()
                .execute(new JsonCallback() {
                    @Override
                    public void onResponse(String result) {
                        Log.e("666666", result);
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    private void showRewardAd(String id) {
        //1.腾讯优量汇2.百度百青藤3，快手联盟4，穿山甲5，sigmob (聚合sdk)
        String jsonSplash = SPUtils.getInstance().getString("splashKey", null);
        if (!TextUtils.isEmpty(jsonSplash)) {
            try {
                AdvertisingList advertisingList = new Gson().fromJson(jsonSplash, AdvertisingList.class);
                if (advertisingList != null && advertisingList.getAdvertisings() != null) {
                    for (AdvertisingList.Advertising advertising : advertisingList.getAdvertisings()) {
                        if (id.equals(advertising.getId())) {
                            if ("1".equals(advertising.getId())) {
                                loadGdtRewardAd(advertising.getRewardId());
                            } else if ("2".equals(advertising.getId())) {
                                loadBaiduRewardAd(advertising.getRewardId());
                            } else if ("3".equals(advertising.getId())) {

                            } else if ("4".equals(advertising.getId())) {
                                loadCsjRewardAd(advertising.getRewardId());
                            } else if ("5".equals(advertising.getId())) {
                                loadSigmobRewardAd(advertising.getRewardId());
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void loadSigmobRewardAd(String rewardId) {
        if (rewardId == null) {
            if (BuildConfig.DEBUG) {
                ToastUtil.showToast("loadSigmobRewardAd 为空");
            }
            return;
        }
        Intent intent = new Intent(requireContext(), SigmobRewardVideoActivity.class);
        intent.putExtra("code", rewardId);
        startActivityForResult(intent, 1);

    }

    private void loadCsjRewardAd(String rewardId) {
        if (rewardId == null) {
            if (BuildConfig.DEBUG) {
                ToastUtil.showToast("loadCsjRewardAd 为空");
            }
            return;
        }
        Intent intent = new Intent(requireContext(), CjsRewardVideoActivity.class);
        intent.putExtra("code", rewardId);
        startActivityForResult(intent, 2);

    }

    private void loadBaiduRewardAd(String rewardId) {
        if (rewardId == null) {
            if (BuildConfig.DEBUG) {
                ToastUtil.showToast("loadBaiduRewardAd 为空");
            }
            return;
        }
        Intent intent = new Intent(requireContext(), BaiduRewardVideoActivity.class);
        intent.putExtra("code", rewardId);
        startActivityForResult(intent, 3);
    }

    private void loadGdtRewardAd(String rewardId) {
        if (rewardId == null) {
            if (BuildConfig.DEBUG) {
                ToastUtil.showToast("loadGdtRewardAd 为空");
            }
            return;
        }
        Intent intent = new Intent(requireContext(), GdtRewardVideoActivity.class);
        intent.putExtra("code", rewardId);
        startActivityForResult(intent, 4);
    }


    @Override
    public void onResume() {
        super.onResume();
//        getUserSignInInfo();
        if (ImHelper.isAuthenticated()) {
            findViewById(R.id.pb_title_center).setVisibility(View.GONE);
            mTvTitle.setText(InternationalizationHelper.getString("JXMsgViewController_OnLine"));
        } else {
            findViewById(R.id.pb_title_center).setVisibility(View.VISIBLE);
            mTvTitle.setText(InternationalizationHelper.getString("JXMsgViewController_GoingOff"));
        }
        if (!HttpUtil.isGprsOrWifiConnected(MyApplication.getInstance())) {
            findViewById(R.id.pb_title_center).setVisibility(View.GONE);
            mTvTitle.setText(InternationalizationHelper.getString("JXMsgViewController_OffLine"));
        }
        if (mAdapter != null) {
            mAdapter.notifyDatasetChanged();
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void processEventBusMsg(EventBusMsg eventBusMsg) {
        if (eventBusMsg.getMessageType() == Constants.EVENTBUS_MSG_AUTH_CONNECT_ING) {
            if (mTvTitle == null) {
                return;
            }
            findViewById(R.id.pb_title_center).setVisibility(View.VISIBLE);
            mTvTitle.setText(InternationalizationHelper.getString("JXMsgViewController_GoingOff"));
        } else if (eventBusMsg.getMessageType() == Constants.EVENTBUS_MSG_AUTH_SUCCESS) {
            if (mTvTitle == null) {
                return;
            }
            findViewById(R.id.pb_title_center).setVisibility(View.GONE);
            mTvTitle.setText(InternationalizationHelper.getString("JXMsgViewController_OnLine"));
            mNetErrorLl.setVisibility(View.GONE);// 网络判断对部分手机有时会失效，坐下兼容(当xmpp在线时，隐藏网络提示)
        } else if (eventBusMsg.getMessageType() == Constants.EVENTBUS_MSG_AUTH_NOT) {
            if (mTvTitle == null) {
                return;
            }
            findViewById(R.id.pb_title_center).setVisibility(View.GONE);
            mTvTitle.setText(InternationalizationHelper.getString("JXMsgViewController_OffLine"));
        } else if (eventBusMsg.getMessageType() == Constants.EVENTBUS_MSG_SLECTOR_PROXY) {
            if (mTvTitle == null) {
                return;
            }
            findViewById(R.id.pb_title_center).setVisibility(View.GONE);
            mTvTitle.setText(getResources().getString(R.string.slector_proxy));
        } else if (eventBusMsg.getMessageType() == Constants.EVENTBUS_ROOM_MEMBER_DOWNLOAD_REFRESH_UI) {
            MyApplication.applicationHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mAdapter != null) {
                        mAdapter.notifyDatasetChanged();
                    }
                }
            });
        }
        if (!HttpUtil.isGprsOrWifiConnected(MyApplication.getInstance())) {
            if (mTvTitle == null) {
                return;
            }
            findViewById(R.id.pb_title_center).setVisibility(View.GONE);
            mTvTitle.setText(InternationalizationHelper.getString("JXMsgViewController_OffLine"));
        }
    }

    /**
     * 加载朋友数据
     */
    private void loadDatas() {
        if (mFriendList != null) {
            mFriendList.clear();
        }
        search = false;
//        Log.e(">>>>","mLoginUserId:"+mLoginUserId);
        mFriendList = FriendDao.getInstance().getNearlyFriendMsg(mLoginUserId);
        List<Friend> mRemoveFriend = new ArrayList<>();
        if (mFriendList.size() > 0) {
            for (Friend friend : mFriendList) {
//                Log.e(">>>>","friend:"+friend.getNickName());
                if (friend != null) {
                    if (friend.getUserId().equals(Constants.ID_NEW_FRIEND_MESSAGE)
                            || friend.getUserId().equals(mLoginUserId)) {
                        mRemoveFriend.add(friend);
                    }
                }
            }
            mFriendList.removeAll(mRemoveFriend);
        }
        updataListView();
//        mAdapter.notifyDatasetChanged();
        mSmartRefreshLayout.finishRefresh();
    }

    private void clearMessageNum(Friend friend) {
        friend.setUnReadNum(0);
        FriendDao.getInstance().markUserMessageRead(mLoginUserId, friend.getUserId());
        MainActivity mMainActivity = (MainActivity) getActivity();
        if (mMainActivity != null) {
            mMainActivity.updateNumData();
        }
        for (int i = 0; i < mFriendList.size(); i++) {
            Friend mF = mFriendList.get(i);
            if (Objects.equals(mF.getUserId(), friend.getUserId())) {
                mF.setUnReadNum(0);
                updataListView();
                mAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private void clearMessageNum(Friend friend, int position) {
        friend.setUnReadNum(0);
        FriendDao.getInstance().markUserMessageRead(mLoginUserId, friend.getUserId());
        MainActivity mMainActivity = (MainActivity) getActivity();
        if (mMainActivity != null) {
            mMainActivity.updateNumData();
        }
        for (Friend mF : mFriendList) {
            if (Objects.equals(mF.getUserId(), friend.getUserId())) {
                mF.setUnReadNum(0);
                break;
            }
        }
        mAdapter.notifyItemChanged(position);
    }

    @Override
    public void onClick(View v) {
        mAdapter.closeAllItems();
        switch (v.getId()) {
            case R.id.tv_title_center:
                ImHelper.checkXmppAuthenticated();
                break;
            case R.id.iv_title_right:
                mMessagePopupWindow = new MessagePopupWindow(getActivity(), this, coreManager);
                mMessagePopupWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                mMessagePopupWindow.showAsDropDown(v,
                        -(mMessagePopupWindow.getContentView().getMeasuredWidth() - v.getWidth() / 2 - 40),
                        0);
                break;
            case R.id.search_public_number:
                // 搜索公众号
                mMessagePopupWindow.dismiss();
                PublicNumberSearchActivity.start(requireContext());
                break;
            case R.id.create_group:
                // 发起群聊
                mMessagePopupWindow.dismiss();
                startActivity(new Intent(getActivity(), SelectContactsActivity.class));
                break;
            case R.id.face_group:
                mMessagePopupWindow.dismiss();
                // 面对面建群
                startActivity(new Intent(getActivity(), FaceToFaceGroup.class));
                break;
            case R.id.add_friends:
                // 添加朋友
                mMessagePopupWindow.dismiss();
                startActivity(new Intent(getActivity(), UserAddActivity.class));
                break;
            case R.id.scanning:
                // 扫一扫
                mMessagePopupWindow.dismiss();
                MainActivity.requestQrCodeScan(getActivity());
                break;
            case R.id.receipt_payment:
                // 收付款
                mMessagePopupWindow.dismiss();
                startActivity(new Intent(getActivity(), ReceiptActivity.class));
                break;
            case R.id.near_person:
                // 附近的人
                mMessagePopupWindow.dismiss();
                startActivity(new Intent(getActivity(), NearPersonActivity.class));
                break;
            case R.id.net_error_ll:
                //网络错误
                startActivity(new Intent(Settings.ACTION_SETTINGS));
                break;
        }
    }

    /**
     * 查询聊天记录
     */
    private void queryChatMessage(String str) {
        List<Friend> data = new ArrayList<>();
        mFriendList = FriendDao.getInstance().getNearlyFriendMsg(mLoginUserId);
        for (int i = 0; i < mFriendList.size(); i++) {
            Friend friend = mFriendList.get(i);
            //新修改
            if (friend.getUserId().equals(Constants.ID_SYSTEM_MESSAGE)) {

            } else {
                List<Friend> friends = ChatMessageDao.getInstance().queryChatMessageByContent(friend, str);
                if (friends != null && friends.size() > 0) {

                    data.addAll(friends);
                }
            }

        }
        if (mFriendList != null) {
            mFriendList.clear();
        }
        search = true;
        mFriendList.addAll(data);
        updataListView();
        mAdapter.notifyDatasetChanged();
    }

    /**
     * 更新列表
     */
    private void updataListView() {
        mAdapter.setData(mFriendList, search, mEditText.getText().toString());

    }

    @Override
    public void onMessageSendStateChange(int messageState, String msgId) {
        updataListView();
    }

    @Override
    public boolean onNewMessage(String fromUserId, ChatMessage message, boolean isGroupMsg) {
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 || requestCode == 2 || requestCode == 3 || requestCode == 4 || requestCode == 5) {
            if (resultCode == RESULT_OK && data != null && data.getBooleanExtra("isOk", false)) {
                getADIncome();
            } else {
                ToastUtil.showToast(getContext(), "领取失败");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mUpdateReceiver);
        ListenerManager.getInstance().removeChatMessageListener(this);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onItemClick(int position, RecyclerView.ViewHolder viewHolder) {
        final Friend friend = mFriendList.get(position);
        mAdapter.closeAllItems();
        if (mInputManager != null) {
            mInputManager.hideSoftInputFromWindow(findViewById(R.id.message_fragment).getWindowToken(), 0); // 强制隐藏键盘
        }
        Intent intent1 = new Intent();
        if (friend.getRoomFlag() == 0) { // 个人
            if (TextUtils.equals(friend.getUserId(), Constants.ID_SK_PAY)) {
                intent1.setClass(getActivity(), PayActivity.class);
            } else {
                intent1.setClass(getActivity(), ChatActivity.class);
                intent1.putExtra(ChatActivity.FRIEND, friend);
            }
        } else {
            Friend mFriend = FriendDao.getInstance().getFriend(mLoginUserId, friend.getUserId());
            if (mFriend == null) {
                ToastUtil.showToast(getContext(), getString(R.string.tip_program_error));
                return;
            }
//            else  if (1 == mFriend.getGroupStatus()) {
//                ToastUtil.showToast(getContext(), R.string.tip_been_kick);
//                return;
//            }
//            else if (2 == mFriend.getGroupStatus()) {
//                ToastUtil.showToast(getContext(), R.string.tip_disbanded);
//                return;
//            }
            else if (3 == mFriend.getGroupStatus()) {
                ToastUtil.showToast(getContext(), R.string.tip_group_disable_by_service);
                return;
            }
            intent1.setClass(getActivity(), MucChatActivity.class);
            intent1.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
            intent1.putExtra(AppConstant.EXTRA_NICK_NAME, friend.getNickName());
        }

        if (search) {
            intent1.putExtra("isserch", true);
            intent1.putExtra("jilu_id", friend.getChatRecordTimeOut());
        } else {
            intent1.putExtra(Constants.NEW_MSG_NUMBER, friend.getUnReadNum());
        }
        startActivity(intent1);
        clearMessageNum(friend, position);
    }

    @Override
    public void onQuickReplyClick(int position, RecyclerView.ViewHolder viewHolder) {
        final Friend friend = mFriendList.get(position);
        if (friend.getRoomFlag() != 0) {
            // 用户可能不在群组里，
            int status = friend.getGroupStatus();
            if (1 == status) {
                ToastUtil.showToast(getContext(), R.string.tip_been_kick);
                return;
            } else if (2 == status) {
                ToastUtil.showToast(getContext(), R.string.tip_disbanded);
                return;
            } else if (3 == status) {
                ToastUtil.showToast(getContext(), R.string.tip_group_disable_by_service);
                return;
            }
        }
        String name = !TextUtils.isEmpty(friend.getRemarkName()) ? friend.getRemarkName() : friend.getNickName();
        ImHelper.checkXmppAuthenticated();
        // TODO: hint是上一条消息，如果有草稿可能会是草稿，
        DialogHelper.verify(
                getContext(),
                getContext().getString(R.string.title_replay_place_holder, name),
                friend.getContent(),
                new QuickReplyDialog.VerifyClickListener() {
                    @Override
                    public void cancel() {

                    }

                    @Override
                    public void send(String str) {
                        if (TextUtils.isEmpty(str)) {
                            ToastUtil.showToast(getContext(), R.string.tip_replay_empty);
                            return;
                        }
                        ImHelper.checkXmppAuthenticated();
                        RoomMember member = RoomMemberDao.getInstance().getSingleRoomMember(friend.getRoomId(), coreManager.getSelf().getUserId());
                        // 判断禁言状态，
                        if (member != null && member.getRole() == 3) {// 普通成员需要判断是否被禁言
                            if (TimeUtils.isSilenceInGroup(friend)) {
                                ToastUtil.showToast(getContext(), InternationalizationHelper.getString("HAS_BEEN_BANNED"));
                                return;
                            }
                        } else if (member == null) {// 也需要判断是否被禁言
                            if (TimeUtils.isSilenceInGroup(friend)) {
                                ToastUtil.showToast(getContext(), InternationalizationHelper.getString("HAS_BEEN_BANNED"));
                                return;
                            }
                        }
                        ChatMessage message = new ChatMessage();
                        // 文本类型，抄自，
                        // 黑名单没考虑，正常情况黑名单会删除会话，
                        message.setType(Constants.TYPE_TEXT);
                        message.setFromUserId(coreManager.getSelf().getUserId());
                        message.setFromUserName(coreManager.getSelf().getNickName());
                        message.setContent(str);
                        // 获取阅后即焚状态(因为用户可能到聊天设置界面 开启/关闭 阅后即焚，所以在onResume时需要重新获取下状态)
                        int isReadDel = PreferenceUtils.getInt(getContext(), Constants.MESSAGE_READ_FIRE + friend.getUserId() + coreManager.getSelf().getUserId(), 0);
                        message.setIsReadDel(isReadDel);
                        if (1 != friend.getRoomFlag()) {
                            PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(getContext());
                            boolean isSupport = privacySetting.getMultipleDevices() == 1;
                            if (isSupport) {
                                message.setFromId(MyApplication.MULTI_RESOURCE);
                            } else {
                                message.setFromId("youjob");
                            }
                        }
                        if (1 == friend.getRoomFlag()) {
                            // 是群聊，
                            message.setToUserId(friend.getUserId());
                            if (friend.getChatRecordTimeOut() == -1 || friend.getChatRecordTimeOut() == 0) {// 永久
                                message.setDeleteTime(-1);
                            } else {
                                long deleteTime = TimeUtils.time_current_time() + (long) (friend.getChatRecordTimeOut() * 24 * 60 * 60);
                                message.setDeleteTime(-1);//没有消息过期时间了  所以发出的消息都是 -1
                            }
                        } else if (friend.getIsDevice() == 1) {
                            message.setToUserId(coreManager.getSelf().getUserId());
                            message.setToId(friend.getUserId());
                            // 我的设备消息不过期？
                        } else {
                            message.setToUserId(friend.getUserId());

                            // sz 消息过期时间
                            if (friend.getChatRecordTimeOut() == -1 || friend.getChatRecordTimeOut() == 0) {// 永久
                                message.setDeleteTime(-1);
                            } else {
                                long deleteTime = TimeUtils.time_current_time() + (long) (friend.getChatRecordTimeOut() * 24 * 60 * 60);
                                if (message.getIsReadDelByInt() > 0) {
                                    message.setDeleteTime(deleteTime);
                                } else {
                                    message.setDeleteTime(-1);//没有消息过期时间了  所以发出的消息都是 -1
                                }
                            }
                        }
                        PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(getContext());
                        boolean isEncrypt = privacySetting.getIsEncrypt() == 1;
                        if (isEncrypt) {
                            message.setIsEncrypt(1);
                        } else {
                            message.setIsEncrypt(0);
                        }
                        message.setReSendCount(ChatMessageDao.fillReCount(message.getType()));
                        message.setPacketId(ToolUtils.getUUID());
                        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
                        // 消息保存在数据库，
                        ChatMessageDao.getInstance().saveNewSingleChatMessage(message.getFromUserId(), message.getToUserId(), message);
                        for (Friend mFriend : mFriendList) {
                            if (mFriend.getUserId().equals(friend.getUserId())) {
                                if (1 == friend.getRoomFlag()) {
                                    ImHelper.sendMucChatMessage(message.getToUserId(), message);
                                    mFriend.setContent(message.getFromUserName() + ": " + message.getContent());
                                } else {
                                    ImHelper.sendChatMessage(message.getToUserId(), message);
                                    mFriend.setContent(message.getContent());
                                }
                                // 清除小红点，
                                clearMessageNum(friend, position);
                                mAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                });
    }

    @Override
    public void onAvatarClick(int position, RecyclerView.ViewHolder viewHolder) {
        final Friend friend = mFriendList.get(position);
        if (friend.getRoomFlag() == 0) {   // 个人
            if (!friend.getUserId().equals(Constants.ID_SYSTEM_MESSAGE)
                    && !friend.getUserId().equals(Constants.ID_NEW_FRIEND_MESSAGE)
                    && !friend.getUserId().equals(Constants.ID_SK_PAY)
                    && friend.getIsDevice() != 1) {
                Intent intent = new Intent(getContext(), BasicInfoActivity.class);
                intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
                startActivity(intent);
            }
        } else {   // 群组
            if (friend.getGroupStatus() == 0) {
                Intent intent = new Intent(getContext(), RoomInfoActivity.class);
                intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
                startActivity(intent);
            }
        }
    }

    @Override
    public void onItemTopClick(int position, RecyclerView.ViewHolder viewHolder) {
        final Friend friend = mFriendList.get(position);
        mAdapter.closeItem(position);
        if (friend.getTopTime() == 0) {
            FriendDao.getInstance().updateTopFriend(friend.getUserId(), friend.getTimeSend());
        } else {
            FriendDao.getInstance().resetTopFriend(friend.getUserId());
        }
        loadDatas();
    }

    @Override
    public void onItemDeleteClick(int position, RecyclerView.ViewHolder viewHolder) {
        final Friend friend = mFriendList.get(position);
        mAdapter.closeItem(position);
        String mLoginUserId = coreManager.getSelf().getUserId();
        if (friend.getRoomFlag() == 0) {
            // 如果是普通的人，从好友表中删除最后一条消息的记录，这样就不会查出来了
            FriendDao.getInstance().resetFriendMessage(mLoginUserId, friend.getUserId());
            // 消息表中删除
            ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, friend.getUserId());
            deleteServerThis(friend.getUserId());
        } else {
            // 从消息表删除
            ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, friend.getUserId());
            FriendDao.getInstance().resetFriendMessage(mLoginUserId, friend.getUserId());
            deleteServerThis(friend.getUserId());
        }
        if (friend.getUnReadNum() > 0) {
            MsgBroadcast.broadcastMsgNumUpdate(getActivity(), false, friend.getUnReadNum());
        }

        // 保留旧代码，
        // 内部和外部的mFriendList都要更新到，
        MessageFragment.this.mFriendList.remove(position);
        mAdapter.setData(MessageFragment.this.mFriendList, false, "");
    }

    private void deleteServerThis(String userId) {
        Map<String, String> par = new HashMap<>();
        par.put("access_token", coreManager.getSelfStatus().accessToken);
        par.put("jid", userId);
        HttpUtils.get().url(coreManager.getConfig().DELETE_MSG_LIST_ONE).params(par).build().execute(new BaseCallback<String>(String.class) {
            @Override
            public void onResponse(ObjectResult<String> result) {
                if (result != null && result.getResultCode() == 1) {
                    Log.i(TAG, "删除一个聊天会话：删除服务器的数据成功");
                } else {
                    Log.e(TAG, "删除一个聊天会话：删除服务器的数据失败");
                }
            }

            @Override
            public void onError(Call call, Exception e) {
                Log.e(TAG, "删除一个聊天会话：删除服务器的数据失败");
            }
        });
    }

    @Override
    public void onItemMarkReadClick(int position, RecyclerView.ViewHolder viewHolder) {
        final Friend friend = mFriendList.get(position);
        mAdapter.closeItem(position);
        if (friend.getUnReadNum() > 0) {
            clearMessageNum(friend, position);
        } else {
            FriendDao.getInstance().markUserMessageUnRead(mLoginUserId, friend.getUserId());
            MsgBroadcast.broadcastMsgNumUpdate(MyApplication.getInstance(), true, 1);
            MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
        }
    }
}
