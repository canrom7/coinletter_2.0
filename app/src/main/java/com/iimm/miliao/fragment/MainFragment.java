package com.iimm.miliao.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.ui.MainActivity;
import com.iimm.miliao.ui.base.EasyFragment;
import com.iimm.miliao.ui.nearby.UserAddActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.view.MyTab;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends EasyFragment {
    private String TAG = "MainFragment";
    private ViewPager vpContent;
    private MyTab myTab;
    private int currentTabIndex = -1;
    private ImageView ivRightBtn;
    private int[] number = {0, 0, 0};
    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(MsgBroadcast.ACTION_MSG_NUM_UPDATE_NEW_FRIEND)) {// 更新消息数量
                Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, Constants.ID_NEW_FRIEND_MESSAGE);
                if (friend != null && friend.getUnReadNum() > 0) {
                    MainActivity activity = (MainActivity) getActivity();
                    myTab.updateByIndex(2, friend.getUnReadNum());
                    if (vpContent.getCurrentItem() != 2) {
                        activity.updateNewFriendMsgNum(friend.getUnReadNum());// 更新底部Tab栏通讯录角标
                        number[2] = friend.getUnReadNum();
                        myTab.update(number);
                    } else {
                        activity.updateNewFriendMsgNum(0);// 更新底部Tab栏通讯录角标
                        number[2] = 0;
                        myTab.update(number);
                    }
                }
            }
        }
    };
    private String mLoginUserId = "";
    private NewFriendFragment mFriendFragment;


    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        mLoginUserId = coreManager.getSelf().getUserId();
        vpContent = findViewById(R.id.vp_content);
        vpContent.setOffscreenPageLimit(3);
        PagerAdapter pagerAdapter = new MainPageAdapter(getFragmentManager(), initFragment());
        vpContent.setAdapter(pagerAdapter);
        initActionBar();
        event();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MsgBroadcast.ACTION_MSG_NUM_UPDATE_NEW_FRIEND);
        getActivity().registerReceiver(mUpdateReceiver, intentFilter);

        int mNewContactsNumber = PreferenceUtils.getInt(getActivity(), Constants.NEW_CONTACTS_NUMBER + mLoginUserId, 0);
        MainActivity activity = (MainActivity) getActivity();
        activity.updateNewFriendMsgNum(mNewContactsNumber);// 更新底部Tab栏通讯录角标
        number[2] = mNewContactsNumber;
        myTab.update(number);
    }

    private List<Fragment> initFragment() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new ContactFragment());
        fragments.add(new GroupFragment());
        mFriendFragment = new NewFriendFragment();
        fragments.add(mFriendFragment);
        return fragments;
    }

    private void event() {
        myTab.setListener(i -> vpContent.setCurrentItem(i));
        vpContent.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                myTab.selectCurrentItem(i);
                currentTabIndex = i;
                if (i == 2) {
                    PreferenceUtils.putInt(getActivity(), Constants.NEW_CONTACTS_NUMBER + mLoginUserId, 0);
                    Friend mNewFriend = FriendDao.getInstance().getFriend(mLoginUserId, Constants.ID_NEW_FRIEND_MESSAGE);
                    if (mNewFriend != null) {
                        myTab.updateByIndex(2, 0);
                        mNewFriend.setUnReadNum(0);
                        MainActivity activity = (MainActivity) getActivity();
                        if (activity != null) {
                            activity.updateNewFriendMsgNum(0);// 更新底部Tab栏通讯录角标
                            number[2] = 0;
                            myTab.update(number);
                        }
                    }
                    if (mFriendFragment != null) {
                        mFriendFragment.visibleChange(true);
                    }
                } else {
                    if (mFriendFragment != null) {
                        mFriendFragment.visibleChange(false);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentTabIndex != -1 && myTab != null) {
            myTab.setCurrentTabColor(currentTabIndex);
        }
    }

    private void initActionBar() {
        myTab = findViewById(R.id.my_tab);
        ivRightBtn = findViewById(R.id.iv_title_right);
        ivRightBtn.setVisibility(View.VISIBLE);
        ivRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserAddActivity.class);
                startActivity(intent);
            }
        });
        if (coreManager.getConfig().isHideSearchFriend || (coreManager.getConfig().ordinaryUserCannotSearchFriend && (coreManager.getSelf().getRole() == null || coreManager.getSelf().getRole().size() == 0))) {
            ivRightBtn.setVisibility(View.GONE);
        } else {
            ivRightBtn.setVisibility(View.VISIBLE);
        }
    }

    private class MainPageAdapter extends FragmentPagerAdapter {
        private List<Fragment> mFragments;

        public MainPageAdapter(FragmentManager fragmentManager, List<Fragment> fragments) {
            super(fragmentManager);
            mFragments = fragments;
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public Fragment getItem(int i) {
            return mFragments.get(i);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(mUpdateReceiver);
    }

}
