package com.ktw.bitbit.ui.me.emot;


import android.os.Parcel;
import android.os.Parcelable;

public class MyEmotBean implements Parcelable {

    /**
     * createTime : 1574838805
     * faceId : 5ddcd8408712e754ebe65811
     * faceName : star
     * fileLength : 0
     * fileSize : 0
     * id : 5dde22158712e7662ceb1cd7
     * isOne : 0
     * type : 0
     * url : http://47.52.74.41:8089/music/mPhoto/8ae1338eed3c419ca8d9c91c42c76945.gif
     * userId : 10000006
     */

    private String createTime;
    private String faceId;
    private String faceName;
    private int fileLength;
    private int fileSize;
    private String id;
    private int isOne;
    private int type;
    private String url;
    private String userId;
    private boolean isCheck;

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public String getFaceName() {
        return faceName;
    }

    public void setFaceName(String faceName) {
        this.faceName = faceName;
    }

    public int getFileLength() {
        return fileLength;
    }

    public void setFileLength(int fileLength) {
        this.fileLength = fileLength;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getIsOne() {
        return isOne;
    }

    public void setIsOne(int isOne) {
        this.isOne = isOne;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.createTime);
        dest.writeString(this.faceId);
        dest.writeString(this.faceName);
        dest.writeInt(this.fileLength);
        dest.writeInt(this.fileSize);
        dest.writeString(this.id);
        dest.writeInt(this.isOne);
        dest.writeInt(this.type);
        dest.writeString(this.url);
        dest.writeString(this.userId);
        dest.writeByte(this.isCheck ? (byte) 1 : (byte) 0);
    }

    public MyEmotBean() {
    }

    protected MyEmotBean(Parcel in) {
        this.createTime = in.readString();
        this.faceId = in.readString();
        this.faceName = in.readString();
        this.fileLength = in.readInt();
        this.fileSize = in.readInt();
        this.id = in.readString();
        this.isOne = in.readInt();
        this.type = in.readInt();
        this.url = in.readString();
        this.userId = in.readString();
        this.isCheck = in.readByte() != 0;
    }

    public static final Creator<MyEmotBean> CREATOR = new Creator<MyEmotBean>() {
        @Override
        public MyEmotBean createFromParcel(Parcel source) {
            return new MyEmotBean(source);
        }

        @Override
        public MyEmotBean[] newArray(int size) {
            return new MyEmotBean[size];
        }
    };
}
