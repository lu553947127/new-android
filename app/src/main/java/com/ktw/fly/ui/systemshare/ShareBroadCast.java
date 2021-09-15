package com.ktw.fly.ui.systemshare;

import android.content.Context;
import android.content.Intent;

import com.ktw.fly.FLYAppConfig;

public class ShareBroadCast {
    public static final String ACTION_FINISH_ACTIVITY = FLYAppConfig.sPackageName + ".action.finish_activity";// 结束之前的页面

    /**
     * 更新消息Fragment的广播
     */
    public static void broadcastFinishActivity(Context context) {
        context.sendBroadcast(new Intent(ACTION_FINISH_ACTIVITY));
    }
}
