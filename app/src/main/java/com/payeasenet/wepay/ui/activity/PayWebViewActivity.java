package com.payeasenet.wepay.ui.activity;

import android.content.Intent;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.just.agentweb.AgentWeb;
import com.just.agentweb.AgentWebConfig;
import com.just.agentweb.WebChromeClient;
import com.just.agentweb.WebViewClient;
import com.iimm.miliao.R;
import com.iimm.miliao.ui.base.BaseActivity;

/**
 * MrLiu253@163.com
 *
 * @time 2020-01-10
 */
public class PayWebViewActivity extends BaseActivity {

    private LinearLayout mLL;
    private AgentWeb mAgentWeb;
    private ConstraintLayout mConstraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_webview);
        getSupportActionBar().hide();
        mLL = findViewById(R.id.mLL);
        mConstraintLayout = findViewById(R.id.pay_webview_bottom);
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        initView();
    }

    private void initView() {
        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent((LinearLayout) mLL, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator(-1, 3)
                .setWebChromeClient(mWebChromeClient)
                .setWebViewClient(mWebViewClient)
                .createAgentWeb()
                .ready()
                .go(coreManager.getConfig().CLOUD_PROTOCOL);
        mAgentWeb.getAgentWebSettings().getWebSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAgentWeb.getAgentWebSettings().getWebSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }

        mConstraintLayout.setOnClickListener(v -> {
                    startActivity(new Intent(PayWebViewActivity.this, OpenWalletActivity.class));
                    finish();
                }
        );
    }

    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }
    };

    private WebViewClient mWebViewClient = new WebViewClient() {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mConstraintLayout.setVisibility(View.VISIBLE);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            mConstraintLayout.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            // super.onReceivedSslError(view, handler, error);
            handler.proceed();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAgentWeb.getWebLifeCycle().onDestroy();
        try {
            AgentWebConfig.clearDiskCache(this);
        } catch (Exception e) {

        }
    }
}
