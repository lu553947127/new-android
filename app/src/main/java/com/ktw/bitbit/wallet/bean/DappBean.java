package com.ktw.bitbit.wallet.bean;

public class DappBean {

    private String url;
    private int img;
    private String title;
    private String content;

    public DappBean() {
    }

    public DappBean(int img, String title) {
        this.img = img;
        this.title = title;
    }

    public DappBean(int img, String title, String content) {
        this.img = img;
        this.title = title;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}