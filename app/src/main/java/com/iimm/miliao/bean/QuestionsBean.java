package com.iimm.miliao.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * MrLiu253@163.com
 *
 * @time 2019-09-06
 */
public class QuestionsBean implements Parcelable {

    /**
     * a : 333
     * q : 5d4cd6066339c412a8187623
     */

    private String a;
    private String q;

    public QuestionsBean(String q, String a) {
        this.q = q;
        this.a = a;
    }

    public String getA() {
        return a == null ? "" : a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getQ() {
        return q == null ? "" : q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.a);
        dest.writeString(this.q);
    }

    public QuestionsBean() {
    }

    protected QuestionsBean(Parcel in) {
        this.a = in.readString();
        this.q = in.readString();
    }

    public static final Parcelable.Creator<QuestionsBean> CREATOR = new Parcelable.Creator<QuestionsBean>() {
        @Override
        public QuestionsBean createFromParcel(Parcel source) {
            return new QuestionsBean(source);
        }

        @Override
        public QuestionsBean[] newArray(int size) {
            return new QuestionsBean[size];
        }
    };
}
