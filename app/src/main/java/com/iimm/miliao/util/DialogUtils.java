package com.iimm.miliao.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.iimm.miliao.R;

public class DialogUtils {
    private static final String TAG = "DialogUtils";
    private static Dialog mDialog;


    public static Dialog createCommonDialog(Context context, DialogConfig config) {
        if (mDialog == null) {
            mDialog = new Dialog(context, R.style.MyDialog);
        }
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_common, null);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        FrameLayout flCustomView = view.findViewById(R.id.fl_custom_view);
        Button bCancel = view.findViewById(R.id.b_cancel);
        View vline = view.findViewById(R.id.v_line);
        Button bConfirm = view.findViewById(R.id.b_confirm);
        TextView tvContent = view.findViewById(R.id.tv_content);
        if (!config.isHaveTitle) {
            tvTitle.setVisibility(View.GONE);
        } else {
            tvTitle.setText(config.title);
        }
        if (config.contentView != 0) {
            View custom = LayoutInflater.from(context).inflate(config.contentView, flCustomView);
            flCustomView.removeAllViews();
            flCustomView.addView(custom);
        } else if (config.vContentView != null) {
            flCustomView.removeAllViews();
            flCustomView.addView(config.vContentView);
        } else {
            tvContent.setText(TextUtils.isEmpty(config.contentMsg) ? "" : config.contentMsg);
            int lineCount = tvContent.getLineCount();
            Log.i(TAG, "内容行数：" + lineCount);
            if (lineCount < 2) {
                tvContent.setGravity(Gravity.CENTER);
            } else {
                tvContent.setGravity(Gravity.LEFT);
            }
        }
        if (!config.isHaveCancel) {
            bCancel.setVisibility(View.GONE);
            vline.setVisibility(View.GONE);
        } else {
            bCancel.setText(config.cancelMsg);
        }
        if (!config.isHaveConfirm) {
            bConfirm.setVisibility(View.GONE);
            vline.setVisibility(View.GONE);
        } else {
            bConfirm.setText(config.confirmMsg);
        }
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                if (config.mDialogLeftClick != null) {
                    config.mDialogLeftClick.onClick(v);
                }
            }
        });
        bConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                if (config.mDialogRightClick != null) {
                    config.mDialogRightClick.onClick(v);
                }
            }
        });
        mDialog.setCancelable(config.isTouchOutClose);
        mDialog.setCanceledOnTouchOutside(config.isTouchOutClose);
        mDialog.setContentView(view);
        return mDialog;
    }

    public static DialogConfig createAskDialogNoTitle(String s, String leftBtn, String rightBtn, DialogRightClick dialogRightClick) {
        DialogConfig dialogConfig = new DialogConfig();
        dialogConfig.isHaveTitle = false;
        dialogConfig.contentMsg = s;
        dialogConfig.cancelMsg = leftBtn;
        dialogConfig.confirmMsg = rightBtn;
        dialogConfig.mDialogRightClick = dialogRightClick;
        return dialogConfig;
    }

    public static DialogConfig createAskDialog(String titleMsg, String s, String leftBtn, String rightBtn, DialogRightClick dialogRightClick) {
        DialogConfig dialogConfig = new DialogConfig();
        dialogConfig.title = titleMsg;
        dialogConfig.isHaveTitle = true;
        dialogConfig.contentMsg = s;
        dialogConfig.cancelMsg = leftBtn;
        dialogConfig.confirmMsg = rightBtn;
        dialogConfig.mDialogRightClick = dialogRightClick;
        return dialogConfig;
    }

    public static DialogConfig createAskCustomDialog(int layoutId, String titleMsg, String s, String leftBtn, String rightBtn, DialogRightClick dialogRightClick) {
        DialogConfig dialogConfig = new DialogConfig();
        dialogConfig.contentView = layoutId;
        dialogConfig.title = titleMsg;
        dialogConfig.isHaveTitle = true;
        dialogConfig.contentMsg = s;
        dialogConfig.cancelMsg = leftBtn;
        dialogConfig.confirmMsg = rightBtn;
        dialogConfig.mDialogRightClick = dialogRightClick;
        return dialogConfig;
    }

    public static DialogConfig createAskCustomDialog(View contentView, String titleMsg, String s, String leftBtn, String rightBtn, DialogRightClick dialogRightClick) {
        DialogConfig dialogConfig = new DialogConfig();
        dialogConfig.vContentView = contentView;
        dialogConfig.title = titleMsg;
        dialogConfig.isHaveTitle = true;
        dialogConfig.contentMsg = s;
        dialogConfig.cancelMsg = leftBtn;
        dialogConfig.confirmMsg = rightBtn;
        dialogConfig.mDialogRightClick = dialogRightClick;
        return dialogConfig;
    }

    public static DialogConfig createHintDialogByLeftButton(boolean isHaveTitle, String titleMsg, String s, String leftBtn, DialogLeftClick dialogLeftClick) {
        DialogConfig dialogConfig = new DialogConfig();
        if (!TextUtils.isEmpty(titleMsg)) {
            dialogConfig.title = titleMsg;
        }
        dialogConfig.isHaveTitle = isHaveTitle;
        dialogConfig.contentMsg = s;
        dialogConfig.cancelMsg = leftBtn;
        dialogConfig.isHaveConfirm = false;
        dialogConfig.mDialogLeftClick = dialogLeftClick;
        return dialogConfig;
    }

    public static void createErCodeDialog(Context context, Bitmap bitmap, View.OnClickListener
            onClickListener) {
        Dialog dialog = new Dialog(context, R.style.MyDialog);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_er_code, null);
        ImageView ivErCode = view.findViewById(R.id.iv_img);
        ivErCode.setImageBitmap(bitmap);
        Button bSaveErCode = view.findViewById(R.id.b_save_er_code);
        bSaveErCode.setOnClickListener(v -> {
            dialog.dismiss();
            onClickListener.onClick(v);
        });
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();
    }


    private static class DialogConfig {
        boolean isHaveTitle = true;
        String title = "标题";
        String contentMsg = "";
        int contentView;
        View vContentView;
        boolean isHaveCancel = true;
        String cancelMsg = "取消";
        boolean isHaveConfirm = true;
        String confirmMsg = "确定";
        DialogLeftClick mDialogLeftClick;
        DialogRightClick mDialogRightClick;
        boolean isTouchOutClose = true;
    }


    public interface DialogLeftClick {
        void onClick(View view);
    }

    public interface DialogRightClick {
        void onClick(View view);
    }
}
