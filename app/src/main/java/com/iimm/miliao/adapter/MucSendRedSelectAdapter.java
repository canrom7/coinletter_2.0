package com.iimm.miliao.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.MucSendRedSelectBean;
import com.iimm.miliao.databinding.ItemMucSendRedSelectBinding;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.ui.base.CoreManager;

import java.util.List;

/**
 * MrLiu253@163.com
 *
 * @time 2020-02-27
 */
public class MucSendRedSelectAdapter extends RecyclerView.Adapter<MucSendRedSelectAdapter.MucSendRedSelectViewHolder> {

    private List<MucSendRedSelectBean> mMucSendRedSelectBeans;

    public MucSendRedSelectAdapter(List<MucSendRedSelectBean> mucSendRedSelectBeans) {
        mMucSendRedSelectBeans = mucSendRedSelectBeans;
    }

    @NonNull
    @Override
    public MucSendRedSelectViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ItemMucSendRedSelectBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.item_muc_send_red_select, viewGroup, false);
        return new MucSendRedSelectViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MucSendRedSelectViewHolder holder, int i) {
        MucSendRedSelectBean mucSendRedSelectBean = mMucSendRedSelectBeans.get(i);
        AvatarHelper.getInstance().displayAvatar(mucSendRedSelectBean.getUserId(), holder.binding.avatarImg, true);

        Friend friend = FriendDao.getInstance().getFriend(CoreManager.getSelf(MyApplication.getContext()).getUserId(), mucSendRedSelectBean.getUserId());
        if (friend != null && !TextUtils.isEmpty(friend.getRemarkName())) {
            holder.binding.userRemarkNameTv.setText("备注：" + friend.getRemarkName());
            holder.binding.userRemarkNameTv.setVisibility(View.VISIBLE);
        } else {
            holder.binding.userRemarkNameTv.setVisibility(View.GONE);
        }
        holder.binding.setMucSendRedSelectBean(mucSendRedSelectBean);
        holder.binding.executePendingBindings();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mucSendRedSelectBean.isSelect()){
                    mucSendRedSelectBean.setSelect(false);
                }else {
                    mucSendRedSelectBean.setSelect(true);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMucSendRedSelectBeans.size();
    }

    public void setData(List<MucSendRedSelectBean> mucRoomMembers) {
        this.mMucSendRedSelectBeans = mucRoomMembers;
        notifyDataSetChanged();
    }

    class MucSendRedSelectViewHolder extends RecyclerView.ViewHolder {

        ItemMucSendRedSelectBinding binding;

        public MucSendRedSelectViewHolder(ItemMucSendRedSelectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }
}
