package com.ktw.bitbit.broadcast;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.ktw.bitbit.FLYAppConfig;

/**
 * 我的群组 && 全部群组
 * 全部直播 && 正在直播
 */
public class MucgroupUpdateUtil {

    public static final String ACTION_UPDATE = FLYAppConfig.sPackageName + ".action.muc_group.update";// 群聊界面

    public static IntentFilter getUpdateActionFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE);
        return intentFilter;
    }

    public static void broadcastUpdateUi(Context context) {
        context.sendBroadcast(new Intent(ACTION_UPDATE));
    }
}
