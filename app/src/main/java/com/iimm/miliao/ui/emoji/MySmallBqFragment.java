package com.iimm.miliao.ui.emoji;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.SBqItem;
import com.iimm.miliao.util.SmileyParser;
import com.iimm.miliao.view.ChatFaceView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by  on 2019/2/25.
 */
public class MySmallBqFragment extends Fragment {
    private ViewPager vp;
    private PageIndicatorView piv_view;
    private static final String TEXT = "InnerStickersInfo.json";
    private List<SBqItem> list;
    private final int spanRow = 3;
    private final int spanColumn = 7;

    private ChatFaceView.MySmallBqListener onEmojiClick;

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
        initData();
        initView();
        return view;
    }

    private void initData() {
        list = new ArrayList<SBqItem>();
        int[][] idMatrix = SmileyParser.Smilies.getIds();
        String[][] strMatrix = SmileyParser.Smilies.getTexts();
        for (int i = 0; i < idMatrix.length; i++) {
            for (int j = 0; j < idMatrix[i].length; j++) {
                SBqItem sBqItem = new SBqItem(idMatrix[i][j], strMatrix[i][j]);
                list.add(sBqItem);
            }
        }
        list = sort(list);
    }

    /**
     * 从竖排 改为 横排 顺序
     *
     * @param
     */
    private List<SBqItem> sort(List<SBqItem> list) {
        List<SBqItem> sortResult = new ArrayList<>();
        int yushu = list.size() % (20);
        int pageSize = 0;
        if (yushu != 0) {
            pageSize = list.size() / (20) + 1;
        } else {
            pageSize = list.size() / (20);
        }
        List<List<SBqItem>> listList = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            int end = i * (20) + 20;
            int start = i * (20);
            if (end < list.size()) {
                listList.add(list.subList(start, end));
            } else {
                listList.add(list.subList(start, list.size()));
            }
        }
        for (int i = 0; i < listList.size(); i++) {
            List<SBqItem> sBqItems = listList.get(i);
//            int lie = sBqItems.size() % spanRow;
//            int size = lie == 0 ? sBqItems.size() / spanRow :sBqItems.size()/spanRow+1;
            for (int j = 0; j < spanColumn; j++) {
                for (int k = 0; k < spanRow; k++) {
                    int index = (k * 7) + j;
                    Log.i("sort", "index:" + index);
                    if (index < sBqItems.size()) {
                        sortResult.add(sBqItems.get(index));
                    } else if (index == 20) {
                        sortResult.add(new SBqItem(R.drawable.e_del, "[del]"));
                    } else {
                        sortResult.add(new SBqItem(0, "FillA"));
                    }
                }
            }
        }
        return sortResult;
    }

    private void initView() {
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int count = 0;
        int pagesize = spanRow*spanColumn;
        if(list.size()%pagesize!=0){
            count = list.size()/pagesize+1;
        }else {
            count = list.size()/pagesize;
        }
        piv_view.initIndicator(count);
        List<View> views = new ArrayList<>();
        for(int i =0;i<count;i++){
            List<SBqItem> lists = new ArrayList<>();
            if((i+1)*pagesize<list.size()){
                lists.addAll(list.subList(i*pagesize,((i+1)*pagesize)));
            }else {
                lists.addAll(list.subList(i*pagesize,list.size()));
            }

            View view = LayoutInflater.from(getContext()).inflate(R.layout.rcv,null);

            RecyclerView rcv = view.findViewById(R.id.rcv);
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), spanRow);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            rcv.setLayoutManager(layoutManager);
            BQadapter bQadapter = new BQadapter(getContext(),lists);
            bQadapter.setOnItemClick(new BQadapter.OnItemClick() {
                @Override
                public void onClicks(SpannableString i) {
                    onEmojiClick.onEmojiClick(i);
                }
            });
            bQadapter.setHeight((width/spanColumn));
            rcv.setAdapter(bQadapter);
            views.add(view);
        }
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                piv_view.setSelectedPage(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        ViewAdapter viewAdapter = new ViewAdapter(views);
        vp.setAdapter(viewAdapter);

    }

    public void init() {
        if (vp != null) {
            vp.setCurrentItem(0);
            piv_view.setSelectedPage(0);
        }
    }

    public void setMySmallBqListener(ChatFaceView.MySmallBqListener onEmojiClick) {
        this.onEmojiClick = onEmojiClick;
    }


}
