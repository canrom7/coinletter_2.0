package com.iimm.miliao.bean;

public class VersionInfo {

    /**
     * forceStatus : 0
     * apkLoadUrl : http://test-file-1259280364.cos.ap-hongkong.myqcloud.com/u/0/0/201907/88e0ed31a95349ebab344c54e4c29e34.apk
     * versionNum : 1
     * projectName : TIGIM
     * versionName : 1.0.0
     * updateContent : 123456
     * 123456
     * 123456
     * 123456
     * thirdLoadURL : 1121111111111
     */

    private int forceStatus;
    private String apkLoadUrl;
    private String downloadUrl;
    private int versionNum;
    private String projectName;
    private String versionName;
    private String updateContent;
    private String thirdLoadURL;

    public int getForceStatus() {
        return forceStatus;
    }

    public void setForceStatus(int forceStatus) {
        this.forceStatus = forceStatus;
    }

    public String getApkLoadUrl() {
        return apkLoadUrl;
    }

    public void setApkLoadUrl(String apkLoadUrl) {
        this.apkLoadUrl = apkLoadUrl;
    }

    public int getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(int versionNum) {
        this.versionNum = versionNum;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getUpdateContent() {
        return updateContent;
    }

    public void setUpdateContent(String updateContent) {
        this.updateContent = updateContent;
    }

    public String getThirdLoadURL() {
        return thirdLoadURL;
    }

    public void setThirdLoadURL(String thirdLoadURL) {
        this.thirdLoadURL = thirdLoadURL;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
