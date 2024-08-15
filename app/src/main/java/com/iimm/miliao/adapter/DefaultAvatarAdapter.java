package com.iimm.miliao.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.makeramen.roundedimageview.RoundedImageView;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.ImgBean;

import java.lang.reflect.Field;
import java.util.List;

public class DefaultAvatarAdapter extends RecyclerView.Adapter<DefaultAvatarAdapter.DefaultAvatarHolder> {
    List<ImgBean> list;
    Context context;
    OnClickLisener onClickLisener;

    public void setOnClickLisener(OnClickLisener onClickLisener) {
        this.onClickLisener = onClickLisener;
    }

    public DefaultAvatarAdapter(List<ImgBean> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public DefaultAvatarHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_defaultavatar, viewGroup, false);
        DefaultAvatarHolder holder = new DefaultAvatarHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull DefaultAvatarHolder defaultAvatarHolder, int i) {
        if (list.get(i).isSelector()) {
            defaultAvatarHolder.avatar_img_selecotr.setVisibility(View.VISIBLE);
        } else {
            defaultAvatarHolder.avatar_img_selecotr.setVisibility(View.GONE);
        }
        Class drawable = R.drawable.class;
        Field field = null;
        try {
            field = drawable.getField(list.get(i).getUrl());
            int res_ID = field.getInt(field.getName());
            defaultAvatarHolder.avatar_img.setImageResource(res_ID);

            defaultAvatarHolder.avatar_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickLisener != null) {
                        onClickLisener.onClick(i);
                    }
                }
            });
        } catch (Exception e) {
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class DefaultAvatarHolder extends RecyclerView.ViewHolder {
        RoundedImageView avatar_img;
        RoundedImageView avatar_img_selecotr;

        public DefaultAvatarHolder(@NonNull View itemView) {
            super(itemView);
            avatar_img = itemView.findViewById(R.id.avatar_img);
            avatar_img_selecotr = itemView.findViewById(R.id.avatar_img_selecotr);
        }
    }

    public interface OnClickLisener {
        void onClick(int i);
    }
}
