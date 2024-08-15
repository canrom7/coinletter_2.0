package com.iimm.miliao.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.makeramen.roundedimageview.RoundedImageView;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.sortlist.BaseSortModel;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.ViewHolder;
import com.iimm.miliao.view.HeadView;

import java.util.List;

public class FriendWithSelectAdapter extends BaseAdapter implements SectionIndexer {

    private Context mContext;
    private List<BaseSortModel<Friend>> mSortFriends;

    public FriendWithSelectAdapter(Context context, List<BaseSortModel<Friend>> sortFriends) {
        mContext = context;
        mSortFriends = sortFriends;

    }

    public void setData(List<BaseSortModel<Friend>> sortFriends) {
        mSortFriends = sortFriends;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mSortFriends.size();
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.row_sort_friend_with_select, parent, false);
        }
        TextView catagoryTitleTv = ViewHolder.get(convertView, R.id.catagory_title);
        ImageView avatar_img = ViewHolder.get(convertView, R.id.avatar_img);
        HeadView avatar_imgS = ViewHolder.get(convertView, R.id.avatar_imgS);
        TextView nick_name_tv = ViewHolder.get(convertView, R.id.nick_name_tv);
        CheckBox checkBox = ViewHolder.get(convertView, R.id.cb_box);

        // 根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(position);
        // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (position == getPositionForSection(section)) {
            catagoryTitleTv.setVisibility(View.VISIBLE);
            catagoryTitleTv.setText(mSortFriends.get(position).getFirstLetter());
        } else {
            catagoryTitleTv.setVisibility(View.GONE);
        }

        final Friend friend = mSortFriends.get(position).getBean();
        checkBox.setChecked(friend.isSelect());
        if (friend.getRoomFlag() == 0) {// 单人
            if (avatar_img instanceof RoundedImageView) {
                if (Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR) {
                    ((RoundedImageView) avatar_img).setOval(true);
                } else {
                    ((RoundedImageView) avatar_img).setOval(false);
                    ((RoundedImageView) avatar_img).setCornerRadius(Constants.AVATAR_CORNER_RADIUS);
                }
            }
            avatar_img.setVisibility(View.VISIBLE);
            avatar_imgS.setVisibility(View.GONE);
            if (friend.getUserId().equals(Constants.ID_SYSTEM_MESSAGE)) {
                int reId = Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR ? R.drawable.im_notice : R.drawable.im_notice_square;
                Glide.with(mContext).load(AvatarHelper.getAvatarUrl(friend.getUserId(), true)).placeholder(reId).error(reId).diskCacheStrategy(DiskCacheStrategy.NONE).into(avatar_img);
            } else if (friend.getUserId().equals(Constants.ID_NEW_FRIEND_MESSAGE)) {
                avatar_img.setImageResource(R.drawable.im_new_friends);
            } else if (friend.getIsDevice() == 1) {
                if ("android".equals(friend.getUserId()) || "ios".equals(friend.getUserId())) {
                    avatar_img.setImageResource(R.drawable.fdy);
                } else if ("pc".equals(friend.getUserId()) || "mac".equals(friend.getUserId()) || "web".equals(friend.getUserId())) {
                    avatar_img.setImageResource(R.drawable.feb);
                }
            } else {
                AvatarHelper.getInstance().displayAvatar(friend.getUserId(), avatar_img, true);
            }
        } else {// 群组
            avatar_img.setVisibility(View.GONE);
            avatar_imgS.setVisibility(View.VISIBLE);
            AvatarHelper.getInstance().displayAvatar(CoreManager.requireSelf(mContext).getUserId(), friend, avatar_imgS);
        }
        // 昵称
        nick_name_tv.setText(!TextUtils.isEmpty(friend.getRemarkName()) ? friend.getRemarkName() : friend.getNickName());
        return convertView;
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = mSortFriends.get(i).getFirstLetter();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的Char ascii值
     */
    public int getSectionForPosition(int position) {
        return mSortFriends.get(position).getFirstLetter().charAt(0);
    }
}
