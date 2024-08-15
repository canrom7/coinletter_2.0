package com.iimm.miliao.ui.message.multi;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.DeleteGroupMemberBean;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.RoomMember;
import com.iimm.miliao.bean.message.MucRoomMember;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.RoomMemberDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.sortlist.BaseSortModel;
import com.iimm.miliao.sortlist.SortHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.HttpUtil;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.ThreadManager;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.view.HeadView;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * 删除群成员
 */
public class DeleteGroupMemberActivity extends BaseActivity {
    private String TAG = "GroupMoreFeatures";
    private EditText mEditText;
    private boolean isSearch;
    private PullToRefreshListView mListView;
    private GroupMoreFeaturesAdapter mAdapter;
    private List<RoomMember> mSortRoomMember;
    private List<RoomMember> mSortTimeMember;
    private List<RoomMember> mSortdefaultMember;
    private List<RoomMember> mSearchSortRoomMember;
    private String mRoomId;
    private boolean isLoadByService;
    private boolean isDelete;

    private RoomMember mRoomMember; //自己
    private Map<String, String> mRemarksMap = new HashMap<>();
    private HashMap<String, Integer> mStringIntegerHashMap;
    private int role;
    private boolean isDisplayAllUser;
    private boolean allowSendCard;
    private TextView mRightTv;
    private List<RoomMember> mSelectList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_all_member);
        mRoomId = getIntent().getStringExtra("roomId");
        isLoadByService = getIntent().getBooleanExtra("isLoadByService", true);
        isDelete = getIntent().getBooleanExtra("isDelete", false); //是否是移除好友进来
        role = getIntent().getIntExtra("role", 0);
        isDisplayAllUser = getIntent().getBooleanExtra("isDisplayAllUser", true);
        allowSendCard = getIntent().getBooleanExtra("allowSendCard", true);

        initActionBar();
        initView();
        initData();

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
        tvTitle.setText(R.string.group_member);
        mRightTv = findViewById(R.id.tv_title_right);
        mRightTv.setBackgroundResource(R.drawable.bg_tight_tv);
        mRightTv.setTextSize(14);
        mRightTv.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void initData() {
        AsyncUtils.doAsync(this, c -> {
            List<Friend> mFriendList = FriendDao.getInstance().getAllFriends(coreManager.getSelf().getUserId());
            for (int i = 0; i < mFriendList.size(); i++) {
                if (!TextUtils.isEmpty(mFriendList.get(i).getRemarkName())) {// 针对该好友进行了备注
                    mRemarksMap.put(mFriendList.get(i).getUserId(), mFriendList.get(i).getRemarkName());
                }
            }
            c.uiThread(r -> {
                mAdapter.notifyDataSetChanged();// 刷新页面
            });
        });
        ThreadManager.getPool().execute(new Runnable() {
            @Override
            public void run() {
                mRoomMember = RoomMemberDao.getInstance().getSingleRoomMember(mRoomId, coreManager.getSelf().getUserId());
                if (!HttpUtil.isNetWorkAvailable()) {
                    List<RoomMember> data = RoomMemberDao.getInstance().getRoomMember(mRoomId);
                    List<RoomMember> baseSortModels = sortKing(filterUser(data));
                    mSortRoomMember.addAll(baseSortModels);
                    mStringIntegerHashMap = new HashMap<>();
                    for (int i = 0; i < mSortRoomMember.size(); i++) {
                        mStringIntegerHashMap.put(mSortRoomMember.get(i).getUserId(), i);
                    }
                }
                if (mAdapter != null) {
                    runOnUiThread(() -> {
                        mAdapter.notifyDataSetChanged();
                        if (isLoadByService) {
                            loadDataByService(true);
                        }
                    });
                }
            }
        });
    }

    /**
     * 把群主 管理员 排到前边
     *
     * @param data
     * @return
     */
    private List<RoomMember> sortKing(List<RoomMember> data) {
        Collections.sort(data, new Comparator<RoomMember>() {
            @Override
            public int compare(RoomMember o1, RoomMember o2) {
                if (o1.getRole() == 3 && o2.getRole() != 3) {
                    return 1;
                } else if (o2.getRole() == 3 && o1.getRole() != 3) {
                    return 0;
                }
                if (o1.getRole() > o2.getRole()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        return data;
    }

    private List<RoomMember> filterUser(List<RoomMember> allUser) {
        List<RoomMember> models = new ArrayList<>();
        if (role != 1 && role != 2 && !isDisplayAllUser) {
            for (RoomMember model : allUser) {
                if (model.getRole() == 1 || model.getRole() == 2) {
                    models.add(model);
                }
                if (model.getUserId().equals(coreManager.getSelf().getUserId())) {
                    models.add(model);
                }
            }
            return models;
        } else {
            return allUser;
        }
    }


    private List<BaseSortModel<RoomMember>> getBaseSortModels(List<RoomMember> data) {
        return SortHelper.toSortedModelListWithManager(data, new HashMap<>(), RoomMember::getUserName, RoomMember::getRole);
    }

    private void initView() {
        mSelectList = new ArrayList<>();
        mSortRoomMember = new ArrayList<>();
        mSortdefaultMember = new ArrayList<>();
        mSortTimeMember = new ArrayList<>();
        mSearchSortRoomMember = new ArrayList<>();
        mListView = findViewById(R.id.pull_refresh_list);
        if (!isLoadByService) {// 不支持刷新
            mListView.setMode(PullToRefreshBase.Mode.DISABLED);
        }
        mAdapter = new GroupMoreFeaturesAdapter(mSortRoomMember);
        mListView.getRefreshableView().setAdapter(mAdapter);

        mEditText = (EditText) findViewById(R.id.search_edit);
        mEditText.setHint(getString(R.string.search_for_contacts));
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
                mListView.setMode(PullToRefreshBase.Mode.DISABLED);
                mSearchSortRoomMember.clear();
                String str = mEditText.getText().toString();
                if (TextUtils.isEmpty(str)) {
                    isSearch = false;
                    mListView.setMode(PullToRefreshBase.Mode.BOTH);
                    mAdapter.setData(mSortRoomMember);
                    return;
                }
                for (int i = 0; i < mSortRoomMember.size(); i++) {
                    if (getName(mSortRoomMember.get(i)).contains(str)) { // 符合搜索条件的好友
                        mSearchSortRoomMember.add((mSortRoomMember.get(i)));
                    }

                }
                mAdapter.setData(mSearchSortRoomMember);
            }
        });

        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                loadDataByService(true);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                loadDataByService(false);
            }
        });
        mRightTv.setText(getString(R.string.add_chat_ok_btn, mSelectList.size() + ""));
        mRightTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectList!=null&&mSelectList.size()>0){
                    String userId = "";
                    for (RoomMember data : mSelectList) {
                        userId = userId + data.getUserId() + ",";
                    }
                    deleteMember(mSelectList, userId.substring(0, userId.length() - 1));
                }else {
                    ToastUtil.showToast("请选择群组成员");
                }
            }
        });
        mListView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final RoomMember baseSortModel;
                if (isSearch) {
                    baseSortModel = mSearchSortRoomMember.get((int) id);
                } else {
                    baseSortModel = mSortRoomMember.get((int) id);
                }

                if (baseSortModel.getUserId().equals(coreManager.getSelf().getUserId())) {
                    ToastUtil.showToast(mContext, R.string.can_not_remove_self);
                    return;
                }
                if (baseSortModel.getRole() == 1) {
                    ToastUtil.showToast(mContext, getString(R.string.tip_cannot_remove_owner));
                    return;
                }

                if (baseSortModel.getRole() == 2 && mRoomMember != null && mRoomMember.getRole() != 1) {
                    ToastUtil.showToast(mContext, getString(R.string.tip_cannot_remove_manager));
                    return;
                }
                String hint = getString(R.string.sure_remove_member_for_group, getName(baseSortModel));

                if (baseSortModel.get_id() != -1) {
                    baseSortModel.set_id(-1);
                    addSelect(baseSortModel);
                } else {
                    baseSortModel.set_id(0);
                    removeSelect(baseSortModel);
                }

                if (isSearch) {
                    mAdapter.setData(mSearchSortRoomMember);
                } else {
                    mAdapter.setData(mSortRoomMember);
                }

            }
        });
    }

    private void addSelect(RoomMember userId) {
        mSelectList.add(userId);
        mRightTv.setText(getString(R.string.add_chat_ok_btn, mSelectList.size() + ""));
    }

    private void removeSelect(RoomMember userId) {
        Iterator<RoomMember> it = mSelectList.iterator();
        while (it.hasNext()) {
            RoomMember roomMember = it.next();
            if (TextUtils.equals(roomMember.getUserId(), userId.getUserId())) {
                it.remove();
            }
        }
        mRightTv.setText(getString(R.string.add_chat_ok_btn, mSelectList.size() + ""));
    }


    public void tip(String tip) {
        ToastUtil.showToast(DeleteGroupMemberActivity.this, tip);
    }

    private void loadDataByService(boolean reset) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoomId);
        if (reset) {
            params.put("joinTime", String.valueOf(0));
        } else {
            long lastRoamingTime = PreferenceUtils.getLong(MyApplication.getContext(), Constants.MUC_MEMBER_LAST_JOIN_TIME + coreManager.getSelf().getUserId() + mRoomId, 0);
            params.put("joinTime", String.valueOf(lastRoamingTime));
        }
        params.put("pageSize", Constants.MUC_MEMBER_SIZE_GROUP);
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<MucRoomMember>(MucRoomMember.class) {
                    @Override
                    public void onResponse(ArrayResult<MucRoomMember> result) {
                        DialogHelper.dismissProgressDialog();
                        if (reset) {
                            mListView.onPullDownRefreshComplete();
                        } else {
                            mListView.onPullUpRefreshComplete();
                        }

                        HashMap<String, String> toRepeatHashMap = new HashMap<>();
                        for (RoomMember member : mSortRoomMember) {
                            toRepeatHashMap.put(member.getUserId(), member.getUserId());
                        }

                        if (Result.checkSuccess(mContext, result)) {
                            mSortTimeMember.clear();
                            List<MucRoomMember> mucRoomMemberList = result.getData();
                            if (mucRoomMemberList.size() == Integer.valueOf(Constants.MUC_MEMBER_PAGE_SIZE)) {
                                mListView.setMode(PullToRefreshBase.Mode.BOTH);
                            } else {
                                mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                            }
                            if (mucRoomMemberList.size() > 0) {
                                List<RoomMember> roomMemberList = new ArrayList<>();
                                for (int i = 0; i < mucRoomMemberList.size(); i++) {
                                    MucRoomMember mucRoomMember = mucRoomMemberList.get(i);
                                    if (!reset &&
                                            toRepeatHashMap.containsKey(mucRoomMember.getUserId())) {
                                        continue;
                                    }
                                    RoomMember roomMember = new RoomMember();
                                    roomMember.setRoomId(mRoomId);
                                    roomMember.setUserId(mucRoomMember.getUserId());
                                    roomMember.setUserName(mucRoomMember.getNickName());
                                    if (TextUtils.isEmpty(mucRoomMember.getRemarkName())) {
                                        roomMember.setCardName(mucRoomMember.getNickName());
                                    } else {
                                        roomMember.setCardName(mucRoomMember.getRemarkName());
                                    }
                                    //离线 就 设置最后一次离线状态
                                    if (mucRoomMember.getOnLineState() == 0) {
                                        roomMember.setLastOnLineTime(mucRoomMember.getOfflineTime());
                                    } else {
                                        //在线
                                        roomMember.setLastOnLineTime(0);
                                    }
                                    roomMember.setTalkTime(mucRoomMember.getTalkTime());
                                    roomMember.setRole(mucRoomMember.getRole());
                                    roomMember.setCreateTime(mucRoomMember.getCreateTime());
                                    roomMember.setVipLevel(mucRoomMember.getVip());
//                                    BaseSortModel<RoomMember> baseSortModel = new BaseSortModel<>();
//                                    baseSortModel.setBean(roomMember);
                                    mSortTimeMember.add(roomMember);
                                    roomMemberList.add(roomMember);
                                }

                                if (reset) {
                                    RoomMemberDao.getInstance().deleteRoomMemberTable(mRoomId);
                                }
                                AsyncUtils.doAsync(this, mucChatActivityAsyncContext -> {
                                    for (int i = 0; i < roomMemberList.size(); i++) {// 在异步任务内存储
                                        RoomMemberDao.getInstance().saveSingleRoomMember(mRoomId, roomMemberList.get(i));
                                    }
                                });

                                RoomInfoActivity.saveMucLastRoamingTime(coreManager.getSelf().getUserId(), mRoomId, mucRoomMemberList.get(mucRoomMemberList.size() - 1).getCreateTime(), reset);
                                List<RoomMember> roomMembers = sortKing(filterUser(roomMemberList));
                                // 刷新本地数据
                                if (reset) {
                                    mSortRoomMember.clear();
                                    mSortRoomMember.addAll(roomMembers);
                                    mSortdefaultMember.addAll(roomMembers);
                                    mAdapter.notifyDataSetInvalidated();
                                } else {
                                    mSortRoomMember.addAll(roomMembers);
                                    mSortdefaultMember.addAll(roomMembers);
                                    mAdapter.notifyDataSetChanged();
                                }
                                DialogHelper.dismissProgressDialog();
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        if (reset) {
                            mListView.onPullDownRefreshComplete();
                        } else {
                            mListView.onPullUpRefreshComplete();
                        }
                        ToastUtil.showErrorNet(getApplicationContext());
                    }
                });
    }

    private String getName(RoomMember member) {
        if (mRoomMember != null && mRoomMember.getRole() == 1) {
            if (!TextUtils.equals(member.getUserName(), member.getCardName())) {// 当userName与cardName不一致时，我们认为群主有设置群内备注
                return member.getCardName();
            } else {
                if (mRemarksMap.containsKey(member.getUserId())) {
                    return mRemarksMap.get(member.getUserId());
                } else {
                    return member.getUserName();
                }
            }
        } else {
            if (mRemarksMap.containsKey(member.getUserId())) {
                return mRemarksMap.get(member.getUserId());
            } else {
                return member.getUserName();
            }
        }
    }


    /**
     * 删除群成员
     */
    private void deleteMember(final List<RoomMember> baseSortModel, final String userId) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoomId);
        params.put("userId", userId);
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBERS_DELETE)
                .params(params)
                .build()
                .execute(new BaseCallback<DeleteGroupMemberBean>(DeleteGroupMemberBean.class) {

                    @Override
                    public void onResponse(ObjectResult<DeleteGroupMemberBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            ToastUtil.showToast(TextUtils.isEmpty(result.getResultMsg()) ? getString(R.string.remove_success) : result.getResultMsg());

                            if (result != null && result.getData() != null && !TextUtils.isEmpty(result.getData().getDelSucceedUserId())) {
                                String[] strings = result.getData().getDelSucceedUserId().split(",");
                                if (strings != null) {
                                    List<String> list = Arrays.asList(strings);
                                    if (list != null) {
                                        for (String data : list) {
                                            if (!TextUtils.isEmpty(data)) {
                                                mEditText.setText("");
                                                RoomMemberDao.getInstance().deleteRoomMember(mRoomId, data);
                                                EventBus.getDefault().post(new EventGroupStatus(10001, Integer.valueOf(data)));
                                                Iterator<RoomMember> it = mSortRoomMember.iterator();
                                                while (it.hasNext()) {
                                                    RoomMember roomMember = it.next();
                                                    if (TextUtils.equals(roomMember.getUserId(), data)) {
                                                        it.remove();
                                                    }
                                                }
                                            }
                                        }
                                        mSelectList.clear();
                                        mRightTv.setText(getString(R.string.add_chat_ok_btn, mSelectList.size() + ""));
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    class GroupMoreFeaturesAdapter extends BaseAdapter {
        List<RoomMember> mSortRoomMember;

        GroupMoreFeaturesAdapter(List<RoomMember> sortRoomMember) {
            this.mSortRoomMember = new ArrayList<>();
            this.mSortRoomMember = sortRoomMember;
        }

        public void setData(List<RoomMember> sortRoomMember) {
            this.mSortRoomMember = sortRoomMember;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mSortRoomMember.size();
        }

        @Override
        public Object getItem(int position) {
            return mSortRoomMember.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.row_room_all_member_delete, parent, false);
                ViewHolder viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();

            RoomMember member = mSortRoomMember.get(position);
            if (member != null) {
                if (position == 0) {
                    viewHolder.catagoryTitleTv.setVisibility(View.VISIBLE);
                    setCatagoryTitle(member, viewHolder.catagoryTitleTv);
                } else {
                    RoomMember member1 = mSortRoomMember.get(position - 1);
                    if (member1.getRole() == member.getRole()) {
                        viewHolder.catagoryTitleTv.setVisibility(View.GONE);
                    } else {
                        viewHolder.catagoryTitleTv.setVisibility(View.VISIBLE);
                        setCatagoryTitle(member, viewHolder.catagoryTitleTv);
                    }
                }
                viewHolder.avatarImg.setVipLevel(member.getVipLevel());
                ImageView headImg = viewHolder.avatarImg.getHeadImage();
                AvatarHelper.getInstance().displayAvatar(getName(member), member.getUserId(), headImg, true);

                viewHolder.userNameTv.setText(getName(member));
                if (isDelete) {
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewHolder.userNameTv.getLayoutParams();
                    layoutParams.removeRule(RelativeLayout.ALIGN_TOP);
                    layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                    viewHolder.userNameTv.setLayoutParams(layoutParams);
                    viewHolder.tvLoginTime.setVisibility(View.GONE);
                } else {
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewHolder.userNameTv.getLayoutParams();
                    layoutParams.addRule(RelativeLayout.ALIGN_TOP, R.id.avatar_img);
                    layoutParams.removeRule(RelativeLayout.CENTER_VERTICAL);
                    viewHolder.userNameTv.setLayoutParams(layoutParams);
                    viewHolder.tvLoginTime.setVisibility(View.VISIBLE);
                }
                if (member.getLastOnLineTime() > 0) {
                    //有上次在线时间
                    viewHolder.tvLoginTime.setText(TimeUtils.getFriendlyTimeDesc(DeleteGroupMemberActivity.this, member.getLastOnLineTime()));
                    viewHolder.tvLoginTime.setTextColor(Color.parseColor("#999999"));
                } else {
                    //正在  在线
                    viewHolder.tvLoginTime.setText(R.string.online);
                    viewHolder.tvLoginTime.setTextColor(Color.parseColor("#1194F5"));
                }
                if (member.get_id() == -1) {
                    viewHolder.checkBox.setChecked(true);
                } else {
                    viewHolder.checkBox.setChecked(false);
                }
            }
            return convertView;
        }

        private void setCatagoryTitle(RoomMember member, TextView catagoryTitleTv) {
            switch (member.getRole()) {
                case 1:
                    catagoryTitleTv.setText(R.string.lord);
                    break;
                case 2:
                    catagoryTitleTv.setText(R.string.administrator);
                    break;
                case 3:
                    catagoryTitleTv.setText(R.string.ordinary_member);
                    break;
                default:
                    catagoryTitleTv.setText("");
                    break;
            }
        }

//        @Override
//        public Object[] getSections() {
//            return null;
//        }

//        @Override
//        public int getPositionForSection(int section) {
//            for (int i = 0; i < getCount(); i++) {
//                String sortStr = mSortRoomMember.get(i).getFirstLetter();
//                char firstChar = sortStr.toUpperCase().charAt(0);
//                if (firstChar == section) {
//                    return i;
//                }
//            }
//            return -1;
//        }
//
//        @Override
//        public int getSectionForPosition(int position) {
//            return mSortRoomMember.get(position).getFirstLetter().charAt(0);
//        }
    }


    public class ViewHolder {
        TextView catagoryTitleTv;
        HeadView avatarImg;
        TextView userNameTv;
        TextView tvLoginTime;
        CheckBox checkBox;

        public ViewHolder(View convertView) {
            catagoryTitleTv = convertView.findViewById(R.id.catagory_title);
            avatarImg = convertView.findViewById(R.id.avatar_img);
            userNameTv = convertView.findViewById(R.id.user_name_tv);
            tvLoginTime = convertView.findViewById(R.id.tv_login_time);
            checkBox = convertView.findViewById(R.id.check_box);
        }
    }
}
