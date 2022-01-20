package com.ktw.bitbit.socket.msg;


import com.alibaba.fastjson.JSONObject;

/**
 * Login Message
 */
public class AuthMessage extends AbstractMessage {

    private String token;

    private String password;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        JSONObject object = new JSONObject();
        object.put("messageHead", this.messageHead);
        object.put("password", this.password);
        object.put("token", this.token);
        String msg = object.toString();
        return msg;
    }

}
