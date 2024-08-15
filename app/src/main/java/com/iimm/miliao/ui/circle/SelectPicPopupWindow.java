package com.iimm.miliao.ui.circle;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.iimm.miliao.R;

/**
 * 发布文字、图片...
 */
public class SelectPicPopupWindow extends PopupWindow {

    public SelectPicPopupWindow(final Activity context, OnClickListener itemsOnClick) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup menuView = (ViewGroup) inflater.inflate(R.layout.popu_discover, null);


        for (int i = 0; i < menuView.getChildCount(); i++) {
            View child = menuView.getChildAt(i);
            if (child instanceof TextView) {
                TextView tvChild = (TextView) child;
                tvChild.setTextColor(context.getResources().getColor(R.color.tb_center_title_skin_simple_white));
                child.setOnClickListener(itemsOnClick);
            }
        }
        menuView.findViewById(R.id.btn_send_picture).setOnClickListener(itemsOnClick);
        menuView.findViewById(R.id.btn_send_voice).setOnClickListener(itemsOnClick);
        menuView.findViewById(R.id.btn_send_video).setOnClickListener(itemsOnClick);
        menuView.findViewById(R.id.btn_send_file).setOnClickListener(itemsOnClick);
        menuView.findViewById(R.id.new_comment).setOnClickListener(itemsOnClick);

        this.setContentView(menuView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        // 设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.Buttom_Popwindow);

        //设置SelectPicPopupWindow弹出窗体的背景
        ColorDrawable dw = new ColorDrawable(0000000000);
        // 透明背景，
        this.setBackgroundDrawable(dw);

        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = 0.7f;
        context.getWindow().setAttributes(lp);

        setOnDismissListener(() -> {
            WindowManager.LayoutParams lp1 = context.getWindow().getAttributes();
            lp1.alpha = 1f;
            context.getWindow().setAttributes(lp1);
        });
    }
}
