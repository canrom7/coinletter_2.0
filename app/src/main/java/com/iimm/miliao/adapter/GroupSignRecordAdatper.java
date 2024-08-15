package com.iimm.miliao.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.redpacket.GroupSignRecord;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.sortlist.BaseSortModel;

import java.util.List;

public class GroupSignRecordAdatper extends RecyclerView.Adapter<GroupSignRecordAdatper.GroupSignRecordHolder> {
    private Context context;
    private List<BaseSortModel<GroupSignRecord>> mSortFriends;
    SeleCtorListener seleCtorListener;

    public GroupSignRecordAdatper(Context context, List<BaseSortModel<GroupSignRecord>> mSortFriends) {
        this.context = context;
        this.mSortFriends = mSortFriends;
    }

    public SeleCtorListener getSeleCtorListener() {
        return seleCtorListener;
    }

    public void setSeleCtorListener(SeleCtorListener seleCtorListener) {
        this.seleCtorListener = seleCtorListener;
    }

    @NonNull
    @Override
    public GroupSignRecordHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_groupsignrecord, viewGroup, false);
        return new GroupSignRecordHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupSignRecordHolder groupSignRcordHolder, int position) {
// 根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(position);
        // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (position == getPositionForSection(section)) {
            groupSignRcordHolder.title.setVisibility(View.VISIBLE);
            groupSignRcordHolder.title.setText(mSortFriends.get(position).getFirstLetter());
        } else {
            groupSignRcordHolder.title.setVisibility(View.GONE);
        }
        groupSignRcordHolder.selector.setOnClickListener(v -> {
            if (seleCtorListener != null) {
                seleCtorListener.selector(position);
            }
        });

        groupSignRcordHolder.selector.setChecked(mSortFriends.get(position).getBean().isIchecked());
        groupSignRcordHolder.nickname.setText(mSortFriends.get(position).getBean().getNickName());
        groupSignRcordHolder.days.setText("连续签到" + mSortFriends.get(position).getBean().getSerialCount() + "天");
        Glide.with(context).load(AvatarHelper.getAvatarUrl(mSortFriends.get(position).getBean().getUserId(), false))
                .into(groupSignRcordHolder.headview);
    }

    @Override
    public int getItemCount() {
        return mSortFriends.size();
    }

    public class GroupSignRecordHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView nickname;
        TextView days;
        RoundedImageView headview;
        CheckBox selector;

        public GroupSignRecordHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            nickname = itemView.findViewById(R.id.nickname);
            days = itemView.findViewById(R.id.days);
            headview = itemView.findViewById(R.id.headview);
            selector = itemView.findViewById(R.id.selector);
        }
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getItemCount(); i++) {
            String sortStr = mSortFriends.get(i).getFirstLetter();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的Char ascii值
     */
    public int getSectionForPosition(int position) {
        return mSortFriends.get(position).getFirstLetter().charAt(0);
    }

    public interface SeleCtorListener {
        void selector(int i);
    }
}
