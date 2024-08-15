package com.iimm.miliao.ui.trill;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.circle.PublicMessage;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseRecAdapter;
import com.iimm.miliao.ui.base.BaseRecViewHolder;
import com.iimm.miliao.ui.base.EasyFragment;
import com.iimm.miliao.util.RecyclerSpace;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 定位标签的短视频
 */
public class TrillTagFragment extends EasyFragment {
    private String TAG = "TrillTagFragment";
    public int pagerIndex, position;
    public boolean isLoad;
    private RecyclerView mPager;
    private PagerSnapHelper snapHelper;
    private LinearLayoutManager layoutManager;
    private ListVideoAdapter videoAdapter;
    private List<PublicMessage> urlList;

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_trill_tag;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        if (createView) {
            initView();
            //            initData();
        }
    }

    private void initView() {
        mPager = findViewById(R.id.rv_list);

        layoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        mPager.setLayoutManager(layoutManager);
        urlList = new ArrayList<>();
        videoAdapter = new ListVideoAdapter(urlList);
        mPager.setAdapter(videoAdapter);
        mPager.addItemDecoration(new RecyclerSpace(3, Color.parseColor("#151621"), 1));

        //解决数据加载不完的问题
        mPager.setNestedScrollingEnabled(false);
        mPager.setHasFixedSize(true);
        //解决数据加载完成后, 没有停留在顶部的问题
        mPager.setFocusable(false);

    }


    public void initData() {
        if (isLoad) {
            return;
        }

        loadData();
    }

    private void loadData() {
        isLoad = true;
        DialogHelper.showDefaulteMessageProgressDialog(getActivity());
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("pageIndex", pagerIndex + "");
        params.put("pageSize", "10");// 给一个尽量大的值
        params.put("userId", coreManager.getSelf().getUserId());

        HttpUtils.get().url(coreManager.getConfig().GET_TRILL_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<PublicMessage>(PublicMessage.class) {
                    @Override
                    public void onResponse(ArrayResult<PublicMessage> result) {
                        DialogHelper.dismissProgressDialog();
                        List<PublicMessage> data = result.getData();
                        if (data != null && data.size() > 0) {
                            urlList.addAll(data);
                            videoAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(getContext());
                    }
                });
    }


    class ListVideoAdapter extends BaseRecAdapter<PublicMessage, VideoViewHolder> {
        public ListVideoAdapter(List<PublicMessage> list) {
            super(list);
        }

        @Override
        public void onHolder(VideoViewHolder holder, PublicMessage bean, int position) {
            ViewGroup.LayoutParams layoutParams = holder.rlRoot.getLayoutParams();
            Log.e(TAG, "onHolder: " + layoutParams.width);
            layoutParams.height = 470;
            holder.rlRoot.setLayoutParams(layoutParams);


            String iUrl = bean.getFirstImageOriginal();
            Glide.with(getContext()).load(iUrl).into(holder.ivImage);
        }

        @Override
        public VideoViewHolder onCreateHolder() {
            return new VideoViewHolder(getViewByRes(R.layout.item_trill_user));
        }
    }

    public class VideoViewHolder extends BaseRecViewHolder implements View.OnClickListener {
        public RelativeLayout rlRoot;
        public ImageView ivImage;
        public ImageView ivInco;
        public TextView tvCount;

        public VideoViewHolder(View rootView) {
            super(rootView);
            this.rlRoot = rootView.findViewById(R.id.rl_root);
            this.ivImage = rootView.findViewById(R.id.iv_image);
            this.ivInco = rootView.findViewById(R.id.iv_inco);
            this.tvCount = rootView.findViewById(R.id.tv_count);
            this.rlRoot.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();


        }
    }
}
