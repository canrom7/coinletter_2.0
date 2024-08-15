package com.iimm.miliao.util.permission;

import android.content.Context;
import android.text.TextUtils;

import com.iimm.miliao.R;
import com.iimm.miliao.util.AppUtils;
import com.iimm.miliao.view.SelectionFrame;
import com.iimm.miliao.view.TipDialog;
import com.yanzhenjie.permission.AndPermission;

import java.util.List;

/**
 * MrLiu253@163.com
 *
 * @time 2019-08-27
 */
public class PermissionDialog {

    private static SelectionFrame dialog;

    /**
     * 只有确定按钮
     *
     * @param context
     * @param data
     */
    public static void show(Context context, List<String> data) {
        TipDialog mTipDialog = new TipDialog(context);
        // 部分 || 所有权限被拒绝且选择了（选择了不再询问 || 部分机型默认为不在询问）
        mTipDialog.setmConfirmOnClickListener(context.getString(R.string.tip_reject_permission_place_holder, TextUtils.join(", ", data)), () ->
                AndPermission.with(context)
                        .runtime()
                        .setting()
                        .start(981));
        if (AppUtils.isContextExisted(context)) {
            mTipDialog.show();
        }
    }


    /**
     * 有确定和取消按钮
     *
     * @param context
     * @param title
     * @param content
     * @param cancel
     * @param confirm
     */
    public static void show(Context context, String title, String content, String cancel, String confirm) {

        if (dialog != null && dialog.isShowing()) {
            return;
        }
        dialog = new SelectionFrame(context);
        dialog.setSomething(title, content, cancel, confirm,
                new SelectionFrame.OnSelectionFrameClickListener() {
                    @Override
                    public void cancelClick() {

                    }

                    @Override
                    public void confirmClick() {
                        AndPermission.with(context)
                                .runtime()
                                .setting()
                                .start(0x01);
                    }
                });
        dialog.show();
    }
}
