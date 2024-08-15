package com.iimm.miliao.ui.circle;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.iimm.miliao.databinding.AcctivityBusinessCircleSearchBinding;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.adapter.PublicMessageSearchAdapter;
import com.iimm.miliao.adapter.SearchUserAdapter;
import com.iimm.miliao.adapter.SearchUserHorizontalAdapter;
import com.iimm.miliao.bean.AttentionUser;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.circle.PublicMessage;
import com.iimm.miliao.databinding.BusinessCircleHeadBinding;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.OnCompleteListener2;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.sortlist.BaseSortModel;
import com.iimm.miliao.sortlist.SortHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ToolUtils;
import com.iimm.miliao.view.TimePopupWindow;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JVCideoPlayerStandardSecond;
import fm.jiecao.jcvideoplayer_lib.MessageEvent;
import fm.jiecao.jcvideoplayer_lib.MessageRefreshEvent;
import okhttp3.Call;

/**
 * MrLiu253@163.com
 * 搜索朋友圈 选择朋友
 *
 * @time 2020-06-03
 */
public class BusinessCircleSearchActivity extends BaseActivity {
    private static int PAGER_SIZE = 5;

    //1 匹配到本地联系人 显示发布时间
    //2 匹配到本地联系人 选择发布时间
    //3 搜索关键字 显示发布人和发布时间
    //4 搜索关键字 选择了横向联系人  选择了发布人  显示发布时间
    //5 点击发布人 选择了发布人  显示发布时间
    //6 点击发布人 选择了发布人和发布时间
    //7 没有搜索到
    private int mTypeSearch;
    private String searchTime = "";


    private String mUserId = "", mType = "", mTime = "", mKey = "";
    private String mName = "";
    private int mPage;
    private boolean mMore;
    private AcctivityBusinessCircleSearchBinding mBinding;
    private String mLoginUserId;

    private List<Friend> mSearchFriends;
    private SearchUserAdapter mSearchUserAdapter;

    private List<Friend> mHorizontalList;
    private SearchUserHorizontalAdapter mSearchUserHorizontalAdapter;

    private List<PublicMessage> mMessages;
    private PublicMessageSearchAdapter mPublicMessageAdapter;
    private String mString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.acctivity_business_circle_search);
        getSupportActionBar().hide();
        mLoginUserId = coreManager.getSelf().getUserId();
        initView();
        initClick();
        upDataFriend();
    }

    public static void showBusinessCircleSearchActivity(Context context) {
        Intent intent = new Intent(context, BusinessCircleSearchActivity.class);
        context.startActivity(intent);
    }

    private void initView() {

        BusinessCircleHeadBinding binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.business_circle_head, null, false);
        mBinding.discoverListview.getRefreshableView().addHeaderView(binding.getRoot());

        mSearchFriends = new ArrayList<>();
        mSearchUserAdapter = new SearchUserAdapter(this, mSearchFriends);
        mBinding.searchUserRl.setLayoutManager(new LinearLayoutManager(this));
        mBinding.searchUserRl.setAdapter(mSearchUserAdapter);

        mHorizontalList = new ArrayList<>();
        mSearchUserHorizontalAdapter = new SearchUserHorizontalAdapter(this, mHorizontalList);
        mBinding.horizontalUserRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mBinding.horizontalUserRv.setAdapter(mSearchUserHorizontalAdapter);


        mMessages = new ArrayList<>();
        mPublicMessageAdapter = new PublicMessageSearchAdapter(this, coreManager, mMessages);
        mBinding.discoverListview.getRefreshableView().setAdapter(mPublicMessageAdapter);


        EventBus.getDefault().register(this);
    }

    private void initClick() {
        //点击了某一条目
        mBinding.discoverListview.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mMessages != null && mMessages.size() > position - 2) {
                    Intent intent = new Intent(getApplicationContext(), BusinessCircleActivityNew.class);
                    intent.putExtra(AppConstant.EXTRA_CIRCLE_TYPE, AppConstant.CIRCLE_TYPE_PERSONAL_SPACE);
                    intent.putExtra(AppConstant.EXTRA_USER_ID, mMessages.get(position - 2).getUserId());
                    intent.putExtra(AppConstant.EXTRA_NICK_NAME, "动态详情");
                    intent.putExtra("pinglun", "");
                    intent.putExtra("dianzan", "");
                    intent.putExtra("isdongtai", true);
                    intent.putExtra("messageid", mMessages.get(position - 2).getMessageId());
                    intent.putExtra("message", true);
                    startActivity(intent);
                }
            }
        });
        //点击选择搜索相关联系人
        mSearchUserAdapter.setOnItemClick(
                new SearchUserAdapter.onItemClick() {
                    @Override
                    public void itemClick(int pos) {
                        if (mSearchFriends != null && mSearchFriends.size() > pos) {
                            mUserId = mSearchFriends.get(pos).getUserId();
                            mPage = 0;
                            mMore = false;
                            mKey = "";
                            mTypeSearch = 1;
                            mString = TextUtils.isEmpty(mSearchFriends.get(pos).getRemarkName()) ? mSearchFriends.get(pos).getNickName() : mSearchFriends.get(pos).getRemarkName();
                            mBinding.searchEdit.setText(mString);
                            mBinding.searchEdit.setEnabled(false);
                            mBinding.searchEdit.setTextColor(ContextCompat.getColor(BusinessCircleSearchActivity.this, R.color.color_00));

                            getBusinessCircle(true, mUserId, "", "", "");
                        }
                    }
                }
        );
        //选择横向联系人
        mSearchUserHorizontalAdapter.setOnItemClick(new SearchUserHorizontalAdapter.onItemClick() {
            @Override
            public void itemClick(int pos) {
                if (mHorizontalList != null && mHorizontalList.size() > pos) {
                    mUserId = mHorizontalList.get(pos).getUserId();
                    mPage = 0;
                    mMore = false;
                    mKey = "";
                    mName = TextUtils.isEmpty(mHorizontalList.get(pos).getRemarkName()) ? mHorizontalList.get(pos).getNickName() : mHorizontalList.get(pos).getRemarkName();
                    if (mTypeSearch == 3) {
                        mTypeSearch = 7;
                    } else if (mTypeSearch == 4) {
                        mTypeSearch = 8;
                    }
                    getBusinessCircle(true, mHorizontalList.get(pos).getUserId(), mType, mTime, "");

                }
            }
        });
        mBinding.discoverListview.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                mPage++;
                getBusinessCircle(false, mUserId, mType, mTime, mKey);
            }
        });
        //筛选发布人
        mBinding.filterConditionPeopleLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectFriendActivity.showSelectFriendActivity(BusinessCircleSearchActivity.this);
            }
        });
        //筛选时间
        mBinding.filterConditionTimeLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePopupWindow timePopupWindow = new TimePopupWindow(BusinessCircleSearchActivity.this, new TimePopupWindow.onSelectedTimeClick() {
                    @Override
                    public void onSelectedTime(int type, String monthStr, String time) {
                        searchTime = time;
                        mPage = 0;
                        mMore = false;
                        mType = type+"";
                        mTime =monthStr;
                        //1 匹配到本地联系人 显示发布时间
                        //2 匹配到本地联系人 选择发布时间

                        //3 搜索关键字 显示发布人和发布时间 横向联系人
                        //4 搜索关键字 显示发布人  选择发布时间 横向联系人
                        //5 搜索关键字 选择了发布人  选择了发布时间
                        //6 搜索关键字 选择了发布人  显示发布时间
                        //7 搜索关键字 选择了横向联系人  选择了发布人  显示发布时间
                        //8 搜索关键字 选择了横向联系人  选择了发布人  选择了发布时间
                        if (mTypeSearch == 1) {
                            mTypeSearch = 2;
                        } else if (mTypeSearch == 3) {
                            mTypeSearch = 4;
                        } else if (mTypeSearch == 6) {
                            mTypeSearch = 5;
                        } else if (mTypeSearch == 7) {
                            mTypeSearch = 8;
                        }
                        getBusinessCircle(true, mUserId, type + "", monthStr, mKey);
                    }
                });
                timePopupWindow.showAtLocation(BusinessCircleSearchActivity.this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 20);
            }
        });
        //清空输入框
        mBinding.searchEditDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete();
            }
        });
        mBinding.tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mBinding.searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //点击搜索的时候隐藏软键盘
                    mKey = mBinding.searchEdit.getText().toString().trim();
                    if (TextUtils.isEmpty(mKey)) {
                        ToastUtil.showToast("请输入搜索内容");
                    } else {
                        mMore = false;
                        mPage = 0;
                        mTypeSearch = 3;
                        getBusinessCircle(true, "", "", "", mBinding.searchEdit.getText().toString().trim());
                    }
                    return true;
                }

                return false;
            }
        });
        mBinding.searchEdit.addTextChangedListener(new TextWatcher() {
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
                if (TextUtils.isEmpty(mUserId)){
                    query(str);
                }
                if (!TextUtils.isEmpty(str) && str.length() > 0) {
                    mBinding.searchEditDelete.setVisibility(View.VISIBLE);
                } else {
                    mBinding.searchEditDelete.setVisibility(View.INVISIBLE);
                }

            }
        });

    }

    //1 匹配到本地联系人 显示发布时间
    //2 匹配到本地联系人 选择发布时间

    //3 搜索关键字 显示发布人和发布时间 横向联系人
    //4 搜索关键字 显示发布人  选择发布时间 横向联系人
    //5 搜索关键字 选择了发布人  选择了发布时间
    //6 搜索关键字 选择了发布人  显示发布时间
    //7 搜索关键字 选择了横向联系人  选择了发布人  显示发布时间
    //8 搜索关键字 选择了横向联系人  选择了发布人  选择了发布时间
    public void showPeopleOrTime() {

        Log.i("OkHttp", "第几个类型" + mTypeSearch);
        //只显示时间
        if (mTypeSearch == 1) {
            mBinding.filterConditionLl.setVisibility(View.VISIBLE);
            mBinding.filterConditionTimeLl.setVisibility(View.VISIBLE);
            mBinding.filterConditionTimeV.setVisibility(View.VISIBLE);
            mBinding.filterConditionPeopleLl.setVisibility(View.GONE);
            mBinding.filterConditionTimeTv.setText("发布时间");


        } else if (mTypeSearch == 2) {
            mBinding.filterConditionLl.setVisibility(View.VISIBLE);
            mBinding.filterConditionTimeLl.setVisibility(View.VISIBLE);
            mBinding.filterConditionTimeV.setVisibility(View.VISIBLE);
            mBinding.filterConditionTimeTv.setText(searchTime);

            mBinding.filterConditionPeopleLl.setVisibility(View.GONE);
        } else if (mTypeSearch == 3) {
            mBinding.filterConditionLl.setVisibility(View.VISIBLE);
            mBinding.filterConditionPeopleLl.setVisibility(View.VISIBLE);
            mBinding.filterConditionTimeLl.setVisibility(View.VISIBLE);
            mBinding.filterConditionTimeV.setVisibility(View.GONE);
            mBinding.filterConditionPeopleTv.setText("发布人");
            mBinding.filterConditionTimeTv.setText("发布时间");
            queryHorizontal(mKey);
        } else if (mTypeSearch == 4) {
            mBinding.filterConditionLl.setVisibility(View.VISIBLE);
            mBinding.filterConditionPeopleLl.setVisibility(View.VISIBLE);
            mBinding.filterConditionTimeLl.setVisibility(View.VISIBLE);
            mBinding.filterConditionTimeV.setVisibility(View.GONE);
            mBinding.filterConditionPeopleTv.setText("发布人");
            mBinding.filterConditionTimeTv.setText(searchTime);
            queryHorizontal(mKey);
        } else if (mTypeSearch == 5) {
            mBinding.filterConditionLl.setVisibility(View.VISIBLE);
            mBinding.filterConditionPeopleLl.setVisibility(View.VISIBLE);
            mBinding.filterConditionTimeLl.setVisibility(View.VISIBLE);
            mBinding.filterConditionTimeV.setVisibility(View.GONE);
            mBinding.filterConditionPeopleTv.setText(mName);
            mBinding.filterConditionTimeTv.setText(searchTime);

        } else if (mTypeSearch == 6) {
            mBinding.filterConditionLl.setVisibility(View.VISIBLE);
            mBinding.filterConditionPeopleLl.setVisibility(View.VISIBLE);
            mBinding.filterConditionTimeLl.setVisibility(View.VISIBLE);
            mBinding.filterConditionTimeV.setVisibility(View.GONE);
            mBinding.filterConditionPeopleTv.setText(mName);
            mBinding.filterConditionTimeTv.setText("发布时间");

        } else if (mTypeSearch == 7) {
            mBinding.filterConditionLl.setVisibility(View.VISIBLE);
            mBinding.filterConditionPeopleLl.setVisibility(View.VISIBLE);
            mBinding.filterConditionTimeLl.setVisibility(View.VISIBLE);
            mBinding.filterConditionTimeV.setVisibility(View.GONE);
            mBinding.filterConditionPeopleTv.setText(mName);
            mBinding.filterConditionTimeTv.setText("发布时间");
            queryHorizontal("");
        } else if (mTypeSearch == 8) {
            mBinding.filterConditionLl.setVisibility(View.VISIBLE);
            mBinding.filterConditionPeopleLl.setVisibility(View.VISIBLE);
            mBinding.filterConditionTimeLl.setVisibility(View.VISIBLE);
            mBinding.filterConditionTimeV.setVisibility(View.GONE);
            mBinding.filterConditionPeopleTv.setText(mName);
            mBinding.filterConditionTimeTv.setText(searchTime);
            queryHorizontal("");
        } else {
            mBinding.filterConditionLl.setVisibility(View.GONE);
            mBinding.filterConditionPeopleLl.setVisibility(View.GONE);
            mBinding.filterConditionTimeLl.setVisibility(View.GONE);
            mBinding.filterConditionTimeV.setVisibility(View.GONE);
            mBinding.filterConditionPeopleTv.setText("发布人");
            mBinding.filterConditionTimeTv.setText("发布时间");
        }

        //1 匹配到本地联系人 显示发布时间
        //2 匹配到本地联系人 选择发布时间

        //3 搜索关键字 显示发布人和发布时间
        //4 搜索关键字 选择了发布人  显示发布时间
        //5 搜索关键字 选择了发布人  选择了发布时间
        //6 搜索关键字 选择了横向联系人  选择了发布人  显示发布时间
        //7 搜索关键字 选择了横向联系人  选择了发布人  选择了发布时间
        if (mTypeSearch == 1 || mTypeSearch == 2 || mTypeSearch == 3 || mTypeSearch == 4 || mTypeSearch == 5 || mTypeSearch == 6 || mTypeSearch == 7 || mTypeSearch == 8) {
            query("");
            showList(true);
            mBinding.searchEdit.setEnabled(false);
            mBinding.searchEdit.setTextColor(ContextCompat.getColor(BusinessCircleSearchActivity.this, R.color.color_00));

        }
    }

    //清空
    public void delete() {
        mUserId = "";
        mType = "";
        mTime = "";
        mKey = "";
        mPage = 0;
        mMore = false;
        mName = "";
        searchTime = "";
        mTypeSearch = 0;
        queryHorizontal("");
        mBinding.searchEdit.setText("");//清空输入内容
        mBinding.searchEdit.setEnabled(true);//可以输入
        query("");//清空输入框
        showView(false);//清空展位图
        showPeopleOrTime();//清空筛选
        showList(false);//隐藏列表
        mMessages.clear();//清空列表
        mPublicMessageAdapter.notifyDataSetChanged();//刷新界面
    }

    //是否显示站位图
    public void showView(boolean isView) {
        if (isView) {
            mBinding.placeholderV.setVisibility(View.VISIBLE);
        } else {
            mBinding.placeholderV.setVisibility(View.GONE);
        }
    }

    //是否显示朋友圈列表
    public void showList(boolean isList) {
        if (isList) {
            mBinding.searchBusinessCircleCl.setVisibility(View.VISIBLE);
        } else {
            mBinding.searchBusinessCircleCl.setVisibility(View.GONE);
        }
    }


    //搜索框
    protected void query(String str) {
        //输入框内容不为空
        if (!TextUtils.isEmpty(str)) {
            List<Friend> friendList = FriendDao.getInstance().querySingleFriendByKey(coreManager.getSelf().getUserId(), str);
            //搜索到本地好友
            if (friendList != null && friendList.size() > 0) {
                mBinding.searchUserContentCl.setVisibility(View.VISIBLE);
                mSearchFriends.clear();
                mSearchFriends.addAll(friendList);
                mSearchUserAdapter.setCurrentSearchKey(str);
                mSearchUserAdapter.setData(str, mSearchFriends);
            } else {
                mBinding.searchUserContentCl.setVisibility(View.GONE);
                mSearchFriends.clear();
                mSearchUserAdapter.setData("", mSearchFriends);

            }
        } else {
            mBinding.searchUserContentCl.setVisibility(View.GONE);
            mSearchFriends.clear();
            mSearchUserAdapter.setData("", mSearchFriends);

        }


    }

    //搜索框匹配横向联系人
    protected void queryHorizontal(String str) {
        //输入框内容不为空
        if (!TextUtils.isEmpty(str)) {
            List<Friend> friendList = FriendDao.getInstance().querySingleFriendByKey(coreManager.getSelf().getUserId(), str);
            //搜索到本地好友
            if (friendList != null && friendList.size() > 0) {
                mBinding.horizontalUserCl.setVisibility(View.VISIBLE);
                mHorizontalList.clear();
                mHorizontalList.addAll(friendList);
                mSearchUserHorizontalAdapter.setCurrentSearchKey(str);
                mSearchUserHorizontalAdapter.setData(str, mHorizontalList);
            } else {
                mBinding.horizontalUserCl.setVisibility(View.GONE);
                mHorizontalList.clear();
                mSearchUserHorizontalAdapter.setData("", mHorizontalList);

            }
        } else {
            mBinding.horizontalUserCl.setVisibility(View.GONE);
            mHorizontalList.clear();
            mSearchUserHorizontalAdapter.setData("", mHorizontalList);

        }


    }

    /**
     * 获取当前朋友的朋友圈
     *
     * @param ismore //true初次加载
     * @param userId //用户id
     * @param type   时间类型 type =0 表示没有经过日期查询 1表示2020-06  2表示2020 3表示01  02  ......12
     * @param time   时间
     * @param key    关键字
     */
    private void getBusinessCircle(boolean ismore, String userId, String type, String time, String key) {

        if (mMore) {
            mBinding.discoverListview.setReleaseLabel(getString(R.string.tip_last_item));
            mBinding.discoverListview.setRefreshingLabel(getString(R.string.tip_last_item));
            refreshComplete();
            return;
        }
        // 使用这个对话框阻止其他操作，以免主线程读写数据库被阻塞anr,
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", TextUtils.isEmpty(userId) ? mLoginUserId : userId);
        params.put("type", type);
        params.put("monthStr", time);
        params.put("keyWord", key);
        params.put("pageIndex", mPage + "");
        params.put("pageSize", PAGER_SIZE + "");
        HttpUtils.get().url(coreManager.getConfig().MSG_WITH_CONDITION)
                .params(params)
                .build()
                .execute(new ListCallback<PublicMessage>(PublicMessage.class) {
                    @Override
                    public void onResponse(ArrayResult<PublicMessage> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(BusinessCircleSearchActivity.this, result)) {
                            List<PublicMessage> data = result.getData();
                            if (data != null) {
                                if (ismore) {
                                    mMessages.clear();
                                    mMessages.addAll(data);
                                    mPublicMessageAdapter.setCurrentSearchKey(mKey);
                                    mPublicMessageAdapter.notifyDataSetChanged();
                                } else {
                                    refreshComplete();
                                    mMessages.addAll(data);
                                    mPublicMessageAdapter.setCurrentSearchKey(mKey);
                                    mPublicMessageAdapter.notifyDataSetChanged();
                                }
                                if (data.size() < PAGER_SIZE) {
                                    mMore = true;
                                } else {
                                    mMore = false;
                                }
                            } else {
                                mMore = false;
                            }
                            if (mMessages.size() == 0) {
                                showView(true);
                            } else {
                                showView(false);
                            }
                            showPeopleOrTime();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(BusinessCircleSearchActivity.this, R.string.tip_server_error);
                    }
                });
    }

    /**
     * 停止刷新动画
     */
    private void refreshComplete() {
        mBinding.discoverListview.postDelayed(() -> mBinding.discoverListview.onRefreshComplete(), 200);
    }

    /**
     * 从服务端获取好友列表，更新数据库
     */
    private void upDataFriend() {
        // 使用这个对话框阻止其他操作，以免主线程读写数据库被阻塞anr,
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        HttpUtils.get().url(coreManager.getConfig().FRIENDS_ATTENTION_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<AttentionUser>(AttentionUser.class) {
                    @Override
                    public void onResponse(ArrayResult<AttentionUser> result) {
                        Log.e("ssssss", TimeUtils.getCurrentTime());
                        if (result.getResultCode() == 1) {
                            try {
                                FriendDao.getInstance().addAttentionUsers(coreManager.getSelf().getUserId(), result.getData(),
                                        new OnCompleteListener2() {

                                            @Override
                                            public void onLoading(int progressRate, int sum) {
                                            }

                                            @Override
                                            public void onCompleted() {
                                                Log.e("ssssss", TimeUtils.getCurrentTime());
                                                loadData();
                                            }
                                        });
                            } catch (SQLException e) {
                                DialogHelper.dismissProgressDialog();
                                e.printStackTrace();
                            }
                        } else {
                            DialogHelper.dismissProgressDialog();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
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

            });
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1090 && resultCode == RESULT_OK && data != null) {
            Friend friend = (Friend) data.getSerializableExtra("friend");
            if (friend != null) {
                mName = TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName();
                mUserId = friend.getUserId();
                mPage = 0;
                mMore = false;
                mKey = "";
                if (mTypeSearch == 4) {
                    mTypeSearch = 5;
                } else if (mTypeSearch == 3) {
                    mTypeSearch = 6;
                }
                queryHorizontal("");
                getBusinessCircle(true, mUserId, mType, mTime, "");

            }
        }


    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEvent message) {
        if (message.message.equals("prepare")) {// 准备播放视频，关闭语音播放
            mPublicMessageAdapter.stopVoice();
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageRefreshEvent message) {
        if (message.isRefresh()) {
            mPage = 0;
            mMore = false;
            if (TextUtils.isEmpty(mUserId)) {
                getBusinessCircle(true, "", mType, mTime, mKey);
            } else {
                getBusinessCircle(true, mUserId, mType, mTime, "");
            }
        }
    }

    @Override
    public void onBackPressed() {
        // 点返回键退出全屏视频，
        // 如果DiscoverFragment用在其他activity, 也要加上，
        if (JVCideoPlayerStandardSecond.backPress()) {
            JCMediaManager.instance().recoverMediaPlayer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        JCVideoPlayer.releaseAllVideos();
        if (mPublicMessageAdapter != null) {
            mPublicMessageAdapter.stopVoice();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 退出页面时关闭视频和语音，
        JCVideoPlayer.releaseAllVideos();
        if (mPublicMessageAdapter != null) {
            mPublicMessageAdapter.stopVoice();
        }
        EventBus.getDefault().unregister(this);
    }

}
