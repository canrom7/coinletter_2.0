package com.iimm.miliao.util;

import com.iimm.miliao.bean.MucSendRedSelectBean;
import com.iimm.miliao.sortlist.PinYinUtil;

import java.util.Comparator;

public class CountryComparatorCode implements Comparator<MucSendRedSelectBean> {
    /**
     * 如果o1小于o2,返回一个负数;如果o1大于o2，返回一个正数;如果他们相等，则返回0;
     */

    @Override
    public int compare(MucSendRedSelectBean o1, MucSendRedSelectBean o2) {



        if (PinYinUtil.getSortLetterBySortKey(PinYinUtil.getPingYin(o1.getNickname())).equals("@") || PinYinUtil.getSortLetterBySortKey(PinYinUtil.getPingYin(o2.getNickname())).equals("#")) {
            return -1;
        } else {
            return PinYinUtil.getSortLetterBySortKey(PinYinUtil.getPingYin(o1.getNickname())).compareTo(PinYinUtil.getSortLetterBySortKey(PinYinUtil.getPingYin(o2.getNickname())));
        }
    }

}
