package com.ktw.fly.wallet;

public interface Apis {

//    String BASE_URL = "http://192.168.0.86:7977/";
    String BASE_URL = "http://116.213.41.159:7977/";

    String USER_ASSET = BASE_URL + "assets/queryuserassets";//用户资产
    String CURRENCY_ADDRESS = BASE_URL + "assets/getformcurrencypath";//充币地址
    String COIN_WITHDRAW = BASE_URL + "assets/gettopupcurrencylog";//充提记录
    String WITHDRAW_OP = BASE_URL + "assets/tocurrency";//提币
}
