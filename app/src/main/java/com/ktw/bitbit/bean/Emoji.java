package com.ktw.bitbit.bean;public class Emoji {    private int id;    private String filename;    private String english;    private int count;    public Emoji() {    }    public Emoji(String english, int count) {        this.english = english;        this.count = count;    }    public int getCount() {        return count;    }    public void setCount(int count) {        this.count = count;    }    public int getId() {        return id;    }    public void setId(int id) {        this.id = id;    }    public String getFilename() {        return filename;    }    public void setFilename(String filename) {        this.filename = filename;    }    public String getEnglish() {        return english;    }    public void setEnglish(String english) {        this.english = english;    }}