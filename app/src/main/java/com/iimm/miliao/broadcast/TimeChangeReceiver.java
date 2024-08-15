package com.iimm.miliao.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.iimm.miliao.bean.EventBusMsg;
import com.iimm.miliao.util.Constants;

import de.greenrobot.event.EventBus;

/**
 * 修改系统时间时通知MainActivity校准时间，不考虑MainActivity不存在的情况，
 */
public class TimeChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) {
            return;
        }

        switch (intent.getAction()) {
            case Intent.ACTION_TIME_CHANGED:
            case Intent.ACTION_DATE_CHANGED:
            case Intent.ACTION_TIMEZONE_CHANGED:
                EventBusMsg eventBusMsg = new EventBusMsg();
                eventBusMsg.setMessageType(Constants.EVENTBUS_TIME_UPDATE);
                EventBus.getDefault().post(eventBusMsg);
        }
    }
}
