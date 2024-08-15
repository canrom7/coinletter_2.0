package com.iimm.miliao.view.window;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.iimm.miliao.ui.tool.MainAlertWebViewActivity;
import com.iimm.miliao.ui.tool.WebViewActivity;

public class WindowShowService extends Service implements WindowUtil.OnPermissionListener {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String viewPath = intent.getStringExtra("viewPath"); //要显示的imageView path
        WindowUtil.getInstance().showPermissionWindow(this, this, viewPath, view -> MainAlertWebViewActivity.start(WindowShowService.this, WebViewActivity.FLOATING_WINDOW_URL));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WindowUtil.getInstance().dismissWindow(this);
    }


    @Override
    public void showPermissionDialog() {

    }
}
