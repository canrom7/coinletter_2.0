package com.iimm.miliao.bean;

public class NodeInfo {

    /**
     * id : 5d1ca4f4fdcd873770c56b0d
     * nodeIp : 789456
     * nodeName : 789456
     * nodePort : 789456
     * realmName : 789456
     * status : 1
     */

    private String id;
    private String nodeIp;
    private String nodeName;
    private String nodePort;
    private String realmName;
    private String hostSocks;
    private String userSocks;
    private int postSocks;
    private String passSocks;
    private int status;
    private int isSocks;
    private boolean available = true;
    private Long delayTime;//延迟时间

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(Long delayTime) {
        this.delayTime = delayTime;
    }

    public String getHostSocks() {
        return hostSocks;
    }

    public void setHostSocks(String hostSocks) {
        this.hostSocks = hostSocks;
    }

    public String getUserSocks() {
        return userSocks;
    }

    public void setUserSocks(String userSocks) {
        this.userSocks = userSocks;
    }

    public int getPostSocks() {
        return postSocks;
    }

    public void setPostSocks(int postSocks) {
        this.postSocks = postSocks;
    }

    public String getPassSocks() {
        return passSocks;
    }

    public void setPassSocks(String passSocks) {
        this.passSocks = passSocks;
    }

    public int getIsSocks() {
        return isSocks;
    }

    public void setIsSocks(int isSocks) {
        this.isSocks = isSocks;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodePort() {
        return nodePort;
    }

    public void setNodePort(String nodePort) {
        this.nodePort = nodePort;
    }

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
