package com.iimm.miliao.view;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.iimm.miliao.R;

public class GetPictureCommonDialog extends BaseCommonBottomDialog implements View.OnClickListener {

    TextView mTvTakingPhotos;
    TextView mTvChoosePicture;
    TextView mTvCancel;

    public GetPictureCommonDialog(Context context, GetPictureCommonDialogListener listener) {
        super(context, R.style.BottomDialog);
        this.listener = listener;
    }

    public GetPictureCommonDialog(Context context) {
        super(context, R.style.BottomDialog);
    }

    @Override
    protected void initView() {
        mTvTakingPhotos = findViewById(R.id.tv_taking_photos);
        mTvChoosePicture = findViewById(R.id.tv_choose_picture);
        mTvCancel = findViewById(R.id.tv_cancel);
        mTvTakingPhotos.setOnClickListener(this);
        mTvChoosePicture.setOnClickListener(this);
        mTvCancel.setOnClickListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.commom_dialog_get_picture;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_taking_photos:
                if (this.isShowing()) {
                    this.dismiss();
                }
                if (listener != null) {
                    listener.onClickTakingPhotos(mTvTakingPhotos);
                }
                break;
            case R.id.tv_choose_picture:
                if (this.isShowing()) {
                    this.dismiss();
                }
                if (listener != null) {
                    listener.onClickChoosePicture(mTvChoosePicture);
                }
                break;
            case R.id.tv_cancel:
                if (this.isShowing()) {
                    this.dismiss();
                }
                if (listener != null) {
                    listener.onClickCancel(mTvCancel);
                }
                break;
        }

    }

    GetPictureCommonDialogListener listener;

    public interface GetPictureCommonDialogListener {
        void onClickTakingPhotos(TextView tvTakingPhotos);

        void onClickChoosePicture(TextView tvChoosePicture);

        void onClickCancel(TextView tvCancel);
    }

    void setListener(GetPictureCommonDialogListener listener) {
        this.listener = listener;
    }


}
