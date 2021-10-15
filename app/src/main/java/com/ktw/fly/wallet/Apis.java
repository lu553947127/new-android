package com.ktw.fly.wallet;

import com.ktw.fly.FLYAppConfig;

public interface Apis {

    String BASE_URL = "http://aachat-test-api.aachain.org/";//测试
//    String BASE_URL = "http://192.168.0.86:7977/";//本地
//    String BASE_URL = "http://116.213.41.159:7977/";//测试

    String USER_ASSET = BASE_URL + "assets/queryuserassets";//用户资产
    String CURRENCY_ADDRESS = BASE_URL + "assets/getformcurrencypath";//充币地址
    String COIN_WITHDRAW = BASE_URL + "assets/gettopupcurrencylog";//充提记录
    String COIN_WITHDRAW_RECORD = BASE_URL + "assets/queryflow";//充提流水记录
    String WITHDRAW_OP = BASE_URL + "assets/tocurrency";//提币
    String DOWNLOAD_URL = BASE_URL + "assets/queryversion";//版本更新

    String SEND_CODE = FLYAppConfig.HOST + "user/sendCodeByType";//发送提币验证码
    String VERIFY_CODE = FLYAppConfig.HOST + "user/validationidentity";//校验资金密码，验证码
}
