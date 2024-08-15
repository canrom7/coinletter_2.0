package com.iimm.miliao.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by  on 2019/3/1.
 */

public class BqBao implements Serializable {


    /**
     * emoDownStatus : 0
     * emoPackCreateTime : 1564986725049
     * emoPackId : 5d47cd6583ff875674bd73d3
     * emoPackImageUrl : C:\Users\Administrator\Desktop\localHTML\img\img.zip
     * emoPackName : 静态图
     * emoPackProfile : 静态表情包
     * emoPackRemark :
     * emoPackSort : 1
     * emoPackStatus : 0
     * emoSaveCount : 2
     */
    private String id;
    private int emoDownStatus;
    private long emoPackCreateTime;
    private String emoPackFileUrl;
    private String emoPackId;
    private String emoPackImageUrl;
    private String emoPackName;
    private String emoPackProfile;
    private String emoPackRemark;
    private int emoPackSort;
    private int emoPackStatus;
    private String emoPackThumbnailUrl;
    private int emoSaveCount;
    private List<ImEmojiStore> imEmojiStoreListInfo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getEmoDownStatus() {
        return emoDownStatus;
    }

    public void setEmoDownStatus(int emoDownStatus) {
        this.emoDownStatus = emoDownStatus;
    }

    public long getEmoPackCreateTime() {
        return emoPackCreateTime;
    }

    public void setEmoPackCreateTime(long emoPackCreateTime) {
        this.emoPackCreateTime = emoPackCreateTime;
    }

    public String getEmoPackFileUrl() {
        return emoPackFileUrl;
    }

    public void setEmoPackFileUrl(String emoPackFileUrl) {
        this.emoPackFileUrl = emoPackFileUrl;
    }

    public String getEmoPackId() {
        return emoPackId;
    }

    public void setEmoPackId(String emoPackId) {
        this.emoPackId = emoPackId;
    }

    public String getEmoPackImageUrl() {
        return emoPackImageUrl;
    }

    public void setEmoPackImageUrl(String emoPackImageUrl) {
        this.emoPackImageUrl = emoPackImageUrl;
    }

    public String getEmoPackName() {
        return emoPackName;
    }

    public void setEmoPackName(String emoPackName) {
        this.emoPackName = emoPackName;
    }

    public String getEmoPackProfile() {
        return emoPackProfile;
    }

    public void setEmoPackProfile(String emoPackProfile) {
        this.emoPackProfile = emoPackProfile;
    }

    public String getEmoPackRemark() {
        return emoPackRemark;
    }

    public void setEmoPackRemark(String emoPackRemark) {
        this.emoPackRemark = emoPackRemark;
    }

    public int getEmoPackSort() {
        return emoPackSort;
    }

    public void setEmoPackSort(int emoPackSort) {
        this.emoPackSort = emoPackSort;
    }

    public int getEmoPackStatus() {
        return emoPackStatus;
    }

    public void setEmoPackStatus(int emoPackStatus) {
        this.emoPackStatus = emoPackStatus;
    }

    public String getEmoPackThumbnailUrl() {
        return emoPackThumbnailUrl;
    }

    public void setEmoPackThumbnailUrl(String emoPackThumbnailUrl) {
        this.emoPackThumbnailUrl = emoPackThumbnailUrl;
    }

    public int getEmoSaveCount() {
        return emoSaveCount;
    }

    public void setEmoSaveCount(int emoSaveCount) {
        this.emoSaveCount = emoSaveCount;
    }

    public List<ImEmojiStore> getImEmojiStoreListInfo() {
        return imEmojiStoreListInfo;
    }

    public void setImEmojiStoreListInfo(List<ImEmojiStore> imEmojiStoreListInfo) {
        this.imEmojiStoreListInfo = imEmojiStoreListInfo;
    }

}
