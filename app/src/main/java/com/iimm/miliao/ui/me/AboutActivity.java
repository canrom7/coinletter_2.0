package com.iimm.miliao.ui.me;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.iimm.miliao.BuildConfig;
import com.iimm.miliao.R;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.DeviceInfoUtil;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.UiUtils;
import com.iimm.miliao.view.SharePopupWindow;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class AboutActivity extends BaseActivity {

    private Tencent mTencent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(InternationalizationHelper.getString("JXAboutVC_AboutUS"));
        ImageView ivRight = (ImageView) findViewById(R.id.iv_title_right);
        ivRight.setImageResource(R.drawable.share);
        ivRight.setVisibility(View.VISIBLE);
        if (coreManager != null && coreManager.getConfig() != null &&
                coreManager.getConfig().wechatLoginStatus != 1 && coreManager.getConfig().qqLoginStatus != 1) {
            ivRight.setVisibility(View.GONE);
        }
        ivRight.setOnClickListener(view -> {
            if (!UiUtils.isNormalClick(view)) {
                return;
            }
            mTencent = Tencent.createInstance(BuildConfig.QQ_APP_ID, AboutActivity.this);
            SharePopupWindow mSharePopupWindow = new SharePopupWindow(AboutActivity.this, mTencent, qqShareListener, coreManager.getConfig().wechatAppId);
            mSharePopupWindow.showAtLocation(findViewById(R.id.about), Gravity.BOTTOM, 0, 0);
        });
        TextView versionTv = (TextView) findViewById(R.id.version_tv);
        versionTv.setText(getString(R.string.app_name) + " " + DeviceInfoUtil.getVersionName(mContext));

        TextView tvCompany = findViewById(R.id.company_tv);
        TextView tvCopyright = findViewById(R.id.copy_right_tv);

        tvCompany.setText(coreManager.getConfig().companyName);
        tvCopyright.setText(coreManager.getConfig().copyright);
        tvCompany.setVisibility(View.GONE);
        tvCopyright.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == com.tencent.connect.common.Constants.REQUEST_QQ_SHARE) {
            Tencent.onActivityResultData(requestCode, resultCode, data, qqShareListener);
        }

    }

    public IUiListener qqShareListener = new IUiListener() {
        @Override
        public void onCancel() {
        }

        @Override
        public void onComplete(Object response) {
            // TODO Auto-generated method stub
            ToastUtil.showToast(AboutActivity.this, response.toString());
        }

        @Override
        public void onError(UiError e) {
            // TODO Auto-generated method stub
            ToastUtil.showToast(AboutActivity.this, e.errorMessage);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTencent != null) {
            mTencent.releaseResource();
        }
    }
}
