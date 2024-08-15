package com.iimm.miliao.bean.redpacket;

public class EventCloudRedReceived {
    private CloudQueryRedPacket mCloudQueryRedPacket;

    public EventCloudRedReceived(CloudQueryRedPacket cloudQueryRedPacket) {
        mCloudQueryRedPacket = cloudQueryRedPacket;
    }

    public CloudQueryRedPacket getCloudQueryRedPacket() {
        return mCloudQueryRedPacket;
    }

    public void setCloudQueryRedPacket(CloudQueryRedPacket cloudQueryRedPacket) {
        mCloudQueryRedPacket = cloudQueryRedPacket;
    }
}
