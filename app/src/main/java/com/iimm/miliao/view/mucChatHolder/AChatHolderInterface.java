package com.iimm.miliao.view.mucChatHolder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.RoomMember;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToolUtils;
import com.iimm.miliao.view.HeadView;
import com.iimm.miliao.view.TimeCountDownView;
import com.iimm.miliao.xmpp.util.ImHelper;

import java.util.List;


public abstract class AChatHolderInterface extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {
    private static String TAG = "AChatHolderInterface";
    public Context mContext;
    public boolean isMysend;
    public List<ChatMessage> chatMessages;
    public boolean isGroup; // 是否是群聊
    public boolean isMultiple; // 多选
    public boolean showPerson;
    public int position, mouseY;
    public ChatHolderFactory.ChatHolderType mHolderType;
    public String mLoginUserId;
    public String mLoginNickName;
    public String mToUserId;
    public ChatMessage mdata;
    // 在群里的身份，为null表示没有身份，比如单聊，
    @Nullable
    public Integer selfGroupRole;

    public HeadView mIvHead; // 头像
    public TextView mTvName; // 名字
    public TextView mTvTime; // 时间
    public View mRootView; // 根布局
    public TimeCountDownView mIvFire; // 消息阅后即焚
    public TextView mTvSendState; // 消息发送状态
    public ImageView mIvFailed; // 消息发送失败感叹号
    public ProgressBar mSendingBar; // 发送中的转圈
    public CheckBox mCboxSelect; // 多选
    public ImageView ivUnRead; // 未读消息红点
    public ChatHolderListener mHolderListener;
    public View itemView;

    public AChatHolderInterface(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
    }

    /**
     * 分析聊天holder的共性
     * 每一个holder都要去 findviewbyid
     * 90%的holder都要加载头像，处理时间控件,显示名字，消息发送状态
     */

    protected abstract int itemLayoutId(boolean isMysend);

    protected abstract void initView(View view);

    protected abstract void fillData(ChatMessage message);

    protected abstract void onRootClick(View v); // 重写此方法获得包裹布局的子view

    public int getLayoutId(boolean isMysend) {
        this.isMysend = isMysend;
        return itemLayoutId(isMysend);
    }

    public void findView() {
        // 初始化 公共的布局
        if (enableNormal()) {
            inflateNormal(itemView);
        }
        // 初始化 子类view 自己的布局
        initView(itemView);
        if (enableUnRead()) {
            ivUnRead = itemView.findViewById(R.id.unread_img_view);
        }
        if (enableFire()) {
            mIvFire = itemView.findViewById(R.id.tcdv_fire);
        }
    }

    public void prepare(ChatMessage message, @Nullable Integer role, boolean secret, int vip) {
        mdata = message;
        if (enableNormal()) {
            // 显示消息状态
            changeMessageState(message);
            // 显示头像,管理员角标，我的设备显示我的设备
            mIvHead.setGroupRole(role);
            mIvHead.setVipLevel(vip);
            String toId = message.getToId();
            String types = !TextUtils.isEmpty(toId) && toId.length() < 8 ? message.getFromId() : message.getFromUserId();

            AvatarHelper.getInstance().displayAvatar(message.getFromUserName(), types, mIvHead.getHeadImage(), true);

            // 显示昵称
            changeNickName(message, secret);
            mCboxSelect.setChecked(message.isMoreSelected);
        }

        fillData(message);

        if (mRootView != null) {
            if (isOnClick()) {
                mRootView.setOnClickListener(this);
            }

            if (isLongClick()) {
                mRootView.setOnTouchListener((v, event) -> {
                    mouseY = (int) event.getY();
                    return false;
                });
                mRootView.setOnLongClickListener(this);
            } else {
                mRootView.setOnLongClickListener(null);
            }
        }

        // 显示阅后即焚
        if (enableFire()) {
            if (mIvFire != null) {
                mIvFire.setVisibility(message.getIsReadDel() ? View.VISIBLE : View.GONE);
            }
        }

        // 开启自动发回执
        if (enableSendRead() && !message.getIsReadDel() && !isMysend) {
            sendReadMessage(message);
        }
    }

    private void changeNickName(ChatMessage message, boolean secret) {
        if (isGroup) {
            mTvName.setVisibility(isMysend ? View.GONE : View.VISIBLE);

            // Todo 有点多此一举，反而会造成其他昵称显示问题，已经在ChatContentView的chagneNameRemark方法内处理好了
/*
            if (!isMysend && !message.isLoadRemark()) {
                Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, message.getFromUserId());
                if (friend != null && !TextUtils.isEmpty(friend.getRemarkName())) {
                    message.setFromUserName(friend.getRemarkName());
                }
                message.setLoadRemark(true);
            }
*/
            String name = message.getFromUserName();
            if (!TextUtils.isEmpty(name) && secret) {
                name = name.substring(0, name.length() - 1) + "*";
            }
            mTvName.setText(name);
        }
    }

    private void changeMessageState(ChatMessage message) {
        if (!isMysend && message.getMessageState() != Constants.MESSAGE_SEND_SUCCESS) {
            message.setMessageState(Constants.MESSAGE_SEND_SUCCESS);
        }

        int state = message.getMessageState();

        boolean read = false;
        if (Constants.MESSAGE_SEND_SUCCESS == state) {
            // 单聊中，我发的显示已读或者送达， 群聊中开始显示人数
            if ((!isGroup && isMysend) || (isGroup && showPerson && !isMysend)) {
                read = true;
            }

            // 未读消息显示小红点
            if (enableUnRead()) {
                boolean show = message.isSendRead() || isMysend || message.getIsReadDel();
                ivUnRead.setVisibility(show ? View.GONE : View.VISIBLE);
            }

        } else {
            if (enableUnRead()) {
                ivUnRead.setVisibility(View.GONE);
            }
        }

        changeVisible(mTvSendState, read);
        changeVisible(mIvFailed, state == Constants.MESSAGE_SEND_FAILED);
        changeVisible(mSendingBar, state == Constants.MESSAGE_SEND_ING);
        changeSendText(message);
    }

    private void changeSendText(ChatMessage message) {
        mTvSendState.setOnClickListener(null);
        if (isGroup) {
            if (showPerson && Constants.SUPPORT_READ_PERSON_COUNT) {
                int count = message.getReadPersons();
                mTvSendState.setText(count + getString(R.string.people));
                mTvSendState.setOnClickListener(this);
            }
        } else {
            if (message.isSendRead()) {
                mTvSendState.setText(R.string.status_read);
            } else {
                mTvSendState.setText(R.string.status_send);
            }
        }
    }

    /**
     * 如果是一条普通的消息，那么就会有发送状态和头像等，如果是特殊的消息需要重写此方法并清空方法体
     *
     * @param view
     * @see SystemViewHolder
     */
    private void inflateNormal(View view) {
        mIvHead = view.findViewById(R.id.chat_head_iv);
        mTvName = view.findViewById(R.id.nick_name);
        mTvTime = view.findViewById(R.id.time_tv);
        mSendingBar = view.findViewById(R.id.progress);
        mIvFailed = view.findViewById(R.id.iv_failed);
        mTvSendState = view.findViewById(R.id.tv_read);

        mCboxSelect = view.findViewById(R.id.chat_msc);

        mIvFailed.setOnClickListener(this);
        mIvHead.setOnClickListener(this);
        mCboxSelect.setOnClickListener(this);
        if (isGroup) {
            mIvHead.setOnLongClickListener(this);
        }
    }

    public void showTime(String time) {
        if (mTvTime == null) {
            return;
        }

        if (!TextUtils.isEmpty(time)) {
            mTvTime.setVisibility(View.VISIBLE);
            mTvTime.setText(time);
        } else {
            mTvTime.setVisibility(View.GONE);
        }
    }

    public void setMultiple(boolean multiple) {
        isMultiple = multiple;
        if (enableNormal()) {
            mCboxSelect.setVisibility(isMultiple ? View.VISIBLE : View.GONE);
        }
    }

    public void setShowPerson(boolean showPerson) {
        if (Constants.SUPPORT_READ_PERSON_COUNT) {
            this.showPerson = showPerson;
        } else {
            this.showPerson = false;
        }
    }

    public void changeVisible(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public String getString(@StringRes int sid) {
        return mContext.getResources().getString(sid);
    }

    public String getString(@StringRes int sid, String splice) {
        return mContext.getResources().getString(sid, splice);
    }

    public int dp2px(float dpValue) {
        float density = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5f);
    }

    public int Px2Dp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    @Override
    public boolean onLongClick(View v) {
        if (mHolderListener != null) {
            mHolderListener.onItemLongClick(v, this, mdata);
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v == mRootView && !isMultiple) {
            // 让子类获得点击事件
            onRootClick(v);
        }

        callOnItemClick(v);
    }

    private void callOnItemClick(View v) {
        if (mHolderListener != null) {
            // 让外界获得点击事件
            mHolderListener.onItemClick(v, this, mdata);
        }
    }

    protected void callOnReplayClick(View v) {
        if (mHolderListener != null) {
            // 让外界获得点击事件
            mHolderListener.onReplayClick(v, this, mdata);
        }
    }

    public void setBoxSelect(boolean select) {
        if (mCboxSelect != null) {
            // mCboxSelect.setVisibility(visible ? View.VISIBLE : View.GONE);
            mCboxSelect.setChecked(select);
        }
    }

    public void sendReadMessage(ChatMessage message) {
        if (ivUnRead != null) {
            ivUnRead.setVisibility(View.GONE);
        }
        if (message.isMySend()) {
            return;
        }
        if (Constants.SUPPORT_RECEIPT && message.isSendRead()) {
            return;
        }
        // 群里的隐身人不发已读，
        if (!RoomMember.shouldSendRead(selfGroupRole)) {
            return;
        }
        if (isGroup && !Constants.SUPPORT_READ_PERSON_COUNT) {
            return;
        }
        if (isGroup && !showPerson) {// 自己发送的消息不发已读
            message.setSendRead(true);
            ChatMessageDao.getInstance().updateMessageRead(mLoginUserId, mToUserId, message.getPacketId(), true);
            return;
        }
        ChatMessage msg = new ChatMessage();
        msg.setType(Constants.TYPE_READ);
        if (message.getFromUserId().equals(message.getToUserId())) {// 我的设备
            msg.setToUserId(mLoginUserId);
        } else {
            msg.setToUserId(mToUserId);
        }
        msg.setContent(message.getPacketId());
        // 发送已读消息 本地置为已读
        msg.setSendRead(true);
        msg.setFromUserId(mLoginUserId);
        msg.setFromUserName(CoreManager.getSelf(MyApplication.getInstance()).getNickName());
        msg.setDoubleTimeSend(TimeUtils.time_current_time_double());
        msg.setPacketId(ToolUtils.getUUID());
        ChatMessageDao.getInstance().updateMessageRead(MyApplication.getLoginUserId(), msg.getToUserId(), message.getPacketId(), true);
        message.setSendRead(true); // 自动发送的已读消息，先给一个已读标志，等有消息回执确认发送成功后在去修改数据库
        if (isGroup) {
            ImHelper.sendMucChatMessage(msg.getToUserId(), msg);
        } else {
            ImHelper.sendChatMessage(msg.getToUserId(), msg);
        }
    }

    // 默认关闭阅后即焚消息, 需要子类自行开启
    public boolean enableFire() {
        return false;
    }

    // 默认关闭未读消息显示红点功能,
    public boolean enableUnRead() {
        return false;
    }

    // 是否是普通消息 普通消息有发送状态，头像，昵称
    public boolean enableNormal() {
        return true;
    }

    // 默认开启长按事件，如果不需要可以子类重写 返回false
    public boolean isLongClick() {
        return true;
    }

    // 默认开启长按事件，如果不需要可以子类重写 返回false
    public boolean isOnClick() {
        return true;
    }

    // 默认关闭自动发送已读消息
    public boolean enableSendRead() {
        return false;
    }

    public void addChatHolderListener(ChatHolderListener listener) {
        mHolderListener = listener;
    }

    public static int getReadDleProgress(ChatMessage message) {
        double newReadTime = message.getReadTime();
        long total = ReadDelManager.analyzeTheTimeAfterBurning(message.getIsReadDelByInt());
        int progress = (int) ((newReadTime / total) * 12 * 30);
        return progress;
    }


    public void setRead() {
        if (!mdata.isSendRead()) {
            ChatMessageDao.getInstance().updateMessageRead(MyApplication.getLoginUserId(), mToUserId, mdata.getPacketId(), true);
            mdata.setSendRead(true);
        }
    }
}
