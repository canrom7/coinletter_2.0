package com.iimm.miliao.ui.trill;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iimm.miliao.AppConstant;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.circle.PublicMessage;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseRecAdapter;
import com.iimm.miliao.ui.base.BaseRecViewHolder;
import com.iimm.miliao.ui.base.EasyFragment;
import com.iimm.miliao.util.RecyclerSpace;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.video.ChatVideoPreviewActivity;
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
public class TrillUserFragment extends EasyFragment {
    private String TAG = "TrillUserFragment";
    boolean isLoad;

    private RecyclerView mPager;
    private GridLayoutManager layoutManager;
    private ListVideoAdapter videoAdapter;
    private List<PublicMessage> urlList;
    private int pagerIndex;

    @Override
    protected int inflateLayoutId() {
        return R.layout.activity_trill_user;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        if (createView) {
            initView();
            // initData();
        }
    }

    private void initView() {
        mPager = findViewById(R.id.rcv_list);

        layoutManager = new GridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false);
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
            AvatarHelper.getInstance().displayUrl(iUrl, holder.ivImage);
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

            String filePath = urlList.get(position).getFirstVideo();
            String iUrl = urlList.get(position).getFirstImageOriginal();
            Intent intent = new Intent(getContext(), ChatVideoPreviewActivity.class);
            intent.putExtra(AppConstant.EXTRA_VIDEO_FILE_PATH, filePath);
            intent.putExtra(AppConstant.EXTRA_VIDEO_FILE_THUMB, iUrl);
            startActivity(intent);

            Log.e(TAG, "onClick: " + filePath);
        }

    }
}
