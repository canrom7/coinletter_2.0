package com.iimm.miliao.ui.dialog.base;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.iimm.miliao.R;
import com.iimm.miliao.util.ScreenUtil;

public abstract class BaseAllScreenDialog extends Dialog {
    public Context context;

    public BaseAllScreenDialog(Context context) {
        super(context, R.style.BottomDialog);
        this.context = context;
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

        lp.width = ScreenUtil.getScreenWidth(getContext());
        lp.height = ScreenUtil.getScreenHeight(getContext());
        o.setAttributes(lp);
    }

    protected abstract void initView();


    protected abstract int getLayoutId();
}

