package com.xuan.xuanhttplibrary.okhttp.callback;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.LogUtils;
import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.FLYReporter;
import com.ktw.bitbit.helper.LoginHelper;

import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public abstract class AbstractCallback<T> implements Callback {
    // 切到主线程再回调子类方法，
    protected boolean mainThreadCallback;
    private Handler mDelivery;
    private final Charset UTF8 = Charset.forName("UTF-8");

    public AbstractCallback() {
        this(true);
    }

    /**
     * @param mainThreadCallback true表示切到主线程再回调子类方法，
     */
    public AbstractCallback(boolean mainThreadCallback) {
        this.mainThreadCallback = mainThreadCallback;
        mDelivery = new Handler(Looper.getMainLooper());
    }

    public abstract void onResponse(T result);

    public abstract void onError(Call call, Exception e);

    @Override
    public void onFailure(Call call, IOException e) {
        Log.i(HttpUtils.TAG, "服务器请求失败", e);
        if (e instanceof ConnectException) {
            Log.i(HttpUtils.TAG, "ConnectException", e);
        }
        if (e instanceof SocketTimeoutException) {
            Log.i(HttpUtils.TAG, "SocketTimeoutException", e);
        }
        errorData(call, e);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        LogUtils.i("URL", "请求接口：" + response.request().url());
        LogUtils.i("PARAM", "请求参数：" + getBody(response));
        if (response.code() == 200) {
            try {
                String body = response.body().string();
                LogUtils.i("BODY", "服务器数据包：" + body);
                successData(parseResponse(call, body));
            } catch (Exception e) {
                com.ktw.bitbit.util.LogUtils.log(response);
                FLYReporter.post("json解析失败, ", e);
                LogUtils.i(HttpUtils.TAG, "数据解析异常:" + e.getMessage());
                errorData(call, new Exception("数据解析异常"));
            }
        } else {
            LogUtils.i(HttpUtils.TAG, response.request().url() + "请求异常");
            errorData(call, new Exception("服务器请求异常"));
        }
    }

    @NonNull
    abstract T parseResponse(Call call, String body);

    protected void successData(final T t) {
        if (mainThreadCallback) {
            mDelivery.post(() -> callOnResponse(t));
        } else {
            callOnResponse(t);
        }
    }

    private void callOnResponse(T t) {
        if (t instanceof Result) {
            int resultCode = ((Result) t).getResultCode();
            if (resultCode == Result.CODE_TOKEN_ERROR) {
                FLYApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_TOKEN_OVERDUE;
                LoginHelper.broadcastLogout(FLYApplication.getContext());
            }
        }
        onResponse(t);
    }

    protected void errorData(final Call call, final Exception e) {
        if (mainThreadCallback) {
            mDelivery.post(() -> onError(call, e));
        } else {
            onError(call, e);
        }
    }

    /**
     * 获取请求参数
     *
     * @param response
     * @return
     */
    private String getBody(Response response){
        RequestBody requestBody = response.request().body();
        String newbody = null;
        if (requestBody != null){
            Buffer buffer = new Buffer();
            try {
                requestBody.writeTo(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null){
                charset = contentType.charset(UTF8);
            }
            newbody = buffer.readString(charset);
        }
        return newbody;
    }
}
