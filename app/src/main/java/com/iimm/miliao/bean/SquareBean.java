package com.iimm.miliao.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * MrLiu253@163.com
 *
 * @time 2019-07-16
 */
public class SquareBean {


    /**
     * currentTime : 1563363740749
     * total : 0
     * resultCode : 1
     * count : 3
     */

    private String currentTime;
    private int total;
    private int resultCode;
    private int count;
    private List<DataBean> data;

    public String getCurrentTime() {
        return currentTime == null ? "" : currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<DataBean> getData() {
        if (data == null) {
            return new ArrayList<>();
        }
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * discoverId : 5d2d609beefcc48524bdf097
         * discoverLinkURL : #
         * discoverName : 短视频
         * discoverNum : 2
         * discoverStatus : 1
         * discoverType : 0
         * discoverUpdateTime : 1563254939
         */

        private String discoverId;
        private String discoverImg;
        private String discoverLinkURL;
        private String discoverName;
        private int discoverNum;
        private int discoverStatus;
        private int discoverType;
        private String discoverUpdateTime;
        private boolean local;//true为本地
        private int image;//本地图片
        private int size;//朋友圈数量

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public DataBean() {
        }

        public DataBean(String discoverName, int image, boolean local, int discoverNum) {
            this.discoverName = discoverName;
            this.local = local;
            this.image = image;
            this.discoverNum = discoverNum;
        }

        public boolean isLocal() {
            return local;
        }

        public void setLocal(boolean local) {
            this.local = local;
        }

        public int getImage() {
            return image;
        }

        public void setImage(int image) {
            this.image = image;
        }

        public String getDiscoverId() {
            return discoverId == null ? "" : discoverId;
        }

        public void setDiscoverId(String discoverId) {
            this.discoverId = discoverId;
        }

        public String getDiscoverImg() {
            return discoverImg == null ? "" : discoverImg;
        }

        public void setDiscoverImg(String discoverImg) {
            this.discoverImg = discoverImg;
        }

        public String getDiscoverLinkURL() {
            return discoverLinkURL == null ? "" : discoverLinkURL;
        }

        public void setDiscoverLinkURL(String discoverLinkURL) {
            this.discoverLinkURL = discoverLinkURL;
        }

        public String getDiscoverName() {
            return discoverName == null ? "" : discoverName;
        }

        public void setDiscoverName(String discoverName) {
            this.discoverName = discoverName;
        }

        public int getDiscoverNum() {
            return discoverNum;
        }

        public void setDiscoverNum(int discoverNum) {
            this.discoverNum = discoverNum;
        }

        public int getDiscoverStatus() {
            return discoverStatus;
        }

        public void setDiscoverStatus(int discoverStatus) {
            this.discoverStatus = discoverStatus;
        }

        public int getDiscoverType() {
            return discoverType;
        }

        public void setDiscoverType(int discoverType) {
            this.discoverType = discoverType;
        }

        public String getDiscoverUpdateTime() {
            return discoverUpdateTime == null ? "" : discoverUpdateTime;
        }

        public void setDiscoverUpdateTime(String discoverUpdateTime) {
            this.discoverUpdateTime = discoverUpdateTime;
        }
    }
}
