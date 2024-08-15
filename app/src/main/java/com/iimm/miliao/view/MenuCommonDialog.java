package com.iimm.miliao.view;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.iimm.miliao.R;

public class MenuCommonDialog extends GetPictureCommonDialog implements View.OnClickListener {
    private String mMenu1;
    private String mMenu2;
    private String mMenu3;
    private MenuCommonClickListener mMenuCommonClickListener;

    public MenuCommonDialog(Context context, String menu1, String menu2, String menu3, MenuCommonClickListener listener) {
        super(context, listener);
        this.mMenuCommonClickListener = listener;
        this.mMenu1 = menu1;
        this.mMenu2 = menu2;
        this.mMenu3 = menu3;
    }


    public MenuCommonDialog(Context context, int menu1, int menu2, int menu3, MenuCommonClickListener listener) {
        super(context, listener);
        this.mMenuCommonClickListener = listener;
        this.mMenu1 = context.getResources().getString(menu1);
        this.mMenu2 = context.getResources().getString(menu2);
        this.mMenu3 = context.getResources().getString(menu3);
    }

    public MenuCommonDialog(Context context, String menu1, String menu2, String menu3) {
        super(context);
        this.mMenu1 = menu1;
        this.mMenu2 = menu2;
        this.mMenu3 = menu3;
    }

    public MenuCommonDialog(Context context, int menu1, int menu2, int menu3) {
        super(context);
        this.mMenu1 = context.getResources().getString(menu1);
        this.mMenu2 = context.getResources().getString(menu2);
        this.mMenu3 = context.getResources().getString(menu3);
    }


    public void setListener(MenuCommonClickListener listener) {
        super.setListener(listener);
        this.mMenuCommonClickListener = listener;
    }

    @Override
    protected void initView() {
        super.initView();
        mTvTakingPhotos.setText(mMenu1);
        mTvChoosePicture.setText(mMenu2);
        mTvCancel.setText(mMenu3);
    }

    @Override
    protected int getLayoutId() {
        return super.getLayoutId();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_taking_photos:
                if (mMenuCommonClickListener != null) {
                    mMenuCommonClickListener.menu1Click(mTvTakingPhotos);
                }
                break;
            case R.id.tv_choose_picture:
                if (mMenuCommonClickListener != null) {
                    mMenuCommonClickListener.menu2Click(mTvChoosePicture);
                }
                break;
            case R.id.tv_cancel:
                if (mMenuCommonClickListener != null) {
                    mMenuCommonClickListener.menu3Click(mTvCancel);
                }
                break;
        }
    }

    public abstract static class MenuCommonClickListener implements GetPictureCommonDialogListener {
        @Override
        public void onClickTakingPhotos(TextView tvTakingPhotos) {

        }

        @Override
        public void onClickChoosePicture(TextView tvChoosePicture) {

        }

        @Override
        public void onClickCancel(TextView tvCancel) {

        }

        public abstract void menu1Click(TextView menu1);

        public abstract void menu2Click(TextView menu2);

        public abstract void menu3Click(TextView menu3);
    }

}
