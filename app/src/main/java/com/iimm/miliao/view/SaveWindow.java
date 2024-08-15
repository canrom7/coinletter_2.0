package com.iimm.miliao.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.util.ScreenUtil;

import cn.imeina.screeencaptureshare.ScreenCaptureShareActivity;

/**
 * Created by zq on 2017/9/20 0020.
 * 保存图片
 */
public class SaveWindow extends Dialog {
    private String TAG = "SaveWindow";
    private TextView mSave, mIdentification;
    private View.OnClickListener itemsOnClick;
    private View ll_save_image,edit_image1;
    private ChatMessage mChatMessage;

    public SaveWindow(Context context, View.OnClickListener itemsOnClick) {
        super(context, R.style.BottomDialog);
        this.itemsOnClick = itemsOnClick;
    }

    public SaveWindow(Context context, ChatMessage chatMessage, View.OnClickListener itemsOnClick) {
        super(context, R.style.BottomDialog);
        this.itemsOnClick = itemsOnClick;
        mChatMessage = chatMessage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_to_galley);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView() {
        mSave = (TextView) findViewById(R.id.save_image);
        ll_save_image = findViewById(R.id.ll_save_image);
        edit_image1 = findViewById(R.id.edit_image1);
        if (mChatMessage != null && mChatMessage.isMySend() && ll_save_image != null) {
            ll_save_image.setVisibility(View.GONE);
        }
        mIdentification = (TextView) findViewById(R.id.identification_qr_code);
        // 设置按钮监听
        mSave.setOnClickListener(itemsOnClick);
        mIdentification.setOnClickListener(itemsOnClick);
        findViewById(R.id.edit_image).setOnClickListener(itemsOnClick);
        edit_image1.setOnClickListener(itemsOnClick);
        Window o = getWindow();
        WindowManager.LayoutParams lp = o.getAttributes();
        // x/y坐标
        // lp.x = 100;
        // lp.y = 100;
        lp.width = ScreenUtil.getScreenWidth(getContext());
        o.setAttributes(lp);
        this.getWindow().setGravity(Gravity.BOTTOM);
        this.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
    }
}
