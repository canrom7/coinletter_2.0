package com.iimm.miliao.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.SecurityQuestionBean;

import java.util.List;

/**
 * MrLiu253@163.com
 * 密保问题适配器
 *
 * @time 2019-08-08
 */
public class SecurityQuestionAdapter extends BaseAdapter {
    private List<SecurityQuestionBean.DataBean> mList;
    private Context mContext;

    public SecurityQuestionAdapter(Context context, List<SecurityQuestionBean.DataBean> list) {
        mList = list;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolde holde = null;
        if (convertView == null) {
            holde = new ViewHolde();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_security_question_select, null);
            holde.itemTv = convertView.findViewById(R.id.item_security_question_tv);
            holde.itemV = convertView.findViewById(R.id.item_security_question_v);
            convertView.setTag(holde);
        } else {
            holde = (ViewHolde) convertView.getTag();
        }
        holde.itemTv.setText(mList.get(position).getQuestion());
        if (mList.size() - 1 == position) {
            holde.itemV.setVisibility(View.GONE);
        } else {
            holde.itemV.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    public void setData(List<SecurityQuestionBean.DataBean> beanList) {
        this.mList = beanList;
        notifyDataSetChanged();
    }

    class ViewHolde {
        TextView itemTv;
        View itemV;
    }
}

