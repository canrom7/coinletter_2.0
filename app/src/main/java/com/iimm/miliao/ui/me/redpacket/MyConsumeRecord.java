package com.iimm.miliao.ui.me.redpacket;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.redpacket.ConsumeRecordItem;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseListActivity;
import com.iimm.miliao.ui.mucfile.XfileUtils;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

/**
 * 消费记录
 * Created by wzw on 2016/9/26.
 */
public class MyConsumeRecord extends BaseListActivity<MyConsumeRecord.MyConsumeHolder> {
    private static final String TAG = "MyConsumeRecord";
    List<ConsumeRecordItem.PageDataEntity> datas = new ArrayList<>();

    @Override
    public void initView() {
        super.initView();
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText("交易记录");
    }

    @Override
    public void initDatas(int pager) {
        if (pager == 0) {
            datas.clear();
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        // 如果是下拉刷新就重新加载第一页
        params.put("pageIndex", pager + "");
        params.put("pageSize", "30");
        HttpUtils.get().url(coreManager.getConfig().CONSUMERECORD_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<ConsumeRecordItem>(ConsumeRecordItem.class) {

                    @Override
                    public void onResponse(ObjectResult<ConsumeRecordItem> result) {
                        if (result.getData().getPageData() != null) {
                            for (ConsumeRecordItem.PageDataEntity data : result.getData().getPageData()) {
                                final double money = data.getMoney();
                                boolean isZero = Double.toString(money).equals("0.0");
                                Log.d(TAG, "bool : " + isZero + " \t" + money);
                                if (!isZero) {
                                    datas.add(data);
                                }
                            }
                            if (result.getData().getPageData().size() != 30) {
                                more = false;
                            } else {
                                more = true;
                            }
                        } else {
                            more = false;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                update(datas);
                            }
                        });
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(MyConsumeRecord.this);
                    }
                });
    }

    @Override
    public MyConsumeHolder initHolder(ViewGroup parent) {
        View v = mInflater.inflate(R.layout.consumerecord_item, parent, false);
        MyConsumeHolder holder = new MyConsumeHolder(v);
        return holder;
    }

    @Override
    public void fillData(MyConsumeHolder holder, int position) {
        ConsumeRecordItem.PageDataEntity info = datas.get(position);
        long time = Long.valueOf(info.getTime());
        String StrTime = XfileUtils.fromatTime(time * 1000, "MM-dd HH:mm");
        holder.nameTv.setText(info.getDesc());
        holder.timeTv.setText(StrTime);
        holder.moneyTv.setText(XfileUtils.fromatFloat(info.getMoney()) + InternationalizationHelper.getString("YUAN"));
        holder.nameTv.setTextColor(Color.BLACK);
        holder.moneyTv.setTextColor(Color.BLACK);
        int type = info.getType();
        switch (info.getStatus()) {
            case 0: //订单创建
                holder.nameTv.setTextColor(Color.parseColor("#23B525"));
                holder.moneyTv.setTextColor(Color.parseColor("#23B525"));
                break;
            case 1: //支付  充值 成功
                if (type == 1 || type == 3 || type == 5
                        || type == 6 || type == 8 || type == 9 || type == 11
                        || type == 13 || type == 14) {
                    holder.moneyTv.setTextColor(Color.parseColor("#EEB026"));
                } else if (type == 2 || type == 4 || type == 7
                        || type == 10 || type == 12 || type == 16) {
                    holder.moneyTv.setTextColor(Color.BLACK);
                }
                break;
            case -1: //交易关闭:
                holder.nameTv.setTextColor(Color.parseColor("#ED6350"));
                holder.moneyTv.setTextColor(Color.parseColor("#ED6350"));
                break;
            case 2:  //审核初始化
                holder.nameTv.setTextColor(Color.parseColor("#23B525"));
                holder.moneyTv.setTextColor(Color.parseColor("#23B525"));
                break;
            case 3:  //审核驳回
                holder.nameTv.setTextColor(Color.parseColor("#ED6350"));
                holder.moneyTv.setTextColor(Color.parseColor("#ED6350"));
                break;
            case 4:   //交易完成
                holder.moneyTv.setTextColor(Color.parseColor("#EEB026"));
                break;
            case 5:   //支付失败
                holder.nameTv.setTextColor(Color.parseColor("#ED6350"));
                holder.moneyTv.setTextColor(Color.parseColor("#ED6350"));
                break;
        }

        if (type == 1 && info.getPayType() == 6) {
            if (info.getStatus() == 1) {
                holder.nameTv.setText(info.getDesc() + "成功");
            } else if (info.getStatus() == 5) {
                holder.nameTv.setText(info.getDesc() + "失败");
            }
        }

        if (type == 15) {
            holder.ivWithDraw.setVisibility(View.VISIBLE);
            switch (info.getStatus()) {
                case 1:
                    holder.ivWithDraw.setImageResource(R.mipmap.withdraw_yes);
                    break;
                case 2:
                    holder.ivWithDraw.setImageResource(R.mipmap.withdrawing);
                    break;
                case 3:
                    holder.ivWithDraw.setImageResource(R.mipmap.withdraw_no);
                    break;
                default:
                    holder.ivWithDraw.setVisibility(View.GONE);
                    break;
            }
        } else {
            holder.ivWithDraw.setVisibility(View.GONE);
        }
    }

    public class MyConsumeHolder extends RecyclerView.ViewHolder {
        public TextView nameTv, timeTv, moneyTv;
        public ImageView ivWithDraw;

        public MyConsumeHolder(View itemView) {
            super(itemView);
            nameTv = (TextView) itemView.findViewById(R.id.textview_name);
            timeTv = (TextView) itemView.findViewById(R.id.textview_time);
            moneyTv = (TextView) itemView.findViewById(R.id.textview_money);
            ivWithDraw = itemView.findViewById(R.id.iv_withdarw_status);
        }
    }
}
