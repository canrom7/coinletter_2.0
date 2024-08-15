package com.iimm.miliao.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * MrLiu253@163.com
 *
 * @time 2020-04-06
 */
public class SpareMessage {


    /**
     * attributes : {"type":"groupchat","id":"c657ea2c3458421f83973aecdddd0481","xmlns":"jabber:client","from":"048874362da746dd8fd879e198e5b35f@muc.csim.chat.top/10000018","to":"10000018@csim.chat.top/android"}
     * children : [{"cData":"{\"fromUserName\":\"jjsj\",\"deleteTime\":-1,\"fromUserId\":\"10000018\",\"timeSend\":1586167517.393,\"messageId\":\"c657ea2c3458421f83973aecdddd0481\",\"type\":1,\"toUserId\":\"048874362da746dd8fd879e198e5b35f\",\"content\":\"***************呢***************我***************\"}","children":[],"name":"body"}]
     * name : message
     * xMLNS : jabber:client
     */

    private AttributesBean attributes;
    private String name;
    private String xMLNS;
    private List<ChildrenBean> children;

    public AttributesBean getAttributes() {
        return attributes;
    }

    public void setAttributes(AttributesBean attributes) {
        this.attributes = attributes;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getxMLNS() {
        return xMLNS == null ? "" : xMLNS;
    }

    public void setxMLNS(String xMLNS) {
        this.xMLNS = xMLNS;
    }

    public List<ChildrenBean> getChildren() {
        if (children == null) {
            return new ArrayList<>();
        }
        return children;
    }

    public void setChildren(List<ChildrenBean> children) {
        this.children = children;
    }

    public static class AttributesBean {
        /**
         * type : groupchat
         * id : c657ea2c3458421f83973aecdddd0481
         * xmlns : jabber:client
         * from : 048874362da746dd8fd879e198e5b35f@muc.csim.chat.top/10000018
         * to : 10000018@csim.chat.top/android
         */

        private String type;
        private String id;
        private String xmlns;
        private String from;
        private String to;

        public String getType() {
            return type == null ? "" : type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getId() {
            return id == null ? "" : id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getXmlns() {
            return xmlns == null ? "" : xmlns;
        }

        public void setXmlns(String xmlns) {
            this.xmlns = xmlns;
        }

        public String getFrom() {
            return from == null ? "" : from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to == null ? "" : to;
        }

        public void setTo(String to) {
            this.to = to;
        }
    }

    public static class ChildrenBean {
        /**
         * cData : {"fromUserName":"jjsj","deleteTime":-1,"fromUserId":"10000018","timeSend":1586167517.393,"messageId":"c657ea2c3458421f83973aecdddd0481","type":1,"toUserId":"048874362da746dd8fd879e198e5b35f","content":"***************呢***************我***************"}
         * children : []
         * name : body
         */

        private String cData;
        private String name;

        public String getcData() {
            return cData == null ? "" : cData;
        }

        public void setcData(String cData) {
            this.cData = cData;
        }

        public String getName() {
            return name == null ? "" : name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
