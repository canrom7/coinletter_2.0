package com.iimm.miliao.ui.dialog;

import android.content.Context;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.ui.dialog.base.BaseAllScreenDialog;

public class SortDialog extends BaseAllScreenDialog {
    TextView defaultSort;
    TextView timeSort;
    SelectorSortListener seleCtorListener;

    public SortDialog(Context context) {
        super(context);
    }

    public void setSeleCtorListener(SelectorSortListener seleCtorListener) {
        this.seleCtorListener = seleCtorListener;
    }

    @Override
    protected void initView() {
        defaultSort = findViewById(R.id.default_sort);
        timeSort = findViewById(R.id.time_sort);
        defaultSort.setOnClickListener(v -> {
            if (seleCtorListener != null) {
                seleCtorListener.selector(0);
            }
        });
        timeSort.setOnClickListener(v -> {
            if (seleCtorListener != null) {
                seleCtorListener.selector(1);
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_sort;
    }

    public interface SelectorSortListener {
        void selector(int i);
    }
}
