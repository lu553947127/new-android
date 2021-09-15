package com.ktw.fly.ui.base;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.ktw.fly.util.LocaleHelper;

public abstract class BaseActivity extends BaseLoginActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.setLocale(this, LocaleHelper.getLanguage(this));
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// 竖屏
    }
}
