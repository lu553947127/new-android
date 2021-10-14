package com.ktw.fly.helper;

import android.content.Context;
import android.widget.Toast;

import com.ktw.fly.R;
import com.ktw.fly.bean.redpacket.RedPacketResult;
import com.ktw.fly.bean.redpacket.RushRedPacket;
import com.ktw.fly.ui.base.CoreManager;
import com.ktw.fly.util.secure.LoginPassword;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * @ProjectName: new-android
 * @Package: com.ktw.fly.helper
 * @ClassName: RedPacketHelper
 * @Description: 发送红包工具类
 * @Author: XY
 * @CreateDate: 2021/10/11
 * @UpdateUser:
 * @UpdateDate: 2021/10/11
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class RedPacketHelper {

    /**
     * 检测是否设置或资金密码
     *
     * @param ctx
     * @param coreManager
     * @param userId
     * @param onError
     * @param onSuccess
     */
    public static void detectionCapitalPassword(Context ctx, CoreManager coreManager, String userId,
                                                Function<Throwable> onError, Function<ObjectResult> onSuccess) {
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        HttpUtils.post().url(coreManager.getConfig().SELECT_USER_CAPITAL_PWD)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkSuccess(ctx, result)) {
                            onSuccess.apply(result);
                        } else {
                            onError.apply(new IllegalStateException(Result.getErrorMessage(ctx, result)));
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(ctx, R.string.net_exception, Toast.LENGTH_SHORT).show();
                    }
                });

    }


    /**
     * 校验资金密码
     *
     * @param ctx
     * @param coreManager
     * @param userId
     * @param password
     * @param onError
     * @param onSuccess
     */
    public static void checkCapitalPassword(Context ctx, CoreManager coreManager, String userId,
                                            String password,
                                            Function<Throwable> onError, Function<ObjectResult> onSuccess) {
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("pwd", LoginPassword.encodeMd5(password));
        HttpUtils.post().url(coreManager.getConfig().CHECK_CAPITAL_PWD)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkSuccess(ctx, result)) {
                            onSuccess.apply(result);
                        } else {
                            onError.apply(new IllegalStateException(Result.getErrorMessage(ctx, result)));
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(ctx, R.string.net_exception, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    /**
     * 发送红包
     *
     * @param ctx
     * @param coreManager
     * @param params
     * @param onError
     * @param onSuccess
     */
    public static void sendRedPacket(Context ctx, CoreManager coreManager, Map<String, String> params,
                                     Function<Throwable> onError, Function<ObjectResult<RedPacketResult>> onSuccess) {

        HttpUtils.post().url(coreManager.getConfig().SEND_RED_PACKET)
                .params(params)
                .build()
                .execute(new BaseCallback<RedPacketResult>(RedPacketResult.class) {

                    @Override
                    public void onResponse(ObjectResult<RedPacketResult> result) {
                        if (Result.checkSuccess(ctx, result)) {
                            onSuccess.apply(result);
                        } else {
                            onError.apply(new IllegalStateException(Result.getErrorMessage(ctx, result)));
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(ctx, R.string.net_exception, Toast.LENGTH_SHORT).show();
                        onError.apply(e);
                    }
                });

    }

    /**
     * 红包是否已经抢过
     *
     * @param ctx
     * @param params
     * @param onError
     * @param onSuccess
     */
    public static void gainRedPacket(Context ctx, Map<String, String> params,
                                     Function<Throwable> onError, Function<ObjectResult<RedPacketResult>> onSuccess) {
        HttpUtils.post().url(CoreManager.requireConfig(ctx).GAIN_RED_PACKET)
                .params(params)
                .build()
                .execute(new BaseCallback<RedPacketResult>(RedPacketResult.class) {

                    @Override
                    public void onResponse(ObjectResult<RedPacketResult> result) {
                        onSuccess.apply(result);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(ctx, R.string.net_exception, Toast.LENGTH_SHORT).show();
                        onError.apply(e);
                    }
                });
    }

    /**
     * 抢红包
     *
     * @param ctx
     * @param params
     * @param onError
     * @param onSuccess
     */
    public static void rushRedPacket(Context ctx, Map<String, String> params,
                                     Function<Throwable> onError, Function<ObjectResult<RushRedPacket>> onSuccess) {

        HttpUtils.post().url(CoreManager.requireConfig(ctx).RUSH_RED_PACKET)
                .params(params)
                .build()
                .execute(new BaseCallback<RushRedPacket>(RushRedPacket.class) {

                    @Override
                    public void onResponse(ObjectResult<RushRedPacket> result) {
                        if (result.getResultCode() == Result.CODE_AUTH_RED_PACKET_ERROR) {
                            Toast.makeText(ctx, result.getResultMsg(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        onSuccess.apply(result);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(ctx, R.string.net_exception, Toast.LENGTH_SHORT).show();
                        onError.apply(e);
                    }
                });
    }

    public interface Function<T> {
        void apply(T t);
    }

    public interface Function2<T, R> {
        void apply(T t, R r);
    }

    public interface Function3<T, R, E> {
        void apply(T t, R r, E e);
    }
}
