package com.iimm.miliao.ui.message.multi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.iimm.miliao.AppConstant;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.RoomMember;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.RoomMemberDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.sortlist.BaseSortModel;
import com.iimm.miliao.sortlist.SideBar;
import com.iimm.miliao.sortlist.SortHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ViewHolder;
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
 * 设置 &&  取消 管理员，隐身人，监控人
 */
public class SetManagerActivity extends BaseActivity {
    private String roomId;
    private int role;
    private String roomJid;
    private List<BaseSortModel<RoomMember>> setManagerList;
    private EditText mEditText;
    private ListView mListView;
    private SetManagerAdapter mSetManagerAdapter;


    private Map<String, String> mRemarksMap = new HashMap<>();
    private SideBar mSideBar;

    public static void start(Context ctx, String roomId, String roomJid, int role) {
        Intent intent = new Intent(ctx, SetManagerActivity.class);
        intent.putExtra("roomId", roomId);
        intent.putExtra("role", role);
        intent.putExtra(AppConstant.EXTRA_USER_ID, roomJid);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_manager);
        if (getIntent() != null) {
            roomId = getIntent().getStringExtra("roomId");
            role = getIntent().getIntExtra("role", Constants.ROLE_MANAGER);
            roomJid = getIntent().getStringExtra(AppConstant.EXTRA_USER_ID);
        }
        initActionBar();
        loadData();
        initView();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        int titleId;
        switch (role) {
            case Constants.ROLE_MANAGER:
                titleId = R.string.design_admin;
                break;
            case Constants.ROLE_INVISIBLE:
                titleId = R.string.set_invisible;
                break;
            case Constants.ROLE_GUARDIAN:
                titleId = R.string.set_guardian;
                break;
            default:
                Reporter.unreachable();
                return;
        }
        TextView textView = findViewById(R.id.tv_title_center);
        textView.setText(titleId);
    }

    private void loadData() {
        mSideBar = (SideBar) findViewById(R.id.sidebar);
        List<Friend> mFriendList = FriendDao.getInstance().getAllFriends(CoreManager.requireSelf(this).getUserId());
        for (int i = 0; i < mFriendList.size(); i++) {
            if (!TextUtils.isEmpty(mFriendList.get(i).getRemarkName())) {// 针对该好友进行了备注
                mRemarksMap.put(mFriendList.get(i).getUserId(), mFriendList.get(i).getRemarkName());
            }
        }

        List<RoomMember> roomMember = RoomMemberDao.getInstance().getRoomMember(roomId);

        // 排序，简单根据role排序，
        // TODO: 讲道理，只要显示普通成员就够了的，其他身份都不能操作的，
        //Collections.sort(roomMember, (o1, o2) -> o1.getRole() - o2.getRole());
        setManagerList = new ArrayList<>(roomMember.size());
        for (RoomMember member : roomMember) {
//            SetManager setManager = new SetManager();
//            setManager.setRole(member.getRole());
//            setManager.setCreateTime(member.getCreateTime());
//            setManager.setUserId(member.getUserId());
//            setManager.setNickName(getName(member));
//            setManagerList.add(setManager);
            String name = getName(member);
            member.setUserName(name);
        }
        Map<String, Integer> existMap = new HashMap<>();
        List<BaseSortModel<RoomMember>> sortedList = SortHelper.toSortedModelList(roomMember, existMap, member -> {
            String name = member.getUserName();
            return name;
        });
        mSideBar.setExistMap(existMap);
        setManagerList.clear();
        setManagerList.addAll(sortedList);
    }

    private String getName(RoomMember member) {
        if (!TextUtils.equals(member.getUserName(), member.getCardName())) {// 当userName与cardName不一致时，我们认为群主有设置群内备注
            return member.getCardName();
        } else {
            if (mRemarksMap.containsKey(member.getUserId())) {
                return mRemarksMap.get(member.getUserId());
            } else {
                return member.getUserName();
            }
        }
    }

    private void initView() {


        mListView = (ListView) findViewById(R.id.set_manager_lv);
        mSetManagerAdapter = new SetManagerAdapter(this);
        mSetManagerAdapter.setData(setManagerList);
        mListView.setAdapter(mSetManagerAdapter);

        /**
         * 群内邀请好友搜索功能
         */
        mEditText = (EditText) findViewById(R.id.search_edit);
//        mEditText.setHint(InternationalizationHelper.getString("JX_Seach"));
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
                String mContent = mEditText.getText().toString();
                List<BaseSortModel<RoomMember>> setManagers = new ArrayList<>();
                if (TextUtils.isEmpty(mContent)) {
                    mSetManagerAdapter.setData(setManagerList);
                }
                for (int i = 0; i < setManagerList.size(); i++) {
                    if (setManagerList.get(i).getBean().getUserName().contains(mContent)) {
                        // 符合搜索条件的好友
                        setManagers.add((setManagerList.get(i)));
                    }
                }
                mSetManagerAdapter.setData(setManagers);
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BaseSortModel<RoomMember> setManager = (BaseSortModel<RoomMember>) mSetManagerAdapter.getItem(i);
                if (setManager.getBean().getUserId().equals(coreManager.getSelf().getUserId())) {
                    ToastUtil.showToast(SetManagerActivity.this, R.string.tip_cannot_set_self_role);
                    return;
                }
                if (setManager.getBean().getRole() == role) {
                    // 如果已经是这个身份，再点击就是取消，
                    if (role == Constants.ROLE_MANAGER) {
                        // 取消管理员，保留旧代码，
                        cancelManager(roomId, setManager);
                    } else {
                        // 取消隐身人，监控人，
                        cancelRole(roomId, setManager, role);
                    }
                } else {
                    if (role == Constants.ROLE_MANAGER) {
                        // 设置管理员，保留旧代码，
                        setManager(roomId, setManager);
                    } else {
                        // 设置隐身人，监控人，
                        setRole(roomId, setManager, role);
                    }
                }
            }
        });

        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = mSetManagerAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListView.setSelection(position);
                }
            }
        });
    }

    private void setManager(String roomId, final BaseSortModel<RoomMember> setManager) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", roomId);
        params.put("touserId", setManager.getBean().getUserId());
        params.put("type", String.valueOf(Constants.ROLE_MANAGER));
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_MANAGER)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            Toast.makeText(SetManagerActivity.this, InternationalizationHelper.getString("JXRoomMemberVC_SetAdministratorSuccess"), Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().post(new EventGroupStatus(10000, 0));
                            setManager.getBean().setRole(Constants.ROLE_MANAGER);
                            mSetManagerAdapter.notifyDataSetChanged();
                        } else if (!TextUtils.isEmpty(result.getResultMsg())) {
                            ToastUtil.showToast(SetManagerActivity.this, result.getResultMsg());
                        } else {
                            ToastUtil.showToast(SetManagerActivity.this, R.string.tip_server_error);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(SetManagerActivity.this);
                    }
                });
    }

    private void setRole(String roomId, final BaseSortModel<RoomMember> setManager, final int role) {
        Integer type;
        switch (role) {
            case Constants.ROLE_INVISIBLE:
                type = 4;
                break;
            case Constants.ROLE_GUARDIAN:
                type = 5;
                break;
            default:
                Reporter.unreachable();
                return;
        }
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", roomId);
        params.put("touserId", setManager.getBean().getUserId());
        params.put("type", type.toString());
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_UPDATE_ROLE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            int tipContent;
                            switch (role) {
                                case Constants.ROLE_INVISIBLE: // 隐身人
                                    tipContent = R.string.tip_set_invisible_success;
                                    break;
                                case Constants.ROLE_GUARDIAN: // 监控人
                                    tipContent = R.string.tip_set_guardian_success;
                                    break;
                                default:
                                    Reporter.unreachable();
                                    return;
                            }
                            ToastUtil.showToast(SetManagerActivity.this, tipContent);
                            // 保留旧代码，抛出去RoomInfoActivity统一处理，
                            EventBus.getDefault().post(new EventGroupStatus(10000, 0));
                            setManager.getBean().setRole(role);
                            mSetManagerAdapter.notifyDataSetChanged();
                        } else if (!TextUtils.isEmpty(result.getResultMsg())) {
                            ToastUtil.showToast(SetManagerActivity.this, result.getResultMsg());
                        } else {
                            ToastUtil.showToast(SetManagerActivity.this, R.string.tip_server_error);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(SetManagerActivity.this);
                    }
                });
    }

    private void cancelManager(String roomId, final BaseSortModel<RoomMember> setManager) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", roomId);
        params.put("touserId", setManager.getBean().getUserId());
        params.put("type", String.valueOf(Constants.ROLE_MEMBER));

        HttpUtils.get().url(coreManager.getConfig().ROOM_MANAGER)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            Toast.makeText(SetManagerActivity.this, InternationalizationHelper.getString("JXRoomMemberVC_CancelAdministratorSuccess"), Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().post(new EventGroupStatus(10000, 0));
                            setManager.getBean().setRole(Constants.ROLE_MEMBER);
                            mSetManagerAdapter.notifyDataSetChanged();
                        } else if (!TextUtils.isEmpty(result.getResultMsg())) {
                            ToastUtil.showToast(SetManagerActivity.this, result.getResultMsg());
                        } else {
                            ToastUtil.showToast(SetManagerActivity.this, R.string.tip_server_error);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(SetManagerActivity.this, getString(R.string.check_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cancelRole(String roomId, final BaseSortModel<RoomMember> setManager, final int role) {
        Integer type;
        switch (role) {
            case Constants.ROLE_INVISIBLE:
                type = -1;
                break;
            case Constants.ROLE_GUARDIAN:
                type = 0;
                break;
            default:
                Reporter.unreachable();
                return;
        }
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", roomId);
        params.put("touserId", setManager.getBean().getUserId());
        params.put("type", type.toString());
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_UPDATE_ROLE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            int tipContent;
                            switch (role) {
                                case Constants.ROLE_INVISIBLE: // 隐身人
                                    tipContent = R.string.tip_cancel_invisible_success;
                                    break;
                                case Constants.ROLE_GUARDIAN: // 监控人
                                    tipContent = R.string.tip_cancel_guardian_success;
                                    break;
                                default:
                                    Reporter.unreachable();
                                    return;
                            }
                            ToastUtil.showToast(SetManagerActivity.this, tipContent);
                            // 保留旧代码，抛出去RoomInfoActivity统一处理，
                            EventBus.getDefault().post(new EventGroupStatus(10000, 0));
                            setManager.getBean().setRole(Constants.ROLE_MEMBER);
                            mSetManagerAdapter.notifyDataSetChanged();
                        } else if (!TextUtils.isEmpty(result.getResultMsg())) {
                            ToastUtil.showToast(SetManagerActivity.this, result.getResultMsg());
                        } else {
                            ToastUtil.showToast(SetManagerActivity.this, R.string.tip_server_error);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(SetManagerActivity.this);
                    }
                });
    }

    private class SetManagerAdapter extends BaseAdapter implements SectionIndexer {

        private List<BaseSortModel<RoomMember>> mSetManager;
        private Context mContext;

        public SetManagerAdapter(Context context) {
            mSetManager = new ArrayList<>();
            mContext = context;
        }

        public void setData(List<BaseSortModel<RoomMember>> setManager) {
            this.mSetManager = setManager;
            notifyDataSetChanged();
        }


        @Override
        public int getCount() {
            return mSetManager.size();
        }

        @Override
        public Object getItem(int i) {
            return mSetManager.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.a_item_set_manager, viewGroup, false);
            }
            TextView catagoryTitleTv = ViewHolder.get(view, R.id.catagory_title);
            ImageView avatar_img = ViewHolder.get(view, R.id.set_manager_iv);
            TextView roleS = ViewHolder.get(view, R.id.roles);
            TextView nick_name_tv = ViewHolder.get(view, R.id.set_manager_tv);
            // 设置头像
            AvatarHelper.getInstance().displayAvatar(mSetManager.get(i).getBean().getUserName(), mSetManager.get(i).getBean().getUserId(), avatar_img, true);
            // 设置职位
            //   roleS.setBackgroundResource(R.drawable.bg_role3);
            switch (mSetManager.get(i).getBean().getRole()) {
                case Constants.ROLE_OWNER:
                    roleS.setText(InternationalizationHelper.getString("JXGroup_Owner"));
                    // ViewCompat.setBackgroundTintList(roleS, ColorStateList.valueOf(getResources().getColor(R.color.color_role1)));
                    break;
                case Constants.ROLE_MANAGER:
                    roleS.setText(InternationalizationHelper.getString("JXGroup_Admin"));
                    // ViewCompat.setBackgroundTintList(roleS, ColorStateList.valueOf(getResources().getColor(R.color.color_role2)));
                    break;
                case Constants.ROLE_MEMBER:
                    roleS.setText(InternationalizationHelper.getString("JXGroup_RoleNormal"));
                    //  ViewCompat.setBackgroundTintList(roleS, ColorStateList.valueOf(getResources().getColor(R.color.color_role3)));
                    break;
                case Constants.ROLE_INVISIBLE:
                    roleS.setText(R.string.role_invisible);
                    //   ViewCompat.setBackgroundTintList(roleS, ColorStateList.valueOf(getResources().getColor(R.color.color_role4)));
                    break;
                case Constants.ROLE_GUARDIAN:
                    roleS.setText(R.string.role_guardian);
                    //  ViewCompat.setBackgroundTintList(roleS, ColorStateList.valueOf(getResources().getColor(R.color.color_role5)));
                    break;
                default:
                    Reporter.unreachable();
                    roleS.setVisibility(View.GONE);
                    break;
            }
            // 设置昵称
            nick_name_tv.setText(mSetManager.get(i).getBean().getCardName());
            // 根据position获取分类的首字母的Char ascii值
            int section = getSectionForPosition(i);
            // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
            if (i == getPositionForSection(section)) {
                catagoryTitleTv.setVisibility(View.VISIBLE);
                catagoryTitleTv.setText(mSetManager.get(i).getFirstLetter());
            } else {
                catagoryTitleTv.setVisibility(View.GONE);
            }


            return view;
        }

        @Override
        public Object[] getSections() {
            return null;
        }

        @Override
        public int getPositionForSection(int section) {
            for (int i = 0; i < getCount(); i++) {
                String sortStr = mSetManager.get(i).getFirstLetter();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int getSectionForPosition(int position) {
            return mSetManager.get(position).getFirstLetter().charAt(0);
        }
    }
}
