package com.iimm.miliao.bean;

/**
 * MrLiu253@163.com
 *
 * @time 2020-01-15
 */
public class MassageDelete {

    /**
     * receiver : 10000105
     * isRead : 0
     * messageId : 6b82c6b2b8c84f46833d8806df621e15
     * body : {"content":"Fghh","deleteTime":-1,"fromUserId":"10000126","fromUserName":"333333","messageId":"6b82c6b2b8c84f46833d8806df621e15","timeSend":1579074869.86,"toUserId":"10000105","type":1}
     * type : 1
     * content : Fghh
     * deleteTime : -1
     * sender : 10000126
     * timeSend : 1.57907486986E9
     * _id : {"date":1579074872000,"machineIdentifier":8170128,"counter":5728655,"processIdentifier":8990,"time":1579074872000,"timeSecond":1579074872,"timestamp":1579074872}
     * contentType : 1
     * direction : 0
     * ts : 1579074870020
     */

    private String body;

    public String getBody() {
        return body == null ? "" : body;
    }

    public void setBody(String body) {
        this.body = body;
    }


    /**
     * chatType : groupchat
     * content : olrJlFYXXOo=
     * fromUserId : 10000004
     * fromUserName : 哦哦哦
     * id : 18bd35451f184bb7b2ad04612b5b0832
     * isEncrypt : 1
     * messageId : 18bd35451f184bb7b2ad04612b5b0832
     * timeSend : 1.5790757361E9
     * to : 819ea59d0431431a958b5418e5437ed2
     * toJid : 819ea59d0431431a958b5418e5437ed2
     * toUserId : 819ea59d0431431a958b5418e5437ed2
     * toUserName : Xjjd
     * type : 1
     */

    private String chatType;
    private String content;
    private String fromUserId;
    private String fromUserName;
    private String id;
    private String messageId;
    private String to;
    private String toJid;
    private String toUserId;
    private String toUserName;



    public String getMessageId() {
        return messageId == null ? "" : messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getToUserId() {
        return toUserId == null ? "" : toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }
}
