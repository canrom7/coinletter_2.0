package com.iimm.miliao.ui.dialog;

import android.content.Context;
import android.widget.Button;
import android.widget.ImageView;

import com.iimm.miliao.R;
import com.iimm.miliao.ui.dialog.base.BaseAllScreenDialog;

public class AlrtDialog extends BaseAllScreenDialog {
    ImageView close;
    Button cancle;
    Button sure;
    SureListener sureListener;

    public void setSureListener(SureListener sureListener) {
        this.sureListener = sureListener;
    }

    public AlrtDialog(Context context) {
        super(context);
    }

    @Override
    protected void initView() {
        cancle = findViewById(R.id.sure);
        close = findViewById(R.id.close);
        sure = findViewById(R.id.sure);
        cancle.setOnClickListener(v -> {
            dismiss();
            if (sureListener != null) {
                sureListener.sure(false);
            }
        });
        sure.setOnClickListener(v -> {
            dismiss();
            if (sureListener != null) {
                sureListener.sure(true);
            }
        });
        close.setOnClickListener(v -> {
            dismiss();
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_alrt;
    }

    public interface SureListener {
        void sure(boolean b);
    }
}
