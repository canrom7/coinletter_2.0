package com.iimm.miliao.xmpp;

import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToolUtils;
import com.iimm.miliao.xmpp.util.ImHelper;

public class MultiLoginDataSync {
    public static void sendSyncMessage(CoreManager coreManager, int type) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(type);
        chatMessage.setFromUserId(coreManager.getSelf().getUserId());
        chatMessage.setFromUserName(coreManager.getSelf().getNickName());
        chatMessage.setToUserId(coreManager.getSelf().getUserId());
        chatMessage.setPacketId(ToolUtils.getUUID());
        chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
        ImHelper.sendChatMessage(coreManager.getSelf().getUserId(), chatMessage);
    }

    /**
     * 调用 "user/get" 接口更新好友信息
     */
    public static void handleFriendByReceiveMessage() {

    }

    /**
     * 调用 "room/getRoom"  接口更新群组信息
     */
    public static void handleFriendByServerResult() {

    }

    /**
     * 调用 "user/get"  接口更新好友信息 同时需要判断与该好友的状态
     */
    public static void handleGroupReceiveMessage() {

    }

    /**
     * 调用 "room/getRoom"  接口更新群组信息 同时需要判断该群组的状态
     */
    public static void handleGroupByServerResult() {

    }
}
