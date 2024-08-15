package com.iimm.miliao.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.collection.Collectiion;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by  on 2019/3/1.
 */

public class CustomBqManageAdapter extends RecyclerView.Adapter<CustomBqManageAdapter.ViewHolder> {
    private List<Collectiion> customBqBeans;
    private Context context;
    private boolean isEdit = true;
    private CustomBqManageListener listener;

    private List<Collectiion> selectPosition = new ArrayList<>();

    public CustomBqManageAdapter(List<Collectiion> customBqBeans, Context context) {
        this.customBqBeans = customBqBeans;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_bq_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Collectiion customBqBean = customBqBeans.get(position);
        Glide.with(context).load(customBqBean.getUrl()).into(holder.ivImg);
        if (isEdit) {
            holder.ivSelect.setVisibility(View.VISIBLE);
            if (customBqBean.isSelect()) {
                holder.ivSelect.setImageResource(R.mipmap.select);
            } else {
                holder.ivSelect.setImageResource(R.mipmap.no_select);
            }
            holder.ivSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.switchSelect(v, position);
                    }
                }
            });
            holder.ivImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.switchSelect(v, position);
                    }
                }
            });
        } else {
            holder.ivSelect.setOnClickListener(null);
            holder.ivSelect.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return customBqBeans.size();
    }

    public void addDelete(Collectiion customBqBean) {
        selectPosition.add(customBqBean);
    }

    public void removeDelete(Collectiion customBqBean) {
        selectPosition.remove(customBqBean);
    }

    public void clearSelect() {
        selectPosition.clear();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImg;
        ImageView ivSelect;

        ViewHolder(View view) {
            super(view);
            ivImg = view.findViewById(R.id.iv_img);
            ivSelect = view.findViewById(R.id.iv_select);
        }
    }

    public void setEdit(boolean edit) {
        isEdit = edit;
        notifyDataSetChanged();
    }


    public interface CustomBqManageListener {

        void switchSelect(View v, int position);
    }

    public void setListener(CustomBqManageListener listener) {
        this.listener = listener;
    }

    public List<Collectiion> getSelectPosition() {
        return selectPosition;
    }
}
