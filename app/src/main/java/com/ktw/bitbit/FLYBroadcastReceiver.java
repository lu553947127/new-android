package com.ktw.bitbit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.ktw.bitbit.FLYAppConfig.BROADCASTTEST_ACTION;

public class FLYBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (BROADCASTTEST_ACTION.equals(intent.getAction())) {
            context.startService(new Intent(context, FLYRestartService.class));
        }
    }
}
