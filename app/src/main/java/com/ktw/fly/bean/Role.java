package com.ktw.fly.bean;

/**
 * 没用了，服务器端接口直接返回int数组了，
 */
public class Role {
    private Integer userId;

    private String phone;// 账号

    private byte role = 0; // 1=游客（用于后台浏览数据）；2=公众号 ；3=机器账号，由系统自动生成；4=客服账号;5=管理员；6=超级管理员；7=财务；
    private byte status = 1; // 状态值 -1:禁用, 1:正常
    private long createTime; // 创建时间
    private long lastLoginTime; // 最后登录时间
    private String promotionUrl;// 客服推广链接

    public byte getRole() {
        return role;
    }

    public void setRole(byte role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return getStatus() == 1;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public String getPromotionUrlIfExists() {
        if (isNormalService()) {
            return promotionUrl;
        }
        return null;
    }

    public boolean isNormalService() {
        return role == 4 && status == 1;
    }

    public String getPromotionUrl() {
        return promotionUrl;
    }

    public void setPromotionUrl(String promotionUrl) {
        this.promotionUrl = promotionUrl;
    }
}
