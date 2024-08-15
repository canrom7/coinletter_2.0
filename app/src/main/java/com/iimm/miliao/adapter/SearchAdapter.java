package com.iimm.miliao.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.util.SkinUtils;
import com.iimm.miliao.view.HeadView;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchAdapter extends RecyclerView.Adapter {
    private List<Friend> mFriends;
    private Context mContext;
    private String currentSearchKey = "";
    private String myUserId = "";

    public SearchAdapter(List<Friend> friends, Context context, String myUserId) {
        this.mFriends = friends;
        this.mContext = context;
        this.myUserId = myUserId;

    }


    public void setCurrentSearchKey(String key) {
        currentSearchKey = key;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_search, viewGroup, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ItemViewHolder holder = (ItemViewHolder) viewHolder;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.itemClick(i);
                }
            }
        });
        Friend friend = mFriends.get(i);
        AvatarHelper.getInstance().displayAvatar(myUserId, friend, holder.ivHeadImg);
        if (friend.getRoomFlag() == 0) {
            //联系人
            if (friend.getRemarkName() != null && friend.getRemarkName().contains(currentSearchKey)) {
                holder.tvName.setVisibility(View.VISIBLE);
                holder.tvPrimaryName.setText(processHighlightedFields(mContext, friend.getRemarkName(), currentSearchKey));
                holder.tvName.setText(processHighlightedFields(mContext, friend.getNickName(), currentSearchKey));
            } else {
                holder.tvPrimaryName.setText(processHighlightedFields(mContext, friend.getNickName(), currentSearchKey));
                holder.tvName.setVisibility(View.GONE);
            }
        } else {
            //群聊
            holder.tvPrimaryName.setText(processHighlightedFields(mContext, friend.getNickName(), currentSearchKey));
            holder.tvName.setVisibility(View.GONE);
        }

        if (i == 0) {
            if (friend.getRoomFlag() == 0) {
                holder.tvType.setText(R.string.contact);
            } else {
                holder.tvType.setText(R.string.group_chat);
            }
        } else if (mFriends.get(i - 1).getRoomFlag() == friend.getRoomFlag()) {
            holder.tvType.setVisibility(View.GONE);
        } else {
            holder.tvType.setVisibility(View.VISIBLE);
            if (friend.getRoomFlag() == 0) {
                holder.tvType.setText(R.string.contact);
            } else {
                holder.tvType.setText(R.string.group_chat);
            }
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


    @Override
    public int getItemCount() {
        return mFriends.size();
    }

    private SearchAdapterListener listener;

    public interface SearchAdapterListener {
        void itemClick(int i);

    }

    public void setListener(SearchAdapterListener listener) {
        this.listener = listener;
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPrimaryName;
        private TextView tvName;
        private View vLine;
        private TextView tvType;
        private HeadView ivHeadImg;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            vLine = itemView.findViewById(R.id.v_line);
            tvType = itemView.findViewById(R.id.tv_type);
            ivHeadImg = itemView.findViewById(R.id.iv_head_img);
            tvPrimaryName = itemView.findViewById(R.id.tv_remark_name);
            tvName = itemView.findViewById(R.id.tv_nickname);
        }
    }


}
