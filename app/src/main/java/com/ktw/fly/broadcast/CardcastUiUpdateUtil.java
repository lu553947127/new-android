package com.ktw.fly.broadcast;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.ktw.fly.FLYAppConfig;

/**
 * 我的朋友
 */
public class CardcastUiUpdateUtil {

    public static final String ACTION_UPDATE_UI = FLYAppConfig.sPackageName + ".action.cardcast.update_ui";

    public static IntentFilter getUpdateActionFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_UI);
        return intentFilter;
    }

    public static void broadcastUpdateUi(Context context) {
        if (context != null) {
            context.sendBroadcast(new Intent(ACTION_UPDATE_UI));
        }
    }
}
