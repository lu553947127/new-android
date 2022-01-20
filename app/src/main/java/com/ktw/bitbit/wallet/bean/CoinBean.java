package com.ktw.bitbit.wallet.bean;

import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.util.DateUtils;

import java.io.Serializable;

public class CoinBean implements Serializable {

    /**
     * id : 1
     * coinId : 1
     * coinName : USDT
     * amount : 95.0
     * tradeStatus : 1
     * type : 1
     * fee : 5.0
     * fromAddress : 111
     * toAddress : 111
     * txId : 121212
     * protocol : ERC20
     * remark :
     * relateRecdId :
     * appId : 0
     * createTime : 1632293966000
     * updateTime : 1632293966000
     * userId : 1223
     * transferNo : null
     */
    private int id;
    private int coinId;//币id
    private String coinName;//币名称
    private double amountS;//转出多少币
    private double sum;//转出多少币
    private int tradeStatus;//交易状态,1充/提币中,2充/提币完成，3充/提币失败
    private int type;//	1:充币；2提币
    private double feeS;//手续费
    private String fromAddress;//	转出地址
    private String toAddress;//	转入地址
    private String txId;//交易哈希
    private String protocol;//类型
    private String remark;//备注
    private String relateRecdId;
    private String appId;
    private long createTime;//	创建时间
    private long createTimes;//	创建时间
    private long updateTime;//	更新时间
    private String userId;
    private String transferNo;//	订单号

    private String number;
    private String suorceName;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getSuorceName() {
        return suorceName;
    }

    public void setSuorceName(String suorceName) {
        this.suorceName = suorceName;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public int getId() {
        return id;
    }

    public String getPath() {
        return "https://aabtc.oss-cn-shanghai.aliyuncs.com/coin/" + coinName + ".png";
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCoinId() {
        return coinId;
    }

    public void setCoinId(int coinId) {
        this.coinId = coinId;
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public double getAmount() {
        return amountS;
    }

    public void setAmount(double amount) {
        this.amountS = amount;
    }

    public int getTradeStatus() {
        return tradeStatus;
    }

    public void setTradeStatus(int tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTypeName() {
        return type == 1 ? FLYApplication.getInstance().getString(R.string.coin)
                : FLYApplication.getInstance().getString(R.string.tv_withdraw);
    }

    public double getFee() {
        return feeS;
    }

    public void setFee(double fee) {
        this.feeS = fee;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRelateRecdId() {
        return relateRecdId;
    }

    public void setRelateRecdId(String relateRecdId) {
        this.relateRecdId = relateRecdId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTransferNo() {
        return transferNo;
    }

    public void setTransferNo(String transferNo) {
        this.transferNo = transferNo;
    }

    public String getTime() {
        if (createTime == 0) {
            return "";
        }
        return DateUtils.getDate(createTime, "yyyy-MM-dd HH:mm:ss");
    }

    public long getCreateTimes() {
        return createTimes;
    }

    public void setCreateTimes(long createTimes) {
        this.createTimes = createTimes;
    }

    public String getTimes() {
        if (createTimes == 0) {
            return "";
        }
        return DateUtils.getDate(createTimes, "yyyy-MM-dd HH:mm:ss");
    }

    public String getUpdateRealTime() {
        if (updateTime == 0) {
            return "";
        }
        return DateUtils.getDate(updateTime, "yyyy-MM-dd HH:mm:ss");
    }
}