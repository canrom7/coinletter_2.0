package com.iimm.miliao.ui.contacts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.roamer.slidelistview.SlideBaseAdapter;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.bean.AttentionUser;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.broadcast.CardcastUiUpdateUtil;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.sortlist.BaseComparator;
import com.iimm.miliao.sortlist.BaseSortModel;
import com.iimm.miliao.sortlist.SideBar;
import com.iimm.miliao.sortlist.SortHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.other.BasicInfoActivity;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ViewHolder;
import com.iimm.miliao.view.PullToRefreshSlideListView;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 黑名单列表
 */
public class BlackActivity extends BaseActivity {
    private PullToRefreshSlideListView mPullToRefreshListView;
    private BlackAdapter mAdapter;
    private List<BaseSortModel<Friend>> mSortFriends;
    private BaseComparator<Friend> mBaseComparator;
    private SideBar mSideBar;
    private TextView mTextDialog;
    private String mLoginUserId;
    private EditText mEditText;

    private boolean isSearch;
    private List<BaseSortModel<Friend>> mSearchSortFriends;

    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CardcastUiUpdateUtil.ACTION_UPDATE_UI)) {
                loadData();
                mAdapter.setData(mSortFriends);
            }
        }
    };

    public BlackActivity() {
        mSortFriends = new ArrayList<BaseSortModel<Friend>>();
        mBaseComparator = new BaseComparator<Friend>();
        mSearchSortFriends = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black);
        mLoginUserId = coreManager.getSelf().getUserId();
        initActionBar();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUpdateReceiver);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText(R.string.black_list);
    }

    private void initView() {

        EditText mEditText = findViewById(R.id.search_et);
        //mEditText.setHint(InternationalizationHelper.getString("JX_Seach"));
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                isSearch = true;
                mSearchSortFriends.clear();
                String str = mEditText.getText().toString();
                if (TextUtils.isEmpty(str)) {
                    isSearch = false;
                    mAdapter.setData(mSortFriends);
                    return;
                }
                for (int i = 0; i < mSortFriends.size(); i++) {
                    String name = !TextUtils.isEmpty(mSortFriends.get(i).getBean().getRemarkName()) ?
                            mSortFriends.get(i).getBean().getRemarkName() : mSortFriends.get(i).getBean().getNickName();
                    if (name.contains(str)) {
                        // 符合搜索条件的好友
                        mSearchSortFriends.add((mSortFriends.get(i)));
                    }
                }
                mAdapter.setData(mSearchSortFriends);
            }
        });


        mPullToRefreshListView = (PullToRefreshSlideListView) findViewById(R.id.pull_refresh_list);
        mPullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Friend friend;

                if (isSearch) {
                    friend = mSearchSortFriends.get(position).bean;
                } else {
                    friend = mSortFriends.get(position).getBean();
                }

                if (friend != null) {
                    Intent intent = new Intent(BlackActivity.this, BasicInfoActivity.class);
                    intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
                    startActivity(intent);
                }
            }
        });

        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mTextDialog = (TextView) findViewById(R.id.text_dialog);
        mSideBar.setTextView(mTextDialog);
//        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
//            @Override
//            public void onTouchingLetterChanged(String s) {
//                // 该字母首次出现的位置
//                int position = mAdapter.getPositionForSection(s.charAt(0));
//                if (position != -1) {
//                    mPullToRefreshListView.setSelection(position);
//                }
//            }
//        });

        mAdapter = new BlackAdapter(this, mSortFriends);
        mPullToRefreshListView.setAdapter(mAdapter);
        getBlackList();

        registerReceiver(mUpdateReceiver, CardcastUiUpdateUtil.getUpdateActionFilter());
    }

    private void loadData() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        AsyncUtils.doAsync(this, e -> {
            Reporter.post("加载数据失败，", e);
            AsyncUtils.runOnUiThread(this, ctx -> {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showToast(ctx, R.string.data_exception);
            });
        }, c -> {
            final List<Friend> friends = FriendDao.getInstance().getAllBlacklists(mLoginUserId);
            Map<String, Integer> existMap = new HashMap<>();
            List<BaseSortModel<Friend>> sortedList = SortHelper.toSortedModelList(friends, existMap, Friend::getShowName);
            c.uiThread(r -> {
                DialogHelper.dismissProgressDialog();
                mSideBar.setExistMap(existMap);
                mSortFriends = sortedList;
                mAdapter.setData(sortedList);
                if (friends.size() == 0) {
                    findViewById(R.id.fl_empty).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.fl_empty).setVisibility(View.GONE);
                }
            });
        });
    }

    /**
     * 获取黑名单列表
     */
    private void getBlackList() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);

        HttpUtils.get().url(coreManager.getConfig().FRIENDS_BLACK_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<AttentionUser>(AttentionUser.class) {
                    @Override
                    public void onResponse(ArrayResult<AttentionUser> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            List<AttentionUser> attentionUsers = result.getData();
                            if (attentionUsers != null && attentionUsers.size() > 0) {
                                for (int i = 0; i < attentionUsers.size(); i++) {
                                    AttentionUser attentionUser = attentionUsers.get(i);
                                    if (attentionUser == null) {
                                        continue;
                                    }
                                    String userId = attentionUser.getToUserId();// 好友的Id
                                    Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, userId);
                                    if (friend == null) {
                                        friend = new Friend();
                                        friend.setOwnerId(attentionUser.getUserId());
                                        friend.setUserId(attentionUser.getToUserId());
                                        friend.setNickName(attentionUser.getToNickName());
                                        friend.setRemarkName(attentionUser.getRemarkName());
                                        friend.setTimeCreate(attentionUser.getCreateTime());
                                        friend.setStatus(Constants.STATUS_BLACKLIST);
                                        friend.setOfflineNoPushMsg(attentionUser.getOfflineNoPushMsg());
                                        friend.setChatRecordTimeOut(attentionUser.getChatRecordTimeOut());// 消息保存天数 -1/0 永久
                                        friend.setCompanyId(attentionUser.getCompanyId());
                                        friend.setRoomFlag(0);
                                        FriendDao.getInstance().createOrUpdateFriend(friend);
                                    } else {
                                        FriendDao.getInstance().updateFriendStatus(mLoginUserId, userId, Constants.STATUS_BLACKLIST);
                                    }
                                }
                            }
                            loadData();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    //////////////其他操作///////////////////
   /* private CardcastActivity mActivity;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (CardcastActivity) getActivity();
    }*/

   /* private BaseSortModel<Friend> mmsortFriend = null;
    private NewFriendMessage mmessage = null;
    private String deletepacketid = null;

    private void showLongClickOperationDialog(final BaseSortModel<Friend> sortFriend) {
        Friend friend = sortFriend.getBean();
        if (friend.getStatus() != Constants.STATUS_BLACKLIST && friend.getStatus() == Constants.STATUS_ATTENTION
                && friend.getStatus() == Constants.STATUS_FRIEND) {
            return;
        }
        CharSequence[] items = new CharSequence[3];
        items[0] = getString(R.string.set_remark_name);// 设置备注名
        if (friend.getStatus() == Constants.STATUS_BLACKLIST) {// 在黑名单中,显示“设置备注名”、“移除黑名单”,"取消关注"，“彻底删除”
            items[1] = getString(R.string.remove_blacklist);
        } else {
            // items[1] = getString(R.string.add_blacklist);
        }
        // items[2] = getString(R.string.cancel_attention);
        items[2] = getString(R.string.delete_all);

        new AlertDialog.Builder(getActivity()).setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:// 设置备注名
                        showRemarkDialog(sortFriend);
                        break;
                    case 1:// 加入黑名单，或者移除黑名单
                        showBlacklistDialog(sortFriend);
                        break;
                    case 2:// 解除关注关系或者解除好友关系
                        showDeleteAllDialog(sortFriend);
                        break;
                }
            }
        }).setCancelable(true).create().show();
    }

    *//**
     * 设置备注名称
     *//*
    private void showRemarkDialog(final BaseSortModel<Friend> sortFriend) {
        DialogHelper.showSingleInputDialog(getActivity(), getString(R.string.set_remark_name), sortFriend.getBean().getShowName(), 2, 2, new InputFilter[]{new InputFilter.LengthFilter(20)}, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = ((EditText) v).getText().toString().trim();
                if (input.equals(sortFriend.getBean().getShowName())) {// 备注名没变
                    return;
                }
                *//*if (!StringUtils.isNickName(input)) {// 不符合昵称
                    if (input.length() != 0) {
                        ToastUtil.showToast(getActivity(), R.string.remark_name_format_error);
                        return;
                    } else {// 不符合昵称，因为长度为0，但是可以做备注名操作，操作就是清除备注名
                        // 判断之前有没有备注名
                        if (TextUtils.isEmpty(sortFriend.getBean().getRemarkName())) {// 如果没有备注名，就不需要清除
                            return;
                        }
                    }
                }*//*
                remarkFriend(sortFriend, input);
            }
        });
    }

    private void remarkFriend(final BaseSortModel<Friend> sortFriend, final String remarkName) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("toUserId", sortFriend.getBean().getUserId());
        params.put("remarkName", remarkName);
        DialogHelper.showDefaulteMessageProgressDialog(getActivity());

        HttpUtils.get().url(coreManager.getConfig().FRIENDS_REMARK)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        String firstLetter = sortFriend.getFirstLetter();
                        mSideBar.removeExist(firstLetter);// 移除之前设置的首字母
                        sortFriend.getBean().setRemarkName(remarkName);// 修改备注名称
                        setSortCondition(sortFriend);
                        Collections.sort(mSortFriends, mBaseComparator);
                        mAdapter.notifyDataSetChanged();
                        // 更新到数据库
                        FriendDao.getInstance().setRemarkName(mLoginUserId, sortFriend.getBean().getUserId(), remarkName);
                        // 更新消息界面（因为昵称变了，所有要更新）
                        MsgBroadcast.broadcastMsgUiUpdate(getActivity());
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(getActivity());
                    }
                });
    }

    */

    /**
     * 移除黑名单
     */
    private void showBlacklistDialog(final BaseSortModel<Friend> sortFriend) {
        removeBlacklist(sortFriend);
    }

    private void removeBlacklist(final BaseSortModel<Friend> sortFriend) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("toUserId", sortFriend.getBean().getUserId());
        DialogHelper.showDefaulteMessageProgressDialog(BlackActivity.this);

        HttpUtils.get().url(coreManager.getConfig().FRIENDS_BLACKLIST_DELETE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(BlackActivity.this, "移出黑名单成功");
                        FriendDao.getInstance().updateFriendStatus(sortFriend.getBean().getOwnerId(), sortFriend.getBean().getUserId(), Constants.STATUS_FRIEND);

                        ChatMessage removeChatMessage = new ChatMessage();
                        removeChatMessage.setContent(coreManager.getSelf().getNickName() + InternationalizationHelper.getString("REMOVE"));
                        removeChatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
                        FriendDao.getInstance().updateLastChatMessage(mLoginUserId, Constants.ID_NEW_FRIEND_MESSAGE, removeChatMessage);

                        mSortFriends.remove(sortFriend);
                        String firstLetter = sortFriend.getFirstLetter();
                        mSideBar.removeExist(firstLetter);// 移除之前设置的首字母
                        mAdapter.notifyDataSetInvalidated();

                        CardcastUiUpdateUtil.broadcastUpdateUi(BlackActivity.this);
                        MsgBroadcast.broadcastMsgUiUpdate(BlackActivity.this);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(BlackActivity.this);
                    }
                });
    }

    /**
     * 彻底删除
     *//*
    private void showDeleteAllDialog(final BaseSortModel<Friend> sortFriend) {
        if (sortFriend.getBean().getStatus() == Constants.STATUS_UNKNOW) {
            return;
        }
        deleteFriend(sortFriend, 1);
    }

    private void deleteFriend(final BaseSortModel<Friend> sortFriend, final int type) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("toUserId", sortFriend.getBean().getUserId());
        String url = mActivity.coreManager.getConfig().FRIENDS_DELETE;
        DialogHelper.showDefaulteMessageProgressDialog(getActivity());

        HttpUtils.get().url(url)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(getActivity(), R.string.delete_all_succ);
                        NewFriendMessage message = NewFriendMessage.createWillSendMessage(coreManager.getSelf(),
                                Constants.TYPE_DELALL, null, sortFriend.getBean());

                        mActivity.sendNewFriendMessage(sortFriend.getBean().getUserId(), message);
                        deletepacketid = message.getPacketId();
                        mmessage = message;
                        mmsortFriend = sortFriend;
                        FriendHelper.removeAttentionOrFriend(mLoginUserId, mmsortFriend.getBean().getUserId());

                        mSortFriends.remove(mmsortFriend);
                        String firstLetter = mmsortFriend.getFirstLetter();
                        // 移除之前设置的首字母
                        mSideBar.removeExist(firstLetter);
                        mAdapter.notifyDataSetInvalidated();

                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.setContent(InternationalizationHelper.getString("JXAlert_DeleteFirend") + " " + mmsortFriend.getBean().getNickName());
                        chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
                        FriendDao.getInstance().updateLastChatMessage(mLoginUserId, Constants.ID_NEW_FRIEND_MESSAGE, chatMessage);

                        NewFriendDao.getInstance().createOrUpdateNewFriend(message);
                        NewFriendDao.getInstance().changeNewFriendState(mmsortFriend.getBean().getUserId(), Constants.STATUS_16);
                        ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mmessage, true);

                        MsgBroadcast.broadcastMsgUiUpdate(getActivity());
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(getActivity());
                    }
                });
    }*/


    class BlackAdapter extends SlideBaseAdapter {

        private List<BaseSortModel<Friend>> mSortFriends;

        public BlackAdapter(Context context, List<BaseSortModel<Friend>> sortFriends) {
            super(context);
            mContext = context;
            mSortFriends = sortFriends;

        }

        public void setData(List<BaseSortModel<Friend>> sortFriends) {
            mSortFriends = sortFriends;
            notifyDataSetChanged();
        }

        @Override
        public int getFrontViewId(int position) {
            return R.layout.row_sort_friend;
        }

        @Override
        public int getLeftBackViewId(int position) {
            return 0;
        }

        @Override
        public int getRightBackViewId(int position) {
            return R.layout.row_item_delete_black;
        }

        @Override
        public int getCount() {
            if (mSortFriends != null) {
                return mSortFriends.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mSortFriends != null) {
                return mSortFriends.get(position);
            }
            return null;
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

            View catagory_v = ViewHolder.get(convertView, R.id.catagory_v);
            TextView catagoryTitleTv = ViewHolder.get(convertView, R.id.catagory_title);
            TextView delete_tv = ViewHolder.get(convertView, R.id.delete_tv);
            TextView nick_name_tv = ViewHolder.get(convertView, R.id.nick_name_tv);
            ImageView avatar_img = ViewHolder.get(convertView, R.id.avatar_img);


            final BaseSortModel<Friend> sortFriend = mSortFriends.get(position);
            final Friend friend = sortFriend.getBean();

            // 根据position获取分类的首字母的Char ascii值
            int section = getSectionForPosition(position);
            // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
            if (position == getPositionForSection(section)) {
                catagoryTitleTv.setVisibility(View.VISIBLE);
                catagoryTitleTv.setText(mSortFriends.get(position).getFirstLetter());
                catagory_v.setVisibility(View.VISIBLE);
            } else {
                catagoryTitleTv.setVisibility(View.GONE);
                catagory_v.setVisibility(View.GONE);
            }

            if (avatar_img instanceof RoundedImageView) {
                if (Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR) {
                    ((RoundedImageView) avatar_img).setOval(true);
                } else {
                    ((RoundedImageView) avatar_img).setOval(false);
                    ((RoundedImageView) avatar_img).setCornerRadius(Constants.AVATAR_CORNER_RADIUS);
                }
            }
            avatar_img.setVisibility(View.VISIBLE);
            AvatarHelper.getInstance().displayAvatar(friend.getUserId(), avatar_img, true);

            nick_name_tv.setText(!TextUtils.isEmpty(friend.getRemarkName()) ? friend.getRemarkName() : friend.getNickName());

            delete_tv.setOnClickListener(v -> showBlacklistDialog(sortFriend));


            return convertView;
        }


        /**
         * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
         */
        public int getPositionForSection(int section) {
            for (int i = 0; i < getCount(); i++) {
                String sortStr = mSortFriends.get(i).getFirstLetter();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * 根据ListView的当前位置获取分类的首字母的Char ascii值
         */
        public int getSectionForPosition(int position) {
            return mSortFriends.get(position).getFirstLetter().charAt(0);
        }
    }

}
