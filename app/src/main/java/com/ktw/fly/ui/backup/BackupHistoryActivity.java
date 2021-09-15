package com.ktw.fly.ui.backup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.ktw.fly.R;
import com.ktw.fly.bean.event.EventSentChatHistory;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.tool.ButtonColorChange;
import com.ktw.fly.util.EventBusHelper;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

public class BackupHistoryActivity extends BaseActivity {

    public static void start(Context ctx) {
        Intent intent = new Intent(ctx, BackupHistoryActivity.class);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_history);

        initActionBar();

        Button btnSelectChat = findViewById(R.id.btnSelectChat);
        btnSelectChat.setOnClickListener((v) -> {
            SelectChatActivity.start(this);
        });
        ButtonColorChange.colorChange(this, btnSelectChat);
        EventBusHelper.register(this);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventSentChatHistory message) {
        finish();
    }

    private void initActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        findViewById(R.id.iv_title_left).setOnClickListener((v) -> {
            onBackPressed();
        });
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.backup_chat_history));
    }
}
