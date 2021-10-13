package com.ktw.fly.ui.me;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.j256.ormlite.stmt.query.In;
import com.ktw.fly.R;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.me.capital.CapitalPasswordActivity;
import com.ktw.fly.ui.me.question.QuestionFeedbackActivity;
import com.ktw.fly.ui.me.question.ViewQuestionActivity;
import com.ktw.fly.ui.me.question.ViewQuestionChildActivity;
import com.ktw.fly.ui.tool.WebViewActivity;

/**
 * 安全中心
 * Created by Harvey on 2/4/21.
 **/
public class SecureSettingActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_settings);
        initActionBar();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText("安全中心");

        findViewById(R.id.rlt_deblocking).setOnClickListener(v -> {
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(WebViewActivity.EXTRA_URL, "url");
            startActivity(intent);
        });
        findViewById(R.id.rlt_deblocking).setOnClickListener(this);
        findViewById(R.id.rlt_freeze_account).setOnClickListener(this);
        findViewById(R.id.rlt_unfreeze_account).setOnClickListener(this);
        findViewById(R.id.rlt_complaints).setOnClickListener(this);
        findViewById(R.id.rlt_cancellation_account).setOnClickListener(this);
        findViewById(R.id.rlt_account_capital).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.rlt_deblocking:
                Intent questionIntent = new Intent(SecureSettingActivity.this, ViewQuestionChildActivity.class);
                questionIntent.putExtra(ViewQuestionActivity.KEY_NAME, "解封账号");
                questionIntent.putExtra(ViewQuestionChildActivity.PAGE_TYPE, ViewQuestionChildActivity.PAGE_TYPE_SEAL);
                startActivity(questionIntent);
                break;
            case R.id.rlt_freeze_account:
                operateAccount(AccountOperateActivity.OPERATE_FREEZE_ACCOUNT);
                break;
            case R.id.rlt_unfreeze_account:
                operateAccount(AccountOperateActivity.OPERATE_UNFREEZE_ACCOUNT);
                break;
            case R.id.rlt_complaints:
                Intent intent = new Intent(SecureSettingActivity.this, QuestionFeedbackActivity.class);
                intent.putExtra("type", QuestionFeedbackActivity.TYPE_COMPLAINT);
                startActivity(intent);
                break;
            case R.id.rlt_cancellation_account:
                operateAccount(AccountOperateActivity.OPERATE_UNSUBSCRIBE_ACCOUNT);
                break;
            case R.id.rlt_account_capital:
                startActivity(new Intent(SecureSettingActivity.this, CapitalPasswordActivity.class));
                break;

        }

    }

    private void operateAccount(int type) {
        Intent intent = new Intent(this, AccountOperateActivity.class);
        intent.putExtra(AccountOperateActivity.TYPE_OPERATE, type);
        startActivity(intent);
    }
}
