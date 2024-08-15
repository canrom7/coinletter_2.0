package com.iimm.miliao.adapter;

import com.contrarywind.adapter.WheelAdapter;

import java.util.List;

/**
 * MrLiu253@163.com
 *
 * @time 2020-06-04
 */
public class ArrayWheelViewAdapter implements WheelAdapter<String> {
    private List<String> mStrings;

    public ArrayWheelViewAdapter(List<String> strings) {
        mStrings = strings;
    }

    @Override
    public int getItemsCount() {
        return mStrings.size();
    }

    @Override
    public String getItem(int index) {
        return mStrings.get(index);
    }

    @Override
    public int indexOf(String o) {
        return mStrings.size();
    }
}
