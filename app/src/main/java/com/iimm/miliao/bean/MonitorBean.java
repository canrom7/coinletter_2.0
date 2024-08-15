package com.iimm.miliao.bean;

/**
 * MrLiu253@163.com
 *
 * @time 2020-02-21
 */
public class MonitorBean {

    private String path;

    public String getPath() {
        return path == null ? "" : path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public MonitorBean(String ip) {
        this.path = ip;
    }

    public MonitorBean() {
    }
}
