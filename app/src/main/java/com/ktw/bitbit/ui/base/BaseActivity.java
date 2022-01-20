package com.ktw.bitbit.ui.base;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ktw.bitbit.R;
import com.ktw.bitbit.util.LocaleHelper;

public abstract class BaseActivity extends BaseLoginActivity {

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.setLocale(this, LocaleHelper.getLanguage(this));
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// 竖屏
    }

    public void setTitleString(int res) {
        getSupportActionBar().hide();
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(res);
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void setTitleString(String res) {
        getSupportActionBar().hide();
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(res);
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
