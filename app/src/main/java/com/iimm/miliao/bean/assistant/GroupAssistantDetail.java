package com.iimm.miliao.bean.assistant;

import java.util.ArrayList;
import java.util.List;

public class GroupAssistantDetail {

    /**
     * helperId : 5ce4b6d257dddb18c8a021b0
     * id : 5ce4bb9f57dddb30dc73c39f
     * roomId : 5ce3cbbf57dddb2728490bbd
     * roomJid : 81b107b8dca944ddb0263263f2e37a77
     * userId : 10000058
     */

    private HelperBean helper;
    private List<KeyWord> keyWords;
    private String helperId;
    private String id;
    private String roomId;
    private String roomJid;
    private int userId;

    public HelperBean getHelper() {
        return helper;
    }

    public void setHelper(HelperBean helper) {
        this.helper = helper;
    }

    public List<KeyWord> getKeyWords() {
        if (keyWords == null) {
            keyWords = new ArrayList<>();
        }
        return keyWords;
    }

    public void setKeyWords(List<KeyWord> keyWords) {
        this.keyWords = keyWords;
    }

    public String getHelperId() {
        return helperId;
    }

    public void setHelperId(String helperId) {
        this.helperId = helperId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomJid() {
        return roomJid;
    }

    public void setRoomJid(String roomJid) {
        this.roomJid = roomJid;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public static class HelperBean {


        private String id;
        private int createTime;
        private String developer;
        private String iconUrl;
        private String name;
        private String desc;
        private int type;
        private String link;
        private String appPackName;
        private String callBackClassName;
        private GroupAssistant.OtherBean other;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getCreateTime() {
            return createTime;
        }

        public void setCreateTime(int createTime) {
            this.createTime = createTime;
        }

        public String getDeveloper() {
            return developer;
        }

        public void setDeveloper(String developer) {
            this.developer = developer;
        }

        public String getIconUrl() {
            return iconUrl;
        }

        public void setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getAppPackName() {
            return appPackName;
        }

        public void setAppPackName(String appPackName) {
            this.appPackName = appPackName;
        }

        public String getCallBackClassName() {
            return callBackClassName;
        }

        public void setCallBackClassName(String callBackClassName) {
            this.callBackClassName = callBackClassName;
        }

        public GroupAssistant.OtherBean getOther() {
            return other;
        }

        public void setOther(GroupAssistant.OtherBean other) {
            this.other = other;
        }

        public static class OtherBean {

            private String appIcon;
            private String appName;
            private String imageUrl;
            private String title;
            private String subTitle;
            private String url;

            public String getAppIcon() {
                return appIcon;
            }

            public void setAppIcon(String appIcon) {
                this.appIcon = appIcon;
            }

            public String getAppName() {
                return appName;
            }

            public void setAppName(String appName) {
                this.appName = appName;
            }

            public String getImageUrl() {
                return imageUrl;
            }

            public void setImageUrl(String imageUrl) {
                this.imageUrl = imageUrl;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getSubTitle() {
                return subTitle;
            }

            public void setSubTitle(String subTitle) {
                this.subTitle = subTitle;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }
        }
    }
}