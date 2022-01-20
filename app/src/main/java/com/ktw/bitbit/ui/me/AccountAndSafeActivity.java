package com.ktw.bitbit.ui.me;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ktw.bitbit.R;
import com.ktw.bitbit.ui.base.BaseActivity;

/**
 * 账号与安全
 * Created by Harvey on 2/4/21.
 **/
public class AccountAndSafeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_safe);
        initActionBar();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.rlt_device_manager).setOnClickListener(v -> {
            startActivity(new Intent(this,DeviceMangerActivity.class));
        });
        findViewById(R.id.rlt_safe_center).setOnClickListener(v -> {
            startActivity(new Intent(this,SecureSettingActivity.class));
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText("账号与安全");
    }
}
