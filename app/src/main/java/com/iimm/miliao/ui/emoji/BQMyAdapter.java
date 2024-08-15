package com.iimm.miliao.ui.emoji;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.ImEmojiStore;

import java.util.List;

public class BQMyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    List<ImEmojiStore> list;
  OnItemClick onItemClick;
    int width;

    public void setHeight(int width) {
        this.width = width;
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public BQMyAdapter(Context context, List<ImEmojiStore> list) {
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
        ImEmojiStore bean = list.get(i);
        if (TextUtils.isEmpty(bean.getThumbnailUrl())) {
            myViewHolder.itemView.setVisibility(View.GONE);
            myViewHolder.itemView.setOnTouchListener(null);
            myViewHolder.imageView.setOnClickListener(null);
        } else {
            Glide.with(context).load(bean.getThumbnailUrl()).into(myViewHolder.imageView);
            myViewHolder.tvName.setText(bean.getEmoMean());
            myViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onItemClick!=null){
                        onItemClick.onClicks(v,i);
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
        void onClicks(View v,int i);
    }
}
