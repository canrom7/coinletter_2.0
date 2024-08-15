package com.iimm.miliao.xmpp.spare;

import android.util.Log;

import com.google.gson.Gson;
import com.iimm.miliao.bean.SpareMessage;


public class ChatSpareMessageEvent implements SpareChatEvent {
    @Override
    public void onOpen() {
        Log.i("SpareConnectionHelper", "备用连接已连接....");
    }

    @Override
    public void onMessage(String id, String event, String message) {

        SpareMessage aaaa =  new Gson().fromJson(message, SpareMessage.class);


    /*    try {
            String body = "";
            SpareMessage  saaaa ;
            XmlPullParser pullParser = Xml.newPullParser();
            pullParser.setInput(new StringReader(message));
            int eventType = pullParser.getEventType();
            while ((eventType != XmlPullParser.END_DOCUMENT)) {
                String name = pullParser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        Log.i("xiaotao", "获取到了" + name);
                        if (TextUtils.equals("body", name)) {
                            saaaa = new SpareMessage();
                            body = pullParser.getAttributeValue(null,"fromUserId");
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (TextUtils.equals("body", name)) {
                            Log.i("xiaotao", "body : " + body);
                        }
                        Log.i("xiaotao", "结束获取到了" + name);
                        break;
                    default:
                        break;
                }
                eventType = pullParser.next();
            }
        } catch (XmlPullParserException e) {
            Log.i("xiaotao", "XmlPullParserException解析失败" + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.i("xiaotao", "IOException解析失败" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.i("xiaotao", "Exception解析失败" + e.getMessage());
            e.printStackTrace();
        }*/


        Log.i("SpareConnectionHelper", "备用连接收到消息...." + message);
    }

    @Override
    public void onClosed() {
        Log.i("SpareConnectionHelper", "备用连接已关闭....");
    }

}
