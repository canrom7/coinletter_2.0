package com.iimm.miliao.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.GroupSignReward;

import java.util.List;

public class GroupSignRewardAdapter extends RecyclerView.Adapter<GroupSignRewardAdapter.GroupSignRewardHolder> {
    List<GroupSignReward> list;
    private Context context;

    public GroupSignRewardAdapter(List<GroupSignReward> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public GroupSignRewardHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_groupsignreward, viewGroup, false);

        return new GroupSignRewardHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupSignRewardHolder groupSignRewardHolder, int i) {
        groupSignRewardHolder.time.setText(list.get(i).getUpdateTime());
        groupSignRewardHolder.day.setText("连续签到" + list.get(i).getCount() + "天");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class GroupSignRewardHolder extends RecyclerView.ViewHolder {
        TextView time;
        TextView day;

        public GroupSignRewardHolder(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.time);
            day = itemView.findViewById(R.id.signday);
        }
    }
}
