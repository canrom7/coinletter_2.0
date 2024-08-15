package com.iimm.miliao.bean;

import java.io.Serializable;

public class ImEmojiStore implements Serializable {
    private static final String TAG = "ImEmojiStore";
    /**
     * emoMean : 挺突然的
     */

    private String emoMean;
    private String fileUrl;
    private String thumbnailUrl;


    public String getEmoMean() {
        return emoMean;
    }

    public void setEmoMean(String emoMean) {
        this.emoMean = emoMean;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

}
