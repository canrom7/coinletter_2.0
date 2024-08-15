package com.iimm.miliao;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.iimm.miliao.ui.base.ActivityStack;

public class CloseService extends Service {
    private boolean IS_SERVICE;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Log.i("info", "准备close: ");




        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("info", "准备close: ");

        ActivityStack.getInstance().exit();
        MyApplication.getInstance().destoryRestart();
        stopSelf();
    }



}
