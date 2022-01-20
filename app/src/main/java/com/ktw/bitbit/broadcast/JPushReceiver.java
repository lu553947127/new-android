package com.ktw.bitbit.broadcast;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.alibaba.fastjson.JSONObject;
import com.ktw.bitbit.BuildConfig;
import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.ui.notification.NotificationProxyActivity;
import com.ktw.bitbit.util.Constants;
import com.ktw.bitbit.util.PreferenceUtils;
import com.ktw.bitbit.view.cjt2325.cameralibrary.util.LogUtil;

import java.util.HashMap;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;

import static android.content.Context.NOTIFICATION_SERVICE;

public class JPushReceiver extends BroadcastReceiver {

    private static final String TAG = "JPush";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            LogUtil.d(TAG, "JPush用户注册成功");
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent
                .getAction())) {
            LogUtil.d(TAG, "接受到推送下来的自定义消息");
            receivingNotification(context, intent.getExtras());
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent
                .getAction())) {
            LogUtil.d(TAG, "接受到推送下来的通知");
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent
                .getAction())) {
            LogUtil.d(TAG, "用户点击打开了通知");
        }
    }

    private void receivingNotification(Context context, Bundle bundle) {
        //弹出消息框
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            String CHANNEL_ONE_ID = "com.jixin.im";
            String CHANNEL_ONE_NAME = "com.jixin.im";
            NotificationChannel notificationChannel;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                        CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.enableLights(true);  //闪光灯
                notificationChannel.setLightColor(Color.BLUE);         //闪关灯的灯光颜色
                notificationChannel.setShowBadge(true); //桌面launcher的消息角标

                boolean isMute = PreferenceUtils.getBoolean(FLYApplication.getContext(), Constants.KEY_IS_MUTE);
                if (isMute) {
                    notificationChannel.enableVibration(false);
                    notificationChannel.setVibrationPattern(new long[]{0});
                    notificationChannel.setSound(null, null);
                } else {
                    notificationChannel.enableVibration(true);
                    notificationChannel.setVibrationPattern(new long[]{
                            100, 200, 300
                    });
                    Uri soundUri = Uri.parse("android:resource://" + BuildConfig.APPLICATION_ID + "/" + R.raw.msg);
                    notificationChannel.setSound(soundUri, Notification.AUDIO_ATTRIBUTES_DEFAULT);
                }
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC); //锁屏显示通知
                manager.createNotificationChannel(notificationChannel);
            }
            String message = bundle.getString(JPushInterface.EXTRA_EXTRA);
            LogUtil.d(TAG, "消息" + message);
            Map<String, String> params = extrasToMap(message);
            Intent localIntent = new Intent(context, NotificationProxyActivity.class);
            Bundle localIntentBundle = new Bundle();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                localIntentBundle.putString(entry.getKey(), entry.getValue());
            }
            localIntent.putExtras(localIntentBundle);

            PendingIntent localPendingIntent = PendingIntent.getActivity(context, 0, localIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder localBuilder = new NotificationCompat.Builder(context, CHANNEL_ONE_ID);

            localBuilder.setSmallIcon(R.mipmap.ic_aa_logo)  //小图标
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(params.get("msg")) //内容
                    .setChannelId(CHANNEL_ONE_ID)
                    .setWhen(System.currentTimeMillis()) //通知的时间
                    .setOngoing(false)
                    .setTicker(context.getString(R.string.app_name))
                    .setAutoCancel(true)  //设置点击信息后自动清除通知
                    .setContentIntent(localPendingIntent)
                    .setDefaults(NotificationCompat.FLAG_FOREGROUND_SERVICE);
            manager.notify(1, localBuilder.build());
        }
    }

    public static Map<String, String> extrasToMap(String extras) {
        Map<String, String> map = new HashMap<>();
        try {
            JSONObject jsonObject = JSONObject.parseObject(extras);
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                if (entry.getValue() != null) {
                    map.put(entry.getKey(), entry.getValue().toString());
                }
            }
        } catch (Exception e) {
            Log.e("Jush",e.getMessage());
        }
        return map;
    }
}
