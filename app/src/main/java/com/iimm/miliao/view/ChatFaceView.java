package com.iimm.miliao.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.BqBao;
import com.iimm.miliao.bean.CustomBqBaoEntity;
import com.iimm.miliao.bean.collection.Collectiion;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.ui.emoji.MyBqFragment;
import com.iimm.miliao.ui.emoji.MyCustomBqFragment;
import com.iimm.miliao.ui.emoji.MySmallBqFragment;
import com.iimm.miliao.util.AppUtils;
import com.iimm.miliao.util.DisplayUtil;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;


/**
 * 表情界面
 *
 * @author Administrator
 */
public class ChatFaceView extends RelativeLayout {
    public static final String MY_BQ_INFO_CACHE = "my_bq_Info"; //我添加的表情
    public static final String MY_C_BQ_INFO_CACHE = "my_c_bq_Info"; //收藏的表情
    private static final String TAG = "ChatFaceView";
    private Context mContext;
    private ViewPager vpContent;
    private MyVpBqAdapter myVpBqAdapter;
    private BqKeyBoardListener listener;
    private ImageView ivAdd;
    private TabLayout tl_menu;
    private List<Fragment> fragments;
    private int bigBqPadding = DisplayUtil.dip2px(getContext(), 5);
    private float menuWidth = DisplayUtil.dip2px(getContext(), 36.5f);
    private FragmentManager supportFragmentManager;
    private BroadcastReceiver refreshCollectionListBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getMyCustomList();
        }
    };

    public ChatFaceView(Context context) {
        super(context);
        init(context);
    }

    public ChatFaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChatFaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @SuppressWarnings("deprecation")
    private void init(Context context) {
        mContext = context;
        LayoutInflater.from(mContext).inflate(R.layout.chat_face_view, this);
        vpContent = (ViewPager) findViewById(R.id.vp_content);
        ivAdd = findViewById(R.id.iv_add);
        tl_menu = findViewById(R.id.tab_menu);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getContext().registerReceiver(
                refreshCollectionListBroadcast, new IntentFilter(com.iimm.miliao.broadcast.OtherBroadcast.CollectionRefresh));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(refreshCollectionListBroadcast);
    }

    /**
     * 初始化view
     */
    public void initView(FragmentManager supportFragmentManager, MySmallBqListener mySmallBqListener, CustomBqListener customBqListener, BqKeyBoardListener bqKeyBoardListener) {
        this.supportFragmentManager = supportFragmentManager;
        MySmallBqFragment smallBqFragment = new MySmallBqFragment();
        smallBqFragment.setMySmallBqListener(mySmallBqListener);
        MyCustomBqFragment customBqFragment = new MyCustomBqFragment();
        customBqFragment.setContent(null);
        customBqFragment.setListener(customBqListener);
        if (fragments == null) {
            fragments = new ArrayList<>();
        }
        fragments.add(0, smallBqFragment);
        fragments.add(1, customBqFragment);
        if (myVpBqAdapter == null) {
            myVpBqAdapter = new MyVpBqAdapter(supportFragmentManager, fragments);
        } else {
            myVpBqAdapter.notifyDataSetChanged();
        }
        vpContent.setAdapter(myVpBqAdapter);
        tl_menu.setupWithViewPager(vpContent, true);
        this.listener = bqKeyBoardListener;
        initMenu(myVpBqAdapter.getCount(), fragments);
        event();
    }

    private void event() {
        tl_menu.setTabGravity(Gravity.LEFT);
        tl_menu.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //  tab.get().setBackgroundColor(Color.parseColor("#70AD47"));
                vpContent.setCurrentItem(tab.getPosition());
                if (tab.getCustomView() != null) {
                    tab.getCustomView().setBackgroundColor(getResources().getColor(R.color.keyboadback));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getCustomView() != null) {
                    tab.getCustomView().setBackgroundColor(getResources().getColor(R.color.white));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        //了解源码得知 线的宽度是根据 tabView的宽度来设置的
        tl_menu.post(new Runnable() {
            @Override
            public void run() {
                try {
                    //拿到tabLayout的mTabStrip属性
                    Field slidingTabIndicatorField = tl_menu.getClass().getDeclaredField("slidingTabIndicator");
                    slidingTabIndicatorField.setAccessible(true);
                    LinearLayout mTabStrip = (LinearLayout) slidingTabIndicatorField.get(tl_menu);
                    for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                        View tabView = mTabStrip.getChildAt(i);
                        //拿到tabView的mTextView属性
                        Field mTextViewField = tabView.getClass().getDeclaredField("textView");
                        mTextViewField.setAccessible(true);
                        View customView = (View) mTextViewField.get(tabView);
                        tabView.setPadding(0, 0, 0, 0);
                        //因为我想要的效果是   字多宽线就多宽，所以测量mTextView的宽度
                        int width = 0;
                        width = customView.getWidth();
                        if (width == 0) {
                            customView.measure(0, 0);
                            width = customView.getMeasuredWidth();
                        }
                        //设置tab左右间距为10dp  注意这里不能使用Padding 因为源码中线的宽度是根据 tabView的宽度来设置的
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabView.getLayoutParams();
                        params.width = (int) menuWidth;//直接固定死
                        params.leftMargin = 0;
                        params.rightMargin = 0;
                        tabView.setLayoutParams(params);
                        tabView.invalidate();
                    }

                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        });
        ivAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    listener.ivAddClick(v);
                }
            }
        });
        if (null != tl_menu.getTabAt(0)) {
            tl_menu.getTabAt(0).getCustomView().setBackgroundColor(getResources().getColor(R.color.keyboadback));
        }

    }


    private void initMenu(int count, List<Fragment> fragments) {
        tl_menu.removeAllTabs();
        for (int i = 0; i < count; i++) {
            if (i == 0) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.bq_tab_small_view, null);
                ImageView icon = view.findViewById(R.id.iv_icon);
                if (AppUtils.isContextExisted(getContext()) && icon != null) {
                    Glide.with(getContext()).load(R.mipmap.bq_small_bg).into(icon);
                }
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams((int) menuWidth, ViewGroup.LayoutParams.MATCH_PARENT);
                view.setLayoutParams(layoutParams);
                tl_menu.addTab(tl_menu.newTab().setCustomView(view), i);
            } else if (i == 1) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.bq_tab_small_view, null);
                ((ImageView) view.findViewById(R.id.iv_icon)).setImageResource(R.mipmap.bq_heart);
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams((int) menuWidth, ViewGroup.LayoutParams.MATCH_PARENT);
                view.setLayoutParams(layoutParams);
                tl_menu.addTab(tl_menu.newTab().setCustomView(view), i);
            } else {
                MyBqFragment fragment = (MyBqFragment) fragments.get(i);
                View view = LayoutInflater.from(getContext()).inflate(R.layout.bq_tab_small_view, null);
                ImageView icon = view.findViewById(R.id.iv_icon);
                icon.setPadding(bigBqPadding, bigBqPadding, bigBqPadding, bigBqPadding);
                if (AppUtils.isContextExisted(getContext()) && icon != null) {
                    Glide.with(getContext()).load(fragment.getContent().getEmoPackThumbnailUrl()).into(icon);
                }
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams((int) menuWidth, ViewGroup.LayoutParams.MATCH_PARENT);
                view.setLayoutParams(layoutParams);
                tl_menu.addTab(tl_menu.newTab().setCustomView(view), i);
            }
        }
        vpContent.setOffscreenPageLimit(count);
    }


    /**
     * 添加一组 页面
     *
     * @param supportFragmentManager
     * @param fragments
     * @param isClearOld
     */
    public void addANewPage(FragmentManager supportFragmentManager, List<Fragment> fragments, boolean isClearOld) {
        if (isClearOld) {
            List<Fragment> deleteFragment = new ArrayList<>();
            for (int i = 0; i < this.fragments.size(); i++) {
                if (i > 1) {
                    deleteFragment.add(this.fragments.get(i));
                }
            }
            this.fragments.removeAll(deleteFragment);
        }
        this.fragments.addAll(fragments);
        updateMenu(supportFragmentManager, isClearOld);
    }


    /**
     * 更新菜单
     *
     * @param supportFragmentManager
     * @param isInit
     */
    private void updateMenu(FragmentManager supportFragmentManager, boolean isInit) {
        if (myVpBqAdapter == null) {
            myVpBqAdapter = new MyVpBqAdapter(supportFragmentManager, fragments);
        } else {
            myVpBqAdapter.notifyDataSetChanged();
        }
        if (isInit) {
            for (int i = 0; i < fragments.size(); i++) {
                if (i == 0) {
                    MySmallBqFragment mySmallBqFragment = (MySmallBqFragment) fragments.get(i);
                    mySmallBqFragment.init();
                } else if (i == 1) {
                    MyCustomBqFragment myCustomBqFragment = (MyCustomBqFragment) fragments.get(i);
                    myCustomBqFragment.init();
                } else {
                    MyBqFragment myBqFragment = (MyBqFragment) fragments.get(i);
                    myBqFragment.init();
                }
            }
        }
        initMenu(fragments.size(), fragments);
        event();
    }


    /**
     * 更新自己收藏的表情包
     *
     * @param dataDefine_bq
     */
    public void updateCustomBq(List<Collectiion> dataDefine_bq) {
        if (fragments.get(1) instanceof MyCustomBqFragment) {

            MyCustomBqFragment customBqFragment = (MyCustomBqFragment) fragments.get(1);
            customBqFragment.updateCustomContent(new CustomBqBaoEntity(dataDefine_bq));
        }
    }


    /**
     * 自定义的表情包
     */
    public void getMyCustomList() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(getContext()).accessToken);
        params.put("userId", CoreManager.requireSelf(getContext()).getUserId());
        HttpUtils.get().url(CoreManager.requireConfig(getContext()).Collection_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<Collectiion>(Collectiion.class) {
                    @Override
                    public void onResponse(ArrayResult<Collectiion> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            List<Collectiion> mCollection = result.getData();
                          /*  PreferenceUtils.putString(getContext()
                                    , ChatFaceView.MY_C_BQ_INFO_CACHE + "_" + CoreManager.requireSelf(MyApplication.getInstance()).getUserId()
                                    , JSON.toJSONString(mCollection));*/
                            updateCustomBq(mCollection);
                        } else {
                            ToastUtil.showToast(MyApplication.getContext(), result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(MyApplication.getContext());
                    }
                });
    }


    /**
     * 获取表情包数据
     *
     * @param isClearOld
     * @param coreManager
     * @param myBqListener
     */
    public void initMyBqBao(final boolean isClearOld, CoreManager coreManager, MyBqListener myBqListener) {
        // 获取添加的表情列表
//        String myBqInfo = PreferenceUtils.getString(getContext(), ChatFaceView.MY_C_BQ_INFO_CACHE + "_" + CoreManager.requireSelf(MyApplication.getInstance()).getUserId());
//        if (!TextUtils.isEmpty(myBqInfo)) {
//            List<Collectiion> mCollection = JSON.parseArray(myBqInfo, Collectiion.class);
//            getMyCustomBq(isClearOld, coreManager, myBqListener, mCollection);
//        } else {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(getContext()).accessToken);
        params.put("userId", CoreManager.requireSelf(getContext()).getUserId());
        HttpUtils.get().url(CoreManager.requireConfig(getContext()).Collection_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<Collectiion>(Collectiion.class) {
                    @Override
                    public void onResponse(ArrayResult<Collectiion> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            List<Collectiion> mCollection = result.getData();
//                                PreferenceUtils.putString(getContext()
//                                        , ChatFaceView.MY_C_BQ_INFO_CACHE + "_" + CoreManager.requireSelf(MyApplication.getInstance()).getUserId()
//                                        , JSON.toJSONString(mCollection));
                            if (mCollection == null) {
                                mCollection = new ArrayList<>();
                            }
                            getMyCustomBq(isClearOld, coreManager, myBqListener, mCollection);
                        } else {
                            ToastUtil.showToast(MyApplication.getContext(), result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(MyApplication.getContext());
                    }
                });
//        }
    }

    private void getMyCustomBq(boolean isClearOld, CoreManager coreManager, MyBqListener myBqListener, List<Collectiion> mCollection) {
//        String myBqInfo = PreferenceUtils.getString(getContext(), ChatFaceView.MY_BQ_INFO_CACHE + "_" + CoreManager.requireSelf(MyApplication.getInstance()).getUserId());
//        if (!TextUtils.isEmpty(myBqInfo)) {
        //获取网络 加载下载的表情包
        Map<String, String> par = new HashMap<>();
        par.put("access_token", coreManager.getSelfStatus().accessToken);
        par.put("pageIndex", "0");
        par.put("pageSize", "1000000");
        HttpUtils.get().url(coreManager.getConfig().LOAD_MY_BQ)
                .params(par)
                .build()
                .execute(new BaseCallback<String>(String.class) {
                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        try {
//                                PreferenceUtils.putString(getContext()
//                                        , ChatFaceView.MY_BQ_INFO_CACHE + "_" + CoreManager.requireSelf(MyApplication.getInstance()).getUserId()
//                                        , result.getData());
                            handlerMyBqInfo(mCollection, result.getData(), isClearOld, myBqListener);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            initBqKeyBoard(new ArrayList<BqBao>(), mCollection, isClearOld, myBqListener);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        initBqKeyBoard(new ArrayList<BqBao>(), mCollection, isClearOld, myBqListener);
                    }
                });
//        } else {
//            try {
//                handlerMyBqInfo(mCollection, myBqInfo, isClearOld, myBqListener);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
    }

    /**
     * 处理我的表情数据
     *
     * @param mCollection
     * @param s
     * @param isClearOld
     * @throws JSONException
     */
    private void handlerMyBqInfo(List<Collectiion> mCollection, String s, boolean isClearOld, MyBqListener myBqListener) throws JSONException {
        List<BqBao> dataCustom_list = new ArrayList<>();
        List<Collectiion> dataDefine_bq = mCollection;
        JSONArray js = new JSONArray(s);
        if (js.length() > 0) {
            JSONObject info = js.getJSONObject(0);
            JSONArray imEmojiStoe = info.getJSONArray("imEmojiStore");
            String id = info.getString("id");
            String userId = info.getString("userId");
            for (int i = 0; i < imEmojiStoe.length(); i++) {
                JSONObject jsonObject = imEmojiStoe.getJSONObject(i);
                try {
                    BqBao bqBao = JSON.parseObject(jsonObject.toString(), BqBao.class);
                    if (bqBao == null || bqBao.getImEmojiStoreListInfo() == null) {
                        continue;
                    }
                    bqBao.setId(id);
                    dataCustom_list.add(bqBao);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        initBqKeyBoard(dataCustom_list, dataDefine_bq, isClearOld, myBqListener);
    }

    /**
     * 初始化 bq 表情键盘
     *
     * @param dataCustom_list
     * @param dataDefine_bq
     * @param isClearOld
     */
    private void initBqKeyBoard(List<BqBao> dataCustom_list, List<Collectiion> dataDefine_bq, boolean isClearOld, MyBqListener myBqListener) {
        List<Fragment> fragments = new ArrayList<>();
        for (int i = 0; i < dataCustom_list.size(); i++) {
            MyBqFragment myBqFragment = new MyBqFragment();
            myBqFragment.setContent(dataCustom_list.get(i));
            Bundle bundle = new Bundle();
            bundle.putInt("position", i);
            myBqFragment.setMyBqListener(myBqListener);
            fragments.add(myBqFragment);
        }
        addANewPage(supportFragmentManager, fragments, isClearOld);
        if (!isClearOld) {
            updateCustomBq(dataDefine_bq);
        }
    }


    /**
     * 更新自定义表情包
     */
    public void updateCustomBq() {
//        PreferenceUtils.putString(getContext(), ChatFaceView.MY_BQ_INFO_CACHE + "_" + CoreManager.requireSelf(MyApplication.getInstance()).getUserId(), "");
        getMyCustomList();
    }

    public void getMyBqBao(boolean b, CoreManager coreManager, MyBqListener myBqListener) {
//        PreferenceUtils.putString(getContext(), ChatFaceView.MY_BQ_INFO_CACHE + "_" + CoreManager.requireSelf(MyApplication.getInstance()).getUserId(), "");
        initMyBqBao(b, coreManager, myBqListener);
    }


    private static class MyVpBqAdapter extends FragmentStatePagerAdapter {
        List<Fragment> fragments;

        MyVpBqAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

    }


    public interface BqKeyBoardListener {
        void ivAddClick(View v);
    }

    public interface MySmallBqListener {
        void onEmojiClick(SpannableString ss);
    }

    public interface CustomBqListener {

        void onClickViewSendBq(boolean b, String custom, String customBq, String defineEmoUrl, String s);

        void goCustomBqManageActivity();
    }

    public interface MyBqListener {

        void onClickViewSendBq(boolean b, String id, String emoMean, String fileUrl, String thumbnailUrl);
    }


    @Override
    public void setVisibility(int visibility) {
        if (visibility == View.VISIBLE) {
            this.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.key_board_v));
        }
        super.setVisibility(visibility);
    }

    public interface BqMustInfoListener {
        FragmentManager getFragmentManager();

        BqKeyBoardListener getBqKeyBoardListener();

        CoreManager getCoreManager();

        CustomBqListener getCustomBqListener();

        MyBqListener getMyBqListener();
    }


}
