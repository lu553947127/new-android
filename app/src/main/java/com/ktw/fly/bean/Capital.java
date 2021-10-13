package com.ktw.fly.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * @ProjectName: new-android
 * @Package: com.ktw.fly.bean
 * @ClassName: Capital
 * @Description: 资产类型
 * @Author: XY
 * @CreateDate: 2021/10/9
 * @UpdateUser:
 * @UpdateDate: 2021/10/9
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class Capital implements Parcelable {
    @SerializedName("capital_id")
    public String capitalId;//资产ID

    @SerializedName("capital_name")
    public String capitalName; //资产名称

    public String capital;//资产金额

    @SerializedName("user_id")
    public String userId;

    public boolean isChoose;

    public String getPath() {
        return "https://aabtc.oss-cn-shanghai.aliyuncs.com/coin/" + capitalName + ".png";
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.capitalId);
        dest.writeString(this.capitalName);
        dest.writeByte(this.isChoose ? (byte) 1 : (byte) 0);
    }

    public Capital() {
    }

    protected Capital(Parcel in) {
        this.capitalId = in.readString();
        this.capitalName = in.readString();
        this.isChoose = in.readByte() != 0;
    }

    public static final Creator<Capital> CREATOR = new Creator<Capital>() {
        @Override
        public Capital createFromParcel(Parcel source) {
            return new Capital(source);
        }

        @Override
        public Capital[] newArray(int size) {
            return new Capital[size];
        }
    };
}
