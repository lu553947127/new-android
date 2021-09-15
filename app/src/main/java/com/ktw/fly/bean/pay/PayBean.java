package com.ktw.fly.bean.pay;


/**
 * {
 *         "msg": "获取成功",
 *         "code": 200,
 *         "url": "https://api2.payunk.com/pay/payorder.html?order_no=DC099592625058050597"
 *     },
 */
public class PayBean{
    private String msg;
    private int code;
    private  String url;
    private String path ;
    private String userName;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
