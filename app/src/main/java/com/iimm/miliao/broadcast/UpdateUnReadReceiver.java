package com.iimm.miliao.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.ui.MainActivity;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.Constants;

/**
 * Created by Administrator on 2016/7/14.
 * 未读消息更新
 */
public class UpdateUnReadReceiver extends BroadcastReceiver {
    private String action = null;
    private MainActivity main = null;

    public UpdateUnReadReceiver(MainActivity main) {
        this.main = main;
    }

    public void onReceive(Context context, Intent intent) {
        action = intent.getAction();
        if (action.equals(MsgBroadcast.ACTION_MSG_NUM_UPDATE)) {
            int operation = intent.getIntExtra(MsgBroadcast.EXTRA_NUM_OPERATION, MsgBroadcast.NUM_ADD);
            int count = intent.getIntExtra(MsgBroadcast.EXTRA_NUM_COUNT, 0);
            main.msg_num_update(operation, count);
        } else if (action.equals(MsgBroadcast.ACTION_MSG_NUM_UPDATE_NEW_FRIEND)) {// 刷新 新的朋友 消息数量
            Friend friend = FriendDao.getInstance().getFriend(CoreManager.requireSelf(context).getUserId(), Constants.ID_NEW_FRIEND_MESSAGE);
            if (friend != null && main != null) {
                main.updateNewFriendMsgNum(friend.getUnReadNum());
            }
        } else if (action.equals(MsgBroadcast.ACTION_MSG_NUM_RESET)) {
            main.msg_num_reset();
        }
    }
}
