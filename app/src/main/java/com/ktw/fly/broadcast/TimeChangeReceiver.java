package com.ktw.fly.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ktw.fly.ui.FLYMainActivity;

/**
 * 修改系统时间时通知MainActivity校准时间，不考虑MainActivity不存在的情况，
 */
public class TimeChangeReceiver extends BroadcastReceiver {
    private FLYMainActivity main;

    public TimeChangeReceiver(FLYMainActivity main) {
        this.main = main;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) {
            return;
        }
        switch (intent.getAction()) {
            case Intent.ACTION_TIME_CHANGED:
            case Intent.ACTION_DATE_CHANGED:
            case Intent.ACTION_TIMEZONE_CHANGED:
                main.checkTime();
        }
    }
}
