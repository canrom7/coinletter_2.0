package com.iimm.miliao.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.iimm.miliao.AppConstant;
import com.iimm.miliao.R;
import com.iimm.miliao.adapter.SearchAdapter;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.sortlist.BaseSortModel;
import com.iimm.miliao.ui.base.EasyFragment;
import com.iimm.miliao.ui.message.ChatActivity;
import com.iimm.miliao.ui.message.MucChatActivity;
import com.iimm.miliao.util.ToolUtils;

import java.util.List;

public abstract class AddressBookSearchFragment extends EasyFragment implements SearchAdapter.SearchAdapterListener {
    RecyclerView mRvSearchList;
    ListView mLvContent;
    static final int MODE_SEARCH = 0;
    static final int MODE_DATA = 1;
    int currentMode = MODE_DATA;
    InputMethodManager mInputManager;
    SearchAdapter searchAdapter;
    List<Friend> searchFriends;
    List<BaseSortModel<Friend>> mSortFriends;

    @Override
    protected int inflateLayoutId() {
        return 0;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        mInputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public void initSearchModule(EditText searchEdit, TextView tvCancel) {
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString().trim();
                str = ToolUtils.sqliteEscape(str);
                if (!TextUtils.isEmpty(str)) {
                    switchMode(MODE_SEARCH);
                    query(str);
                } else {
                    if (searchFriends != null) {
                        searchFriends.clear();
                    }
                    switchMode(MODE_DATA);
                    loadData();
                }
            }
        });
        searchEdit.setOnFocusChangeListener((v, hasFocus) -> {
            searchEditFocusChange(hasFocus);
            if (hasFocus) {
                mRvSearchList.setVisibility(View.VISIBLE);
                searchEdit.setTextColor(getResources().getColor(R.color.search_color_focus));
                tvCancel.setVisibility(View.VISIBLE);
            } else {
                mRvSearchList.setVisibility(View.GONE);
                searchEdit.setTextColor(getResources().getColor(R.color.search_color_cancle));
            }
        });

        tvCancel.setOnClickListener(v -> {
            tvCancelOnClick();
            tvCancel.setVisibility(View.GONE);
            searchEdit.setText("");
            searchEdit.clearFocus();
            if (mInputManager != null) {
                mInputManager.hideSoftInputFromWindow(searchEdit.getApplicationWindowToken(), 0);
            }
        });

    }

    protected abstract void tvCancelOnClick();

    protected void query(String str) {
        if (searchAdapter == null) {
            searchAdapter = new SearchAdapter(searchFriends, getContext(), coreManager.getSelf().getUserId());
            searchAdapter.setListener(this);
            mRvSearchList.setLayoutManager(new LinearLayoutManager(getContext()));
            mRvSearchList.setAdapter(searchAdapter);
        }
        searchFriends.clear();
        searchAdapter.setCurrentSearchKey(str);
        List<Friend> friendList = FriendDao.getInstance().queryFriendByKey(coreManager.getSelf().getUserId(),str);
        if (friendList != null && !friendList.isEmpty()) {
            searchFriends.addAll(friendList);
        }
        searchAdapter.notifyDataSetChanged();
    }

    protected abstract void loadData();

    protected abstract void searchEditFocusChange(boolean hasFocus);

    protected void switchMode(int modeSearch) {
        currentMode = modeSearch;
        if (currentMode == MODE_SEARCH) {
            //搜索模式
            mLvContent.setVisibility(View.GONE);
            mRvSearchList.setVisibility(View.VISIBLE);
            mRvSearchList.setOnClickListener(this);
        } else {
            //数据模式
            mLvContent.setVisibility(View.VISIBLE);
            mRvSearchList.setVisibility(View.GONE);
            mRvSearchList.setOnClickListener(null);
        }
    }

    @Override
    public void itemClick(int i) {
        Friend friend = searchFriends.get(i);
        if (friend.getRoomFlag() == 0) {
            //单聊
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra(ChatActivity.FRIEND, friend);
            intent.putExtra("isserch", false);
            startActivity(intent);
        } else {
            //群聊
            Intent intent = new Intent(getActivity(), MucChatActivity.class);
            intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
            intent.putExtra(AppConstant.EXTRA_NICK_NAME, friend.getNickName());
            intent.putExtra(AppConstant.EXTRA_IS_GROUP_CHAT, true);
            startActivity(intent);
            if (friend.getUnReadNum() > 0) {// 如该群组未读消息数量大于1, 刷新MessageFragment
                MsgBroadcast.broadcastMsgNumReset(getActivity());
                MsgBroadcast.broadcastMsgUiUpdate(getActivity());
            }
        }
    }
}
