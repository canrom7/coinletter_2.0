package com.iimm.miliao.sp;

import android.content.Context;

public class FriendTableExpandSp extends CommonSp {
    private static final String SP_NAME = "friend_expand";// FILE_NAME
    private static final String STRONG_REMIND_STATUS = "strong_remind_status_";
    private static FriendTableExpandSp instance;

    private FriendTableExpandSp(Context context) {
        super(context, SP_NAME);
    }

    public static final FriendTableExpandSp getInstance(Context context) {
        if (instance == null) {
            synchronized (FriendTableExpandSp.class) {
                if (instance == null) {
                    instance = new FriendTableExpandSp(context);
                }
            }
        }
        return instance;
    }


    /**
     * 开启的时候  是关闭强提醒
     *
     * @param isOpen true  关闭强提醒   false  关闭强提醒
     */
    public void setStrongRemindStatusByGroupId(String groupID, boolean isOpen) {
        setValue(STRONG_REMIND_STATUS + groupID, isOpen);
    }

    /**
     * 开启的时候  是关闭强提醒  默认是开启强提醒
     *
     * @param groupID
     */
    public boolean getStrongRemindStatusByGroupId(String groupID) {
        return getValue(STRONG_REMIND_STATUS + groupID, false);
    }

}
