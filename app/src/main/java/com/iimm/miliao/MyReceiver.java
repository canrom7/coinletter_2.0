package com.iimm.miliao;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.iimm.miliao.ui.base.ActivityStack;

/**
 * @author: clm
 * @date: 2021/11/17
 **/
public class MyReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i("info", "onclose......准备................");
        ActivityStack.getInstance().exit();
        MyApplication.getInstance().destory();

    }

}


