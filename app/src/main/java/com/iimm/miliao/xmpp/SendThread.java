package com.iimm.miliao.xmpp;

import android.util.Log;

import com.google.gson.Gson;
import com.iimm.miliao.BuildConfig;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SendThread extends Thread {
    private String TAG = "SendThread";
    private long flushTime;

    @Override
    public void run() {
        try {
            while (!XmppReceiptImpl.receiptThreadStop) {
                // 每秒醒来一次，判断5秒没发回执就发一次，或者消息数量大于100也发一次，
                if (!XmppReceiptImpl.batchReceiptMessageQueue.isEmpty()) {
                    if (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - flushTime) > 15
                            || XmppReceiptImpl.batchReceiptMessageQueue.size() > 60)
                        flushBatchReceiptMessageQueue();
                }
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (Exception e) {
            Log.e(TAG, "发回执线程结束", e);
        }
    }

    private void flushBatchReceiptMessageQueue() {
        flushTime = System.currentTimeMillis();
        // 消息列表的使用是异步的，为免被马上清空，克隆一份，
        sendReceipt(new ArrayList<>(XmppReceiptImpl.batchReceiptMessageQueue));
        XmppReceiptImpl.batchReceiptMessageQueue.clear();
    }

    private void sendReceipt(List<String> messageIdList) {
        Log.d(TAG, "sendReceipt() called with: messageIdList = [" + messageIdList + "]");
        Receipt receipt = new Receipt(messageIdList);
        try {
            if (XmppConnectionImpl.getInstance() != null && XmppConnectionImpl.getInstance().getXMPPConnection() != null) {
                XMPPTCPConnection xmpptcpConnection = XmppConnectionImpl.getInstance().getXMPPConnection();
                if (xmpptcpConnection != null && xmpptcpConnection.isConnected()) {
                    xmpptcpConnection.sendStanza(receipt);
                }
            }
            if (BuildConfig.DEBUG) {
                Log.d("消息监听", "发回执----" + receipt.toString() + "----" + new Gson().toJson(messageIdList));
            }
        } catch (Exception e) {
            Log.e(TAG, "send failed", e);
        }
    }
}
