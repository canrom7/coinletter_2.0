package com.iimm.miliao.ui.emoji;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.collection.Collectiion;

import java.util.List;

public class BQCustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    List<Collectiion> list;
    OnItemClick onItemClick;
    int width;

    public void setHeight(int width) {
        this.width = width;
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public BQCustomAdapter(Context context, List<Collectiion> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bq, viewGroup, false);
        return new BQItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final BQItemHolder myViewHolder = (BQItemHolder) viewHolder;
        ViewGroup.LayoutParams layoutParams = myViewHolder.itemView.getLayoutParams();
        layoutParams.width = width;
        myViewHolder.ll.setLayoutParams(layoutParams);
        final Collectiion item = list.get(i);
        if (item.getType().equals(Collectiion.FULLA)) {
            myViewHolder.itemView.setVisibility(View.GONE);
            myViewHolder.tvName.setVisibility(View.GONE);
            //  myViewHolder.imageView.setOnTouchListener(null);
            myViewHolder.imageView.setOnClickListener(null);
            return;
        }
        if (item.getType().equals(Collectiion.MANAGE)) {
            myViewHolder.imageView.setImageResource(R.mipmap.coutom_bq_manage);
            myViewHolder.tvName.setVisibility(View.GONE);
            myViewHolder.imageView.setOnTouchListener(null);
            myViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onItemClick!=null){
                        onItemClick.onClicks(v,i,0);
                    }

                }
            });
        } else {
            Glide.with(context).load(item.getUrl()).into(myViewHolder.imageView);
            myViewHolder.tvName.setVisibility(View.GONE);
            myViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onItemClick!=null){
                        onItemClick.onClicks(v,i,1);
                    }

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    public class BQItemHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView tvName;
        LinearLayout ll;
        public BQItemHolder(@NonNull View itemView) {
            super(itemView);
            ll = itemView.findViewById(R.id.ll);
            imageView = itemView.findViewById(R.id.iv_bq);
            tvName = itemView.findViewById(R.id.tv_name);
        }
    }
    public  interface  OnItemClick{
        void onClicks(View v,int i,int type );
    }
}

