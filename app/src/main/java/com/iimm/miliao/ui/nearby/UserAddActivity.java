package com.iimm.miliao.ui.nearby;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.User;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.contacts.ContactsActivity;
import com.iimm.miliao.ui.contacts.ContactsMsgInviteActivity;
import com.iimm.miliao.util.UiUtils;
import com.iimm.miliao.view.NoDoubleClickListener;

/**
 * MrLiu253@163.com
 * 添加好友
 *
 * @time 2019-07-01
 */
public class UserAddActivity extends BaseActivity implements View.OnClickListener {


    private User mUser;
    private TextView mCommunication;
    private EditText mSearchEdit;
    private TextView mSearchText;
    private ConstraintLayout mDynamicCL;
    private TextView mSearchTv;
    private ConstraintLayout mGraySearchCL;

    public static void start(Context ctx) {
        Intent intent = new Intent(ctx, UserAddActivity.class);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_add);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(this);
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.add_friend);
        tvTitle.setText(InternationalizationHelper.getString("JXNearVC_AddFriends"));
        mUser = coreManager.getSelf();

        initView();

    }

    private void initView() {

        mCommunication = findViewById(R.id.communication_tv);
        mCommunication.setText("我的账号：" + mUser.getAccount());
        findViewById(R.id.search_cl).setOnClickListener(this);
        findViewById(R.id.phone_contact_cl).setOnClickListener(this);
        findViewById(R.id.sMS_invites_cl).setOnClickListener(this);

        mDynamicCL = findViewById(R.id.dynamic_cl);
        mSearchEdit = findViewById(R.id.search_edit);
        mSearchText = findViewById(R.id.search_et);
        mGraySearchCL = findViewById(R.id.gray_search_cl);
        mSearchTv = findViewById(R.id.search_tv);
        mGraySearchCL.setOnClickListener(this);
        mDynamicCL.setOnClickListener(this);
        if (coreManager.getConfig().registerUsername == 1) {
            mSearchEdit.setHint(getString(R.string.username_hint));
            mSearchText.setHint(getString(R.string.username_hint));
        }
        if(coreManager.getConfig().registerUsername == 0){
            mSearchEdit.setHint(getString(R.string.telephone_hint));
            mSearchText.setHint(getString(R.string.telephone_hint));
        }
        if(coreManager.getConfig().registerUsername == 2){
            mSearchEdit.setHint("用户名/手机号");
            mSearchText.setHint("用户名/手机号");
        }
        findViewById(R.id.qr_iv).setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                DialogHelper.showQRDialog(UserAddActivity.this, mUser.getNickName(), "-1", coreManager.getConfig().website, mUser.getUserId(), null);
            }
        });
        mSearchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(mSearchEdit.getText().toString().trim())) {
                    mGraySearchCL.setVisibility(View.GONE);
                } else {
                    mGraySearchCL.setVisibility(View.VISIBLE);
                    mSearchTv.setText(mSearchEdit.getText().toString().trim());
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mDynamicCL.getVisibility() == View.VISIBLE) {
            mDynamicCL.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        if (!UiUtils.isNormalClick(v)) {
            return;
        }
        switch (v.getId()) {
            case R.id.iv_title_left://返回
                if (mDynamicCL.getVisibility() == View.VISIBLE) {
                    mDynamicCL.setVisibility(View.GONE);
                } else {
                    finish();
                }

                break;
            case R.id.gray_search_cl://开始搜索
                if (TextUtils.isEmpty(mSearchEdit.getText().toString())) {
                    return;
                }
                Intent intent = new Intent(mContext, UserListGatherActivity.class);
                intent.putExtra("key_word", mSearchEdit.getText().toString());
                intent.putExtra("sex", 0);
                intent.putExtra("min_age", 0);
                intent.putExtra("max_age", 200);
                intent.putExtra("show_time", 0);
                startActivity(intent);
                break;
            case R.id.search_cl://搜索好友

                mDynamicCL.setVisibility(View.VISIBLE);
                if (mSearchEdit != null) {
                    //设置可获得焦点
                    mSearchEdit.setFocusable(true);
                    mSearchEdit.setFocusableInTouchMode(true);
                    //请求获得焦点
                    mSearchEdit.requestFocus();
                    //调用系统输入法
                    InputMethodManager inputManager = (InputMethodManager) mSearchEdit
                            .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.showSoftInput(mSearchEdit, 0);
                }
                break;
            case R.id.phone_contact_cl://手机联系人
                Intent intentGroup = new Intent(UserAddActivity.this, ContactsActivity.class);
                startActivity(intentGroup);
                break;
            case R.id.sMS_invites_cl://邀请好友
                startActivity(new Intent(mContext, ContactsMsgInviteActivity.class));

                break;
        }

    }
}
