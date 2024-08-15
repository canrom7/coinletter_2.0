package com.iimm.miliao.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.ImEmojiStore;
import com.iimm.miliao.util.ScreenUtil;

import java.util.List;

/**
 * Created by  on 2019/3/1.
 */

public class BqDetailAdapter extends RecyclerView.Adapter<BqDetailAdapter.ViewHolder> {
    private List<ImEmojiStore> datas;
    private Context context;
    private int itemWidth;

    public BqDetailAdapter(List<ImEmojiStore> datas, Context context) {
        this.datas = datas;
        this.context = context;
        itemWidth = ScreenUtil.getScreenWidth(context) / 4;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bq_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.width = itemWidth;
        layoutParams.height = itemWidth;
        holder.itemView.setLayoutParams(layoutParams);
        Glide.with(context).load(datas.get(position).getFileUrl()).into(holder.ivBq);
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBq;

        ViewHolder(View view) {
            super(view);
            ivBq = view.findViewById(R.id.iv_bq);
        }
    }
}
