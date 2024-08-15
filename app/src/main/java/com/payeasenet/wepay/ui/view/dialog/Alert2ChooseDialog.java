package com.payeasenet.wepay.ui.view.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


/**
 /**
 * 类 Alert2ChooseDialog
 * <p>
 * 描述:弹出窗口
 * </p>
 * 创建日期：2017年4月24日
 *
 * @author zhaoyong.chen@ehking.com
 * @version 1.0
 */

public class Alert2ChooseDialog {
    private AlertDialog myAlertDialog;
    private OnConfirmClickListener onConfirmClickListener;
    private OnCancelClickListener onCancleClickListener;
    public void setOnConfirmClickListener(OnConfirmClickListener onConfirmClickListener) {
        this.onConfirmClickListener = onConfirmClickListener;
    }

    public void setOnCancleClickListener(OnCancelClickListener onCancleClickListener) {
        this.onCancleClickListener = onCancleClickListener;
    }


    public void showStatus(final Activity activity, int id, String message) {
        if (myAlertDialog == null) {
            myAlertDialog = new AlertDialog.Builder(activity).create();
        }
        myAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable());
        if (!myAlertDialog.isShowing())
            myAlertDialog.show();
        myAlertDialog.setContentView(com.ehking.sdk.wepay.R.layout.dialog_status);
        TextView title = myAlertDialog.findViewById(com.ehking.sdk.wepay.R.id.tv_title);
        ImageView iv = myAlertDialog.findViewById(com.ehking.sdk.wepay.R.id.iv);
        iv.setImageResource(id);
        if (id == com.ehking.sdk.wepay.R.mipmap.ic_not_pass) {
            title.setTextColor(Color.parseColor("#E13F40"));
        } else {
            title.setTextColor(Color.parseColor("#000000"));
        }
        myAlertDialog.setCanceledOnTouchOutside(true);
        title.setText(message);
    }

    public void showStatus(final Activity activity, int id, String message, DialogInterface.OnDismissListener dismissHandler) {
        if (myAlertDialog == null) {
            myAlertDialog = new AlertDialog.Builder(activity).create();
        }
        myAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable());
        if (!myAlertDialog.isShowing())
            myAlertDialog.show();
        myAlertDialog.setContentView(com.ehking.sdk.wepay.R.layout.dialog_status);
        TextView title = myAlertDialog.findViewById(com.ehking.sdk.wepay.R.id.tv_title);
        ImageView iv = myAlertDialog.findViewById(com.ehking.sdk.wepay.R.id.iv);
        iv.setImageResource(id);
        if (id == com.ehking.sdk.wepay.R.mipmap.ic_not_pass) {
            title.setTextColor(Color.parseColor("#E13F40"));
        } else {
            title.setTextColor(Color.parseColor("#000000"));
        }
        myAlertDialog.setCanceledOnTouchOutside(true);
        myAlertDialog.setOnDismissListener(dismissHandler);
        title.setText(message);
    }


    public void showStatus(final Activity activity, int id, String message, boolean delayed, final boolean finish) {
        if (myAlertDialog == null) {
            myAlertDialog = new AlertDialog.Builder(activity).create();
        }
        myAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable());
        if (!myAlertDialog.isShowing())
            myAlertDialog.show();
        myAlertDialog.setContentView(com.ehking.sdk.wepay.R.layout.dialog_status);
        TextView title = myAlertDialog.findViewById(com.ehking.sdk.wepay.R.id.tv_title);
        ImageView iv = myAlertDialog.findViewById(com.ehking.sdk.wepay.R.id.iv);
        iv.setImageResource(id);
        if (id == com.ehking.sdk.wepay.R.mipmap.ic_not_pass) {
            title.setTextColor(Color.parseColor("#E13F40"));
        } else {
            title.setTextColor(Color.parseColor("#000000"));
        }
        if(delayed){
            iv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    myAlertDialog.dismiss();
                    if(finish){
                        activity.finish();
                    }
                }
            },1500);
        }
        myAlertDialog.setCanceledOnTouchOutside(false);
        title.setText(message);
    }

    public void showSub(final Activity activity, String message, String sub) {

        if (myAlertDialog == null) {
            myAlertDialog = new AlertDialog.Builder(
                    activity).create();
        }
        myAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable());
        if (!myAlertDialog.isShowing())
            myAlertDialog.show();
        myAlertDialog
                .setContentView(com.ehking.sdk.wepay.R.layout.dialog_sub);
        TextView title = myAlertDialog.findViewById(com.ehking.sdk.wepay.R.id.tv_title);
        myAlertDialog.setCanceledOnTouchOutside(true);
        title.setText(message);
    }

    public void showMessage(final Activity activity, String message, String leftButton, String rightButton) {
        if (myAlertDialog == null) {
            myAlertDialog = new AlertDialog.Builder(
                    activity).create();
        }
        myAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable());
        if (!myAlertDialog.isShowing())
            myAlertDialog.show();
        myAlertDialog
                .setContentView(com.ehking.sdk.wepay.R.layout.dialog_message);
        TextView title = myAlertDialog.findViewById(com.ehking.sdk.wepay.R.id.tv_title);
        TextView confirm = myAlertDialog.findViewById(com.ehking.sdk.wepay.R.id.confirm);
        TextView cancel = myAlertDialog.findViewById(com.ehking.sdk.wepay.R.id.cancel);
        myAlertDialog.setCanceledOnTouchOutside(false);
        if (!TextUtils.isEmpty(leftButton)) {
            cancel.setText(leftButton);
        } else {
            cancel.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(rightButton)) {
            confirm.setText(rightButton);
        }
        myAlertDialog.setCanceledOnTouchOutside(false);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAlertDialog.dismiss();
                if(onCancleClickListener!=null){
                    onCancleClickListener.cancel("");
                }
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAlertDialog.dismiss();
                if (onConfirmClickListener != null) {
                    onConfirmClickListener.confirm("");
                }
            }
        });
        title.setText(message);

    }

    public void dismiss() {
        if (myAlertDialog != null) {
            myAlertDialog.dismiss();
        }
    }

    public interface OnConfirmClickListener {

        void confirm(String str);


    }

    public interface OnCancelClickListener {

        void cancel(String str);


    }
}