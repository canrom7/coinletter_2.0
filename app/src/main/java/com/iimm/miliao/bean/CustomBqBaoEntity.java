package com.iimm.miliao.bean;

import com.iimm.miliao.bean.collection.Collectiion;

import java.util.List;

/**
 * Created by  on 2019/3/4.
 */

public class CustomBqBaoEntity {
    List<Collectiion> customBqBeans;

    public CustomBqBaoEntity(List<Collectiion> dataDefine_bq) {
        this.customBqBeans = dataDefine_bq;

    }

    public List<Collectiion> getCustomBqBeans() {
        return customBqBeans;
    }

    public void setCustomBqBeans(List<Collectiion> customBqBeans) {
        this.customBqBeans = customBqBeans;
    }
}
