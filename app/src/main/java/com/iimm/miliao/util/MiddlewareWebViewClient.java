package com.iimm.miliao.util;

import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;

import com.just.agentweb.MiddlewareWebClientBase;

/**
 * MrLiu253@163.com
 *
 * @time 2020-03-02
 */
public class MiddlewareWebViewClient extends MiddlewareWebClientBase {

    public MiddlewareWebViewClient() {
    }

    private static int count = 1;

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        Log.i("Info", "MiddlewareWebViewClient -- >  shouldOverrideUrlLoading:" + request.getUrl().toString() + "  c:" + (count++));
        return super.shouldOverrideUrlLoading(view, request);

    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.i("Info", "MiddlewareWebViewClient -- >  shouldOverrideUrlLoading:" + url + "  c:" + (count++));
        return super.shouldOverrideUrlLoading(view, url);

    }
}


