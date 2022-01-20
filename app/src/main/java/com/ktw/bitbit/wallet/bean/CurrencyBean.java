package com.ktw.bitbit.wallet.bean;

import android.text.TextUtils;

import java.io.Serializable;

public class CurrencyBean implements Serializable {
    private boolean isSelect;

    public CurrencyBean(boolean isSelect) {
        this.isSelect = isSelect;
    }

    public CurrencyBean() {

    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    private String f01;//币种ID
    private String f03;//币对名称
    private String f05;//是否开启充币：S-是；F-否
    private String f06;//是否开启提币：S-是；F-否
    private String f08;//创建时间
    private String f09;//提币最小限制数量
    private String f10;//提币最大限制数量
    private String f11;//提币手续费
    private String f13;//充币最小数量
    private String f15;//类别 以逗号分开
    private String freeAssets;//冻结资产
    private String freezeAssets;//可用资产
    private String sumAsstes;//	总资产
    private String usdt;//	换算USDT

    public String getUsdt() {
        return "$ " + (TextUtils.isEmpty(usdt) ? 0.00 : usdt);
    }

    public void setUsdt(String usdt) {
        this.usdt = usdt;
    }

    public String getF01() {
        return f01;
    }

    public void setF01(String f01) {
        this.f01 = f01;
    }

    public String getCurrencyName() {
        return f03;
    }

    public String getPath() {
        return "https://aabtc.oss-cn-shanghai.aliyuncs.com/coin/" + f03 + ".png";
    }

    public void setCurrencyName(String f03) {
        this.f03 = f03;
    }

    public boolean isCanCoin() {
        return "S".equalsIgnoreCase(f05);
    }

    public void setF05(String f05) {
        this.f05 = f05;
    }

    public boolean isCanWithdraw() {
        return "S".equalsIgnoreCase(f06);
    }

    public void setF06(String f06) {
        this.f06 = f06;
    }

    public String getCreateTime() {
        return f08;
    }

    public void setCreateTime(String f08) {
        this.f08 = f08;
    }

    public String getMinWithdrawNumber() {
        return f09;
    }

    public void setMinWithdrawNumber(String f09) {
        this.f09 = f09;
    }

    public String getMaxWithdrawNumber() {
        return f10;
    }

    public void setMaxWithdrawNumber(String f10) {
        this.f10 = f10;
    }

    public String getWithDrawFee() {
        return f11;
    }

    public void setWithDrawFee(String f11) {
        this.f11 = f11;
    }

    public String getMinCoinNumber() {
        return f13;
    }

    public void setMinCoinNumber(String f13) {
        this.f13 = f13;
    }

    public String getType() {
        return f15;
    }

    public void setType(String f15) {
        this.f15 = f15;
    }

    public String getFreeAssets() {
        return freeAssets;
    }

    public void setFreeAssets(String freeAssets) {
        this.freeAssets = freeAssets;
    }

    public String getFreezeAssets() {
        return freezeAssets;
    }

    public void setFreezeAssets(String freezeAssets) {
        this.freezeAssets = freezeAssets;
    }

    public String getSumAsstes() {
        return sumAsstes;
    }

    public void setSumAsstes(String sumAsstes) {
        this.sumAsstes = sumAsstes;
    }

    public String getF03() {
        return f03;
    }

    public String getF05() {
        return f05;
    }

    public String getF06() {
        return f06;
    }

    public String getF08() {
        return f08;
    }

    public String getF09() {
        return f09;
    }

    public String getF10() {
        return f10;
    }

    public String getF11() {
        return f11;
    }

    public String getF13() {
        return f13;
    }

    public String getF15() {
        return f15;
    }

    public void setF03(String f03) {
        this.f03 = f03;
    }

    public void setF08(String f08) {
        this.f08 = f08;
    }

    public void setF09(String f09) {
        this.f09 = f09;
    }

    public void setF10(String f10) {
        this.f10 = f10;
    }

    public void setF11(String f11) {
        this.f11 = f11;
    }

    public void setF13(String f13) {
        this.f13 = f13;
    }

    public void setF15(String f15) {
        this.f15 = f15;
    }

    @Override
    public String toString() {
        return "CurrencyBean{" +
                "isSelect=" + isSelect +
                ", f01='" + f01 + '\'' +
                ", f03='" + f03 + '\'' +
                ", f05='" + f05 + '\'' +
                ", f06='" + f06 + '\'' +
                ", f08='" + f08 + '\'' +
                ", F09='" + f09 + '\'' +
                ", F10='" + f10 + '\'' +
                ", F11='" + f11 + '\'' +
                ", F13='" + f13 + '\'' +
                ", F15='" + f15 + '\'' +
                ", freeAssets='" + freeAssets + '\'' +
                ", freezeAssets='" + freezeAssets + '\'' +
                ", sumAsstes='" + sumAsstes + '\'' +
                ", usdt='" + usdt + '\'' +
                '}';
    }
}