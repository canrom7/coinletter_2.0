package com.iimm.miliao.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.NodeInfo;

import java.util.List;


/**
 * Created by Administrator on 2019/3/27 0027.
 */

public class NodeAdapter extends BaseAdapter {
    private List<NodeInfo> mNodeList;
    private Context mContext;
    LayoutInflater inflater;

    public NodeAdapter(Context context, List<NodeInfo> listBeans) {
        mContext = context;
        mNodeList = listBeans;
        inflater = LayoutInflater.from(context);
    }

    private void setData(List<NodeInfo> listBeans) {
        mNodeList = listBeans;
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return mNodeList == null ? 0 : mNodeList.size();
    }

    @Override
    public Object getItem(int position) {
        return mNodeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NodeViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_node, null);
            viewHolder = new NodeViewHolder();
            viewHolder.tv_item_node = (TextView) convertView.findViewById(R.id.tv_item_node);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (NodeViewHolder) convertView.getTag();
        }
        if (!TextUtils.isEmpty(mNodeList.get(position).getNodeName())) {
            viewHolder.tv_item_node.setText(mNodeList.get(position).getNodeName());
        }
        return convertView;
    }


    public interface OnNodeItemClickListener {
        void onItemClick(RecyclerView.ViewHolder viewHolder, int position);
    }

    private OnNodeItemClickListener mOnNodeItemClickListener;

    public void setOnNodeClickListener(OnNodeItemClickListener OnNodeItemClickListener) {
        mOnNodeItemClickListener = OnNodeItemClickListener;
    }

    /**
     * 公告 图片消息
     */
    public class NodeViewHolder {
        private TextView tv_item_node;
    }
}
