package com.dhh.easy.chat.wxapi;

public class EventUpdateBandAccount {
    public int type;
    public String result;
    public String msg;

    public EventUpdateBandAccount(int type, String result, String ok) {
        this.type = type;
        this.result = result;
        this.msg = ok;
    }
}
