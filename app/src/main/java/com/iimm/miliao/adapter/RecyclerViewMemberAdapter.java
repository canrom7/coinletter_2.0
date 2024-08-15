package com.iimm.miliao.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.message.MucRoomMember;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecyclerViewMemberAdapter extends RecyclerView.Adapter<RecyclerViewMemberAdapter.GridViewHolder> {
    private String TAG = "GridViewMemberAdapter";
    private Context mContext;
    private List<MucRoomMember> mMucRoomMemberList = new ArrayList<>();
    private Map<String, String> mRemarksMap;
    private int mMemberRole;
    private OnItemClickListener mItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onAddMemberClick();

        void onDeleteMemberClick();
    }

    public void setOnClickListener(OnItemClickListener onClickListener) {
        mItemClickListener = onClickListener;
    }

    public RecyclerViewMemberAdapter(Context context) {
        mContext = context;
    }

    public RecyclerViewMemberAdapter(Context context, List<MucRoomMember> mucRoomMembers) {
        mMucRoomMemberList.addAll(mucRoomMembers);
        mContext = context;
    }

    public void setData(List<MucRoomMember> mucRoomMembers) {
        mMucRoomMemberList.clear();
        mMucRoomMemberList.addAll(mucRoomMembers);
        notifyDataSetChanged();
    }

    public void setRemarkMap(Map<String, String> remarkMap) {
        mRemarksMap = remarkMap;
    }

    public void setMemberRole(int role) {
        mMemberRole = role;
    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_group_user, viewGroup, false);
        //创建一个ViewHodler对象
        GridViewHolder gridViewHolder = new GridViewHolder(itemView);
        //把ViewHolder传出去
        return gridViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder gridViewHolder, int position) {
        gridViewHolder.rl_group_member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    if (position == mMucRoomMemberList.size() + 1) {
                        mItemClickListener.onDeleteMemberClick();
                    } else if (position == mMucRoomMemberList.size()) {
                        mItemClickListener.onAddMemberClick();
                    } else {
                        mItemClickListener.onItemClick(position);
                    }
                }
            }
        });
        if (position == mMucRoomMemberList.size() + 1) {
            gridViewHolder.memberName.setText("");
            gridViewHolder.imageView.setImageResource(R.drawable.bg_room_info_minus_btn);
        } else if (position == mMucRoomMemberList.size()) {
            gridViewHolder.memberName.setText("");
            gridViewHolder.imageView.setImageResource(R.drawable.bg_room_info_add_btn);
        } else {
            handlerMemberView(position, gridViewHolder.memberName, gridViewHolder.imageView);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (mMucRoomMemberList == null) {
            return 0;
        }
        if (mMemberRole == Constants.ROLE_OWNER || mMemberRole == Constants.ROLE_MANAGER) {
            return mMucRoomMemberList.size() + 2;
        } else {
            return mMucRoomMemberList.size();
        }

    }


    public void handlerMemberView(int position, TextView memberName, ImageView imageView) {
        MucRoomMember mMucRoomMember = mMucRoomMemberList.get(position);
        String name;
        if (mMemberRole == 1) {// 群主 群内备注>好友备注>群内昵称
            if (!TextUtils.isEmpty(mMucRoomMember.getRemarkName())) {
                name = mMucRoomMember.getRemarkName();
            } else {
                if (mRemarksMap != null && mRemarksMap.containsKey(mMucRoomMemberList.get(position).getUserId())) {// 群组内 我的好友 显示 我对他备注的名字
                    name = mRemarksMap.get(mMucRoomMember.getUserId());
                } else {
                    name = mMucRoomMember.getNickName();
                }
            }
        } else {
            if (mRemarksMap != null && mRemarksMap.containsKey(mMucRoomMemberList.get(position).getUserId())) {// 群组内 我的好友 显示 我对他备注的名字
                name = mRemarksMap.get(mMucRoomMember.getUserId());
            } else {
                name = mMucRoomMember.getNickName();
            }
        }
        AvatarHelper.getInstance().displayAvatar(name, mMucRoomMember.getUserId(), imageView, true);
        memberName.setText(name);
    }

    class GridViewHolder extends RecyclerView.ViewHolder {
        View rl_group_member;
        ImageView imageView;
        TextView memberName;

        GridViewHolder(View itemView) {
            super(itemView);
            rl_group_member = itemView.findViewById(R.id.rl_group_member);
            imageView = itemView.findViewById(R.id.member_avatar);
            memberName = itemView.findViewById(R.id.member_name);
        }
    }
}


