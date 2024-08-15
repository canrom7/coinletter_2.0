package com.iimm.miliao.timer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author WangQX
 * @date 2019/8/2 0002 16:08
 * description:
 */
public class ItemTimer {
    private static final String TAG = "ItemTimer";
    private ItemTimerListener listener;
    private long time;
    private int count;
    private boolean isValid;
    private Timer timer;

    public ItemTimer() {
    }

    synchronized void init(long time, int count, ItemTimerListener listener) {
        this.listener = listener;
        this.time = time;
        this.count = count;
        isValid = false;
        timer = new Timer();
    }

    public ItemTimer(long time, int count, ItemTimerListener listener) {
        init(time, count, listener);
    }

    public synchronized void stop() {
        isValid = false;
        listener = null;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public boolean isValid() {
        return isValid;
    }

    public synchronized void start() {
        isValid = true;
        if (timer == null) {
            return;
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isValid) {
                    return;
                }
                if (count <= 0) {
                    if (listener != null) {
                        listener.onTimeOut();
                    }
                    isValid = false;
                } else {
                    if (listener != null) {
                        listener.onTimeStart();
                    }
                    count--;
                }
            }
        }, time, time);
    }
}