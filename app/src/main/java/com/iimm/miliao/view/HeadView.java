package com.iimm.miliao.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.makeramen.roundedimageview.RoundedImageView;
import com.iimm.miliao.R;
import com.iimm.miliao.util.Constants;

/**
 * 单人的头像，
 * 群组是另外的组合头像，
 */
public class HeadView extends RelativeLayout {

    private RoundedImageView ivHead;
    private ImageView ivFrame;
    private View layout;
    private ImageView ivVip;

    public HeadView(Context context) {
        super(context);
        init();
    }

    public HeadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HeadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HeadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        layout = View.inflate(getContext(), R.layout.view_head, this);
        ivHead = layout.findViewById(R.id.ivHead);
        ivFrame = layout.findViewById(R.id.ivFrame);
        ivVip = layout.findViewById(R.id.iv_vip);
        ivVip.setVisibility(GONE);

    }

    public ImageView getHeadImage() {
        return ivHead;
    }

    public ImageView getImage() {
        return ivFrame;
    }

    public void setGroupRole(Integer role) {
        if (role == null) {
            ivFrame.setVisibility(View.GONE);
            return;
        }
        switch (role) {
            case Constants.ROLE_OWNER:
                if (Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR) {
                    ivFrame.setImageResource(R.mipmap.frame_group_owner_circle);
                } else {
                    ivFrame.setImageResource(R.mipmap.frame_group_owner);
                }
                ivFrame.setVisibility(View.VISIBLE);
                break;
            case Constants.ROLE_MANAGER:
                if (Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR) {
                    ivFrame.setImageResource(R.mipmap.frame_group_manager_circle);
                } else {
                    ivFrame.setImageResource(R.mipmap.frame_group_manager);
                }
                ivFrame.setVisibility(View.VISIBLE);
                break;
            default:
                ivFrame.setVisibility(View.GONE);
        }
    }

    public void setRound(boolean round) {
        ivHead.setOval(round);
    }


    /**
     * 设置Vip 等级
     *
     * @param vip
     */
    public void setVipLevel(int vip) {
        ivVip.setVisibility(GONE);
        if (vip > 0) {
            ivVip.setVisibility(VISIBLE);
            switch (vip) {
                case 1:
                    ivVip.setImageResource(R.mipmap.vip1);
                    break;
                case 2:
                    ivVip.setImageResource(R.mipmap.vip2);
                    break;
                case 3:
                    ivVip.setImageResource(R.mipmap.vip3);
                    break;
                case 4:
                    ivVip.setImageResource(R.mipmap.vip4);
                    break;
                case 5:
                    ivVip.setImageResource(R.mipmap.vip5);
                    break;
                case 6:
                    ivVip.setImageResource(R.mipmap.vip6);
                    break;
                case 7:
                    ivVip.setImageResource(R.mipmap.vip7);
                    break;
                case 8:
                    ivVip.setImageResource(R.mipmap.vip8);
                    break;
                case 9:
                    ivVip.setImageResource(R.mipmap.vip9);
                    break;
                case 10:
                    ivVip.setImageResource(R.mipmap.vip10);
                    break;
                default:
                    ivVip.setVisibility(GONE);
                    break;
            }
        }

    }
}
