package com.iimm.miliao.ui.message.multi;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.makeramen.roundedimageview.RoundedImageView;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.bean.message.MucRoomMember;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.sortlist.BaseComparator;
import com.iimm.miliao.sortlist.BaseSortModel;
import com.iimm.miliao.sortlist.SideBar;
import com.iimm.miliao.sortlist.SortHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.AppExecutors;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.DisplayUtil;
import com.iimm.miliao.util.JsonUtils;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ToolUtils;
import com.iimm.miliao.util.UiUtils;
import com.iimm.miliao.util.ViewHolder;
import com.iimm.miliao.view.HorizontalListView;
import com.iimm.miliao.view.VerifyDialog;
import com.iimm.miliao.xmpp.ListenerManager;
import com.iimm.miliao.xmpp.util.ImHelper;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 群组邀请好友
 */
public class AddContactsActivity extends BaseActivity {
    private EditText mEditText;
    private boolean isSearch;
    private SideBar mSideBar;
    private TextView mTextDialog;
    private ListView mListView;
    private ListViewAdapter mAdapter;
    private List<BaseSortModel<Friend>> mSortFriends;
    private List<BaseSortModel<Friend>> mSearchSortFriends;
    private BaseComparator<Friend> mBaseComparator;
    private HorizontalListView mHorizontalListView;
    private HorListViewAdapter mHorAdapter;
    private List<Friend> mFriendSearch;
    private List<String> mSelectPositions;
    private Button mOkBtn;
    private String mLoginUserId;
    private String mRoomId;
    private String mRoomJid;
    private String mRoomDes;
    private String mRoomName;
    private List<String> mExistIds;
    // Todo 邀请成员 是否开启群主验证 2018.5.16
    private String mCreator;
    private VerifyDialog verifyDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contacts);
        if (getIntent() != null) {
            mRoomId = getIntent().getStringExtra("roomId");
            mRoomJid = getIntent().getStringExtra("roomJid");
            mRoomDes = getIntent().getStringExtra("roomDes");
            mRoomName = getIntent().getStringExtra("roomName");
            String ids = getIntent().getStringExtra("exist_ids");
//            mExistIds = JSON.parseObject(ids, new TypeReference<Set<String>>() {
//            }.getType());
            mCreator = getIntent().getStringExtra("roomCreator");
        }
        mLoginUserId = coreManager.getSelf().getUserId();
        mSortFriends = new ArrayList<>();
        mSearchSortFriends = new ArrayList<>();
        mBaseComparator = new BaseComparator<>();
        mAdapter = new ListViewAdapter();
        mSelectPositions = new ArrayList<>();
        mHorAdapter = new HorListViewAdapter();
        initActionBar();
        initView();
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
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.list_view);
        mHorizontalListView = (HorizontalListView) findViewById(R.id.horizontal_list_view);
        mListView.setAdapter(mAdapter);
        mHorizontalListView.setAdapter(mHorAdapter);
        mOkBtn = (Button) findViewById(R.id.ok_btn);
        // mOkBtn.setBackgroundColor(SkinUtils.getSkin(this).getAccentColor());
        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mSideBar.setVisibility(View.VISIBLE);
        mTextDialog = (TextView) findViewById(R.id.text_dialog);
        mSideBar.setTextView(mTextDialog);
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = mAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListView.setSelection(position);
                }
            }
        });
        /**
         * 群内邀请好友搜索功能
         */
        mEditText = (EditText) findViewById(R.id.search_et);
        mEditText.setHint(InternationalizationHelper.getString("JX_Seach"));
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
                    /*String name = !TextUtils.isEmpty(mSortFriends.get(i).getBean().getRemarkName()) ?
                            mSortFriends.get(i).getBean().getRemarkName() : mSortFriends.get(i).getBean().getNickName();*/
                    String name = mSortFriends.get(i).getBean().getRemarkName() + mSortFriends.get(i).getBean().getNickName();
                    if (name.contains(str)) {
                        // 符合搜索条件的好友
                        mSearchSortFriends.add((mSortFriends.get(i)));
                    }
                }
                mAdapter.setData(mSearchSortFriends);
            }
        });
        mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size() + ""));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Friend friend;
                if (isSearch) {
                    friend = mSearchSortFriends.get(position).bean;
                } else {
                    friend = mSortFriends.get(position).bean;
                }
                for (int i = 0; i < mSortFriends.size(); i++) {
                    if (mSortFriends.get(i).getBean().getUserId().equals(friend.getUserId())) {
                        if (friend.getStatus() != 100) {
                            friend.setStatus(100);
                            mSortFriends.get(i).getBean().setStatus(100);
                            addSelect(friend.getUserId());
                        } else {
                            friend.setStatus(101);
                            mSortFriends.get(i).getBean().setStatus(101);
                            removeSelect(friend.getUserId());
                        }

                        if (isSearch) {
                            mAdapter.setData(mSearchSortFriends);
                        } else {
                            mAdapter.setData(mSortFriends);
                        }
                    }
                }
            }
        });
        mHorizontalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                for (int i = 0; i < mSortFriends.size(); i++) {
                    if (mSortFriends.get(i).getBean().getUserId().equals(mSelectPositions.get(position))) {
                        mSortFriends.get(i).getBean().setStatus(101);
                        mAdapter.setData(mSortFriends);
                    }
                }
                mSelectPositions.remove(position);
                mHorAdapter.notifyDataSetInvalidated();
                mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size() + ""));
            }
        });
        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UiUtils.isNormalClick(v)) {
                    return;
                }
                if (mSelectPositions.size() <= 0) {
                    ToastUtil.showToast(AddContactsActivity.this, R.string.tip_select_at_lease_one_member);
                    return;
                }
                List<String> inviteIdList = new ArrayList<>();
                List<String> inviteNameList = new ArrayList<>();
                boolean isEmity = true;
                for (String fid : mSelectPositions) {
                    Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, fid);
                    if (friend != null) {
                        inviteIdList.add(fid);
                        inviteNameList.add(friend.getNickName());
                        isEmity = false;
                    }
                }
                if (isEmity) {
                    return;
                }
                // 因为ios不要这样格式["10004541","10007042"]的字符串,，为了兼容他们，我们需要另外拼接一下
                String ids = JSON.toJSONString(inviteIdList); // ["10004541","10007042"]
                String names = JSON.toJSONString(inviteNameList); // ["haha","ccc"]
                final String ios_ids = ids.substring(1, ids.length() - 1).replace("\"", ""); // 10004541,10007042
                final String ios_name = names.substring(1, names.length() - 1).replace("\"", ""); // haha,ccc
                boolean isNeedOwnerAllowInviteFriend = PreferenceUtils.getBoolean(mContext, Constants.IS_NEED_OWNER_ALLOW_NORMAL_INVITE_FRIEND + mRoomJid, false);
                if (isNeedOwnerAllowInviteFriend) {// 群主开启了'群聊邀请确认'功能(需要群主确认进群)
                    if (mLoginUserId.equals(mCreator)) {// 我为群主，直接邀请
                        inviteFriend(ids);
                    } else {
                        verifyDialog = new VerifyDialog(AddContactsActivity.this);
                        verifyDialog.setVerifyClickListener("", new VerifyDialog.VerifyClickListener() {
                            @Override
                            public void cancel() {

                            }

                            @Override
                            public void send(String str) {
                                // 给群主发送一条单聊消息
                                ChatMessage message = new ChatMessage();
                                message.setType(Constants.TYPE_GROUP_VERIFY);
                                message.setFromUserId(mLoginUserId);
                                message.setToUserId(mCreator);
                                message.setFromUserName(coreManager.getSelf().getNickName());
                                message.setIsEncrypt(0);
                                String s = JsonUtils.initJsonContent(ios_ids, ios_name, mRoomJid, "0", str);
                                message.setObjectId(s);
                                message.setDoubleTimeSend(TimeUtils.time_current_time_double());
                                message.setPacketId(ToolUtils.getUUID());
                                ImHelper.sendChatMessage(mCreator, message);
                                ChatMessage cm = message.clone(false);
                                cm.setType(Constants.TYPE_TIP);
                                cm.setContent(getString(R.string.tip_send_reason_success));
                                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, mRoomJid, cm)) {
                                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, mRoomJid, cm, true);
                                }
                                finish();
                            }
                        });
                        verifyDialog.show();
                    }
                } else {// 直接邀请
                    inviteFriend(ids);
                }
            }
        });
        loData();
    }

    private void loData() {
        DialogHelper.showDefaulteMessageProgressDialog(AddContactsActivity.this);
        Map<String, String> params = new ArrayMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoomId);
        params.put("joinTime", String.valueOf(0));
        params.put("pageSize", "20000");
        HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<MucRoomMember>(MucRoomMember.class) {
                    @Override
                    public void onResponse(ArrayResult<MucRoomMember> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            mExistIds = new ArrayList<>();
                            List<MucRoomMember> mucRoomMemberList = result.getData();
                            for (int i = 0; i < mucRoomMemberList.size(); i++) {
                                mExistIds.add(mucRoomMemberList.get(i).getUserId());
                            }
                            loadData();
                        } else {
                            ToastUtil.showToast(AddContactsActivity.this, "请求失败请重试");
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(getApplicationContext());
                    }
                });
    }

    private void loadData() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<Friend> friends = FriendDao.getInstance().getAllFriends(mLoginUserId);
                Map<String, Integer> existMap = new HashMap<>();
                List<BaseSortModel<Friend>> sortedList = SortHelper.toSortedModelList(friends, existMap, f -> {
                    if (isExist(f)) {
                        // 过滤掉已经在群里的，
                        return null;
                    } else {
                        return f.getShowName();
                    }
                });
                MyApplication.applicationHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        DialogHelper.dismissProgressDialog();
                        mSideBar.setExistMap(existMap);
                        mSortFriends = sortedList;
                        mAdapter.setData(sortedList);
                    }
                });
            }
        });
    }

    /**
     * 是否存在已经在那个房间的好友
     */
    private boolean isExist(Friend friend) {
        return mExistIds.contains(friend.getUserId());
    }

    private void addSelect(String userId) {
        mSelectPositions.add(userId);
        mHorAdapter.notifyDataSetInvalidated();
        mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size() + ""));
    }

    private void removeSelect(String userId) {
        for (int i = 0; i < mSelectPositions.size(); i++) {
            if (mSelectPositions.get(i).equals(userId)) {
                mSelectPositions.remove(i);
            }
        }
        mHorAdapter.notifyDataSetInvalidated();
        mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size() + ""));
    }

    /**
     * 邀请好友
     */
    private void inviteFriend(String inviteUsers) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoomId);
        params.put("text", inviteUsers);
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(AddContactsActivity.this, result)) {
                            ToastUtil.showToast(mContext, getString(R.string.invite_success));
                            setResult(RESULT_OK);
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(mContext);
                    }
                });
    }

    class ListViewAdapter extends BaseAdapter implements SectionIndexer {
        List<BaseSortModel<Friend>> mSortFriends;

        public ListViewAdapter() {
            mSortFriends = new ArrayList<>();
        }

        public void setData(List<BaseSortModel<Friend>> sortFriends) {
            mSortFriends = sortFriends;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mSortFriends.size();
        }

        @Override
        public Object getItem(int position) {
            return mSortFriends.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.row_select_contacts, parent, false);
            }
            TextView catagoryTitleTv = ViewHolder.get(convertView, R.id.catagory_title);
            CheckBox checkBox = ViewHolder.get(convertView, R.id.check_box);
            ImageView avatarImg = ViewHolder.get(convertView, R.id.avatar_img);
            TextView userNameTv = ViewHolder.get(convertView, R.id.user_name_tv);
            TextView userRemarkNameTv = ViewHolder.get(convertView, R.id.user_remark_name_tv);
            // 根据position获取分类的首字母的Char ascii值
            int section = getSectionForPosition(position);
            // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
            if (position == getPositionForSection(section)) {
                catagoryTitleTv.setVisibility(View.VISIBLE);
                catagoryTitleTv.setText(mSortFriends.get(position).getFirstLetter());
            } else {
                catagoryTitleTv.setVisibility(View.GONE);
            }
            Friend friend = mSortFriends.get(position).getBean();
            if (friend != null) {
                AvatarHelper.getInstance().displayAvatar(friend.getUserId(), avatarImg, true);
                userNameTv.setText(friend.getNickName());

                if (TextUtils.isEmpty(friend.getRemarkName())) {
                    userRemarkNameTv.setVisibility(View.GONE);
                } else {
                    userRemarkNameTv.setVisibility(View.VISIBLE);
                    userRemarkNameTv.setText("备注: " + friend.getRemarkName());
                }
                checkBox.setChecked(false);
                if (friend.getStatus() == 100) {
                    checkBox.setChecked(true);
                } else {
                    checkBox.setChecked(false);
                }
            }
            return convertView;
        }

        @Override
        public Object[] getSections() {
            return null;
        }

        @Override
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

        @Override
        public int getSectionForPosition(int position) {
            return mSortFriends.get(position).getFirstLetter().charAt(0);
        }
    }

    class HorListViewAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mSelectPositions.size();
        }

        @Override
        public Object getItem(int position) {
            return mSelectPositions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new RoundedImageView(mContext);
                int size = DisplayUtil.dip2px(mContext, 36);
                ((RoundedImageView) convertView).setOval(false);
                ((RoundedImageView) convertView).setCornerRadius(Constants.AVATAR_CORNER_RADIUS);
                AbsListView.LayoutParams param = new AbsListView.LayoutParams(size, size);
                convertView.setLayoutParams(param);
            }
            ImageView imageView = (ImageView) convertView;
            String selectPosition = mSelectPositions.get(position);
            AvatarHelper.getInstance().displayAvatar(selectPosition, imageView, true);
            return convertView;
        }
    }
}
