package com.iimm.miliao.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * MrLiu253@163.com
 *
 * @time 2019-08-09
 */
public class SecurityQuestionBean {


    private String currentTime;
    private int resultCode;
    private List<DataBean> data;

    public String getCurrentTime() {
        return currentTime == null ? "" : currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public List<DataBean> getData() {
        if (data == null) {
            return new ArrayList<>();
        }
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {


        private String createdAt;
        private String id;
        private String question;
        private int status;
        private String updatedAt;

        public String getCreatedAt() {
            return createdAt == null ? "" : createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getId() {
            return id == null ? "" : id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getQuestion() {
            return question == null ? "" : question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getUpdatedAt() {
            return updatedAt == null ? "" : updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}
