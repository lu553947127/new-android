package com.ktw.fly.ui.base;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;

import com.ktw.fly.FLYAppConfig;

public abstract class ActionBackActivity extends StackActivity {
    protected Context mContext;
    protected String TAG;// 获取Tag，用于日志输出等标志
    private boolean isDestroyed = false;

    public ActionBackActivity() {
        TAG = this.getClass().getSimpleName();
        // TAG长度不能超过23，否则崩溃，
        if (TAG.length() > 23) {
            TAG = TAG.substring(0, 23);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mContext = this;
        if (FLYAppConfig.DEBUG) {
            Log.e(FLYAppConfig.TAG, TAG + " onCreate");
        }
    }

    @Override
    public boolean isDestroyed() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            return super.isDestroyed();
        }
        return isDestroyed;
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;
        // mContext不设置为null，避免各种不必要的崩溃，
        // 有大量http回调里使用这个mContext, 可能在退出activity后回调时崩溃，
//        mContext = null;
        if (FLYAppConfig.DEBUG) {
            Log.e(FLYAppConfig.TAG, TAG + " onDestroy");
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return onHomeAsUp();
        }
        return super.onOptionsItemSelected(item);
    }

    protected boolean onHomeAsUp() {
        finish();
        return true;
    }
}
