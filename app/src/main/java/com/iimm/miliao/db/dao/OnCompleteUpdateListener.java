package com.iimm.miliao.db.dao;

import android.support.annotation.WorkerThread;

public interface OnCompleteUpdateListener {// User to FriendDao addAttentionUsers addRooms

    @WorkerThread
    void onLoading(int progressRate, int sum);

    @WorkerThread
    void onCompleted();

    void update();
}
