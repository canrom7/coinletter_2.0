package com.iimm.miliao.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.roamer.slidelistview.SlideBaseAdapter;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.message.NewFriendMessage;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.NewFriendDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.ui.other.BasicInfoActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.ViewHolder;

import java.util.List;

public class NewFriendAdapter extends SlideBaseAdapter {
    Button action_btn_1;
    Button action_btn_2;
    private String mLoginUserId;
    private Context mContext;
    private List<NewFriendMessage> mNewFriends;
    private NewFriendActionListener mListener;
    private String huihua = null;
    private String username = null;

    public NewFriendAdapter(Context context, String loginUserId, List<NewFriendMessage> newFriends, NewFriendActionListener listener) {
        super(context);
        mContext = context;
        mNewFriends = newFriends;
        mListener = listener;
        mLoginUserId = loginUserId;
    }

    @Override
    public int getCount() {
        return mNewFriends.size();
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = createConvertView(position);
        }
        ImageView avatar_img = ViewHolder.get(convertView, R.id.new_friend_avatar);
        TextView nick_name_tv = ViewHolder.get(convertView, R.id.nick_name_tv);
        TextView des_tv = ViewHolder.get(convertView, R.id.des_tv);
        action_btn_1 = ViewHolder.get(convertView, R.id.action_btn_1);
        action_btn_2 = ViewHolder.get(convertView, R.id.action_btn_2);
        TextView tvDelete = ViewHolder.get(convertView, R.id.delete_tv);

        final NewFriendMessage newFriend = mNewFriends.get(position);
        tvDelete.setOnClickListener(v -> {
            mNewFriends.remove(position);
            NewFriendDao.getInstance().removeById(newFriend.get_id());
            notifyDataSetChanged();
        });
        // 设置头像
        AvatarHelper.getInstance().displayAvatar(newFriend.getNickName(), newFriend.getUserId(), avatar_img, true);
        // 昵称
        nick_name_tv.setText(newFriend.getNickName());
        // 重置状态
        action_btn_1.setVisibility(View.GONE);
        action_btn_2.setVisibility(View.GONE);
        action_btn_1.setOnClickListener(new AgreeListener(position));
        action_btn_2.setOnClickListener(new FeedbackListener(position));
        action_btn_1.setText(R.string.pass);
        action_btn_2.setText(R.string.answer);
        avatar_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // 头像的点击事件
                Intent intent = new Intent(mContext, BasicInfoActivity.class);
                intent.putExtra(AppConstant.EXTRA_USER_ID, newFriend.getUserId());
                mContext.startActivity(intent);
            }
        });
        // Friend friend = FriendDao.getInstance().getFriend(newFriend.getOwnerId(), newFriend.getUserId());
        int status = newFriend.getState();
        Log.e("state", "-----" + status);
        username = newFriend.getNickName();
        huihua = newFriend.getContent();
        if (status == Constants.STATUS_11) { // 别人发过来的验证消息
            action_btn_1.setVisibility(View.VISIBLE);
            //action_btn_2.setVisibility(View.VISIBLE);
        }
        // 显示朋友的状态
        fillFriendState(status, newFriend.getUserId(), des_tv);
        return convertView;
    }

    private void fillFriendState(int status, String userId, TextView tvDesc) {
        switch (status) {
            case Constants.STATUS_20:
                tvDesc.setText("");
                break;
            case Constants.STATUS_10:
                tvDesc.setText(InternationalizationHelper.getString("JXFriendObject_WaitPass"));
                break;
            case Constants.STATUS_11:
                // tvDesc.setText(InternationalizationHelper.getString("JXUserInfoVC_Hello"));
                tvDesc.setText(huihua);
                break;
            case Constants.STATUS_12:
                tvDesc.setText(InternationalizationHelper.getString("JXFriendObject_Passed"));
                break;
            case Constants.STATUS_13:
                tvDesc.setText(InternationalizationHelper.getString("JXFriendObject_PassGo"));
                break;
            case Constants.STATUS_14:
                tvDesc.setText(huihua);
                //action_btn_2.setVisibility(View.VISIBLE);
                break;
            case Constants.STATUS_15:
                tvDesc.setText(huihua);
                action_btn_1.setVisibility(View.VISIBLE);
                //action_btn_2.setVisibility(View.VISIBLE);
                break;
            case Constants.STATUS_16:
                tvDesc.setText(InternationalizationHelper.getString("JXAlert_DeleteFirend") + username);
                break;
            case Constants.STATUS_17:
                tvDesc.setText(username + mContext.getString(R.string.delete_me));
                break;
            case Constants.STATUS_18:
                tvDesc.setText(InternationalizationHelper.getString("JXFriendObject_AddedBlackList") + username);
                break;
            case Constants.STATUS_19:
                tvDesc.setText(username + InternationalizationHelper.getString("JXFriendObject_PulledBlack"));
                break;
            case Constants.STATUS_21:
                tvDesc.setText(username + mContext.getString(R.string.add_me_as_friend));
                break;
            case Constants.STATUS_22:
                Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, userId);
                if (friend != null && friend.getStatus() == Constants.STATUS_SYSTEM) {
                    tvDesc.setText(mContext.getString(R.string.added_notice_friend) + username);
                } else {
                    tvDesc.setText(mContext.getString(R.string.added_friend) + username);
                }
                break;
            case Constants.STATUS_24:
                tvDesc.setText(username + mContext.getString(R.string.cancel_black_me));
                break;
            case Constants.STATUS_25:
                tvDesc.setText("通过手机联系人加为好友");
                break;
            case Constants.STATUS_26:
                tvDesc.setText(huihua);
                break;
        }
    }

    @Override
    public int getFrontViewId(int position) {
        return R.layout.row_new_friend;
    }

    @Override
    public int getLeftBackViewId(int position) {
        return 0;
    }

    @Override
    public int getRightBackViewId(int position) {
        return R.layout.row_item_one_delete;
    }

    public interface NewFriendActionListener {
        void addAttention(int position);// 加关注

        void removeBalckList(int position);// 移除黑名单

        void agree(int position);// 同意加好友

        void feedback(int position);
    }

    // 加关注
    private class AddAttentionListener implements View.OnClickListener {
        private int position;

        public AddAttentionListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.addAttention(position);
            }
        }
    }

    // 移除黑名单
    private class RemoveBlackListListener implements View.OnClickListener {
        private int position;

        public RemoveBlackListListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.removeBalckList(position);
            }
        }
    }

    // 同意加好友
    private class AgreeListener implements View.OnClickListener {
        private int position;

        public AgreeListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.agree(position);
            }
        }
    }

    // 回话
    private class FeedbackListener implements View.OnClickListener {
        private int position;

        public FeedbackListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.feedback(position);
            }
        }
    }
}
