package com.iimm.miliao.bean.circle;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * @描述: 赞的实体
 */
public class Praise implements Serializable {
    private static final long serialVersionUID = -7269886584765396602L;
    private String praiseId;
    private String userId;//赞的人的id
    @JSONField(name = "nickname")
    private String nickName;//赞的人的昵称
    private Long time;//赞的时间，

    public String getPraiseId() {
        return praiseId;
    }

    public void setPraiseId(String praiseId) {
        this.praiseId = praiseId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickname) {
        this.nickName = nickname;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
