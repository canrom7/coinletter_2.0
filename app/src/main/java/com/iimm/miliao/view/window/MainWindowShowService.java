package com.iimm.miliao.view.window;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.iimm.miliao.ui.tool.MainAlertWebViewActivity;

import org.greenrobot.eventbus.EventBus;


/**
 * MrLiu253@163.com
 *
 * @time 2020-06-09
 */
public class MainWindowShowService extends Service implements WindowUtil.OnPermissionListener {

    private boolean isZoom;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String viewPath = intent.getStringExtra("viewPath"); //要显示的imageView path
        isZoom = intent.getBooleanExtra("isZoom", false);
        if (isZoom) {
            //显示缩放按钮
            WindowUtil.getInstance().showPermissionWindow(this, this, viewPath, view -> {
                EventBus.getDefault().post(new MoveToStackEvent());
            });
            WindowUtil.getInstance().ChangeZoomIcon(getApplicationContext());
        } else {
            WindowUtil.getInstance().showPermissionWindow(this, this, viewPath, view -> MainAlertWebViewActivity.start(MainWindowShowService.this, MainAlertWebViewActivity.FLOATING_WINDOW_URL));
            WindowUtil.getInstance().changeIcon(getApplicationContext(), viewPath);
        }
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
