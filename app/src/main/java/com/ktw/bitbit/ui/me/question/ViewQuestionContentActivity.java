package com.ktw.bitbit.ui.me.question;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.ktw.bitbit.R;
import com.ktw.bitbit.ui.base.BaseActivity;

/**
 * 问题内容
 */
public class ViewQuestionContentActivity extends BaseActivity {

    private String type;
    private String name;
    private String content;
    private TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_question_content);

        initView();
        initData();
    }

    private void initData() {

    }

    private void initView() {
        initActionBar();

        tvContent = findViewById(R.id.tvContent);
        tvContent.setText(Html.fromHtml(content));
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        Intent intent = getIntent();
        if (intent != null) {
            type = intent.getStringExtra(ViewQuestionActivity.KEY_TYPE);
            name = intent.getStringExtra(ViewQuestionActivity.KEY_NAME);
            content = intent.getStringExtra(ViewQuestionActivity.KEY_CONTENT);
            tvTitle.setText(name);
        }
    }

}
