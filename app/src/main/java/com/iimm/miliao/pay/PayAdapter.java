package com.iimm.miliao.pay;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.BankRechargeStatusBean;
import com.iimm.miliao.bean.CodePay;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.Transfer;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;


public class PayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ChatMessage> mChatMessageSource;

    public PayAdapter(List<ChatMessage> chatMessages) {
        this.mChatMessageSource = chatMessages;
        if (mChatMessageSource == null) {
            mChatMessageSource = new ArrayList<>();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int resource;
        if (viewType == PayType.TRANSFER_BACK) {
            resource = R.layout.item_pay_transfer_back;
            return new TransferBackHolder(LayoutInflater.from(parent.getContext()).inflate(resource, parent, false));
        } else if (viewType == PayType.PAYMENT_SUCCESS) {
            resource = R.layout.item_pay_payment;
            return new PaymentHolder(LayoutInflater.from(parent.getContext()).inflate(resource, parent, false));
        } else if (viewType == PayType.RECEIPT_SUCCESS) {
            resource = R.layout.item_pay_receipt;
            return new ReceiptHolder(LayoutInflater.from(parent.getContext()).inflate(resource, parent, false));
        } else if (viewType == PayType.RECHARGE_RESULT) {
            resource = R.layout.item_bank_recharge_result;
            return new BankRechargeHolder(LayoutInflater.from(parent.getContext()).inflate(resource, parent, false));
        } else {
            resource = R.layout.item_pay_unkonw;
            return new SystemViewHolder(LayoutInflater.from(parent.getContext()).inflate(resource, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage chatMessage = mChatMessageSource.get(position);
        if (holder instanceof TransferBackHolder) {
            Transfer transfer = JSON.parseObject(chatMessage.getContent(), Transfer.class);
            ((TransferBackHolder) holder).mNotifyTimeTv.setText(TimeUtils.f_long_2_str(chatMessage.getTimeSend() * 1000));
            ((TransferBackHolder) holder).mMoneyTv.setText("￥" + transfer.getMoney());
            Friend friend = FriendDao.getInstance().getFriend(transfer.getUserId(), transfer.getToUserId());
            if (friend != null) {
                ((TransferBackHolder) holder).mBackReasonTv.setText(MyApplication.getContext().getString(R.string.transfer_back_reason_out_time,
                        TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName()));
            }
            ((TransferBackHolder) holder).mBackTimeTv.setText(TimeUtils.f_long_2_str(transfer.getOutTime() * 1000));
            ((TransferBackHolder) holder).mTransferTimeTv.setText(TimeUtils.f_long_2_str(transfer.getCreateTime() * 1000));
        } else if (holder instanceof PaymentHolder) {
            CodePay codePay = JSON.parseObject(chatMessage.getContent(), CodePay.class);
            ((PaymentHolder) holder).mMoneyTv.setText("￥" + codePay.getMoney());
            if (codePay.getType() == 1) {// 付款码
                ((PaymentHolder) holder).mReceiptUserTv.setText(codePay.getToUserName());
            } else {// 二维码收款
                ((PaymentHolder) holder).mReceiptUserTv.setText(codePay.getUserName());
            }
        } else if (holder instanceof ReceiptHolder) {
            CodePay codePay = JSON.parseObject(chatMessage.getContent(), CodePay.class);
            ((ReceiptHolder) holder).mMoneyTv.setText("￥" + codePay.getMoney());
            if (codePay.getType() == 1) {// 付款码
                ((ReceiptHolder) holder).mPaymentUserTv.setText(codePay.getUserName());
            } else {// 二维码收款
                ((ReceiptHolder) holder).mPaymentUserTv.setText(codePay.getToUserName());
            }
        } else if (holder instanceof BankRechargeHolder) {
            BankRechargeHolder bankRechargeHolder = (BankRechargeHolder) holder;
            BankRechargeStatusBean bankRechargeStatusBean = JSON.parseObject(chatMessage.getContent(), BankRechargeStatusBean.class);
            if (bankRechargeStatusBean != null) {
                bankRechargeHolder.mTvMoney.setText(TextUtils.isEmpty(bankRechargeStatusBean.getAmount()) ? "0.00" : bankRechargeStatusBean.getAmount());
                String statusHint = "";
                String statusMsg = "";
                int color = 0;
                String payStatus = bankRechargeStatusBean.getPayStatus();
                if (TextUtils.isEmpty(payStatus)) {
                    statusMsg = "";
                    statusHint = "";
                    color = Color.BLACK;
                } else {
                    switch (payStatus) {
                        case "0":
                            color = Color.parseColor("#EEB026");
                            statusHint = "处理中";
                            statusMsg = "请稍后,该订单正在处理中";
                            break;
                        case "1":
                            color = Color.parseColor("#3F9E10");
                            statusHint = "充值成功";
                            statusMsg = "充值成功,已存入零钱";
                            break;
                        case "6":
                            color = Color.parseColor("#CB5858");
                            statusHint = "订单超时";
                            statusMsg = "请确认您已支付成功,如您已付款,请联系客服,核实处理;如你多次恶意提交虚假订单，系统将对您账户进行封禁处理";
                            break;
                    }
                }
                if (!TextUtils.isEmpty(bankRechargeStatusBean.getStatusMsg())) {
                    statusMsg = bankRechargeStatusBean.getStatusMsg();
                }
                bankRechargeHolder.mTvStatus.setText(statusHint);
                bankRechargeHolder.mTvStatus.setTextColor(color);
                bankRechargeHolder.mTvRemarks.setText(statusMsg);
            }
        } else {
            ((SystemViewHolder) holder).mSystemTv.setText(chatMessage.getContent());
        }
    }


    @Override
    public int getItemCount() {
        return mChatMessageSource.size();
    }

    @Override
    public int getItemViewType(int position) {
        int type = mChatMessageSource.get(position).getType();
        if (type == Constants.TYPE_TRANSFER_BACK || type == Constants.TYPE_CLOUD_TRANSFER_RETURN) {
            return PayType.TRANSFER_BACK;
        } else if (type == Constants.TYPE_PAYMENT_OUT || type == Constants.TYPE_RECEIPT_OUT) {
            return PayType.PAYMENT_SUCCESS;
        } else if (type == Constants.TYPE_PAYMENT_GET || type == Constants.TYPE_RECEIPT_GET) {
            return PayType.RECEIPT_SUCCESS;
        } else if (type == Constants.TYPE_RECHARGE_GET) {
            return PayType.RECHARGE_RESULT;
        } else if (type == Constants.TYPE_RECHARGE_H5_GET) {
            return PayType.RECHARGE_RESULT;
        } else {
            return PayType.UN_KNOW;
        }
    }

    class TransferBackHolder extends RecyclerView.ViewHolder {
        TextView mNotifyTimeTv;
        TextView mMoneyTv;
        TextView mBackReasonTv;
        TextView mBackTimeTv;
        TextView mTransferTimeTv;

        public TransferBackHolder(View itemView) {
            super(itemView);
            mNotifyTimeTv = itemView.findViewById(R.id.easy_pay_transfer_notify_time_tv);
            mMoneyTv = itemView.findViewById(R.id.easy_pay_transfer_money_tv);
            mBackReasonTv = itemView.findViewById(R.id.easy_pay_transfer_reason);
            mBackTimeTv = itemView.findViewById(R.id.easy_pay_transfer_back_time_tv);
            mTransferTimeTv = itemView.findViewById(R.id.easy_pay_transfer_transfer_time);
        }
    }

    class PaymentHolder extends RecyclerView.ViewHolder {

        TextView mMoneyTv;
        TextView mReceiptUserTv;

        public PaymentHolder(View itemView) {
            super(itemView);
            mMoneyTv = itemView.findViewById(R.id.easy_pay_payment_money_tv);
            mReceiptUserTv = itemView.findViewById(R.id.easy_pay_payment_receipt_user_tv);
        }
    }

    class ReceiptHolder extends RecyclerView.ViewHolder {

        TextView mMoneyTv;
        TextView mPaymentUserTv;

        public ReceiptHolder(View itemView) {
            super(itemView);
            mMoneyTv = itemView.findViewById(R.id.easy_pay_receipt_money_tv);
            mPaymentUserTv = itemView.findViewById(R.id.easy_pay_receipt_payment_user_tv);
        }
    }

    class SystemViewHolder extends RecyclerView.ViewHolder {

        TextView mSystemTv;

        public SystemViewHolder(View itemView) {
            super(itemView);
            mSystemTv = itemView.findViewById(R.id.chat_content_tv);
        }
    }

    class BankRechargeHolder extends RecyclerView.ViewHolder {

        private final TextView mTvMoney;
        private final TextView mTvStatus;
        private final TextView mTvRemarks;

        public BankRechargeHolder(View view) {
            super(view);
            mTvMoney = view.findViewById(R.id.tv_money);
            mTvStatus = view.findViewById(R.id.tv_status);
            mTvRemarks = view.findViewById(R.id.tv_remarks);
        }
    }
}
