package com.iimm.miliao.ui.emoji;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.SBqItem;

import java.util.List;

public class BQadapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    List<SBqItem> list;
    OnItemClick onItemClick;
    int width;

    public void setHeight(int width) {
        this.width = width;
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public BQadapter(Context context, List<SBqItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_samll_bq, viewGroup, false);
        return new BQItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final BQItemHolder myViewHolder = (BQItemHolder) viewHolder;
        ViewGroup.LayoutParams layoutParams = myViewHolder.back.getLayoutParams();
        layoutParams.width = width;
        myViewHolder.back.setLayoutParams(layoutParams);
        final SBqItem item = list.get(i);
        if (item.getContent().equals("FillA")) {
            myViewHolder.itemView.setVisibility(View.GONE);
            myViewHolder.imageView.setOnClickListener(null);
        } else {
            Glide.with(context).load(item.getId()).into(myViewHolder.imageView);
            myViewHolder.back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int res = item.getId();
                    String text = item.getContent();
                    SpannableString ss = new SpannableString(text);
                    Drawable d = context.getResources().getDrawable(res);
                    // 设置表情图片的显示大小
                    d.setBounds(0, 0, (int) (d.getIntrinsicWidth() / 1.95), (int) (d.getIntrinsicHeight() / 1.95));
                    ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
                    ss.setSpan(span, 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    if(onItemClick!=null){
                        onItemClick.onClicks(ss);
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
        LinearLayout back;
        public BQItemHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_bq);
            back = itemView.findViewById(R.id.back);
        }
    }
    public  interface  OnItemClick{
        void onClicks(SpannableString i );
    }
}
