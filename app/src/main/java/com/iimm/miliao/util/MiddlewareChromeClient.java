package com.iimm.miliao.util;

import android.util.Log;
import android.webkit.JsResult;
import android.webkit.WebView;

import com.just.agentweb.MiddlewareWebChromeBase;

/**
 * MrLiu253@163.com
 *
 * @time 2020-03-02
 */
public class MiddlewareChromeClient extends MiddlewareWebChromeBase {
    public MiddlewareChromeClient() {
    }
    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        Log.i("Info","onJsAlert:"+url);
        return super.onJsAlert(view, url, message, result);
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        Log.i("Info","onProgressChanged:");
    }
}
