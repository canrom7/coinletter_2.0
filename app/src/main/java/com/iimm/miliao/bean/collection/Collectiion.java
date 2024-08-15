package com.iimm.miliao.bean.collection;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/9/15 0015.
 * 添加为表情
 */

public class Collectiion implements Serializable {
    public static final String BIAO_QING = "bq";
    public static final String MANAGE = "manage";
    public static final String FULLA = "fulla";
    /**
     * createTime : 1505449718
     * emojiId : 59bb56f64adfdc1d3a0ad91d
     * type : 0
     * url : http://file.xxx.co/u/8295/10008295/201709/o/e24ef4bbe03146fb91116e9287bf26bb.jpg
     * userId : 10008295
     */

    private int createTime;
    private String emojiId;
    private String type;
    private String url;
    private int userId;
    private boolean isSelect;

    public Collectiion(String type) {
        this.type = type;
    }

    public Collectiion() {

    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    public String getEmojiId() {
        return emojiId;
    }

    public void setEmojiId(String emojiId) {
        this.emojiId = emojiId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
