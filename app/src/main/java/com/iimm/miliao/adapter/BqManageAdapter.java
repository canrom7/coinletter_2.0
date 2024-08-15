package com.iimm.miliao.adapter;

import android.content.Context;
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

public class BqManageAdapter extends RecyclerView.Adapter<BqManageAdapter.ViewHolder> {
    private List<BqBao> bqBaoList;
    private Context context;
    private BqManageListener listener;

    public BqManageAdapter(List<BqBao> bqBaoList, Context context) {
        this.bqBaoList = bqBaoList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bq_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        BqBao bqBao = bqBaoList.get(position);
        Glide.with(context).load(bqBao.getEmoPackThumbnailUrl()).into(holder.bqImg);
        holder.tvBqName.setText(bqBao.getEmoPackName());
        holder.bDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.deleteBqClick(v, position);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return bqBaoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView bqImg;
        TextView tvBqName;
        Button bDelete;

        ViewHolder(View view) {
            super(view);
            bqImg = view.findViewById(R.id.bq_img);
            tvBqName = view.findViewById(R.id.tv_bq_name);
            bDelete = view.findViewById(R.id.b_delete);
        }
    }

    public void setListener(BqManageListener listener) {
        this.listener = listener;
    }


    public interface BqManageListener {


        void deleteBqClick(View v, int position);
    }
}
