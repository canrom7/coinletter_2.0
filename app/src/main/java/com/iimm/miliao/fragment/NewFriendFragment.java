package com.iimm.miliao.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.roamer.slidelistview.SlideListView;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.R;
import com.iimm.miliao.adapter.NewFriendAdapter;
import com.iimm.miliao.bean.AddAttentionResult;
import com.iimm.miliao.bean.AttentionUser;
import com.iimm.miliao.bean.EventMessage;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.bean.message.NewFriendMessage;
import com.iimm.miliao.broadcast.CardcastUiUpdateUtil;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.NewFriendDao;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.helper.FriendHelper;
import com.iimm.miliao.sortlist.SideBar;
import com.iimm.miliao.ui.contacts.TalkHistoryActivity;
import com.iimm.miliao.util.AppExecutors;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ToolUtils;
import com.iimm.miliao.view.PullToRefreshSlideListView;
import com.iimm.miliao.xmpp.ListenerManager;
import com.iimm.miliao.xmpp.listener.NewFriendListener;
import com.iimm.miliao.xmpp.util.ImHelper;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

public class NewFriendFragment extends SearchFragment implements NewFriendListener {
    private String mLoginUserId;
    private List<NewFriendMessage> mNewFriends = new ArrayList<>();
    private NewFriendAdapter mAdapter;
    private Handler mHandler = new Handler();
    private NewFriendAdapter.NewFriendActionListener mNewFriendActionListener = new NewFriendAdapter.NewFriendActionListener() {

        @Override
        public void addAttention(int position) {
            doAgreeOrAttention(position, 0);
        }

        @Override
        public void removeBalckList(int position) {
            removeBlacklist(position);
        }

        @Override
        public void agree(int position) {
            doAgreeOrAttention(position, 1);
        }

        @Override
        public void feedback(int position) {
            doFeedbackOrSayHello(position, 1);
        }
    };
    private PullToRefreshSlideListView mPRSlideListView;

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_new_friend;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        mLoginUserId = coreManager.getSelf().getUserId();
        View include = findViewById(R.id.include2);
        include.setBackgroundColor(getResources().getColor(R.color.white));
        EditText editText = findViewById(R.id.search_edit);
        editText.setHint(R.string.search_for_friends);
        TextView tvCancel = findViewById(R.id.tv_cancel);
        initSearchModule(editText, tvCancel);
        TextView tvDialog = findViewById(R.id.text_dialog);
        tvDialog.setVisibility(View.GONE);
        mRvSearchList = findViewById(R.id.rv_search_list);
        mRvSearchList.setVisibility(View.GONE);
        SideBar sideBar = findViewById(R.id.sidebar);
        sideBar.setVisibility(View.GONE);
        initView();
        ListenerManager.getInstance().addNewFriendListener(this);
        loadData();

        FriendDao.getInstance().markUserMessageRead(mLoginUserId, Constants.ID_NEW_FRIEND_MESSAGE);
        // 将所有消息都变为已读
        NewFriendDao.getInstance().markNewFriendRead(mLoginUserId);

        NewFriendDao.getInstance().resetAllNewFriendUnRead(mLoginUserId);
    }

    private void initView() {
        mPRSlideListView = (PullToRefreshSlideListView) findViewById(R.id.pull_refresh_list);
        mPRSlideListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mAdapter = new NewFriendAdapter(getContext(), coreManager.getSelf().getUserId(), mNewFriends, mNewFriendActionListener);
        mPRSlideListView.getRefreshableView().setAdapter(mAdapter);
        mPRSlideListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<SlideListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<SlideListView> refreshView) {
                loadData();
            }
        });
        mPRSlideListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                NewFriendMessage mNewFriendMessage = mNewFriends.get(position - 1);
//                goTalkHistoryActivity(mNewFriendMessage);
            }
        });
    }

    @Override
    public void switchMode(int modeSearch) {
        currentMode = modeSearch;
        if (currentMode == MODE_SEARCH) {
            //搜索模式
            mPRSlideListView.setVisibility(View.GONE);
            mRvSearchList.setVisibility(View.VISIBLE);
        } else {
            //数据模式
            mPRSlideListView.setVisibility(View.VISIBLE);
            mRvSearchList.setVisibility(View.GONE);
        }
    }


    @Override
    protected void tvCancelOnClick() {

    }

    @Override
    protected void loadData() {
        AppExecutors.getInstance().networkIO().execute(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                final List<NewFriendMessage> friends = NewFriendDao.getInstance().getAllNewFriendMsg(mLoginUserId);
                // 保证至少200ms的刷新过程
                long delayTime = 200 - (startTime - System.currentTimeMillis());
                if (delayTime < 0) {
                    delayTime = 0;
                }
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mNewFriends.clear();
                        if (friends != null && friends.size() > 0) {
                            mNewFriends.addAll(friends);
                        }
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                        if (mPRSlideListView != null) {
                            mPRSlideListView.onRefreshComplete();
                        }
                    }
                }, delayTime);
            }
        });
    }

    @Override
    protected void searchEditFocusChange(boolean hasFocus) {

    }


    private void goTalkHistoryActivity(NewFriendMessage mNewFriendMessage) {
        Intent intent = new Intent(getActivity(), TalkHistoryActivity.class);
        intent.putExtra(AppConstant.EXTRA_FRIEND, mNewFriendMessage.getUserId());
        startActivity(intent);
    }

    /**
     * 加关注或者同意别人的加好友
     *
     * @param position 1、同意加好友
     */
    private void doAgreeOrAttention(final int position, final int type) {
        final NewFriendMessage friend = mNewFriends.get(position);
        DialogHelper.showDefaulteMessageProgressDialog(getActivity());
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("toUserId", friend.getUserId());
        HttpUtils.get().url(coreManager.getConfig().ADD_FRIENDS)
                .params(params)
                .build()
                .execute(new BaseCallback<AddAttentionResult>(AddAttentionResult.class) {
                    @Override
                    public void onResponse(ObjectResult<AddAttentionResult> result) {
                        DialogHelper.dismissProgressDialog();
                        int toastResId = type == 0 ? R.string.add_friend_succ : R.string.agreed;
                        ToastUtil.showToast(getContext(), toastResId);

                        // int messageType = type == 0 ? Constants.TYPE_FRIEND : Constants.TYPE_PASS;
                        int messageType = Constants.TYPE_PASS;
                        NewFriendMessage message = NewFriendMessage.createWillSendMessage(coreManager.getSelf(),
                                messageType, null, friend);
                        ImHelper.sendNewFriendMessage(friend.getUserId(), message);
                        NewFriendDao.getInstance().ascensionNewFriend(message, Constants.STATUS_FRIEND);
                        FriendHelper.addFriendExtraOperation(mLoginUserId, friend.getUserId());

                        mNewFriends.set(position, message);
                        mAdapter.notifyDataSetChanged();

                        NewFriendDao.getInstance().changeNewFriendState(friend.getUserId(), Constants.STATUS_12);
                        ListenerManager.getInstance().notifyNewFriend(mLoginUserId, friend, true);
                        // 刷新通讯录
                        CardcastUiUpdateUtil.broadcastUpdateUi(getContext());
                        ImHelper.syncMyFriendToOtherMachine();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(getContext());
                    }
                });
    }

    /**
     * @param position
     * @param type     0打招呼<br/>
     *                 1回话<br/>
     */
    public void doFeedbackOrSayHello(final int position, final int type) {
        int titleResId = type == 0 ? R.string.say_hello_dialog_title : R.string.feedback;
        String hint;
        if (type == 0) {
            hint = getString(R.string.say_hello_dialog_hint);
        } else {
            hint = InternationalizationHelper.getString("JX_Talk");
        }
        DialogHelper.showLimitSingleInputDialog(getActivity(), getString(titleResId), hint, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String text = ((EditText) v).getText().toString().trim();
                doFeedbackOrSayHello(position, type, text);
            }
        });
    }

    public void doFeedbackOrSayHello(int position, int type, String text) {
        if (TextUtils.isEmpty(text)) {
            text = getString(R.string.say_hello_default);
        }
        NewFriendMessage friend = mNewFriends.get(position);
        int messageType = type == 0 ? Constants.TYPE_SAYHELLO : Constants.TYPE_FEEDBACK;
        NewFriendMessage message = NewFriendMessage.createWillSendMessage(coreManager.getSelf(), messageType, text, friend);
        NewFriendDao.getInstance().createOrUpdateNewFriend(message);
        if (friend.getState() == Constants.STATUS_11 || friend.getState() == Constants.STATUS_15) {
            NewFriendDao.getInstance().changeNewFriendState(friend.getUserId(), Constants.STATUS_15);
        } else {
            NewFriendDao.getInstance().changeNewFriendState(friend.getUserId(), Constants.STATUS_14);
        }
        NewFriendDao.getInstance().updateNewFriendContent(friend.getUserId(), text);

        ImHelper.sendNewFriendMessage(friend.getUserId(), message);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(Constants.TYPE_TEXT); //文本类型
        chatMessage.setFromUserId(mLoginUserId);
        chatMessage.setFromUserName(coreManager.getSelf().getNickName());
        chatMessage.setToUserId(friend.getUserId());
        chatMessage.setContent(text);
        chatMessage.setMessageState(Constants.MESSAGE_SEND_SUCCESS);
        chatMessage.setMySend(true);
        chatMessage.setPacketId(ToolUtils.getUUID());
        chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
        ChatMessageDao.getInstance().saveNewSingleAnswerMessage(mLoginUserId, friend.getUserId(), chatMessage);
/*
        ListenerManager.getInstance().notifyNewFriend(mLoginUserId, friend, true);
*/
        ToastUtil.showToast(getContext(), R.string.feedback_succ);
        loadData();
        //发送回话的消息
        mAdapter.notifyDataSetChanged();
/*
        // 通知主界面更新UI
        EventBus.getDefault().post(new MessageEventHongdian(123));
*/
    }

    private void removeBlacklist(final int position) {
        final NewFriendMessage friend = mNewFriends.get(position);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("toUserId", friend.getUserId());
        DialogHelper.showDefaulteMessageProgressDialog(getActivity());

        HttpUtils.get().url(coreManager.getConfig().FRIENDS_BLACKLIST_DELETE)
                .params(params)
                .build()
                .execute(new BaseCallback<AttentionUser>(AttentionUser.class) {

                    @Override
                    public void onResponse(ObjectResult<AttentionUser> result) {
                        DialogHelper.dismissProgressDialog();
                        int currentStatus = Constants.STATUS_UNKNOW;
                        if (result.getData() != null) {
                            currentStatus = result.getData().getStatus();
                        }
                        FriendDao.getInstance().updateFriendStatus(friend.getOwnerId(), friend.getUserId(), currentStatus);

                        NewFriendMessage message = null;
                        switch (currentStatus) {
                            case Constants.STATUS_ATTENTION:
                                message = NewFriendMessage.createWillSendMessage(coreManager.getSelf(), Constants.TYPE_NEWSEE,
                                        null, friend);
                                ImHelper.sendNewFriendMessage(friend.getUserId(), message);
                                FriendHelper.addAttentionExtraOperation(friend.getOwnerId(), friend.getUserId());
                                break;
                            case Constants.STATUS_FRIEND:
                                message = NewFriendMessage.createWillSendMessage(coreManager.getSelf(), Constants.TYPE_FRIEND,
                                        null, friend);
                                ImHelper.sendNewFriendMessage(friend.getUserId(), message);
                                FriendHelper.addFriendExtraOperation(friend.getOwnerId(), friend.getUserId());
/*
                                EventBus.getDefault().post(new MessageEventHongdian(123));
*/
                                break;
                            default:// 其他，理论上不可能
                                break;
                        }
                        ToastUtil.showToast(getActivity(), R.string.remove_blacklist_succ);
                        mNewFriends.set(position, message);
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(getActivity());
                    }
                });
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

    }

    @Override
    public void onNewFriendSendStateChange(String toUserId, NewFriendMessage message, int messageState) {
    }

    @Override
    public boolean onNewFriend(NewFriendMessage message) {
        loadData();
        return false;
    }

    public void visibleChange(boolean isVisible) {
        if (isVisible) {
            loadData();
            FriendDao.getInstance().markUserMessageRead(mLoginUserId, Constants.ID_NEW_FRIEND_MESSAGE);
            // 将所有消息都变为已读
            NewFriendDao.getInstance().markNewFriendRead(mLoginUserId);

            NewFriendDao.getInstance().resetAllNewFriendUnRead(mLoginUserId);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().post(new EventMessage());
    }
}
