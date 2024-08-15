package com.iimm.miliao.ui.dialog.base;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.iimm.miliao.R;
import com.iimm.miliao.util.ScreenUtil;

public abstract class BaseCommonAllDialog extends Dialog {
    public BaseCommonAllDialog(Context context, int themeResId) {
        super(context, R.style.BottomDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        setCanceledOnTouchOutside(true);
        setWidth();
        initView();

    }

    private void setWidth() {
        Window o = getWindow();
        WindowManager.LayoutParams lp = o.getAttributes();
        // x/y坐标
        // lp.x = 100;
        // lp.y = 100;
        lp.width = ScreenUtil.getScreenWidth(getContext());
        lp.height = ScreenUtil.getScreenHeight(getContext());
        o.setAttributes(lp);
        this.getWindow().setGravity(Gravity.BOTTOM);
        this.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
    }

    protected abstract void initView();


    protected abstract int getLayoutId();


}
