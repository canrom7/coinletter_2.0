package com.iimm.miliao.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * MrLiu253@163.com
 * 指定选择人数
 *
 * @time 2020-02-27
 */
public class EventRedSelect {
    private List<MucSendRedSelectBean> mBeans;

    public EventRedSelect(List<MucSendRedSelectBean> beans) {
        mBeans = beans;
    }

    public EventRedSelect() {
    }

    public List<MucSendRedSelectBean> getBeans() {
        if (mBeans == null) {
            return new ArrayList<>();
        }
        return mBeans;
    }

    public void setBeans(List<MucSendRedSelectBean> beans) {
        mBeans = beans;
    }
}
