package com.iimm.miliao.bean;

/**
 * 所有广告位的别名
 *
 * 列举你项目中的所有广告位，并给每个广告位起个名字
 *
 * 用于初始化广告位ID 以及 广告的请求
 */
public final class TogetherAdAlias {

    //开屏
    public static final String AD_SPLASH = "ad_splash";

    //激励广告
    public static final String AD_REWARD = "ad_reward";

    private TogetherAdAlias() {
        // 私有构造函数,防止被实例化
    }

    // 添加其他广告位别名...
}