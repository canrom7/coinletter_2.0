package com.iimm.miliao.xmpp.spare;

public interface SpareChatEvent {

    void onOpen();

    void onMessage(String id, String event, String message);

    void onClosed();

}
