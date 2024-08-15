package com.iimm.miliao.bean;

/**
 * Created by  on 2019/2/26.
 */

public class SBqItem {

    int id;  //资源文件ID
    String content; //资源标识


    public SBqItem(int id, String content) {
        this.id = id;
        this.content = content;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
