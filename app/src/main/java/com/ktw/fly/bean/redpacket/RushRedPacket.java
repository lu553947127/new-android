package com.ktw.fly.bean.redpacket;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * @ProjectName: new-android
 * @Package: com.ktw.fly.bean.redpacket
 * @ClassName: RushRedPacket
 * @Description: 抢红包后返回的数据
 * @Author: XY
 * @CreateDate: 2021/10/12
 * @UpdateUser:
 * @UpdateDate: 2021/10/12
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class RushRedPacket implements Parcelable {

    public RedCapital redCapital;

    public RedCount redCount;

    public List<Red> redList;

    public RedUser redUser;

    public static class RedCapital implements Parcelable{
        public String capitalName;

        @SerializedName("capital_count")
        public String capitalCount;

        @SerializedName("capital_type")
        public String capitalType;


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.capitalName);
            dest.writeString(this.capitalCount);
            dest.writeString(this.capitalType);
        }

        public RedCapital() {
        }

        protected RedCapital(Parcel in) {
            this.capitalName = in.readString();
            this.capitalCount = in.readString();
            this.capitalType = in.readString();
        }

        public static final Creator<RedCapital> CREATOR = new Creator<RedCapital>() {
            @Override
            public RedCapital createFromParcel(Parcel source) {
                return new RedCapital(source);
            }

            @Override
            public RedCapital[] newArray(int size) {
                return new RedCapital[size];
            }
        };
    }

    public static class RedCount implements Parcelable{
        @SerializedName("unclaimed_red_envelope_capital")
        public String unclaimedRedEnvelopeCapital;
        @SerializedName("time")
        public String time;
        @SerializedName("red_envelope_count")
        public String redEnvelopeCount;
        @SerializedName("unclaimed_red_envelope_count")
        public String unclaimedRedEnvelopeCount;
        @SerializedName("received_red_envelope_count")
        public String receivedRedEnvelopeCount;
        @SerializedName("received_red_envelope_capital")
        public String receivedRedEnvelopeCapital;

        // 1 红包已领完   2 红包已过期   0 红包未领完
        public int status;


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.unclaimedRedEnvelopeCapital);
            dest.writeString(this.time);
            dest.writeString(this.redEnvelopeCount);
            dest.writeString(this.unclaimedRedEnvelopeCount);
            dest.writeString(this.receivedRedEnvelopeCount);
            dest.writeString(this.receivedRedEnvelopeCapital);
            dest.writeInt(this.status);
        }

        public RedCount() {
        }

        protected RedCount(Parcel in) {
            this.unclaimedRedEnvelopeCapital = in.readString();
            this.time = in.readString();
            this.redEnvelopeCount = in.readString();
            this.unclaimedRedEnvelopeCount = in.readString();
            this.receivedRedEnvelopeCount = in.readString();
            this.receivedRedEnvelopeCapital = in.readString();
            this.status = in.readInt();
        }

        public static final Creator<RedCount> CREATOR = new Creator<RedCount>() {
            @Override
            public RedCount createFromParcel(Parcel source) {
                return new RedCount(source);
            }

            @Override
            public RedCount[] newArray(int size) {
                return new RedCount[size];
            }
        };
    }

    public static class Red implements Parcelable{
        @SerializedName("receive_capital")
        public String receiveCapital;
        @SerializedName("red_envelopes_id")
        public String redEnvelopesId;

        @SerializedName("receive_name")
        public String receiveName;
        @SerializedName("receive_time")
        public String receiveTime;

        public String amount;
        public String id;

        @SerializedName("currency_id")
        public String currencyId;
        @SerializedName("receive_id")
        public String receiveId;
        public int status;


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.receiveCapital);
            dest.writeString(this.redEnvelopesId);
            dest.writeString(this.receiveName);
            dest.writeString(this.receiveTime);
            dest.writeString(this.amount);
            dest.writeString(this.id);
            dest.writeString(this.currencyId);
            dest.writeString(this.receiveId);
            dest.writeInt(this.status);
        }

        public Red() {
        }

        protected Red(Parcel in) {
            this.receiveCapital = in.readString();
            this.redEnvelopesId = in.readString();
            this.receiveName = in.readString();
            this.receiveTime = in.readString();
            this.amount = in.readString();
            this.id = in.readString();
            this.currencyId = in.readString();
            this.receiveId = in.readString();
            this.status = in.readInt();
        }

        public static final Creator<Red> CREATOR = new Creator<Red>() {
            @Override
            public Red createFromParcel(Parcel source) {
                return new Red(source);
            }

            @Override
            public Red[] newArray(int size) {
                return new Red[size];
            }
        };
    }

    public static class RedUser implements Parcelable{
        @SerializedName("user_id")
        public String userId;
        @SerializedName("user_name")
        public String userName;
        public String redId;


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.userId);
            dest.writeString(this.userName);
            dest.writeString(this.redId);
        }

        public RedUser() {
        }

        protected RedUser(Parcel in) {
            this.userId = in.readString();
            this.userName = in.readString();
            this.redId = in.readString();
        }

        public static final Creator<RedUser> CREATOR = new Creator<RedUser>() {
            @Override
            public RedUser createFromParcel(Parcel source) {
                return new RedUser(source);
            }

            @Override
            public RedUser[] newArray(int size) {
                return new RedUser[size];
            }
        };
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.redCapital, flags);
        dest.writeParcelable(this.redCount, flags);
        dest.writeTypedList(this.redList);
        dest.writeParcelable(this.redUser, flags);
    }

    public RushRedPacket() {
    }

    protected RushRedPacket(Parcel in) {
        this.redCapital = in.readParcelable(RedCapital.class.getClassLoader());
        this.redCount = in.readParcelable(RedCount.class.getClassLoader());
        this.redList = in.createTypedArrayList(Red.CREATOR);
        this.redUser = in.readParcelable(RedUser.class.getClassLoader());
    }

    public static final Creator<RushRedPacket> CREATOR = new Creator<RushRedPacket>() {
        @Override
        public RushRedPacket createFromParcel(Parcel source) {
            return new RushRedPacket(source);
        }

        @Override
        public RushRedPacket[] newArray(int size) {
            return new RushRedPacket[size];
        }
    };
}
