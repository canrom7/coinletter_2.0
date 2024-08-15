package com.iimm.miliao.bean;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.android.databinding.library.baseAdapters.BR;

/**
 * MrLiu253@163.com
 *
 * @time 2020-02-27
 */
public class MucSendRedSelectBean extends BaseObservable {


    /**
     * active : 1582781901
     * call : 300137
     * cleanedTime : 0
     * createTime : 1582781901
     * deleteLastChatTime : 0
     * isAddFirend : 1
     * modifyTime : 0
     * nickname : Jdiis
     * offlineNoPushMsg : 0
     * offlineTime : 1582771123
     * onLineState : 1
     * role : 1
     * sub : 1
     * talkTime : 0
     * userId : 10000019
     * videoMeetingNo : 350137
     * vip : 0
     */

    private String nickname;
    private String userId;
    private boolean mSelect;

    @Bindable
    public String getNickname() {
        return nickname == null ? "" : nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        notifyPropertyChanged(BR.nickname);
    }
    @Bindable
    public String getUserId() {
        return userId == null ? "" : userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
        notifyPropertyChanged(BR.userId);
    }
    @Bindable
    public boolean isSelect() {
        return mSelect;
    }

    public void setSelect(boolean select) {
        mSelect = select;
        notifyPropertyChanged(BR.select);
    }
}
