package com.iimm.miliao.ui.trill;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.circle.PublicMessage;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.base.BaseRecAdapter;
import com.iimm.miliao.ui.base.BaseRecViewHolder;
import com.iimm.miliao.ui.xrce.RecordxActivity;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.RecyclerSpace;
import com.iimm.miliao.util.StatusBarUtil;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fm.jiecao.jcvideoplayer_lib.VideotillManager;
import okhttp3.Call;


/**
 * 抖音模块
 */
public class TrillActivity extends BaseActivity implements View.OnClickListener {

    public static final boolean OPEN_COMM = false;   // 不可点击发布评论按钮
    public static final boolean OPEN_FRIEND = false; // 是否开启短视频加好友功能，使头像不可点击
    public static List<PublicMessage> urlList;
    private RecyclerView mPager;
    private ListVideoAdapter videoAdapter;
    //    private PagerSnapHelper snapHelper;
    private RecyclerView.LayoutManager layoutManager;
    private boolean isLoad;
    private boolean shouldReset = false;
    private int pagerIndex;
    private int position = -1;
    private BroadcastReceiver closeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
    private RelativeLayout rlTitle;
    private TextView tvTitle;
    private int mLable = 0;
    private ProxyAdapter proxyAdapter;
    public View.OnClickListener tabListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            int curr = -1;

            switch (v.getId()) {
                case R.id.ll_tab1:
                    curr = 1;
                    tvTitle.setText(R.string.video_food);
                    break;
                case R.id.ll_tab2:
                    curr = 2;
                    tvTitle.setText(R.string.video_scenery);
                    break;
                case R.id.ll_tab3:
                    curr = 3;
                    tvTitle.setText(R.string.video_culture);
                    break;
                case R.id.ll_tab4:
                    curr = 4;
                    tvTitle.setText(R.string.video_recreation);
                    break;
                case R.id.ll_tab5:
                    curr = 5;
                    tvTitle.setText(R.string.video_hotel);
                    break;
                case R.id.ll_tab6:
                    curr = 6;
                    tvTitle.setText(R.string.video_shopping);
                    break;
                case R.id.ll_tab7:
                    curr = 7;
                    tvTitle.setText(R.string.video_sport);
                    break;
                default:
                    curr = 0;
                    tvTitle.setText(R.string.video_all);
                    break;

            }


            if (mLable == curr) {
                return;
            }

            mLable = curr;
            pagerIndex = 0;
            loadData(true);
        }
    };

    private boolean showTitle = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setStatusBar(this, Color.parseColor("#151621"));
        setContentView(R.layout.activity_trill);
        getSupportActionBar().hide();
        // ImmersionBar.with(this).init();
        initData();
        initView();
        addListener();
        broadcast();
    }

    private void addListener() {

        mPager.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {

                //当前RecyclerView显示出来的最后一个的item的position
                int lastPosition = -1;

                //当前状态为停止滑动状态SCROLL_STATE_IDLE时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                    if (layoutManager instanceof GridLayoutManager) {
                        //通过LayoutManager找到当前显示的最后的item的position
                        lastPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
                    } else if (layoutManager instanceof LinearLayoutManager) {
                        lastPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                    }
                    //时判断界面显示的最后item的position是否等于itemCount总数-1也就是最后一个item的position
                    //如果相等则说明已经滑动到最后了
                    if (lastPosition == recyclerView.getLayoutManager().getItemCount() - 1 && !isLoad) {
                        Log.e(TAG, "onScrollStateChanged: 滑动到底了");
                        pagerIndex++;
                        position = lastPosition;
                        loadData(false);
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                float distance = getScollYDistance();
                if (dy > 2 && distance > 35) {
                    startTranslateAnim(false);
                }

                if (dy < -4) {
                    startTranslateAnim(true);
                }
            }
        });


    }

    public int getScollYDistance() {
        LinearLayoutManager mlayoutManager = (LinearLayoutManager) layoutManager;
        int position = mlayoutManager.findFirstVisibleItemPosition();
        View firstVisiableChildView = mlayoutManager.findViewByPosition(position);
        int itemHeight = firstVisiableChildView.getHeight();
        return (position) * itemHeight - firstVisiableChildView.getTop();
    }

    public void startTranslateAnim(boolean show) {

        if (showTitle == show) {
            return;
        }
        showTitle = show;
        float fromy = -300;
        float toy = 0;

        if (!show) {
            fromy = 0;
            toy = -300;
        }

        TranslateAnimation animation = new TranslateAnimation(0, 0, fromy, toy);
        animation.setDuration(500);
        animation.setFillAfter(true);
        rlTitle.startAnimation(animation);
    }

    private void broadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MsgBroadcast.ACTION_MSG_CLOSE_TRILL);
        registerReceiver(closeReceiver, intentFilter);
    }

    private void initData() {
        urlList = new ArrayList<>();
        pagerIndex = PreferenceUtils.getInt(this, "trill_index", 0);
        position = PreferenceUtils.getInt(this, "trill_position", 0);
        loadData(false);
    }

    private void initView() {
        findViewById(R.id.iv_title_left).setOnClickListener(this);
        findViewById(R.id.iv_title_add).setOnClickListener(this);

        mPager = findViewById(R.id.rv_pager);

        tvTitle = findViewById(R.id.tv_text);
        rlTitle = findViewById(R.id.rl_title);
        videoAdapter = new ListVideoAdapter(urlList);
        layoutManager = new GridLayoutManager(TrillActivity.this, 2);
        mPager.setLayoutManager(layoutManager);

        proxyAdapter = new ProxyAdapter(videoAdapter);
        mPager.setAdapter(proxyAdapter);

        mPager.addItemDecoration(new RecyclerSpace(8, Color.parseColor("#151621"), 1));

        View headView = LayoutInflater.from(this).inflate(R.layout.layout_trill_tag, null);
        proxyAdapter.addHeaderView(headView);

        //解决数据加载不完的问题
        //        mPager.setNestedScrollingEnabled(false);
        //        mPager.setHasFixedSize(true);
        //        //解决数据加载完成后, 没有停留在顶部的问题
        //        mPager.setFocusable(false);

        headView.findViewById(R.id.ll_tab1).setOnClickListener(tabListener);
        headView.findViewById(R.id.ll_tab2).setOnClickListener(tabListener);
        headView.findViewById(R.id.ll_tab3).setOnClickListener(tabListener);
        headView.findViewById(R.id.ll_tab4).setOnClickListener(tabListener);
        headView.findViewById(R.id.ll_tab5).setOnClickListener(tabListener);
        headView.findViewById(R.id.ll_tab6).setOnClickListener(tabListener);
        headView.findViewById(R.id.ll_tab7).setOnClickListener(tabListener);
        headView.findViewById(R.id.ll_tab8).setOnClickListener(tabListener);
    }

    private void loadData(final boolean clear) {
        isLoad = true;

        if (clear) {
            urlList.clear();
            proxyAdapter.notifyDataSetChanged();
            videoAdapter.notifyDataSetChanged();
        }

        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("pageIndex", Integer.toString(pagerIndex));
        params.put("pageSize", "20");// 给一个尽量大的值
        params.put("userId", coreManager.getSelf().getUserId());
        if (mLable > 0) {
            params.put("lable", Integer.toString(mLable));
        }

        HttpUtils.get().url(coreManager.getConfig().GET_TRILL_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<PublicMessage>(PublicMessage.class) {
                    @Override
                    public void onResponse(ArrayResult<PublicMessage> result) {
                        isLoad = false;
                        DialogHelper.dismissProgressDialog();
                        List<PublicMessage> data = result.getData();

                        if (data.size() == 0) {
                            shouldReset = true;
                        } else if (data.size() > 0) {

                            urlList.addAll(data);
                            //                            videoAdapter.notifyAll();
                            proxyAdapter.notifyDataSetChanged();
                            videoAdapter.notifyDataSetChanged();
                            layoutManager.scrollToPosition(position);
                            if (data.size() < 20) {
                                shouldReset = true;
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(TrillActivity.this);
                        isLoad = false;
                    }
                });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (shouldReset) {
            PreferenceUtils.putInt(this, "trill_index", 0);
            PreferenceUtils.putInt(this, "trill_position", 0);
        } else {
            PreferenceUtils.putInt(this, "trill_index", pagerIndex);
            PreferenceUtils.putInt(this, "trill_position", position % 10);
        }
        shouldReset = false;
        unregisterReceiver(closeReceiver);

        VideotillManager.instance().releaseVideo();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_title_left:
                finish();
                break;
            case R.id.iv_title_add:
                Intent intent = new Intent(this, RecordxActivity.class);
                startActivity(intent);
                //                    intentPreview("/storage/emulated/0/RecordVideo/VID_20181121_163426.mp4");
                break;
        }
    }


    /**
     * 跳转到视频预览界面
     */
    private void intentPreview(int position) {
        Intent intent = new Intent(TrillActivity.this, TriListActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("pagerIndex", pagerIndex);
        intent.putExtra("tab_name", tvTitle.getText().toString().trim());
        intent.putExtra("tab_lable", mLable);
        startActivity(intent);

        Log.e(TAG, "onClick: " + position);
    }

    class ListVideoAdapter extends BaseRecAdapter<PublicMessage, VideoViewHolder> {
        public ListVideoAdapter(List<PublicMessage> list) {
            super(list);
        }

        @Override
        public void onHolder(VideoViewHolder holder, PublicMessage bean, int position) {
            String imageUrl = bean.getFirstImageOriginal();
            if (TextUtils.isEmpty(imageUrl)) {
                AvatarHelper.getInstance().asyncDisplayOnlineVideoThumb(bean.getFirstVideo(), holder.mIvImage);
            } else {
                Glide.with(mContext)
                        .load(imageUrl)
                        .placeholder(R.drawable.default_gray)
                        .error(R.drawable.default_gray)
                        .into(holder.mIvImage);
            }
            //  Glide.with(TrillActivity.this).load(iUrl).into(holder.mIvImage);
            AvatarHelper.getInstance().displayAvatar(bean.getNickName(), bean.getUserId(), holder.mIvInco, false);
            holder.mTvName.setText(bean.getNickName());

            String title = TextUtils.isEmpty(bean.getBody().getText()) ? "" : bean.getBody().getText();
            holder.mTvContent.setText(title);
        }

        @Override
        public VideoViewHolder onCreateHolder() {
            return new VideoViewHolder(getViewByRes(R.layout.item_trill_tag));
        }
    }

    public class VideoViewHolder extends BaseRecViewHolder implements View.OnClickListener {
        public ImageView mIvImage;
        public ImageView mIvInco;
        public TextView mTvCount;
        public TextView mTvName;
        public TextView mTvContent;

        public VideoViewHolder(View rootView) {
            super(rootView);

            mTvContent = rootView.findViewById(R.id.tv_content);
            mTvName = rootView.findViewById(R.id.tv_name);
            mTvCount = rootView.findViewById(R.id.tv_count);
            mIvInco = rootView.findViewById(R.id.iv_avatar);
            mIvImage = rootView.findViewById(R.id.iv_image);

            rootView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition() - 1; // 减1一个头布局
            intentPreview(position);
        }
    }

    public class HeadViewHolder extends BaseRecViewHolder {

        public HeadViewHolder(View itemView) {
            super(itemView);
        }

        public void onBind() {

        }
    }

    // 增加头部的recyaler适配器
    public class ProxyAdapter extends RecyclerView.Adapter {
        private static final int HEADER_TYPE = 1;
        private static final int NORMAL_TYPE = 0;

        final RecyclerView.Adapter mAdapter;
        private List<View> mHeaderViews;

        public ProxyAdapter(RecyclerView.Adapter adapter) {
            if (adapter == null) {
                throw new IllegalArgumentException();
            }
            mHeaderViews = new ArrayList<>();
            mAdapter = adapter;
        }

        public void addHeaderView(View view) {
            if (mHeaderViews.add(view)) {
                mAdapter.notifyDataSetChanged();
            }
        }

        public void removeHeaderView(View view) {
            if (mHeaderViews.remove(view)) {
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager instanceof GridLayoutManager) {   // 布局是GridLayoutManager所管理
                final GridLayoutManager gridLayoutManager = (GridLayoutManager) manager;
                gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        // 如果是Header、Footer的对象则占据spanCount的位置，否则就只占用1个位置
                        return isHeader(position) ? gridLayoutManager.getSpanCount() : 1;
                    }
                });
            }
        }

        private boolean isHeader(int position) {
            return position >= 0 && position < mHeaderViews.size();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            RecyclerView.ViewHolder viewHolder;
            if (viewType == HEADER_TYPE) {
                viewHolder = new HeadViewHolder(mHeaderViews.get(0));
            } else {
                viewHolder = mAdapter.onCreateViewHolder(viewGroup, viewType);
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            if (viewHolder instanceof HeadViewHolder) {
                ((HeadViewHolder) viewHolder).onBind();
            } else {
                mAdapter.onBindViewHolder(viewHolder, position - mHeaderViews.size());
            }

        }

        @Override
        public int getItemCount() {
            return mAdapter.getItemCount() + mHeaderViews.size();
        }


        @Override
        public int getItemViewType(int position) {
            final int numHeaderView = mHeaderViews.size();
            if (position < numHeaderView) {
                return HEADER_TYPE;
            } else {
                return NORMAL_TYPE;
            }
        }
    }

}

