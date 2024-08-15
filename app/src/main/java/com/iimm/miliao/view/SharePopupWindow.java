package com.iimm.miliao.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.helper.ShareSdkHelper;
import com.iimm.miliao.ui.me.AboutActivity;
import com.iimm.miliao.util.BitmapUtil;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.permission.AndPermissionUtils;
import com.iimm.miliao.util.permission.OnPermissionClickListener;
import com.iimm.miliao.util.permission.PermissionDialog;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;

import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;

public class SharePopupWindow extends PopupWindow implements OnClickListener {
    private AboutActivity mContent;
    private Tencent mTencent;
    private IUiListener qqShareListener;
    private String mWxId;
    private final int REQUEST_WRITE = 1;//申请权限的请求码


    public SharePopupWindow(AboutActivity context, Tencent mTencent, IUiListener qqShareListener, String wxId) {
        this.mContent = context;
        this.mTencent = mTencent;
        this.qqShareListener = qqShareListener;
        this.mWxId = wxId;
        initView();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) mContent.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mMenuView = inflater.inflate(R.layout.view_share, null);
        setContentView(mMenuView);
        setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);

        setFocusable(true);
        setAnimationStyle(R.style.Buttom_Popwindow);

        // 因为某些机型是虚拟按键的,所以要加上以下设置防止挡住按键.
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        mMenuView.findViewById(R.id.platformshare_wechat).setOnClickListener(this);
        mMenuView.findViewById(R.id.platformshare_moment).setOnClickListener(this);
        mMenuView.findViewById(R.id.platformshare_qq).setOnClickListener(this);
        mMenuView.findViewById(R.id.cancel).setOnClickListener(this);

        //是否支持QQ分享
        if (MyApplication.mCoreManager != null
                && MyApplication.mCoreManager.getConfig() != null
                && MyApplication.mCoreManager.getConfig().qqLoginStatus == 1) {
            mMenuView.findViewById(R.id.platformshare_qq_v).setVisibility(View.VISIBLE);
            mMenuView.findViewById(R.id.platformshare_qq).setVisibility(View.VISIBLE);
        } else {
            mMenuView.findViewById(R.id.platformshare_qq_v).setVisibility(View.GONE);
            mMenuView.findViewById(R.id.platformshare_qq).setVisibility(View.GONE);
        }
        if (MyApplication.mCoreManager != null
                && MyApplication.mCoreManager.getConfig() != null
                && MyApplication.mCoreManager.getConfig().wechatLoginStatus == 1) {
            mMenuView.findViewById(R.id.platformshare_wechat).setVisibility(View.VISIBLE);
            mMenuView.findViewById(R.id.platformshare_moment_v).setVisibility(View.VISIBLE);
            mMenuView.findViewById(R.id.platformshare_moment).setVisibility(View.VISIBLE);
        } else {
            mMenuView.findViewById(R.id.platformshare_wechat).setVisibility(View.GONE);
            mMenuView.findViewById(R.id.platformshare_moment_v).setVisibility(View.GONE);
            mMenuView.findViewById(R.id.platformshare_moment).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        String shareUrl = mContent.coreManager.getConfig().website;
        if (TextUtils.isEmpty(shareUrl)) {
            shareUrl = Constants.SHARE_URL;
        }
        dismiss();
        switch (v.getId()) {
            case R.id.platformshare_wechat:

                ShareSdkHelper.shareWechat(mContent, MyApplication.getContext().getString(R.string.app_name) + mContent.getString(R.string.suffix_share_content),
                        MyApplication.getContext().getString(R.string.app_name) + mContent.getString(R.string.suffix_share_content),
                        shareUrl, mWxId);
                break;
            case R.id.platformshare_moment:
                ShareSdkHelper.shareWechatMoments(mContent, MyApplication.getContext().getString(R.string.app_name) + mContent.getString(R.string.suffix_share_content),
                        MyApplication.getContext().getString(R.string.app_name) + mContent.getString(R.string.suffix_share_content),
                        shareUrl, mWxId);//mContent.coreManager.getConfig().website
                break;
            case R.id.platformshare_qq:
                String finalShareUrl = shareUrl;
                AndPermissionUtils.AuthorizationStorage(mContent, new OnPermissionClickListener() {
                    @Override
                    public void onSuccess() {
                        String uri = BitmapUtil.saveBitmap(mContent.getResources());
                        ShareSdkHelper.shareQQ(mTencent, mContent, MyApplication.getContext().getString(R.string.app_name) + mContent.getString(R.string.suffix_share_content),
                                MyApplication.getContext().getString(R.string.app_name) + mContent.getString(R.string.suffix_share_content),
                                finalShareUrl, uri, qqShareListener);
                    }

                    @Override
                    public void onFailure(List<String> data) {
                        if (data.size() > 0) {
                            PermissionDialog.show(mContent, data);
                        }
                    }
                });
                break;
            case R.id.cancel:
                break;
        }
    }

    private void platformShare(Platform platform) {
        Platform.ShareParams params = new Platform.ShareParams();
        // 网页
        params.setShareType(Platform.SHARE_WEBPAGE);
       /* // 图片 ...其他类型
        params.setShareType(Platform.SHARE_IMAGE);*/
        Bitmap logo = BitmapFactory.decodeResource(mContent.getResources(), R.mipmap.icon);
        params.setImageData(logo);
       /* // else setting logo method
          params.setImagePath("file path");
          params.setImageUrl("image path");*/
        params.setTitle(MyApplication.getContext().getString(R.string.app_name) + mContent.getString(R.string.suffix_share_content));
        // 分享至朋友圈，该字段不显示
        params.setText(MyApplication.getContext().getString(R.string.app_name) + mContent.getString(R.string.suffix_share_content));
        /* if share image, you cannot setting Url*/
        params.setUrl(mContent.coreManager.getConfig().website);
        platform.setPlatformActionListener(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                ToastUtil.showToast(MyApplication.getContext(), R.string.share_succes);
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                ToastUtil.showToast(MyApplication.getContext(), R.string.share_failed);
            }

            @Override
            public void onCancel(Platform platform, int i) {
                ToastUtil.showToast(MyApplication.getContext(), R.string.share_cancel);
            }
        });
        platform.share(params);
    }
}
