package com.iimm.miliao.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.SquareBean;
import com.iimm.miliao.util.ViewHolder;

import java.util.List;

/**
 * MrLiu253@163.com
 *
 * @time 2019-07-16
 */
public class SquareAdapter extends BaseAdapter {
    private List<SquareBean.DataBean> mSquareBeans;
    private Context mContext;

    public SquareAdapter(List<SquareBean.DataBean> squareBeans, Context context) {
        mSquareBeans = squareBeans;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mSquareBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_square, parent, false);
        }
        RelativeLayout item_square = ViewHolder.get(convertView, R.id.item_square_rl);
        ImageView imageView = ViewHolder.get(convertView, R.id.square_item_image);
        TextView title = ViewHolder.get(convertView, R.id.square_item_tv);
        View view = ViewHolder.get(convertView, R.id.square_item_view);

        String imageUrl = mSquareBeans.get(position).getDiscoverImg();
        Glide.with(mContext)
                .load(imageUrl)
                .placeholder(R.drawable.default_gray)
                .error(R.drawable.default_gray)
                .into(imageView);
        title.setText(mSquareBeans.get(position).getDiscoverName());
        if (position == 0) {
            view.setVisibility(View.GONE);
            item_square.setBackgroundResource(R.drawable.square_top);
        } else if (position == mSquareBeans.size() - 1) {
            view.setVisibility(View.VISIBLE);
            item_square.setBackgroundResource(R.drawable.square_bottom);
        } else {
            view.setVisibility(View.VISIBLE);
            item_square.setBackgroundResource(R.drawable.square_center);
        }
        if (getCount() == 1) {
            view.setVisibility(View.GONE);
            item_square.setBackgroundResource(R.drawable.msg_list_selector_background);
        }
        return convertView;
    }

    public void setData(List<SquareBean.DataBean> squareBeans) {
        this.mSquareBeans = squareBeans;
        notifyDataSetChanged();
    }


    /**
     * 局部更新数据，调用一次getView()方法；Google推荐的做法
     *
     * @param listView 要更新的listview
     * @param position 要更新的位置
     */
    public void notifyDataSetChanged(ListView listView, int position) { /**第一个可见的位置**/
        int firstVisiblePosition = listView.getFirstVisiblePosition(); /**最后一个可见的位置**/
        int lastVisiblePosition = listView.getLastVisiblePosition(); /**在看见范围内才更新，不可见的滑动后自动会调用getView方法更新**/
        if (position >= firstVisiblePosition && position <= lastVisiblePosition) { /**获取指定位置view对象**/
            View view = listView.getChildAt(position - firstVisiblePosition);
            getView(position, view, listView);
        }
    }

}
