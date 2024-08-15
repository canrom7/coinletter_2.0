package com.iimm.miliao.ui.company;

import android.util.Log;
import android.widget.Filter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//过滤数据
public class SearchFilter<T> extends Filter {
    List<T> mArrayList;//数据源
    List<String> filterKeys;
    List<String> keyInClassNames;
    String id;//唯一标识用于去重；
    ResultListener<T> resultListener;
    Map<String, Object> map;

    public SearchFilter(List<T> mArrayList, List<String> filterKeys, String id) {
        this.mArrayList = mArrayList;
        this.filterKeys = filterKeys;
        this.id = id;
    }

    public void setKeyInClassNames(List<String> keyInClassNames) {
        this.keyInClassNames = keyInClassNames;
    }

    public void setResultListener(ResultListener<T> resultListener) {
        this.resultListener = resultListener;
    }

    //执行筛选
    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults filterResults = new FilterResults();
        List<T> list = new ArrayList<>();
        Log.e("sssss", mArrayList.size() + "");
        for (Iterator<T> iterator = mArrayList.iterator(); iterator.hasNext(); ) {
            T name = iterator.next();
            Map<String, Object> map = object2Map(name);
            System.out.println("---> name=" + map);
            for (int i = 0; i < filterKeys.size(); i++) {
                String ss = map.get(filterKeys.get(i)) + "";
                ss = ss.toLowerCase();
                charSequence = charSequence.toString().toLowerCase();
                Log.e("sssss", ss.contains(charSequence) + "");
                Log.e("sssss", charSequence + "");
                Log.e("sssss", ss + "");
                if (ss.contains(charSequence)) {
                    if (list.size() > 0) {
                        boolean bl = false;
                        for (int j = 0; j < list.size(); j++) {
                            String key = map.get(id) + "";
                            Map<String, Object> map1 = object2Map(list.get(j));
                            String key2 = map1.get(id) + "";
                            if (key.equals(key2)) {
                                bl = true;
                            }
                        }
                        if (!bl) {
                            list.add(name);
                        }
                    } else {
                        list.add(name);
                    }
                }
            }

        }
        filterResults.values = list;
        return filterResults;
    }

    //筛选结果
    @Override
    protected void publishResults(CharSequence arg0, FilterResults results) {
        if (resultListener != null) {
            resultListener.result((List) results.values);
        }
    }

    public Map<String, Object> object2Map(Object obj) {

        Map<String, Object> map = new HashMap<>();
        if (obj == null) {
            return map;
        }
        Class clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                Log.e("wwwwww", field.getName());
                Log.e("wwwwww", field.get(obj).getClass().getName());
                boolean b = false;
                if (keyInClassNames != null && keyInClassNames.size() > 0) {
                    String simpleName = field.get(obj).getClass().getSimpleName();
                    for (int n = 0; n < keyInClassNames.size(); n++) {
                        if (simpleName.equals(keyInClassNames.get(n))) {
                            b = true;
                            n = keyInClassNames.size();
                        }
                    }
                }
                if (b) {
                    Class<?> classType = Class.forName(field.get(obj).getClass().getName());
                    Map<String, Object> stringObjectMap = object3Map(classType, field.get(obj));
                    map.putAll(stringObjectMap);
                } else {
                    map.put(field.getName(), field.get(obj));
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public Map<String, Object> object3Map(Class clazz, Object obj) {

        Map<String, Object> map = new HashMap<>();
        if (obj == null) {
            return map;
        }
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                Log.e("wwwwww", field.getName());
                Log.e("wwwwww", field.get(obj) + "");

                map.put(field.getName(), field.get(obj));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public interface ResultListener<T> {
        void result(List<T> results);
    }
}
