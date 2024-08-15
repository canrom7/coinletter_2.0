package com.iimm.miliao.bean;

/*
 * 广告提供商枚举
 * 不需要的就删除，只保留需要的即可
 */
public enum AdProviderType {


    //腾讯优量汇 也叫广点通
    GDT("gdt"),

    //穿山甲
    CSJ("csj"),

    //快手
    KS("ks"),

    //百度百青藤
    BAIDU("baidu");

    private String type;

    AdProviderType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}