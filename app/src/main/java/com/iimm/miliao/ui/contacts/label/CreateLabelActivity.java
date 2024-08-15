package com.iimm.miliao.ui.contacts.label;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.roamer.slidelistview.SlideBaseAdapter;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.Label;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.LabelDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.other.BasicInfoActivity;
import com.iimm.miliao.util.DisplayUtil;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.UiUtils;
import com.iimm.miliao.util.ViewHolder;
import com.iimm.miliao.view.PullToRefreshSlideListView;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

public class CreateLabelActivity extends BaseActivity implements View.OnClickListener {
    private EditText mLabelEdit;
    private TextView mLabelUserSizeTv;

    private PullToRefreshSlideListView mListView;
    private LabelAdapter mLabelAdapter;
    private List<Friend> mFriendList;

    private String mLoginUserId;

    private boolean isEditLabel;// 创建  || 编辑 标签
    private String labelId;// 编辑标签传入的值
    private Label mOldLabel;

    /**
     * Todo add 2018.6.20 intent from 发布说说-谁可以看
     */
    private boolean isFromSeeCircleActivity;
    private TextView mTvTitle, mTitleRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_label);
        mLoginUserId = coreManager.getSelf().getUserId();
        isEditLabel = getIntent().getBooleanExtra("isEditLabel", false);
        if (isEditLabel) {
            labelId = getIntent().getStringExtra("labelId");
            isFromSeeCircleActivity = getIntent().getBooleanExtra("IS_FROM_SEE_CIRCLE_ACTIVITY", false);
            mOldLabel = LabelDao.getInstance().getLabel(mLoginUserId, labelId);
        }
        initActionBar();
        initView();
        initEvent();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setVisibility(View.GONE);
        TextView mTitleLeft = (TextView) findViewById(R.id.tv_title_left);
        mTitleLeft.setText(getString(R.string.cancel));
        mTitleLeft.setOnClickListener(this);
        mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        if (isEditLabel) {
            mTvTitle.setText(R.string.edit_tag);
        } else {
            mTvTitle.setText(R.string.create_tag);
        }
        mTitleRight = (TextView) findViewById(R.id.tv_title_right);
        mTitleRight.setText(getString(R.string.create));
        mTitleRight.setBackgroundResource(R.drawable.bg_tight_tv);
        mTitleRight.setTextSize(14);
        mTitleRight.setTextColor(getResources().getColor(android.R.color.white));
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTitleRight.getLayoutParams();
        params.width = DisplayUtil.dip2px(this, 43);
        params.height = DisplayUtil.dip2px(this, 28);
        mTitleRight.setLayoutParams(params);

        changeTitle(1, "");
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        mFriendList = new ArrayList<>();
        mLabelEdit = (EditText) findViewById(R.id.label_name_et);
        mLabelUserSizeTv = (TextView) findViewById(R.id.label_user_size);

        if (isEditLabel) {
            List<String> list = JSON.parseArray(mOldLabel.getUserIdList(), String.class);
            if (list != null) {
                for (String s : list) {
                    Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, s);
                    if (friend != null) {
                        mFriendList.add(friend);
                    }
                }
            }
            mLabelEdit.setTextColor(getResources().getColor(R.color.app_black));
            mLabelEdit.setText(mOldLabel.getGroupName());
            Editable etext = mLabelEdit.getText();
            Selection.setSelection(etext, etext.length());
            mLabelUserSizeTv.setText(getString(R.string.tag_member) + "(" + mFriendList.size() + ")");
        }

        findViewById(R.id.add_label_user).setOnClickListener(this);
        mListView = (PullToRefreshSlideListView) findViewById(R.id.pull_refresh_list);
        mLabelAdapter = new LabelAdapter(this);
        mListView.setAdapter(mLabelAdapter);
        mListView.getRefreshableView().setAdapter(mLabelAdapter);
        mListView.setMode(PullToRefreshBase.Mode.DISABLED);
        mListView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position = (int) id;
                Friend friend = mFriendList.get(position);
                if (friend != null) {
                    Intent intent = new Intent(CreateLabelActivity.this, BasicInfoActivity.class);
                    intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
                    startActivity(intent);
                }
            }
        });
    }

    private void initEvent() {
        mLabelEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                changeTitle(0, s.toString().trim());
                if (!TextUtils.isEmpty(s.toString().trim())) {
                    mLabelEdit.setTextColor(getResources().getColor(R.color.app_black));
                    if (mFriendList.size() > 0) {
                        changeTitle(2, "");
                    } else {
                        changeTitle(1, "");
                    }
                } else {
                    mLabelEdit.setTextColor(getResources().getColor(R.color.Grey_400));
                    changeTitle(1, "");
                }
            }
        });
    }

    private void changeTitle(int i, String str) {
        if (i == 0) {
            if (TextUtils.isEmpty(str)) {
                if (isEditLabel) {
                    mTvTitle.setText(R.string.edit_tag);
                } else {
                    mTvTitle.setText(R.string.create_tag);
                }
            } else {
                mTvTitle.setText(str);
            }
        } else if (i == 1) {
            mTitleRight.setAlpha(0.5f);
            mTitleRight.setOnClickListener(null);
        } else {
            mTitleRight.setAlpha(1.0f);
            mTitleRight.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (!UiUtils.isNormalClick(v)) {
            return;
        }
        switch (v.getId()) {
            case R.id.tv_title_left:
                finish();
                break;
            case R.id.tv_title_right:
                if (isEditLabel) {
                    DialogHelper.showDefaulteMessageProgressDialog(this);
                    if (!mOldLabel.getGroupName().equals(mLabelEdit.getText().toString())) {// 标签名已改变
                        updateLabelName();
                    }
                    updateLabelUserIdList(mOldLabel);
                } else {
                    createLabel();
                }
                break;
            case R.id.add_label_user://添加成员
                Intent intent = new Intent(this, SelectLabelFriendActivity.class);
                List<String> ids = new ArrayList<>();
                for (int i = 0; i < mFriendList.size(); i++) {
                    ids.add(mFriendList.get(i).getUserId());
                }
                intent.putExtra("exist_ids", JSON.toJSONString(ids));
                startActivityForResult(intent, 0x01);
                break;
        }
    }

    // 创建标签
    private void createLabel() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("groupName", mLabelEdit.getText().toString());
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().FRIENDGROUP_ADD)
                .params(params)
                .build()
                .execute(new BaseCallback<Label>(Label.class) {
                    @Override
                    public void onResponse(ObjectResult<Label> result) {
                        if (result.getResultCode() == 1) {
                            LabelDao.getInstance().createLabel(result.getData());
                            updateLabelUserIdList(result.getData());
                        } else {
                            DialogHelper.dismissProgressDialog();
                            ToastUtil.showLongToast(CreateLabelActivity.this, result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                    }
                });
    }

    // 修改标签名称
    private void updateLabelName() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("groupId", mOldLabel.getGroupId());
        params.put("groupName", mLabelEdit.getText().toString());

        HttpUtils.get().url(coreManager.getConfig().FRIENDGROUP_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Label>(Label.class) {
                    @Override
                    public void onResponse(ObjectResult<Label> result) {
                        if (result.getResultCode() == 1) {
                            LabelDao.getInstance().updateLabelName(mLoginUserId, mOldLabel.getGroupId(), mLabelEdit.getText().toString());
                        } else {
                            ToastUtil.showLongToast(CreateLabelActivity.this, result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorData(CreateLabelActivity.this);
                    }
                });
    }

    // 修改标签下的成员
    private void updateLabelUserIdList(final Label label) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("groupId", label.getGroupId());
        String userIdListStr = "";
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < mFriendList.size(); i++) {
            if (i == mFriendList.size() - 1) {
                userIdListStr += mFriendList.get(i).getUserId();
            } else {
                userIdListStr += mFriendList.get(i).getUserId() + ",";
            }
            strings.add(mFriendList.get(i).getUserId());
        }
        params.put("userIdListStr", userIdListStr);
        final String userIdList = JSON.toJSONString(strings);

        HttpUtils.get().url(coreManager.getConfig().FRIENDGROUP_UPDATEGROUPUSERLIST)
                .params(params)
                .build()
                .execute(new BaseCallback<Label>(Label.class) {
                    @Override
                    public void onResponse(ObjectResult<Label> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            LabelDao.getInstance().updateLabelUserIdList(mLoginUserId, label.getGroupId(), userIdList);
                            if (isFromSeeCircleActivity) {
                                setResult(RESULT_OK, new Intent());// 通知 谁可以看 刷新
                                finish();
                            } else {
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                    }
                });
    }

    @Override
    @SuppressLint("SetTextI18n")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x01 && resultCode == RESULT_OK) {
            String userIds = data.getStringExtra("inviteId");
            String userNames = data.getStringExtra("inviteName");
            List<String> userIdList = JSON.parseArray(userIds, String.class);
            List<String> userNameList = JSON.parseArray(userNames, String.class);
            for (int i = 0; i < userIdList.size(); i++) {
                Friend friend = new Friend();
                friend.setUserId(userIdList.get(i));
                friend.setNickName(userNameList.get(i));
                mFriendList.add(friend);
            }
            mLabelAdapter.notifyDataSetChanged();
            if (mFriendList.size() > 0 && !TextUtils.isEmpty(mLabelEdit.getText().toString())) {
                changeTitle(2, "");
            } else {
                changeTitle(1, "");
            }
            mLabelUserSizeTv.setText(getString(R.string.tag_member) + "(" + mFriendList.size() + ")");
        }
    }

    class LabelAdapter extends SlideBaseAdapter {

        public LabelAdapter(Context context) {
            super(context);
        }

        @Override
        public int getFrontViewId(int position) {
            return R.layout.row_create_label;
        }

        @Override
        public int getLeftBackViewId(int position) {
            return 0;
        }

        @Override
        public int getRightBackViewId(int position) {
            return R.layout.row_item_delete2;
        }

        @Override
        public int getCount() {
            if (mFriendList != null) {
                return mFriendList.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mFriendList != null) {
                return mFriendList.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = createConvertView(position);
            }
            ImageView labelIv = ViewHolder.get(convertView, R.id.label_avatar);
            TextView labelUserNameTv = ViewHolder.get(convertView, R.id.label_user_name);
            TextView delete_tv = ViewHolder.get(convertView, R.id.delete_tv);
            final Friend friend = mFriendList.get(position);
            if (friend != null) {
                AvatarHelper.getInstance().displayAvatar(friend.getUserId(), labelIv);
                labelUserNameTv.setText(!TextUtils.isEmpty(friend.getRemarkName()) ? friend.getRemarkName() : friend.getNickName());
            }
            delete_tv.setOnClickListener(v -> {
                if (mFriendList.size() == 1) {// 最后一个也被删除了
                    changeTitle(1, "");
                } else {
                    changeTitle(2, "");
                }
                mFriendList.remove(position);
                notifyDataSetChanged();
                mLabelUserSizeTv.setText(getString(R.string.tag_member) + "(" + mFriendList.size() + ")");
            });
            return convertView;
        }
    }
}
