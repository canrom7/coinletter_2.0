package com.iimm.miliao.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.ImageView;

import com.iimm.miliao.R;
import com.iimm.miliao.ui.base.EasyFragment;
import com.iimm.miliao.util.MyX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.ycbjie.webviewlib.DefaultHandler;
import com.ycbjie.webviewlib.InterWebListener;
import com.ycbjie.webviewlib.WebProgress;
import com.ycbjie.webviewlib.X5WebChromeClient;
import com.ycbjie.webviewlib.X5WebUtils;
import com.ycbjie.webviewlib.X5WebView;

import java.util.HashMap;
import java.util.Map;

/**
 * MrLiu253@163.com
 * 自定义的界面
 *
 * @time 2019-07-17
 */
public class DiyFragment2 extends EasyFragment {

    private X5WebView webView;
    private WebProgress progress;
    private MyX5WebChromeClient webChromeClient;
    private String mUrl = "";
    private String mReffer; //调起微信支付时需要请求头携带的参数

    @Override
    protected int inflateLayoutId() {
        return R.layout.dir_fragment2;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {

        initActionBar();
        initView();
    }

    private void initActionBar() {
        ImageView mIvTitleRight = (ImageView) findViewById(R.id.iv_title_right);
        mIvTitleRight.setImageResource(R.drawable.selector_refresh);
        appendClick(mIvTitleRight);
        findViewById(R.id.iv_title_left).setVisibility(View.INVISIBLE);
        appendClick(findViewById(R.id.iv_title_left));
    }


    private void initView() {
        webView = findViewById(R.id.web_view);
        progress = findViewById(R.id.progress);
        progress.show();
        progress.setColor(this.getResources().getColor(R.color.colorPrimaryDark));

        init();
        if (coreManager.getConfig() != null && coreManager.getConfig().getTabBarConfigList != null) {
            mUrl = coreManager.getConfig().getTabBarConfigList.getTabBarLinkUrl();
        } else {
            mUrl = "";
        }
        webView.loadUrl(mUrl);


        webChromeClient = new MyX5WebChromeClient(webView, getActivity(), this);
        webChromeClient.setWebListener(interWebListener);
        webView.setWebChromeClient(webChromeClient);
        WebSettings settings = webView.getSettings();
//        settings.setAppCacheEnabled(true);
//        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);//开启DOM缓存，关闭的话H5自身的一些操作是无效的
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setLoadsImagesAutomatically(true);//先加载界面 ，再加载 图片
        webView.setWebViewClient(new WebViewClient(){


            @Override
            public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest request) {
                ///获取请求uir
                String url = request.getUrl().toString();
                ///获取RequestHeader中的所有 key value
                Map<String, String> lRequestHeaders = request.getRequestHeaders();
                Log.e("测试URI",url);
                for (Map.Entry<String, String> lStringStringEntry : lRequestHeaders.entrySet()) {
                    Log.d("测试header", lStringStringEntry.getKey() + "  " + lStringStringEntry.getValue());
                }
                if (lRequestHeaders.containsKey("Referer")) {
                    mReffer = lRequestHeaders.get("Referer");
                }

                return super.shouldInterceptRequest(webView, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                if (url.startsWith("weixin://wap/pay?")) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                if(url.startsWith("alipays:") || url.startsWith("alipay")) {
                    try {
                        startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
                    } catch (Exception e) {
                        new AlertDialog.Builder(getActivity())
                                .setMessage("未检测到支付宝客户端，请安装后重试。")
                                .setPositiveButton("立即安装", (dialog, which) -> {
                                    Uri alipayUrl = Uri.parse("https://d.alipay.com");
                                    startActivity(new Intent("android.intent.action.VIEW", alipayUrl));
                                }).setNegativeButton("取消", null).show();
                    }
                    return true;
                }
                // 服务器要求标识 这是imApp内的浏览器
                Map<String, String> head = new HashMap<>();
                if (!TextUtils.isEmpty(mReffer)) {
                    head.put("referer", mReffer);
                }
                webView.loadUrl(url, head);
                if(mUrl.equals(url)){
                    findViewById(R.id.iv_title_left).setVisibility(View.INVISIBLE);
                }else{
                    findViewById(R.id.iv_title_left).setVisibility(View.VISIBLE);
                }


                return true;
            }
        });
        webView.getX5WebViewClient().setWebListener(interWebListener);
        initWebViewBridge();
    }

    private InterWebListener interWebListener = new InterWebListener() {
        @Override
        public void hindProgressBar() {
            progress.hide();
        }

        @Override
        public void showErrorView(@X5WebUtils.ErrorType int type) {
            switch (type) {
                //没有网络
                case X5WebUtils.ErrorMode.NO_NET:
                    Log.e("DiyFragment", "没有网络");
                    break;
                //404，网页无法打开
                case X5WebUtils.ErrorMode.STATE_404:
                    Log.e("DiyFragment", "404，网页无法打开");
                    break;
                //onReceivedError，请求网络出现error
                case X5WebUtils.ErrorMode.RECEIVED_ERROR:
                    Log.e("DiyFragment", "onReceivedError，请求网络出现error");
                    break;
                //在加载资源时通知主机应用程序发生SSL错误
                case X5WebUtils.ErrorMode.SSL_ERROR:
                    Log.e("DiyFragment", "在加载资源时通知主机应用程序发生SSL错误");
                    break;
                default:
                    break;
            }
        }

        @Override
        public void startProgress(int newProgress) {
            progress.setWebProgress(newProgress);
        }

        @Override
        public void showTitle(String title) {

        }
    };


    @JavascriptInterface
    public void initWebViewBridge() {
        webView.setDefaultHandler(new DefaultHandler());
    }

    /**
     * 上传图片之后的回调
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == X5WebChromeClient.FILE_CHOOSER_RESULT_CODE) {
            webChromeClient.uploadMessage(intent, resultCode);
        } else if (requestCode == X5WebChromeClient.FILE_CHOOSER_RESULT_CODE_5) {
            webChromeClient.uploadMessageForAndroid5(intent, resultCode);
        }
    }

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};


    private int REQUEST_PERMISSION_CODE = 520;

    private void init() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {

        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_left://
                if (webView != null && webView.canGoBack()) {
                    webView.goBack();
                    if(webView.canGoBack()){
                        findViewById(R.id.iv_title_left).setVisibility(View.VISIBLE);
                    }else{
                        findViewById(R.id.iv_title_left).setVisibility(View.INVISIBLE);

                    }
                }else{
                    findViewById(R.id.iv_title_left).setVisibility(View.INVISIBLE);

                }
//                String url = coreManager.getConfig().getTabBarConfigList.getTabBarLinkUrl();
//                try {
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                    startActivity(intent);
//                } catch (Exception e) {
//                    // 无论如何不能在这里崩溃，
//                    // 比如手机没有浏览器，
//                    Reporter.unreachable(e);
//                }
                break;
            case R.id.iv_title_right:
                if (webView != null) {
                    webView.reload();
                }
                break;
            default:
                break;
        }

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onResume() {
        super.onResume();
        if (webView != null) {
            webView.getSettings().setJavaScriptEnabled(true);
            if (webView != null && webView.canGoBack()) {
                findViewById(R.id.iv_title_left).setVisibility(View.VISIBLE);

            }else{
                findViewById(R.id.iv_title_left).setVisibility(View.INVISIBLE);

            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (webView != null) {
            webView.getSettings().setJavaScriptEnabled(false);
        }
    }


    @Override
    public void onDestroyView() {
        try {
            if (webView != null) {
                webView.clearHistory();
                webView.destroy();
                webView = null;
            }
        } catch (Exception e) {
            Log.e("DiyFragment", e.getMessage());
        }
        super.onDestroyView();
    }
}
