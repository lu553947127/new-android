package com.ktw.fly.ui.me.emot;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class EmotBean implements Parcelable {

    /**
     * clollectNumber : 0
     * createBy : 0
     * createTime : 1584516997
     * desc : 111
     * id : 5e71cf85fa15eb41cc77003a
     * modifyTime : 1584794237
     * name : 海天使就职
     * number : 0
     * path : ["http://47.75.208.70:8089/temp/20200318/01.gif"]
     * special : false
     * type : 0
     */

    private int clollectNumber;
    private int createBy;
    private int createTime;
    private String desc;
    private String id;
    private int modifyTime;
    private String name;
    private int number;
    private boolean special;
    private int type;
    private List<String> path;

    public int getClollectNumber() {
        return clollectNumber;
    }

    public void setClollectNumber(int clollectNumber) {
        this.clollectNumber = clollectNumber;
    }

    public int getCreateBy() {
        return createBy;
    }

    public void setCreateBy(int createBy) {
        this.createBy = createBy;
    }

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(int modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isSpecial() {
        return special;
    }

    public void setSpecial(boolean special) {
        this.special = special;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<String> getPath() {
        return path;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.clollectNumber);
        dest.writeInt(this.createBy);
        dest.writeInt(this.createTime);
        dest.writeString(this.desc);
        dest.writeString(this.id);
        dest.writeInt(this.modifyTime);
        dest.writeString(this.name);
        dest.writeInt(this.number);
        dest.writeByte(this.special ? (byte) 1 : (byte) 0);
        dest.writeInt(this.type);
        dest.writeStringList(this.path);
    }

    public EmotBean() {
    }

    protected EmotBean(Parcel in) {
        this.clollectNumber = in.readInt();
        this.createBy = in.readInt();
        this.createTime = in.readInt();
        this.desc = in.readString();
        this.id = in.readString();
        this.modifyTime = in.readInt();
        this.name = in.readString();
        this.number = in.readInt();
        this.special = in.readByte() != 0;
        this.type = in.readInt();
        this.path = in.createStringArrayList();
    }

    public static final Creator<EmotBean> CREATOR = new Creator<EmotBean>() {
        @Override
        public EmotBean createFromParcel(Parcel source) {
            return new EmotBean(source);
        }

        @Override
        public EmotBean[] newArray(int size) {
            return new EmotBean[size];
        }
    };
}
