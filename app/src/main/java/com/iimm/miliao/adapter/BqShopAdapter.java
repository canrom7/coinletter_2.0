package com.iimm.miliao.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.BqBao;


import java.util.List;


/**
 * Created by  on 2019/3/1.
 */

public class BqShopAdapter extends RecyclerView.Adapter<BqShopAdapter.ViewHolder> {
    private String TAG = "BqShopAdapter";
    private List<BqBao> bqBaoList;
    private Context context;
    private BqShopListener listener;

    public BqShopAdapter(List<BqBao> bqBaoList, Context context) {
        this.bqBaoList = bqBaoList;
        this.context = context;
    }

    public void setData(List<BqBao> bqBaoList) {
        this.bqBaoList = bqBaoList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bq_shop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        BqBao bqBao = bqBaoList.get(position);
        Glide.with(context).load(bqBao.getEmoPackThumbnailUrl()).into(holder.bqImg);
        holder.tvBqName.setText(bqBao.getEmoPackId() == null ? "" : bqBao.getEmoPackName());
        holder.tvDesc.setText(bqBao.getEmoPackProfile() == null ? "" : bqBao.getEmoPackProfile());
        initBtn(holder, bqBao.getEmoDownStatus(), position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(v, position);
                }
            }
        });


    }

    private void initBtn(ViewHolder holder, int status, final int position) {
        if (status == 1) {
            //添加过了不能再添加
            Drawable drawable = context.getResources().getDrawable(R.drawable.bq_shop_no_add_bt);
            holder.bAddBq.setBackground(drawable);
            holder.bAddBq.setTextColor(context.getResources().getColor(R.color.bq_no_add_color));
            holder.bAddBq.setText(context.getResources().getString(R.string.have_been_added));
            holder.bAddBq.setClickable(false);
        } else {
            holder.bAddBq.setClickable(true);
            holder.bAddBq.setText(context.getResources().getString(R.string.add_to));
            Drawable drawable = context.getResources().getDrawable(R.drawable.bq_shop_add_bt);
            holder.bAddBq.setBackground(drawable);
            holder.bAddBq.setTextColor(context.getResources().getColor(R.color.bq_add_color));
            holder.bAddBq.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onAddBqClick(v, position);
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return bqBaoList == null ? 0 : bqBaoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView bqImg;
        TextView tvBqName;
        Button bAddBq;
        TextView tvDesc;

        ViewHolder(View view) {
            super(view);
            bqImg = view.findViewById(R.id.bq_img);
            tvBqName = view.findViewById(R.id.tv_bq_name);
            bAddBq = view.findViewById(R.id.b_add);
            tvDesc = view.findViewById(R.id.tv_desc);
        }
    }

    public void setListener(BqShopListener listener) {
        this.listener = listener;
    }


    public interface BqShopListener {

        void onAddBqClick(View v, int position);

        void onItemClick(View v, int position);

    }
}
