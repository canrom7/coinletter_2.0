package com.iimm.miliao.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.company.StructBeanNetInfo;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.ui.company.EmployeesDetailActivity;
import com.iimm.miliao.ui.company.SearchFilter;
import com.iimm.miliao.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class EmployeesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    List<StructBeanNetInfo.DepartmentsBean.EmployeesBean> data;
    SearchFilter<StructBeanNetInfo.DepartmentsBean.EmployeesBean> nameFilter;
    private int departpostion;
    private StructBeanNetInfo structBeanNetInfo;

    public void setStructBeanNetInfo(StructBeanNetInfo structBeanNetInfo) {
        this.structBeanNetInfo = structBeanNetInfo;
    }

    public SearchFilter getFilter() {
        if (nameFilter == null) {
            List<String> filterKeys = new ArrayList<>();
            filterKeys.add("nickname");
            nameFilter = new SearchFilter<>(data, filterKeys, "id");
        }
        return nameFilter;
    }

    public void setDepartpostion(int departpostion) {
        this.departpostion = departpostion;
    }

    public EmployeesAdapter(Context context, List<StructBeanNetInfo.DepartmentsBean.EmployeesBean> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_employees, viewGroup, false);

        return new EmployeesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        EmployeesViewHolder holder = (EmployeesViewHolder) viewHolder;
        StructBeanNetInfo.DepartmentsBean.EmployeesBean bean = data.get(i);
        if (holder.head instanceof RoundedImageView) {
            if (Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR) {
                ((RoundedImageView) holder.head).setOval(true);
            } else {
                ((RoundedImageView) holder.head).setOval(false);
                ((RoundedImageView) holder.head).setCornerRadius(Constants.AVATAR_CORNER_RADIUS);
            }
        }
        Glide.with(context).load(AvatarHelper.getAvatarUrl(bean.getUserId() + "", false))
                .into(holder.head);
        holder.name.setText(bean.getNickname());
        holder.position.setText(bean.getPosition());
        if (bean.getRole() == 3) {
            holder.identity.setVisibility(View.VISIBLE);
        } else {
            holder.identity.setVisibility(View.GONE);
        }
        holder.item.setOnClickListener(v -> {
            Intent intent = new Intent(context, EmployeesDetailActivity.class);
            intent.putExtra("departmentposition", departpostion);
            intent.putExtra("userposition", i);
            Bundle bundle = new Bundle();
            bundle.putSerializable("companyinfo", structBeanNetInfo);
            intent.putExtras(bundle);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class EmployeesViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView head;
        TextView name;
        TextView position;
        TextView identity;
        ConstraintLayout item;

        public EmployeesViewHolder(@NonNull View itemView) {
            super(itemView);
            head = itemView.findViewById(R.id.head);
            name = itemView.findViewById(R.id.name);
            position = itemView.findViewById(R.id.position);
            identity = itemView.findViewById(R.id.identity);
            item = itemView.findViewById(R.id.item);
        }
    }

}
