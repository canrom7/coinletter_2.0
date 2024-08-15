package com.iimm.miliao.ui.circle;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.adapter.AddressBookAdapter;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.databinding.AcctivitySelectFriendBinding;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.sortlist.BaseSortModel;
import com.iimm.miliao.sortlist.SortHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MrLiu253@163.com
 * 选择用户
 *
 * @time 2020-06-04
 */
public class SelectFriendActivity extends BaseActivity {
    private AcctivitySelectFriendBinding mBinding;
    private String mLoginUserId;
    private List<BaseSortModel<Friend>> mSortFriends;
    private AddressBookAdapter mAddressBookAdapter;

    public static void showSelectFriendActivity(Activity context) {
        Intent intent = new Intent(context, SelectFriendActivity.class);
        context.startActivityForResult(intent, 1090);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.acctivity_select_friend);
        mLoginUserId = coreManager.getSelf().getUserId();
        initView();
    }

    private void initView() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView textView = findViewById(R.id.tv_title_center);
        textView.setText("选择联系人");

        mSortFriends = new ArrayList<>();
        mAddressBookAdapter = new AddressBookAdapter(this, mSortFriends, true);
        mBinding.selectFriendRv.setAdapter(mAddressBookAdapter);
        loadData();
        mBinding.selectFriendRv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mSortFriends != null && mSortFriends.size() > position) {
                    Intent intent = new Intent();
                    intent.putExtra("friend", mSortFriends.get(position).bean);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }


    protected void loadData() {
        if (!DialogHelper.isShowing()) {
            DialogHelper.showDefaulteMessageProgressDialog(this);
        }
        AsyncUtils.doAsync(this, e -> {
            Reporter.post("加载数据失败，", e);
            AsyncUtils.runOnUiThread(this, ctx -> {
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
                mSortFriends = sortedList;
                mAddressBookAdapter.setData(sortedList);

            });
        });
    }
}
