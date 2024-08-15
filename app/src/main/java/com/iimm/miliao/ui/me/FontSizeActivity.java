package com.iimm.miliao.ui.me;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.iimm.miliao.R;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.view.ControlFontSize;
import com.iimm.miliao.view.NoDoubleClickListener;

/**
 * Created by Administrator on 2017/12/5 0005.
 */

public class FontSizeActivity extends BaseActivity {

    private ControlFontSize mControlFontSize;
    private TextView tv1, tv2;
    private int fontType = 0;// 默认标准
    private int fontSize = Constants.DEFAULT_TEXT_SIZE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_font_size);
        initActionBar();
        initView();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.font_size));
        TextView tvRight = (TextView) findViewById(R.id.tv_title_right);
        tvRight.setText(getString(R.string.finish));
        tvRight.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                PreferenceUtils.putInt(FontSizeActivity.this, Constants.FONT_SIZE_TYPE, fontType);
                finish();
            }
        });
    }

    private void initView() {
        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);
        mControlFontSize = (ControlFontSize) findViewById(R.id.control_font);
        fontType = PreferenceUtils.getInt(mContext, Constants.FONT_SIZE_TYPE, 0);
        fontSize = PreferenceUtils.getInt(mContext, Constants.FONT_SIZE_TYPE, 0) + Constants.DEFAULT_TEXT_SIZE;
        tv1.setTextSize(fontSize);
        tv2.setTextSize(fontSize);
        ImageView ava1 = findViewById(R.id.ava1);
        ImageView ava2 = findViewById(R.id.ava2);
        if (ava1 instanceof RoundedImageView) {
            if (Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR) {
                ((RoundedImageView) ava1).setOval(true);
            } else {
                ((RoundedImageView) ava1).setOval(false);
                ((RoundedImageView) ava1).setCornerRadius(Constants.AVATAR_CORNER_RADIUS);
            }
        }
        if (ava2 instanceof RoundedImageView) {
            if (Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR) {
                ((RoundedImageView) ava2).setOval(true);
            } else {
                ((RoundedImageView) ava2).setOval(false);
                ((RoundedImageView) ava2).setCornerRadius(Constants.AVATAR_CORNER_RADIUS);
            }
        }

        mControlFontSize.setCurrentProgress(PreferenceUtils.getInt(mContext, Constants.FONT_SIZE_TYPE, 2));
        mControlFontSize.setOnPointResultListener(new ControlFontSize.OnPointResultListener() {
            @Override
            public void onPointResult(int position) {
                switch (position) {
                    case 0:
                        fontType = 0;
                        setTextSize(Constants.DEFAULT_TEXT_SIZE - 2);
                        break;
                    case 1:
                        fontType = 1;
                        setTextSize(Constants.DEFAULT_TEXT_SIZE - 1);
                        break;
                    case 2:
                        fontType = 2;
                        setTextSize(Constants.DEFAULT_TEXT_SIZE);
                        break;
                    case 3:
                        fontType = 3;
                        setTextSize(Constants.DEFAULT_TEXT_SIZE + 1);
                        break;
                    case 4:
                        fontType = 4;
                        setTextSize(Constants.DEFAULT_TEXT_SIZE + 2);
                        break;
                }
            }
        });
    }

    private void setTextSize(int size) {
        tv1.setTextSize(size);
        tv2.setTextSize(size);
    }
}
