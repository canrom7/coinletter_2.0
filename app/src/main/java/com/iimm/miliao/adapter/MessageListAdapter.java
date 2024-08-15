package com.iimm.miliao.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.makeramen.roundedimageview.RoundedImageView;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.HtmlUtils;
import com.iimm.miliao.util.StringUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.UiUtils;
import com.iimm.miliao.xmpp.util.ImHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * MrLiu253@163.com
 *
 * @time 2019-09-17
 */
public class MessageListAdapter extends RecyclerSwipeAdapter<MessageListAdapter.MessageViewHolder> {
    private Context mContext;
    private List<Friend> mFriendList;
    private String mString;
    private boolean search;
    private MessageItemClickListener mMessageItemClickListener;

    public void setMessageItemClickListener(MessageItemClickListener itemClickListener) {
        mMessageItemClickListener = itemClickListener;
    }

    public interface MessageItemClickListener {
        void onItemClick(int position, RecyclerView.ViewHolder viewHolder);

        void onQuickReplyClick(int position, RecyclerView.ViewHolder viewHolder);

        void onAvatarClick(int position, RecyclerView.ViewHolder viewHolder);

        void onItemTopClick(int position, RecyclerView.ViewHolder viewHolder);

        void onItemDeleteClick(int position, RecyclerView.ViewHolder viewHolder);

        void onItemMarkReadClick(int position, RecyclerView.ViewHolder viewHolder);
    }

    public MessageListAdapter(Context context, List<Friend> friends, MessageItemClickListener itemClickListener) {
        this.mContext = context;
        this.mFriendList = friends;
        mMessageItemClickListener = itemClickListener;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_nearly_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder viewHelper, int posit) {
        int position = viewHelper.getAdapterPosition();
        viewHelper.swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);

        final Friend friend = mFriendList.get(position);
        if (friend.getRoomFlag() == 1) {
            AvatarHelper.getInstance().displayLatestMessageItemAvatar(friend.getRoomId(), friend, viewHelper.mHeadView);
        } else {
            AvatarHelper.getInstance().displayLatestMessageItemAvatar(friend.getUserId(), friend, viewHelper.mHeadView);
        }
        String name = !TextUtils.isEmpty(friend.getRemarkName()) ? friend.getRemarkName() : friend.getNickName();
        viewHelper.mNameTv.setText(name);
        viewHelper.mTipTv.setVisibility(View.GONE);

        if (friend.getRoomFlag() != 0) {// 群组 @
            if (friend.getIsAtMe() == 1) {
                viewHelper.mTipTv.setText("[有人@我]");
                viewHelper.mTipTv.setVisibility(View.VISIBLE);
            } else if (friend.getIsAtMe() == 2) {
                viewHelper.mTipTv.setText("[@全体成员]");
                viewHelper.mTipTv.setVisibility(View.VISIBLE);
            }
        }
        if (friend.getType() == Constants.TYPE_TEXT) {// 文本消息 表情
            String s = StringUtils.replaceSpecialChar(friend.getContent());
            CharSequence content = HtmlUtils.transform200SpanString(s.replaceAll("\n", "\r\n"), true);
            // TODO: 这样匹配的话正常消息里的&8824也会被替换掉，
            if (content.toString().contains("&8824")) {// 草稿
                content = content.toString().replaceFirst("&8824", "");

                viewHelper.mTipTv.setText(InternationalizationHelper.getString("JX_Draft"));
                viewHelper.mTipTv.setVisibility(View.VISIBLE);
            }
            String[] split = content.toString().split(":");
            if (split.length > 1) {
                String s1 = split[0];
                s1 = s1.trim();
//                boolean isAllowSecretlyChat = PreferenceUtils.getBoolean(mContext,
//                        Constants.IS_SEND_CARD + friend.getUserId(), true);
//                if (!isAllowSecretlyChat) {
//                    if (!TextUtils.isEmpty(s1)) {
//
//                        s1 = s1.substring(0, s1.length() - 1) + "*";
//                    }
//                }
                viewHelper.mContentTv.setText(s1 + ":" + split[1]);
            } else {
                viewHelper.mContentTv.setText(content);
            }
        } else {
            if (friend.getType() == Constants.TYPE_GROUP_SIGN) {
                if (friend.getContent().equals("0")) {
                    viewHelper.mContentTv.setText("群签到功能已关闭");
                } else {
                    viewHelper.mContentTv.setText("群签到功能已开启");
                }
            } else {
                viewHelper.mContentTv.setText(HtmlUtils.addSmileysToMessage(
                        ImHelper.getSimpleContent(friend.getType(), friend.getContent()),
                        false
                ));
            }
        }

        // 搜索下匹配关键字高亮显示
        if (search) {
            String text = viewHelper.mContentTv.getText().toString();
            SpannableString spannableString = StringUtils.matcherSearchTitle(Color.parseColor("#fffa6015"),
                    text, mString);
            viewHelper.mContentTv.setText(spannableString);
        }

        UiUtils.updateNumToMessageFragment(viewHelper.mNumTv, friend.getUnReadNum(), friend);
        viewHelper.mTimeTv.setText(TimeUtils.getFriendlyTimeDesc(mContext, friend.getTimeSend()));
//        if (friend.getUserId().equals(Constants.ID_SK_PAY)) {
//            viewHelper.mReplayV.setVisibility(View.GONE);
//        } else {
//            viewHelper.mReplayV.setVisibility(View.VISIBLE);
//        }

        if (friend.getOfflineNoPushMsg() == 1) {
            viewHelper.mNotPushV.setVisibility(View.VISIBLE);
        } else {
            viewHelper.mNotPushV.setVisibility(View.GONE);
        }

        final long time = friend.getTopTime();
        if (time == 0) {
            viewHelper.mTopTv.setText(InternationalizationHelper.getString("JX_Top"));
            viewHelper.mConstraintLayout.setBackgroundResource(R.drawable.msg_list_selector_background);
            viewHelper.ivTopIcon.setVisibility(View.GONE);
        } else {
            viewHelper.mTopTv.setText(InternationalizationHelper.getString("JX_CancelTop"));
            viewHelper.mConstraintLayout.setBackgroundResource(R.drawable.msg_list_selector_background_top);
            viewHelper.ivTopIcon.setVisibility(View.VISIBLE);
        }
        if (friend.getUnReadNum() > 0) {
            viewHelper.mMarkReadTv.setText(mContext.getString(R.string.mark_read));
        } else {
            viewHelper.mMarkReadTv.setText(mContext.getString(R.string.mark_unread));
        }
        mItemManger.bind(viewHelper.itemView, position);
        viewHelper.mConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMessageItemClickListener != null) {
                    mMessageItemClickListener.onItemClick(position, viewHelper);
                }
            }
        });
        viewHelper.mHeadView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!UiUtils.isNormalClick(view)) {
                    return;
                }
                if (mMessageItemClickListener != null) {
                    mMessageItemClickListener.onAvatarClick(position, viewHelper);
                }
            }
        });
        viewHelper.mReplayV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMessageItemClickListener != null) {
                    mMessageItemClickListener.onQuickReplyClick(position, viewHelper);
                }
            }
        });
        viewHelper.mTopTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMessageItemClickListener != null) {
                    mMessageItemClickListener.onItemTopClick(position, viewHelper);
                }
            }
        });
        viewHelper.mMarkReadTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMessageItemClickListener != null) {
                    mMessageItemClickListener.onItemMarkReadClick(position, viewHelper);
                }
            }
        });
        viewHelper.mDeleteTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMessageItemClickListener != null) {
                    mMessageItemClickListener.onItemDeleteClick(position, viewHelper);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFriendList.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.message_sl;
    }


    public void setData(List<Friend> friendList, boolean search, String txt) {
        // adapter内部使用额外的list以解决异步操作数据源导致崩溃的问题，
        this.mFriendList = new ArrayList<>(friendList);
        this.search = search;
        this.mString = txt;
        notifyDataSetChanged();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout mConstraintLayout;
        SwipeLayout swipeLayout;
        RoundedImageView mHeadView;
        TextView mNameTv;
        TextView mTipTv;
        TextView mContentTv;
        TextView mNumTv;
        TextView mTimeTv;
        View mReplayV;
        View mNotPushV;
        TextView mTopTv;
        TextView mMarkReadTv;
        TextView mDeleteTv;
        ImageView ivTopIcon;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            mConstraintLayout = itemView.findViewById(R.id.item_friend_warp);
            swipeLayout = itemView.findViewById(R.id.message_sl);
            mHeadView = itemView.findViewById(R.id.avatar_imgS);
            mNameTv = itemView.findViewById(R.id.nick_name_tv);
            mTipTv = itemView.findViewById(R.id.item_message_tip);
            mContentTv = itemView.findViewById(R.id.content_tv);
            mNumTv = itemView.findViewById(R.id.num_tv);
            mTimeTv = itemView.findViewById(R.id.time_tv);
            mReplayV = itemView.findViewById(R.id.replay_iv);
            mNotPushV = itemView.findViewById(R.id.not_push_iv);
            mTopTv = itemView.findViewById(R.id.top_tv);
            mMarkReadTv = itemView.findViewById(R.id.read_unread_tv);
            mDeleteTv = itemView.findViewById(R.id.delete_tv);
            ivTopIcon=itemView.findViewById(R.id.iv_top_icon);

        }
    }


}
