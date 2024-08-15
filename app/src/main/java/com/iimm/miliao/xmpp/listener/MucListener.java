package com.iimm.miliao.xmpp.listener;

public interface MucListener {
    // 群组被删除
    void onDeleteMucRoom(String toUserId);

    // 我被踢出群组
    void onMyBeDelete(String toUserId);

    // 群组内昵称发生改变
    void onNickNameChange(String toUserId, String changedUserId, String changedName);

    // 群被禁言了
    void onGroupVoiceBanned(String toUserId, long time);

    // 我被禁言了
    void onMyVoiceBanned(String toUserId, long time);
}
