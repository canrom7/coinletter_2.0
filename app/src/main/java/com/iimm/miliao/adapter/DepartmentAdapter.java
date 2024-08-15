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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.company.StructBeanNetInfo;
import com.iimm.miliao.ui.company.DepartmentDetailActivity;

import java.util.List;

public class DepartmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<StructBeanNetInfo.DepartmentsBean> datalist;
    Context context;
    private int companypostion;
    private StructBeanNetInfo structBeanNetInfo;
    private OnclickItemListener onclickItemListener;

    public void setOnclickItemListener(OnclickItemListener onclickItemListener) {
        this.onclickItemListener = onclickItemListener;
    }


    public DepartmentAdapter(List<StructBeanNetInfo.DepartmentsBean> datalist, Context context) {
        this.datalist = datalist;
        this.context = context;
    }

    public void setStructBeanNetInfo(StructBeanNetInfo structBeanNetInfo) {
        this.structBeanNetInfo = null;
        this.structBeanNetInfo = structBeanNetInfo;
    }

    public void setCompanypostion(int companypostion) {
        this.companypostion = companypostion;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_department, viewGroup, false);
        DepartmentViewHolder viewHolder = new DepartmentViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        DepartmentViewHolder holder = (DepartmentViewHolder) viewHolder;
        StructBeanNetInfo.DepartmentsBean bean = datalist.get(i);
        holder.department_name.setText(bean.getDepartName() + "(" + bean.getEmployees().size() + ")");
        holder.wx_group.setOnClickListener(v -> {

            if (onclickItemListener != null) {
                onclickItemListener.onGroupItemClick(i);
            }
        });
        holder.item.setOnClickListener(v -> {
            Intent intent = new Intent(context, DepartmentDetailActivity.class);
            intent.putExtra("companypostion", companypostion);
            intent.putExtra("departpostion", (i + 1));
            Bundle bundle = new Bundle();
            bundle.putSerializable("company", structBeanNetInfo);
            intent.putExtras(bundle);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }

    private class DepartmentViewHolder extends RecyclerView.ViewHolder {
        private TextView department_name;
        private LinearLayout wx_group;
        private ConstraintLayout item;

        public DepartmentViewHolder(@NonNull View itemView) {
            super(itemView);
            department_name = itemView.findViewById(R.id.department_name);
            wx_group = itemView.findViewById(R.id.wx_group);
            item = itemView.findViewById(R.id.item);

        }
    }

    public interface OnclickItemListener {
        void onGroupItemClick(int i);

    }
}
