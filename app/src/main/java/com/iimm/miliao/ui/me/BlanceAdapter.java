package com.iimm.miliao.ui.me;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.BlanceInfo;
import com.iimm.miliao.util.UiUtils;

import java.util.List;

public class BlanceAdapter extends RecyclerSwipeAdapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<BlanceInfo.MethodBean> blanceInfos;
    private OnClickeListener onClickeListener;

    public void setOnClickeListener(OnClickeListener onClickeListener) {
        this.onClickeListener = onClickeListener;
    }

    public BlanceAdapter(Context context, List<BlanceInfo.MethodBean> blanceInfos) {
        this.context = context;
        this.blanceInfos = blanceInfos;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int itemType) {
        if (itemType == -1) {
            View view = LayoutInflater.from(context).inflate(R.layout.adapter_blance_item, viewGroup, false);
            return new BlanceItemHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.adapter_blance, viewGroup, false);
            return new BlanceHolder(view);
        }


    }

    @Override
    public int getItemViewType(int position) {
        return blanceInfos.get(position).getType();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        BlanceInfo.MethodBean methodBean = blanceInfos.get(i);
        switch (methodBean.getType()) {
            case -1:
                if (viewHolder instanceof BlanceItemHolder) {
                    ((BlanceItemHolder) viewHolder).addLl.setOnClickListener(v -> {
                        if (!UiUtils.isNormalClick(v)) {
                            return;
                        }
                        if (methodBean.getWithdrawWaySort() == 0) {
                            if (onClickeListener != null) {
                                onClickeListener.AddAlipay(i);
                            }
                        } else if (methodBean.getWithdrawWaySort() == 1) {
                            if (onClickeListener != null) {
                                onClickeListener.AddBank(i);
                            }
                        } else {
                            if (onClickeListener != null) {
                                onClickeListener.AddDynamic(i);
                            }
                        }
                    });
                    ((BlanceItemHolder) viewHolder).addcontentTv.setText(String.format("添加新的%s", methodBean.getWithdrawWayName()));
                }
                break;
            case 1:
                if (viewHolder instanceof BlanceHolder) {
                    ((BlanceHolder) viewHolder).itemlogo.setImageResource(R.drawable.alipay_logo);
                    ((BlanceHolder) viewHolder).itemcomtent.setText(methodBean.getAlipayNumber());
                }
                break;
            case 5:
                if (viewHolder instanceof BlanceHolder) {
                    ((BlanceHolder) viewHolder).itemlogo.setImageResource(R.mipmap.bank_icon);
                    ((BlanceHolder) viewHolder).itemcomtent.setText(String.format("%s (%s)", methodBean.getBankName(), methodBean.getBankCardNo()));
                }
                break;
            default:
                if (viewHolder instanceof BlanceHolder) {
                    ((BlanceHolder) viewHolder).itemlogo.setImageResource(R.mipmap.icon_dynamic);
                    ((BlanceHolder) viewHolder).itemcomtent.setText(methodBean.getOtherNode1() + " " + methodBean.getOtherNode2());
                }
                break;
        }
        if (viewHolder instanceof BlanceHolder) {
            mItemManger.bind(viewHolder.itemView, i);
            ((BlanceHolder) viewHolder).item.setShowMode(SwipeLayout.ShowMode.LayDown);
            ((BlanceHolder) viewHolder).delete.setOnClickListener(v -> {
                closeAllItems();
                if (onClickeListener != null) {
                    onClickeListener.delete(i);
                }
            });
            ((BlanceHolder) viewHolder).mLinearLayout.setOnClickListener(v -> {
                if (isOpen(i)) {
                    closeAllItems();
                } else {
                    if (onClickeListener != null) {
                        onClickeListener.onClick(i);
                    }
                }

            });
            if (methodBean.isSelect()) {
                ((BlanceHolder) viewHolder).mImageView.setVisibility(View.VISIBLE);
            } else {
                ((BlanceHolder) viewHolder).mImageView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return blanceInfos.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.item;
    }

    public void setData(List<BlanceInfo.MethodBean> list) {
        this.blanceInfos = list;
        notifyDatasetChanged();
    }

    public void removeList(int position) {
        blanceInfos.remove(position);//删除数据源,移除集合中当前下标的数据
        notifyItemRemoved(position);//刷新被删除的地方
        notifyItemRangeChanged(position, getItemCount()); //刷新被删除数据，以及其后面的数据
    }

    public class BlanceHolder extends RecyclerView.ViewHolder {
        private ImageView addlogo;
        private ImageView delete;
        private ImageView itemlogo;
        private TextView itemcomtent;
        private SwipeLayout item;
        private RelativeLayout mLinearLayout;
        private ImageView mImageView;


        public BlanceHolder(@NonNull View itemView) {
            super(itemView);
            addlogo = itemView.findViewById(R.id.addlogo);
            delete = itemView.findViewById(R.id.delete);
            itemlogo = itemView.findViewById(R.id.itemlogo);
            itemcomtent = itemView.findViewById(R.id.itemcomtent);
            item = itemView.findViewById(R.id.item);
            mLinearLayout = itemView.findViewById(R.id.item_ll);
            mImageView = itemView.findViewById(R.id.select);
        }
    }

    public class BlanceItemHolder extends RecyclerView.ViewHolder {
        private LinearLayout addLl;
        private TextView addcontentTv;

        public BlanceItemHolder(@NonNull View itemView) {
            super(itemView);
            addLl = itemView.findViewById(R.id.add);
            addcontentTv = itemView.findViewById(R.id.addcontent);
        }
    }

    public interface OnClickeListener {
        void onClick(int postion);

        void delete(int postion);

        void AddAlipay(int postion);

        void AddBank(int postion);

        void AddDynamic(int postion);
    }
}
