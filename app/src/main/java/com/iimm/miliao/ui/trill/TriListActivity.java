package com.iimm.miliao.ui.trill;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.danikula.videocache.HttpProxyCacheServer;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.circle.PublicMessage;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.base.BaseRecAdapter;
import com.iimm.miliao.ui.base.BaseRecViewHolder;
import com.iimm.miliao.ui.message.InstantMessageActivity;
import com.iimm.miliao.ui.xrce.RecordxActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ToolUtils;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import fm.jiecao.jcvideoplayer_lib.VideotillManager;
import okhttp3.Call;

public class TriListActivity extends BaseActivity implements View.OnClickListener {

    boolean shareBack;
    private RecyclerView mPager;
    private ListVideoAdapter videoAdapter;
    private List<PublicMessage> urlList;
    private PagerSnapHelper snapHelper;
    private RecyclerView.LayoutManager layoutManager;
    private boolean isLoad;
    private int pagerIndex;
    private int position = -1;
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            videoAdapter.notifyDataSetChanged();
            layoutManager.scrollToPosition(position);
            return true;
        }
    });
    private TextView tvTitle;
    private int mLable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trill);
        getSupportActionBar().hide();

        initData();
        initView();
        addListener();
        loadData();
    }

    private void initView() {

        mPager = findViewById(R.id.rv_pager);
        tvTitle = findViewById(R.id.tv_text);
        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mPager);
        videoAdapter = new ListVideoAdapter(urlList);
        layoutManager = new LinearLayoutManager(this);
        mPager.setLayoutManager(layoutManager);


        findViewById(R.id.iv_title_add).setOnClickListener(v -> {
            Intent intent = new Intent(this, RecordxActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.iv_title_left).setOnClickListener(this);

        mPager.setAdapter(videoAdapter);

        layoutManager.scrollToPosition(position);

        String name = getIntent().getStringExtra("tab_name");
        tvTitle.setText(name);
    }

    private void initData() {
        position = getIntent().getIntExtra("position", 0);
        pagerIndex = getIntent().getIntExtra("pagerIndex", 0);
        mLable = getIntent().getIntExtra("tab_lable", 0);

        urlList = new ArrayList<>();
    }

    private void loadData() {
        isLoad = true;
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("pageIndex", pagerIndex + "");
        params.put("pageSize", "10");// 给一个尽量大的值
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
                        if (data != null && data.size() > 0) {
                            urlList.addAll(data);
                            Log.e(TAG, "onResponse: " + urlList.size());
                            videoAdapter.notifyDataSetChanged();
                            layoutManager.scrollToPosition(position);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(TriListActivity.this);
                        isLoad = false;
                    }
                });
    }

    private void addListener() {
        mPager.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE://停止滚动

                        if (urlList == null || urlList.size() == 0) {
                            return;
                        }

                        View view = snapHelper.findSnapView(layoutManager);
                        RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(view);
                        position = recyclerView.getChildLayoutPosition(view);

                        if (viewHolder != null && viewHolder instanceof VideoViewHolder) {
                            JcvTrillVideo video = ((VideoViewHolder) viewHolder).mp_video;
                            //                            Log.e(TAG, position + " : 滚动到: " + video.getCurrState());
                            video.startVideo();
                        }

                        if (urlList.size() > position + 1) {
                            Log.e(TAG, "onScrollStateChanged: " + (position + 1) + ",  size " + urlList.size());
                            HttpProxyCacheServer proxy = MyApplication.getProxy(TriListActivity.this);
                            proxy.getProxyUrl(urlList.get(position + 1).getFirstVideo());
                        }

                        if (!isLoad && position > urlList.size() - 4) {
                            pagerIndex++;
                            loadData();
                        }

                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING://拖动
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING://惯性滑动
                        break;
                }

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        VideotillManager.instance().pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: " + shareBack);
        if (!shareBack) {
            VideotillManager.instance().play();
        } else {
            VideotillManager.instance().releaseVideo();
            JCMediaManager.instance().releaseMediaPlayer();
            handler.sendEmptyMessageDelayed(1, 500);

        }
        shareBack = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        JCMediaManager.addOnJcvdListener(null);
        VideotillManager.instance().releaseVideo();
    }

    @Override
    public void onClick(View v) {
        finish();
    }

    public void onShare(String videoUrl, long fileSize, int position) {
        String userid = coreManager.getSelf().getUserId();
        shareBack = true;
        this.position = position;
        String packetId = ToolUtils.getUUID();
        ChatMessage message = new ChatMessage(); //
        message.setContent(videoUrl);
        message.setFromUserId("10010"); // url
        message.setFromUserName("10010");
        message.setFilePath(videoUrl);
        message.setDownload(false);
        message.setUpload(true);
        message.setFileSize((int) fileSize);
        message.setToUserId(userid);
        message.setPacketId(packetId);
        message.setType(Constants.TYPE_VIDEO);
        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
        ChatMessageDao.getInstance().saveNewSingleChatMessage(userid, "10010", message);

        Intent intent = new Intent(mContext, InstantMessageActivity.class);
        intent.putExtra("fromUserId", "10010");
        intent.putExtra("messageId", packetId);
        mContext.startActivity(intent);
    }

    class ListVideoAdapter extends BaseRecAdapter<PublicMessage, VideoViewHolder> {
        public ListVideoAdapter(List<PublicMessage> list) {
            super(list);
        }

        @Override
        public void onHolder(VideoViewHolder holder, PublicMessage bean, int position) {
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            holder.mp_video.updateDatas(bean, coreManager.getSelf(), coreManager.getConfig().MSG_COMMENT_ADD, coreManager.getSelfStatus().accessToken);
            holder.mp_video.onShareListener(TriListActivity.this);
            holder.mp_video.setPosiont(position);

            Log.e(TAG, "onHolder: " + position + " ,,  " + TriListActivity.this.position);
            if (position == TriListActivity.this.position) {
                holder.mp_video.startVideo();
            }
        }

        @Override
        public VideoViewHolder onCreateHolder() {
            return new VideoViewHolder(getViewByRes(R.layout.item_trill));
        }
    }

    public class VideoViewHolder extends BaseRecViewHolder {
        public View rootView;
        public JcvTrillVideo mp_video;

        public VideoViewHolder(View rootView) {
            super(rootView);
            this.rootView = rootView;
            this.mp_video = rootView.findViewById(R.id.mp_video);
        }
    }

}
