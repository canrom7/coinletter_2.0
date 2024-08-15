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

import com.makeramen.roundedimageview.RoundedImageView;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.company.StructBeanNetInfo;
import com.iimm.miliao.ui.company.CompanyDetailActivity;
import com.iimm.miliao.view.NoDoubleClickListener;

import java.util.List;

public class CompanyListAdapter extends RecyclerView.Adapter<CompanyListAdapter.CompanyListViewHolder> {
    private List<StructBeanNetInfo> mStructData;
    private Context context;

    public CompanyListAdapter(List<StructBeanNetInfo> mStructData, Context context) {
        this.mStructData = mStructData;
        this.context = context;
    }


    @NonNull
    @Override
    public CompanyListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_companylist, viewGroup, false);
        CompanyListViewHolder holder = new CompanyListViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CompanyListViewHolder companyListViewHolder, int i) {
        if (i == 0) {
            companyListViewHolder.show.setVisibility(View.VISIBLE);
        } else {
            companyListViewHolder.show.setVisibility(View.GONE);
        }
        StructBeanNetInfo info = mStructData.get(i);
        companyListViewHolder.company_name.setText(info.getCompanyName());
        companyListViewHolder.item.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                Intent intent = new Intent(context, CompanyDetailActivity.class);
                intent.putExtra("postion", i);
                Bundle bundle = new Bundle();
                bundle.putSerializable("data", info);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mStructData.size();
    }

    public class CompanyListViewHolder extends RecyclerView.ViewHolder {
        LinearLayout show;
        RoundedImageView logo;
        TextView company_name;
        ConstraintLayout item;

        public CompanyListViewHolder(@NonNull View itemView) {
            super(itemView);
            show = itemView.findViewById(R.id.show);
            logo = itemView.findViewById(R.id.logo);
            company_name = itemView.findViewById(R.id.company_name);
            item = itemView.findViewById(R.id.item);
        }
    }
}
