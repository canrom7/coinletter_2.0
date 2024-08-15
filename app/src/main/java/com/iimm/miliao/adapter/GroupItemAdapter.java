package com.iimm.miliao.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.sortlist.BaseSortModel;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.ui.message.multi.RoomInfoActivity;
import com.iimm.miliao.ui.other.BasicInfoActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.UiUtils;

import java.util.List;

public class GroupItemAdapter extends RecyclerView.Adapter<GroupItemAdapter.GroupViewHolder> implements SectionIndexer {
    private String TAG = "GroupItemAdapter";
    private Context mContext;
    private List<BaseSortModel<Friend>> mSortFriends;

    private GroupItemClickListener mGroupItemClickListener;

    public void setGroupItemClickListener(GroupItemClickListener itemClickListener) {
        mGroupItemClickListener = itemClickListener;
    }

    public interface GroupItemClickListener {
        void onItemClick(int position, RecyclerView.ViewHolder viewHolder);
    }

    public GroupItemAdapter(Context context, List<BaseSortModel<Friend>> sortFriends) {
        mContext = context;
        mSortFriends = sortFriends;
    }

    public void setData(List<BaseSortModel<Friend>> sortFriends) {
        mSortFriends = sortFriends;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mSortFriends == null ? 0 : mSortFriends.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.group_list_item, viewGroup, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder viewHolder, int position) {
        // 根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(position);
        // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (position == getPositionForSection(section)) {
            viewHolder.catagory_title.setVisibility(View.VISIBLE);
            viewHolder.catagory_title.setText(mSortFriends.get(position).getFirstLetter());
        } else {
            viewHolder.catagory_title.setVisibility(View.GONE);
        }
        final Friend friend = mSortFriends.get(position).getBean();
        AvatarHelper.getInstance().displayLatestMessageItemAvatar(CoreManager.getSelf(mContext).getUserId(), friend, viewHolder.avatar_img);

        // 昵称
        viewHolder.nick_name_tv.setText(!TextUtils.isEmpty(friend.getRemarkName()) ? friend.getRemarkName() : friend.getNickName());
        // 个性签名
        // des_tv.setText(friend.getDescription());

        // 点击头像跳转详情
        viewHolder.avatar_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!UiUtils.isNormalClick(view)) {
                    return;
                }
                if (friend.getRoomFlag() == 0) {  // 单人
                    if (!friend.getUserId().equals(Constants.ID_SYSTEM_MESSAGE)
                            && !friend.getUserId().equals(Constants.ID_NEW_FRIEND_MESSAGE)
                            && friend.getIsDevice() != 1) {
                        Intent intent = new Intent(mContext, BasicInfoActivity.class);
                        intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
                        mContext.startActivity(intent);
                    }
                } else {  // 群组
                    Intent intent = new Intent(mContext, RoomInfoActivity.class);
                    intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
                    mContext.startActivity(intent);
                }
            }
        });
        viewHolder.group_list_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mGroupItemClickListener != null) {
                    mGroupItemClickListener.onItemClick(position, viewHolder);
                }
            }
        });
    }

    @Override
    public Object[] getSections() {
        return null;
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

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        View group_list_item;
        TextView catagory_title;
        RoundedImageView avatar_img;
        TextView nick_name_tv;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            group_list_item = itemView.findViewById(R.id.group_list_item);
            catagory_title = itemView.findViewById(R.id.catagory_title);
            avatar_img = itemView.findViewById(R.id.avatar_img);
            nick_name_tv = itemView.findViewById(R.id.nick_name_tv);
        }
    }
}
