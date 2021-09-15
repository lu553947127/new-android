package com.ktw.fly.bean;

public class QuestionBean {

    /**
     * content : <p>通讯号号设置问题一答案</p>
     * id : 601fd6aa372da92d9aacbae1
     * title : 通讯号号设置问题一
     * type : 1
     */

    private String content;
    private String id;
    private String title;
    private String type;
    private int drawableId;

    public QuestionBean() {
    }

    public QuestionBean(int drawableId, String title, String type) {
        this.drawableId = drawableId;
        this.title = title;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDrawableId() {
        return drawableId;
    }

    public void setDrawableId(int drawableId) {
        this.drawableId = drawableId;
    }
}
