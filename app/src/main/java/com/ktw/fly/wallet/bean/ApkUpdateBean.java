package com.ktw.fly.wallet.bean;

import java.io.Serializable;

/**
 * 软件更新实体类
 */

public class ApkUpdateBean implements Serializable{

    private String createTime;
    private String downloadAddress;
    private String versionNo;
    private String isUpdates;
    private String iOS;
    private String type;
    private String content;

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getDownloadAddress() {
        return downloadAddress;
    }

    public void setDownloadAddress(String downloadAddress) {
        this.downloadAddress = downloadAddress;
    }

    public String getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(String versionNo) {
        this.versionNo = versionNo;
    }

    public String getIsUpdates() {
        return isUpdates;
    }

    public void setIsUpdates(String isUpdates) {
        this.isUpdates = isUpdates;
    }

    public String getiOS() {
        return iOS;
    }

    public void setiOS(String iOS) {
        this.iOS = iOS;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
