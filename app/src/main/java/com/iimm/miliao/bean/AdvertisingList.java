package com.iimm.miliao.bean;

import java.io.Serializable;
import java.util.ArrayList;

public class AdvertisingList implements Serializable {
    private ArrayList<Advertising> advertising;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<Advertising> getAdvertisings() {
        return advertising;
    }

    public void setAdvertisings(ArrayList<Advertising> advertisings) {
        this.advertising = advertisings;
    }

    public static class Advertising implements Serializable {
        private String rewardId;
        private String advertisingKey;
        private String name;
        private String id;
        private String splashId;

        // 无参构造函数
        public Advertising() {
        }

        // Getter和Setter方法
        public String getRewardId() {
            return rewardId;
        }

        public void setRewardId(String rewardId) {
            this.rewardId = rewardId;
        }

        public String getAdvertisingKey() {
            return advertisingKey;
        }

        public void setAdvertisingKey(String advertisingKey) {
            this.advertisingKey = advertisingKey;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSplashId() {
            return splashId;
        }

        public void setSplashId(String splashId) {
            this.splashId = splashId;
        }

        // toString方法，用于打印对象信息
        @Override
        public String toString() {
            return "Advertising{" +
                    "rewardId='" + rewardId + '\'' +
                    ", advertisingKey='" + advertisingKey + '\'' +
                    ", name='" + name + '\'' +
                    ", id='" + id + '\'' +
                    ", splashId='" + splashId + '\'' +
                    '}';
        }
    }
}
