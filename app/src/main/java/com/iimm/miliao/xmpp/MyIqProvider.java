package com.iimm.miliao.xmpp;

import android.util.Log;

import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

public class MyIqProvider extends IQProvider<Enable> {
    private String TAG = "MyIqProvider";

    @Override
    public Enable parse(XmlPullParser parser, int initialDepth) throws Exception {
        Log.d(TAG, "parse() called with: parser = [" + parser + "], initialDepth = [" + initialDepth + "]");
        return new Enable();
    }
}