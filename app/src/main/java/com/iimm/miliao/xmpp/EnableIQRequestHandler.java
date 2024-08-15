package com.iimm.miliao.xmpp;

import com.iimm.miliao.util.log.LogUtils;

import org.jivesoftware.smack.iqrequest.IQRequestHandler;
import org.jivesoftware.smack.packet.IQ;

public class EnableIQRequestHandler implements IQRequestHandler {
    public static String TAG = "EnableIQRequestHandler";
    public static Thread sendBatchReceiptThread;

    @Override
    public IQ handleIQRequest(IQ iqRequest) {
        LogUtils.i(TAG, "handleIQRequest() called with: iqRequest = [" + iqRequest + "]");
        sendBatchReceiptThread = new SendThread();
        sendBatchReceiptThread.start();
        return null; // 不响应这个enable，
    }

    @Override
    public Mode getMode() {
        return Mode.async;
    }

    @Override
    public IQ.Type getType() {
        return IQ.Type.set;
    }

    @Override
    public String getElement() {
        return Enable.ELEMENT;
    }

    @Override
    public String getNamespace() {
        return Enable.NAMESPACE;
    }
}