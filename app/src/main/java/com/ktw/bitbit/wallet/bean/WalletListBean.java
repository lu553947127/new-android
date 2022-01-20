package com.ktw.bitbit.wallet.bean;

import java.io.Serializable;
import java.util.List;

public class WalletListBean implements Serializable {
    private String address;
    private String sum;
    private List<CurrencyBean> list;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSum() {
        return sum;
    }

    public void setSum(String sum) {
        this.sum = sum;
    }

    public List<CurrencyBean> getList() {
        return list;
    }

    public void setList(List<CurrencyBean> list) {
        this.list = list;
    }
}