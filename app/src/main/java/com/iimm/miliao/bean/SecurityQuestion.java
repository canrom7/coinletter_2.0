package com.iimm.miliao.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * MrLiu253@163.com
 *
 * @time 2019-08-12
 */
public class SecurityQuestion {
    private List<QuestionsBean> data;

    public SecurityQuestion() {
    }

    public SecurityQuestion(List<QuestionsBean> data) {
        this.data = data;
    }

    public List<QuestionsBean> getData() {
        if (data == null) {
            return new ArrayList<>();
        }
        return data;
    }

    public void setData(List<QuestionsBean> data) {
        this.data = data;
    }
}
