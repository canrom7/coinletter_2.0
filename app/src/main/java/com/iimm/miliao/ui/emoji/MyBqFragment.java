package com.iimm.miliao.ui.emoji;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.BqBao;
import com.iimm.miliao.bean.ImEmojiStore;
import com.iimm.miliao.util.AppUtils;
import com.iimm.miliao.view.ChatFaceView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by  on 2019/2/25.
 */
public class MyBqFragment extends Fragment {
    private ViewPager vp;
    private BqRecyclerView.PageAdapter myAdapter = null;
    private PageIndicatorView piv_view;
    private final int spanRow = 2;
    private final int spanColumn = 4;
    private BqBao bqBao;
    private List<ImEmojiStore> sortResult = new ArrayList<>();
    List<View> views;
    int width;
    private ViewAdapter viewAdapter;

    private ImageView imageView;
    private ChatFaceView.MyBqListener myBqListener;
    private int postion;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bq, null);
        piv_view = view.findViewById(R.id.piv_view);
        vp = view.findViewById(R.id.vp);
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        width = wm.getDefaultDisplay().getWidth();
        initView();
        return view;
    }

    private void initView() {
        if (bqBao == null) {
            return;
        }
        List<ImEmojiStore> list = bqBao.getImEmojiStoreListInfo();
        int yushu = list.size() % (8);
        int pageSize = 0;
        if (yushu != 0) {
            pageSize = list.size() / (8) + 1;
        } else {
            pageSize = list.size() / (8);
        }
        List<List<ImEmojiStore>> listList = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            int end = i * (8) + 8;
            int start = i * (8);
            if (end < list.size()) {
                listList.add(list.subList(start, end));
            } else {
                listList.add(list.subList(start, list.size()));
            }
        }
        for (int i = 0; i < listList.size(); i++) {
            List<ImEmojiStore> sBqItems = listList.get(i);
            for (int j = 0; j < spanColumn; j++) {
                for (int k = 0; k < spanRow; k++) {
                    int index = (k * 4) + j;
                    Log.i("sort", "index:" + index);
                    if (index < sBqItems.size()) {
                        sortResult.add(sBqItems.get(index));
                    } else {
                        sortResult.add(new ImEmojiStore());
                    }
                }
            }
        }
        getViews();
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                piv_view.setSelectedPage(i);
                postion = i;
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        viewAdapter = new ViewAdapter(views);
        vp.setAdapter(viewAdapter);
    }

    private void onClickView(View v, int position) {
        if (myBqListener != null) {
            myBqListener.onClickViewSendBq(false, MyBqFragment.this.bqBao.getEmoPackId(),
                    sortResult.get(position).getEmoMean(),
                    sortResult.get(position).getFileUrl(),
                    sortResult.get(position).getThumbnailUrl());
        }
    }

    public void getViews() {
        if (views != null) {
            views.clear();
        }
        int count = 0;
        int pagesize = spanRow * spanColumn;
        if (sortResult.size() % pagesize != 0) {
            count = sortResult.size() / pagesize + 1;
        } else {
            count = sortResult.size() / pagesize;
        }

        piv_view.initIndicator(count);
        views = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            List<ImEmojiStore> lists = new ArrayList<>();
            if ((i + 1) * pagesize < sortResult.size()) {
                lists.addAll(sortResult.subList(i * pagesize, ((i + 1) * pagesize)));
            } else {
                lists.addAll(sortResult.subList(i * pagesize, sortResult.size()));
            }

            View view = LayoutInflater.from(getContext()).inflate(R.layout.rcv, null);

            RecyclerView rcv = view.findViewById(R.id.rcv);
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), spanRow);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            rcv.setLayoutManager(layoutManager);
            BQMyAdapter bQadapter = new BQMyAdapter(getContext(), lists);
            bQadapter.setOnItemClick(new BQMyAdapter.OnItemClick() {
                @Override
                public void onClicks(View v, int i) {
                    onClickView(v, i + postion * spanRow * spanColumn);
                }
            });
            bQadapter.setHeight((width / spanColumn));
            rcv.setAdapter(bQadapter);
            views.add(view);
        }
    }

    public void setContent(BqBao bqBao) {
        this.bqBao = bqBao;
    }

    public BqBao getContent() {
        return bqBao;
    }

    public void init() {
        if (vp != null) {
            vp.setCurrentItem(0);
            piv_view.setSelectedPage(0);
        }
    }

    @Override
    public void onDestroyView() {
        if (AppUtils.isContextExisted(getContext())) {
            Glide.with(getContext()).pauseRequests();
        }
        super.onDestroyView();
    }

    public void setMyBqListener(ChatFaceView.MyBqListener myBqListener) {
        this.myBqListener = myBqListener;
    }
}
