package com.iimm.miliao.ui.tool;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.androidyuan.lib.screenshot.ScreenShotActivity;
import com.androidyuan.lib.screenshot.Shooter;
import com.iimm.miliao.BuildConfig;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.OrderInfo;
import com.iimm.miliao.bean.collection.CollectionEvery;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.helper.ShareSdkHelper;
import com.iimm.miliao.ui.account.AuthorDialog;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.circle.range.SendShuoshuoActivity;
import com.iimm.miliao.ui.message.InstantMessageActivity;
import com.iimm.miliao.util.AppUtils;
import com.iimm.miliao.util.BitmapUtil;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.JsonUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ToolUtils;
import com.iimm.miliao.util.permission.AndPermissionUtils;
import com.iimm.miliao.util.permission.OnPermissionClickListener;
import com.iimm.miliao.util.permission.PermissionDialog;
import com.iimm.miliao.view.ComplaintDialog;
import com.iimm.miliao.view.ExternalOpenDialog;
import com.iimm.miliao.view.MatchKeyWordEditDialog;
import com.iimm.miliao.view.ModifyFontSizeDialog;
import com.iimm.miliao.view.PayDialog;
import com.iimm.miliao.view.WebMoreDialog;
import com.iimm.miliao.view.window.WindowShowService;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.JsonCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.imeina.screeencaptureshare.ScreenCaptureShareActivity;
import okhttp3.Call;

/**
 * web
 */
public class MyWebViewActivity extends BaseActivity implements OnPermissionClickListener {

    public static final String EXTRA_URL = "url";
    public static final String EXTRA_DOWNLOAD_URL = "download_url";

    public static String FLOATING_WINDOW_URL;
    public static boolean IS_FLOATING;
    boolean isReported;
    private TextView mTitleTv;
    private ImageView mTitleLeftIv;
    private ImageView mTitleRightIv;
    private ProgressBar mProgressBar;
    private WebView mWebView;
    private boolean isAnimStart = false;
    private int currentProgress;
    private String mUrl; // 网址URL
    private String mDownloadUrl;// ShareSdk 分享链接进来的应用下载地址(跳转，当本地不存在对应应用时使用)
    private JsSdkInterface jsSdkInterface;
    // js sdk设置的分享数据，
    private String shareBeanContent;
    // 群助手 交互的数据
    private String mShareParams;
    private Tencent mTencent;
    private String mReffer; //调起微信支付时需要请求头携带的参数

    public static void start(Context ctx, String url) {
        Intent intent = new Intent(ctx, MyWebViewActivity.class);
        intent.putExtra(EXTRA_URL, url);
        ctx.startActivity(intent);
    }

    public static void start(Context ctx, String url, String shareParams) {
        Intent intent = new Intent(ctx, MyWebViewActivity.class);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra("shareParams", shareParams);
        ctx.startActivity(intent);
    }

    /**
     * 解析出url参数中的键值对
     * 如 "index.jsp?Action=del&id=123"，解析出Action:del,id:123存入map中
     *
     * @param URL url地址
     * @return url请求参数部分
     */
    public static Map<String, String> URLRequest(String URL) {
        Map<String, String> mapRequest = new HashMap<String, String>();

        String[] arrSplit = null;

        String strUrlParam = TruncateUrlPage(URL);
        if (strUrlParam == null) {
            return mapRequest;
        }
        //每个键值为一组 www.2cto.com
        arrSplit = strUrlParam.split("[&]");
        for (String strSplit : arrSplit) {
            String[] arrSplitEqual = null;
            arrSplitEqual = strSplit.split("[=]");

            //解析出键值
            if (arrSplitEqual.length > 1) {
                //正确解析
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);

            } else {
                if (arrSplitEqual[0] != "") {
                    //只有参数没有值，不加入
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        Log.d("mapRequestwebweb", "URLRequest: " + mapRequest.get("webAppName"));
        return mapRequest;
    }

    /**
     * 去掉url中的路径，留下请求参数部分
     *
     * @param strURL url地址
     * @return url请求参数部分
     */
    private static String TruncateUrlPage(String strURL) {
        String strAllParam = null;
        String[] arrSplit = null;

        strURL = strURL.trim();

        arrSplit = strURL.split("[?]");
        if (strURL.length() > 1) {
            if (arrSplit.length > 1) {
                if (arrSplit[1] != null) {
                    strAllParam = arrSplit[1];
                }
            }
        }

        return strAllParam;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        if (getIntent() != null) {
            mUrl = getIntent().getStringExtra(EXTRA_URL);
            mDownloadUrl = getIntent().getStringExtra(EXTRA_DOWNLOAD_URL);
            mShareParams = getIntent().getStringExtra("shareParams");
            initCheck();
            initActionBar();
        }
    }

    @Override
    public void onBackPressed() {
//        if (mWebView != null && mWebView.canGoBack()) {
//            mWebView.goBack();
//        } else {
//            finish();
//        }
        moveTaskToBack(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //点击悬浮窗进来之后是不会有弹窗的，返回之后才有，这里判断一下进来的场景
        if (IS_FLOATING) {
            getCurrentUrl();
            startService(new Intent(MyWebViewActivity.this, WindowShowService.class));
        }
    }

    private void initCheck() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("webUrl", mUrl);

        HttpUtils.get().url(coreManager.getConfig().URL_CHECK)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result.getResultCode() != 1) {// 网址被多次举报，将网址改为警告地址，不让访问
                            isReported = true;
                        }
                        init();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        init();
                    }
                });
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveTaskToBack(true);
            }
        });
        mTitleTv = findViewById(R.id.tv_title_center);
        mTitleLeftIv = findViewById(R.id.iv_title_left_left);
        mTitleLeftIv.setImageResource(R.drawable.icon_close);
        mTitleRightIv = findViewById(R.id.iv_title_right);
        mTitleRightIv.setVisibility(View.INVISIBLE);
        mTitleRightIv.setImageResource(R.drawable.chat_more);
    }

    private void init() {
        initView();
        initClient();
        initEvent();
        stopService(new Intent(MyWebViewActivity.this, WindowShowService.class));
    }

    private void initView() {
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mWebView = (WebView) findViewById(R.id.mWebView);
        /* 设置支持Js */
        mWebView.getSettings().setJavaScriptEnabled(true);
        /* 设置为true表示支持使用js打开新的窗口 */
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        /* 设置缓存模式 */
//        mWebView.getSettings().setAppCacheEnabled(true);
//        mWebView.getSettings().setDatabaseEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setLoadsImagesAutomatically(true);//先加载界面 ，再加载 图片


        /* 设置为使用webview推荐的窗口 */
        mWebView.getSettings().setUseWideViewPort(true);
        /* 设置为使用屏幕自适配 */
        mWebView.getSettings().setLoadWithOverviewMode(true);
        /* 设置是否允许webview使用缩放的功能,我这里设为false,不允许 */
        mWebView.getSettings().setBuiltInZoomControls(false);
        /* 提高网页渲染的优先级 */
        mWebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);

        /* HTML5的地理位置服务,设置为true,启用地理定位 */
        mWebView.getSettings().setGeolocationEnabled(true);
        /* 设置可以访问文件 */
        mWebView.getSettings().setAllowFileAccess(true);

        if (isReported) {// 加载警告网址
            mWebView.loadUrl("file:////android_asset/prohibit.html");
        } else {
            int openStatus = openApp(mUrl);
            if (openStatus == 1) {// 该链接为跳转链接，方法内已跳转，直接return
                finish();
            } else if (openStatus == 2) {// 该链接为跳转链接，但本地未安装该应该，加载该应用下载地址
                mWebView.loadUrl(mDownloadUrl);
            } else if (openStatus == 5) {// 该链接为跳转链接，跳转到本地授权

            } else {
                // 服务器要求标识 这是imApp内的浏览器
                Map<String, String> head = new HashMap<>();
                head.put("user-agent", "app-tigimapp");
                mWebView.loadUrl(mUrl, head);
            }
        }
    }

    private void initClient() {
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setAlpha(1.0f);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.e(TAG, "shouldOverrideUrlLoading: " + url);
                if (url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                int openStatus = openApp(url);
                if (openStatus == 1) {// 该链接为跳转链接，方法内已跳转，直接return
                    return true;
                } else if (openStatus == 2) {// 该链接为跳转链接，但本地未安装该应该，加载该应用下载地址
                    view.loadUrl(mDownloadUrl);
                } else if (openStatus == 5) {// 该链接为跳转链接， 该链接为跳转链接，跳转到本地授权

                } else {
                    // 服务器要求标识 这是imApp内的浏览器
                    Map<String, String> head = new HashMap<>();
                    if (!TextUtils.isEmpty(mReffer)) {
                        head.put("referer", mReffer);
                    }
                    head.put("user-agent", "app-tigimapp");
                    view.loadUrl(url, head);
                }
                return true;
            }

            @Override
            public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
                Log.e(TAG, "onReceivedSslError sslError=" + sslError.toString());
                sslErrorHandler.proceed();
               /* if (sslError.getPrimaryError() == android.net.http.SslError.SSL_INVALID) {// 校验过程遇到了bug
                } else {
                    sslErrorHandler.cancel();
                }*/
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    webView.getSettings()
                            .setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                ///获取请求uir
                String url = request.getUrl().toString();
                ///获取RequestHeader中的所有 key value
                Map<String, String> lRequestHeaders = request.getRequestHeaders();
                Log.e("测试URI", url);
                for (Map.Entry<String, String> lStringStringEntry : lRequestHeaders.entrySet()) {
                    Log.d("测试header", lStringStringEntry.getKey() + "  " + lStringStringEntry.getValue());
                }
                if (lRequestHeaders.containsKey("Referer")) {
                    mReffer = lRequestHeaders.get("Referer");
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });

        // 获取网页加载进度
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                currentProgress = mProgressBar.getProgress();
                if (newProgress >= 100 && !isAnimStart) {
                    // 防止调用多次动画
                    isAnimStart = true;
                    mProgressBar.setProgress(newProgress);
                    // 开启属性动画让进度条平滑消失
                    startDismissAnimation(mProgressBar.getProgress());
                } else {
                    // 开启属性动画让进度条平滑递增
                    startProgressAnimation(newProgress);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                mTitleTv.setText(title);
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
            }
        });

        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                try {
                    // 不处理下载，直接抛出去，
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                } catch (Exception ignored) {
                    // 无论如何不要崩溃，比如没有浏览器，
                    ToastUtil.showToast(MyWebViewActivity.this, R.string.download_error);
                }
            }
        });

        jsSdkInterface = new JsSdkInterface(this, new MyJsSdkListener());
        jsSdkInterface.setShareParams(mShareParams);
        mWebView.addJavascriptInterface(jsSdkInterface, "AndroidWebView");
    }

    private void initEvent() {
        mTitleRightIv.setOnClickListener(view -> {
            WebMoreDialog mWebMoreDialog = new WebMoreDialog(MyWebViewActivity.this, getCurrentUrl(), new WebMoreDialog.BrowserActionClickListener() {
                @Override
                public void floatingWindow() {
                    showFloating();
                }

                @Override
                public void sendToFriend() {
                    forwardToFriend();
                }

                @Override
                public void shareToLifeCircle() {
                    shareMoment();
                }

                @Override
                public void collection() {
                    onCollection(getCurrentUrl());
                }

                @Override
                public void searchContent() {
                    search();
                }

                @Override
                public void copyLink() {
                    ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboardManager.setText(getCurrentUrl());
                    Toast.makeText(MyWebViewActivity.this, getString(R.string.tip_copied_to_clipboard), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void openOutSide() {
                    ExternalOpenDialog externalOpenDialog = new ExternalOpenDialog(mContext, getCurrentUrlWithPar());
                    externalOpenDialog.show();
                }

                @Override
                public void modifyFontSize() {
                    setWebFontSiz();
                }

                @Override
                public void refresh() {
                    mWebView.reload();
                }

                @Override
                public void complaint() {
                    report();
                }

                @Override
                public void shareWechat() {
                    String title = mTitleTv.getText().toString().trim();
                    String url = getCurrentUrl();
                    if (AppUtils.isContextExisted(MyWebViewActivity.this)) {
                        ShareSdkHelper.shareWechat(
                                MyWebViewActivity.this, title, url, url, coreManager.getConfig().wechatAppId
                        );
                    }
                }

                @Override
                public void shareWechatMoments() {
                    String title = mTitleTv.getText().toString().trim();
                    String url = getCurrentUrl();
                    ShareSdkHelper.shareWechatMoments(
                            MyWebViewActivity.this, title, url, url, coreManager.getConfig().wechatAppId
                    );
                }

                @Override
                public void shareQQ() {
                    AndPermissionUtils.AuthorizationStorage(MyWebViewActivity.this, MyWebViewActivity.this);
                }
                @Override
                public void edit() {
                    MyWebViewActivity.this.onClickReqPermission();
                }
            });
            mWebMoreDialog.show();
        });
    }
    private static final int REQ_CODE_PER = 0x2304;
    private static final int REQ_CODE_ACT = 0x2305;


    /**
     * This is an example for using Shooter.
     * This method will request permission and take screenshot on this Activity.
     */
    public void onClickReqPermission() {
        if (Build.VERSION.SDK_INT >= 21) {
            startActivityForResult(createScreenCaptureIntent(), REQ_CODE_PER);
        }
    }

    /**
     * using {@see ScreenShotActivity} to take screenshot on current Activity directly.
     * If you press home it will take screenshot on another app.
     * @param view
     */
    public void onClickShot(View view) {
        startActivityForResult(ScreenShotActivity.createIntent(this, null,0), REQ_CODE_ACT);
        toast("Press home key,open another app.");//if you want to take screenshot on another app.
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Intent createScreenCaptureIntent() {
        //Here using media_projection instead of Context.MEDIA_PROJECTION_SERVICE to  make it successfully build on low api.
        return ((MediaProjectionManager) getSystemService("media_projection")).createScreenCaptureIntent();
    }

    private String getSavedPath() {
        return getExternalFilesDir("screenshot").getAbsoluteFile() + "/"
                + SystemClock.currentThreadTimeMillis() + ".png";
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case REQ_CODE_ACT: {
                if (resultCode == RESULT_OK && data != null) {
                    toast("Screenshot saved at " + data.getData().toString());
                }
                else{
                    toast("You got wrong.");
                }
            }
            break;
            case REQ_CODE_PER: {
                if (resultCode == RESULT_OK && data != null) {
                    Shooter shooter = new Shooter(this, resultCode, data);
                    shooter.startScreenShot(getSavedPath(), new Shooter.OnShotListener() {
                                @Override
                                public void onFinish(String path) {
                                    //here is done status.
//                                    toast("Screenshot saved at " + path);
                                    ScreenCaptureShareActivity.screenCapture(mWebView, path);
                                }

                                @Override
                                public void onError() {
                                    toast("You got wrong.");
                                }
                            }
                    );
                } else if (resultCode == RESULT_CANCELED) {
                    //user canceled.
                } else {

                }
            }
        }
    }


    private void toast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }
    @Override
    protected void onDestroy() {
        if (jsSdkInterface != null)
            jsSdkInterface.release();
        super.onDestroy();
        if (mTencent != null) {
            mTencent.releaseResource();
        }
    }

    /**
     * 根据url跳转至其他app
     */
    private int openApp(String url) {
        if (TextUtils.isEmpty(url)) {
            return 0;
        }
        try {
            // 内部授权
            if (url.contains("websiteAuthorh/index.html")) {
                url = URLDecoder.decode(url, "UTF-8");
                String webAppName = URLRequest(url).get("webAppName");
                String webAppsmallImg = URLRequest(url).get("webAppsmallImg");
                String appId = URLRequest(url).get("appId");
                String redirectURL = URLRequest(url).get("callbackUrl");

                Log.d("zx", "openApp: " + webAppName + "," + webAppsmallImg + "," + url);
                AuthorDialog dialog = new AuthorDialog(mContext);
                dialog.setDialogData(webAppName, webAppsmallImg);
                dialog.setmConfirmOnClickListener(new AuthorDialog.ConfirmOnClickListener() {
                    @Override
                    public void confirm() {
                        HttpUtils.get().url(coreManager.getConfig().AUTHOR_CHECK)
                                .params("appId", appId)
                                .params("state", coreManager.getSelfStatus().accessToken)
                                .params("callbackUrl", redirectURL)
                                .build().execute(new JsonCallback() {
                            @Override
                            public void onResponse(String result) {
                                Log.e("onResponse", "onResponse: " + result);
                                JSONObject json = JSON.parseObject(result);
                                int code = json.getIntValue("resultCode");
                                if (1 == code) {
                                    String html = json.getJSONObject("data").getString("callbackUrl") + "?code=" + json.getJSONObject("data").getString("code");
                                    Map<String, String> head = new HashMap<>();
                                    head.put("user-agent", "app-tigimapp");
                                    mWebView.loadUrl(html, head);
                                }
                            }

                            @Override
                            public void onError(Call call, Exception e) {

                            }
                        });
                    }

                    @Override
                    public void AuthorCancel() {

                    }
                });

                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                return 5;
            }

            if (url.startsWith("weixin://wap/pay?")) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);

                return 1;
            }
            if (url.startsWith("alipays:") || url.startsWith("alipay")) {
                try {
                    startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
                } catch (Exception e) {
                    new AlertDialog.Builder(MyWebViewActivity.this)
                            .setMessage("未检测到支付宝客户端，请安装后重试。")
                            .setPositiveButton("立即安装", (dialog, which) -> {
                                Uri alipayUrl = Uri.parse("https://d.alipay.com");
                                startActivity(new Intent("android.intent.action.VIEW", alipayUrl));
                            }).setNegativeButton("取消", null).show();
                }
                return 1;
            }

            if (!url.startsWith("http") && !url.startsWith("https") && !url.startsWith("ftp")) {
                Uri uri = Uri.parse(url);
                String host = uri.getHost();
                String scheme = uri.getScheme();
                // host 和 scheme 都不能为null
                if (!TextUtils.isEmpty(host) && !TextUtils.isEmpty(scheme)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    if (AppUtils.isSupportIntent(this, intent)) {
                        startActivity(intent);
                        return 1;
                    } else {
                        return 2;
                    }
                }
            }
        } catch (Exception e) {
            return 3;
        }
        return 4;
    }

    /****************************************************
     * Start
     ***************************************************/
    private String getCurrentUrl() {
        Log.e("TAG_CURRENT_URL", mWebView.getUrl());
        String currentUrl = mWebView.getUrl();
        if (TextUtils.isEmpty(currentUrl)) {
            currentUrl = mUrl;
        }
        if (currentUrl.contains("?")) {// ?号后面的都不显示
            currentUrl = currentUrl.substring(0, currentUrl.indexOf("?"));
        }

        FLOATING_WINDOW_URL = currentUrl;

        if (currentUrl.contains("https://view.officeapps.live.com/op/view.aspx?src=")) {
            currentUrl = currentUrl.replace("https://view.officeapps.live.com/op/view.aspx?src=", "");
        }

        return currentUrl;
    }


    private String getCurrentUrlWithPar() {
        Log.e("TAG_CURRENT_URL", mWebView.getUrl());
        String currentUrl = mWebView.getUrl();
        if (TextUtils.isEmpty(currentUrl)) {
            currentUrl = mUrl;
        }
        return currentUrl;
    }

    /**
     * 浮窗
     */
    private void showFloating() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || AppUtils.checkAlertWindowsPermission(this)) {
            IS_FLOATING = !IS_FLOATING;
            if (IS_FLOATING) {
                startService(new Intent(MyWebViewActivity.this, WindowShowService.class));
                finish();
            } else {
                stopService(new Intent(MyWebViewActivity.this, WindowShowService.class));
            }
        } else {
            PermissionDialog.show(MyWebViewActivity.this, "悬浮窗权限", "您的手机没有授予悬浮窗权限，请开启后再试", "暂不开启", "现在去开启");
        }
    }

    /**
     * 发送给朋友
     */
    private void initChatByUrl(String url) {
        String title = mTitleTv.getText().toString().trim();
        String content = JsonUtils.initJsonContent(title, getCurrentUrl(), url);
        initChatByContent(content, Constants.TYPE_LINK);
    }

    private void initChatByContent(String content, int type) {
        String mLoginUserId = coreManager.getSelf().getUserId();

        ChatMessage message = new ChatMessage();
        message.setType(type);
        if (type == Constants.TYPE_LINK) {
            message.setContent(content);
        } else if (type == Constants.TYPE_SHARE_LINK) {
            message.setObjectId(content);
        } else {
            throw new IllegalStateException("未知类型: " + type);
        }
        message.setPacketId(ToolUtils.getUUID());
        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
        // Todo 将封装好的消息存入10010 号的msg 表内，在跳转至转发->聊天界面(跳转传值均为10010号与msgId)，之后在聊天界面内通过这两个值查询到对用消息，发送
        String mNewUserId = "10010";
        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, mNewUserId, message)) {
            Intent intent = new Intent(MyWebViewActivity.this, InstantMessageActivity.class);
            intent.putExtra("fromUserId", mNewUserId);
            intent.putExtra("messageId", message.getPacketId());
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(mContext, "消息封装失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void forwardToFriend() {
        if (shareBeanContent != null) {
            initChatByContent(shareBeanContent, Constants.TYPE_SHARE_LINK);
        } else {
            selectShareImage();
        }
    }

    private void selectShareImage() {
        String str = mWebView.getUrl();
        if (TextUtils.isEmpty(str)) {
            str = getCurrentUrl();
        }
        HtmlFactory.instance().queryImage(str, new HtmlFactory.DataListener<String>() {// 检索该网页包含的图片

            @Override
            public void onResponse(List<String> data, String title) {
                if (data != null && data.size() > 0) {
                    // 弹窗让用户选择图片 作为链接封面
                    SelectImageDialog dialog = new SelectImageDialog(MyWebViewActivity.this, getCurrentUrl(), data, url -> initChatByUrl(url));
                    dialog.show();
                } else {
                    initChatByUrl("");
                }
            }

            @Override
            public void onError(String error) {
                initChatByUrl("");
            }
        });
    }

    /**
     * 分享到生活圈
     */
    private void shareMoment() {
        Intent intent = new Intent(MyWebViewActivity.this, SendShuoshuoActivity.class);
        intent.putExtra(Constants.BROWSER_SHARE_MOMENTS_CONTENT, getCurrentUrl());
        startActivity(intent);
    }

    /**
     * 收藏
     * 链接 当做 文本类型 收藏
     */
    private String collectionParam(String content) {
        com.alibaba.fastjson.JSONArray array = new com.alibaba.fastjson.JSONArray();
        JSONObject json = new JSONObject();
        int type = CollectionEvery.TYPE_TEXT;
        json.put("type", String.valueOf(type));
        String msg = "";
        String collectContent = "";
        msg = content;
        collectContent = content;
        json.put("msg", msg);
        json.put("collectContent", collectContent);
        json.put("collectType", -1);// 与消息无关的收藏
        array.add(json);
        return JSON.toJSONString(array);
    }

    private void onCollection(final String content) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("emoji", collectionParam(content));

        HttpUtils.get().url(coreManager.getConfig().Collection_ADD)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result.getResultCode() == 1) {
                            Toast.makeText(mContext, InternationalizationHelper.getString("JX_CollectionSuccess"), Toast.LENGTH_SHORT).show();
                        } else if (!TextUtils.isEmpty(result.getResultMsg())) {
                            ToastUtil.showToast(mContext, result.getResultMsg());
                        } else {
                            ToastUtil.showToast(mContext, R.string.tip_server_error);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(mContext);
                    }
                });
    }

    /**
     * 搜索页面内容
     */
    private void search() {
        MatchKeyWordEditDialog matchKeyWordEditDialog = new MatchKeyWordEditDialog(this, mWebView);
        Window window = matchKeyWordEditDialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);// 软键盘弹起
            matchKeyWordEditDialog.show();
        }
    }

    /**
     * 调整字体
     */
    private void setWebFontSiz() {
        ModifyFontSizeDialog modifyFontSizeDialog = new ModifyFontSizeDialog(this, mWebView);
        modifyFontSizeDialog.show();
    }

    /**
     * 投诉
     */
    private void report() {
        ComplaintDialog complaintDialog = new ComplaintDialog(this, report -> {
            Map<String, String> params = new HashMap<>();
            params.put("access_token", coreManager.getSelfStatus().accessToken);
            params.put("webUrl", getCurrentUrl());
            params.put("reason", String.valueOf(report.getReportId()));
            DialogHelper.showDefaulteMessageProgressDialog(MyWebViewActivity.this);

            HttpUtils.get().url(coreManager.getConfig().USER_REPORT)
                    .params(params)
                    .build()
                    .execute(new BaseCallback<Void>(Void.class) {

                        @Override
                        public void onResponse(ObjectResult<Void> result) {
                            DialogHelper.dismissProgressDialog();
                            if (result.getResultCode() == 1) {
                                ToastUtil.showToast(MyWebViewActivity.this, R.string.report_success);
                            }
                        }

                        @Override
                        public void onError(Call call, Exception e) {
                            DialogHelper.dismissProgressDialog();
                        }
                    });
        });
        complaintDialog.show();
    }

    /****************************************************
     * End
     ***************************************************/

    /**
     * progressBar递增动画
     */
    private void startProgressAnimation(int newProgress) {
        ObjectAnimator animator = ObjectAnimator.ofInt(mProgressBar, "progress", currentProgress, newProgress);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    /**
     * progressBar消失动画
     */
    private void startDismissAnimation(final int progress) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(mProgressBar, "alpha", 1.0f, 0.0f);
        anim.setDuration(1500);  // 动画时长
        anim.setInterpolator(new DecelerateInterpolator());
        // 关键, 添加动画进度监听器
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = valueAnimator.getAnimatedFraction();      // 0.0f ~ 1.0f
                int offset = 100 - progress;
                mProgressBar.setProgress((int) (progress + offset * fraction));
            }
        });

        anim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                // 动画结束
                mProgressBar.setProgress(0);
                mProgressBar.setVisibility(View.GONE);
                isAnimStart = false;
            }
        });
        anim.start();
    }

    private class MyJsSdkListener implements JsSdkInterface.Listener {

        @Override
        public void onFinishPlay(String path) {
            mWebView.evaluateJavascript("playFinish()", value -> {
            });
        }

        @Override
        public void onUpdateShareData(String shareBeanContent) {
            MyWebViewActivity.this.shareBeanContent = shareBeanContent;
        }

        @Override
        public void onChooseChtatPayInApp(String appId, String prepayId, String sign) {
            DialogHelper.showDefaulteMessageProgressDialog(mContext);
            Map<String, String> params = new HashMap<String, String>();
            params.put("access_token", coreManager.getSelfStatus().accessToken);
            params.put("appId", appId);
            params.put("prepayId", prepayId);
            params.put("sign", sign);

            // 获取订单信息
            HttpUtils.get().url(coreManager.getConfig().PAY_GET_ORDER_INFO)
                    .params(params)
                    .build()
                    .execute(new BaseCallback<OrderInfo>(OrderInfo.class) {

                        @Override
                        public void onResponse(ObjectResult<OrderInfo> result) {
                            DialogHelper.dismissProgressDialog();
                            if (result.getResultCode() == 1 && result.getData() != null) {
                                PayDialog payDialog = new PayDialog(mContext, appId, prepayId, sign, result.getData(), new PayDialog.PayResultListener() {
                                    @Override
                                    public void payResult(String result) {
                                        mWebView.loadUrl("javascript:chat.paySuccess(" + result + ")");
                                    }
                                });
                                payDialog.show();
                            }
                        }

                        @Override
                        public void onError(Call call, Exception e) {
                            DialogHelper.dismissProgressDialog();
                        }
                    });
        }

    }

    public IUiListener qqShareListener = new IUiListener() {
        @Override
        public void onCancel() {
        }

        @Override
        public void onComplete(Object response) {
            // TODO Auto-generated method stub
            ToastUtil.showToast(MyWebViewActivity.this, response.toString());
        }

        @Override
        public void onError(UiError e) {
            // TODO Auto-generated method stub
            ToastUtil.showToast(MyWebViewActivity.this, e.errorMessage);
        }
    };

    @Override
    public void onSuccess() {
        String uri = BitmapUtil.saveBitmap(getResources());
        mTencent = Tencent.createInstance(BuildConfig.QQ_APP_ID, MyWebViewActivity.this);
        String title = mTitleTv.getText().toString().trim();
        String url = getCurrentUrl();
        ShareSdkHelper.shareQQ(mTencent, MyWebViewActivity.this, title,
                url,
                url, uri, qqShareListener);
    }

    @Override
    public void onFailure(List<String> data) {
        if (data.size() > 0) {
            PermissionDialog.show(this, data);
        }
    }
}
