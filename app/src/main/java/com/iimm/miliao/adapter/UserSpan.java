package com.iimm.miliao.adapter;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.iimm.miliao.R;

public class UserSpan extends ClickableSpan {
    int color = -1;
    private Context context;
    private String userId;
    private String nickName;// 可能也是备注名字，但是不影响，传递到BusinessCircleActivity页面，一样的会查询出备注名

    public UserSpan(Context context, String userId, String nickName) {
        this(-1, context, userId, nickName);
    }

    /**
     * constructor
     *
     * @param color   the link color
     * @param context
     * @param
     */
    public UserSpan(int color, Context context, String userId, String nickName) {
        if (color != -1) {
            this.color = color;
        }
        this.context = context;
        this.userId = userId;
        this.nickName = nickName;
    }

    /**
     * Performs the click action associated with this span.
     */
    public void onClick(View widget) {

    }

    /**
     * Makes the text without underline.
     */
    @Override
    public void updateDrawState(TextPaint ds) {
        if (color == -1) {
            ds.setColor(context.getResources().getColor(R.color.color_576b94));
        } else {
            ds.setColor(color);
        }
        ds.setUnderlineText(false);
    }

    public static void setClickableSpan(Context context, SpannableStringBuilder builder, String appendData, String userId) {
        builder.append(appendData);
        int end = builder.length();
        int length = appendData.length();
        builder.setSpan(new UserSpan(context, userId, appendData), end - length, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}