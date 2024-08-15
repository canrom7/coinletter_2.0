package com.iimm.miliao.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebView;
import com.ycbjie.webviewlib.X5LogUtils;
import com.ycbjie.webviewlib.X5WebChromeClient;

import static android.app.Activity.RESULT_OK;

/**
 * MrLiu253@163.com
 *
 * @time 2020-03-04
 */
public class MyX5WebChromeClient extends X5WebChromeClient {

    private Fragment fragment;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadMessageForAndroid5;

    /**
     * 构造方法
     *
     * @param webView
     * @param activity 上下文
     */
    public MyX5WebChromeClient(WebView webView, Activity activity, Fragment fragment) {
        super(webView, activity);
        this.fragment = fragment;
    }

    @Override
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        super.openFileChooser(uploadMsg, acceptType);
        openFileChooserImpl(uploadMsg);
    }

    @Override
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        super.openFileChooser(uploadMsg);
        openFileChooserImpl(uploadMsg);
    }

    @Override
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        X5LogUtils.i("-------openFileChooser222-------");
        super.openFileChooser(uploadMsg, acceptType, capture);
        openFileChooserImpl(uploadMsg);
    }

    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> uploadMsg, FileChooserParams fileChooserParams) {
        X5LogUtils.i("-------onShowFileChooser2222-------");
        openFileChooserImplForAndroid5(uploadMsg);
        return true;

    }

    private void openFileChooserImpl(ValueCallback<Uri> uploadMsg) {
        if (fragment != null) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            fragment.startActivityForResult(
                    Intent.createChooser(i, "文件选择"), FILE_CHOOSER_RESULT_CODE);
        }
    }

    /**
     * 打开文件夹，Android5.0以上
     *
     * @param uploadMsg msg
     */
    private void openFileChooserImplForAndroid5(ValueCallback<Uri[]> uploadMsg) {
        if (fragment != null) {
            this.mUploadMessageForAndroid5 = uploadMsg;
            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("image/*");
            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "图片选择");
            fragment.startActivityForResult(chooserIntent, FILE_CHOOSER_RESULT_CODE_5);
        }
    }

    @Override
    public void uploadMessage(Intent intent, int resultCode) {
        super.uploadMessage(intent, resultCode);
        if (null == mUploadMessage) {
            return;
        }
        Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
        mUploadMessage.onReceiveValue(result);
        mUploadMessage = null;
    }

    @Override
    public void uploadMessageForAndroid5(Intent intent, int resultCode) {
        super.uploadMessageForAndroid5(intent, resultCode);
        if (null == mUploadMessageForAndroid5) {
            return;
        }
        Uri result = (intent == null || resultCode != RESULT_OK) ? null : intent.getData();
        if (result != null) {
            mUploadMessageForAndroid5.onReceiveValue(new Uri[]{result});
        } else {
            mUploadMessageForAndroid5.onReceiveValue(new Uri[]{});
        }
        mUploadMessageForAndroid5 = null;
    }
}
