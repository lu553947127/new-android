package com.ktw.fly.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @ProjectName: new-android
 * @Package: com.ktw.fly.bean
 * @ClassName: RedBacketCount
 * @Description: 红包汇总
 * @Author: XY
 * @CreateDate: 2021/10/14
 * @UpdateUser:
 * @UpdateDate: 2021/10/14
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class RedBacketCount {

    public List<RedBackerList> selectRedEnvelopesInfoCountUserInfo;
    public List<CapitalList> selectRedEnvelopesInfoCountUser;

    public class RedBackerList {

        public String red_envelope_capital;//单个红包金额--普通红包才有这个字段

        public String create_time;//创建时间

        public String received_red_envelope_count;//已领取红包个数

        public String received_red_envelope_capital;//已领取红包金额

        public String unclaimed_red_envelope_capital;//未领取红包金额

        public String end_time;//结束时间

        public String capital_count;//红包总额
        public String id;

        public String capital_type;//资金类型

        public String red_envelopes_type;//红包类型

        public String red_envelope_count;//红包数量

        public String unclaimed_red_envelope_count; //未领取红包个数


        public String currency_name;

        public String receive_capital;

        public String user_name;

        public String receive_time;

        public boolean extend;
    }

    public class CapitalList {
        public String capitalType;
        public String Number;
        public String capitalCount;
        public boolean select;

        public String getPath() {
            return "https://aabtc.oss-cn-shanghai.aliyuncs.com/coin/" + capitalType + ".png";
        }
    }


}
