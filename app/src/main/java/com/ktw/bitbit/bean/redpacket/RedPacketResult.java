package com.ktw.bitbit.bean.redpacket;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @ProjectName: new-android
 * @Package: com.ktw.fly.bean.redpacket
 * @ClassName: RedPacketResult
 * @Description: 发送红包返回数据
 * @Author: XY
 * @CreateDate: 2021/10/11
 * @UpdateUser:
 * @UpdateDate: 2021/10/11
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class RedPacketResult implements Serializable {
    public String redId;
    public String currencyId;
    public String currencyName;
    public String userId;
    @SerializedName("red_envelope_pwd")
    public String redEnvelopePwd;
    public String type;
    @SerializedName("red_envelope_name")
    public String redEnvelopeName;

    public String redType;
    @SerializedName("user_image")
    public String userImage;
    public String userName;


    @NonNull
    public String toJson() {
        JSONObject object = new JSONObject();
        object.put("redId", this.redId);
        object.put("currencyId", this.currencyId);
        object.put("currencyName", this.currencyName);
        object.put("userId", this.userId);
        object.put("red_envelope_pwd", this.redEnvelopePwd);
        object.put("red_envelope_name", this.redEnvelopeName);
        object.put("type", this.type);
        object.put("userImage", this.userImage);
        object.put("userName", this.userName);
        object.put("redType", this.redType);

        return object.toJSONString();
    }
}
