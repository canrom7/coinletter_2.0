package com.iimm.miliao.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.util.SkinUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MrLiu253@163.com
 * 朋友圈搜索好友 Horizontal
 *
 * @time 2020-06-03
 */
public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.SearchUserHolder> {

    private Context mContext;
    private List<Friend> mFriends;
    private String currentSearchKey = "";
    private onItemClick mOnItemClick;

    public SearchUserAdapter(Context context, List<Friend> friends) {
        mContext = context;
        mFriends = friends;
    }


    public void setOnItemClick(onItemClick onItemClick) {
        mOnItemClick = onItemClick;
    }

    public void setCurrentSearchKey(String key) {
        currentSearchKey = key;
    }

    @NonNull
    @Override
    public SearchUserHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_search_user, viewGroup, false);
        return new SearchUserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchUserHolder holder, int i) {

        AvatarHelper.getInstance().displayLatestMessageItemAvatar(mFriends.get(i).getUserId(), mFriends.get(i), holder.ivHeadImg);
        //备注
        if (mFriends != null && mFriends.get(i) != null && !TextUtils.isEmpty(mFriends.get(i).getRemarkName())) {//&& mFriends.get(i).getRemarkName().contains(currentSearchKey)
            holder.tvPrimaryName.setText(processHighlightedFields(mContext, mFriends.get(i).getRemarkName(), currentSearchKey));

            holder.tvName.setVisibility(View.VISIBLE);
            holder.tvName.setText(processHighlightedFields(mContext, "昵称：" + mFriends.get(i).getNickName(), currentSearchKey));
        } else {
            holder.tvPrimaryName.setText(processHighlightedFields(mContext, mFriends.get(i).getNickName(), currentSearchKey));
            holder.tvName.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClick != null) {
                    mOnItemClick.itemClick(i);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return mFriends.size();
    }

    public void setData(String str, List<Friend> searchFriends) {
        this.currentSearchKey = str;
        this.mFriends = searchFriends;
        notifyDataSetChanged();
    }


    public class SearchUserHolder extends RecyclerView.ViewHolder {
        private TextView tvPrimaryName;
        private TextView tvName;
        private RoundedImageView ivHeadImg;

        public SearchUserHolder(@NonNull View itemView) {
            super(itemView);
            ivHeadImg = itemView.findViewById(R.id.iv_head_img);
            tvPrimaryName = itemView.findViewById(R.id.tv_remark_name);
            tvName = itemView.findViewById(R.id.tv_nickname);
        }
    }

    public static SpannableStringBuilder processHighlightedFields(final Context context, String feedText, String key) {
        SpannableStringBuilder builder = new SpannableStringBuilder(feedText);
        Pattern urlPattern = Pattern.compile("(" + key + ")");
        Matcher urlMatcher = urlPattern.matcher(feedText);
        while (urlMatcher.find()) {
            builder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {

                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);

                    ds.setColor(SkinUtils.getSkin(context).getAccentColor());
                }
            }, urlMatcher.start(), urlMatcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }

    public interface onItemClick {
        void itemClick(int pos);
    }
}
