package com.ktw.fly.ui.me;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ktw.fly.R;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.tool.WebViewActivity;

/**
 * 投诉与建议选项列表
 **/
public class ComplaintSuggestionsListActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_suggestions_list);
        initActionBar();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle =  findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.complaints_suggestions);


        findViewById(R.id.rlt_complaints).setOnClickListener(this);
        findViewById(R.id.rlt_suggestion).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.rlt_complaints:
                submitComplaintSuggestions(ComplaintAndSuggestionsSubmitActivity.TYPE_COMPLAINT);
                break;
            case R.id.rlt_suggestion:
                submitComplaintSuggestions(ComplaintAndSuggestionsSubmitActivity.TYPE_SUGGESTIONS);
                break;


        }

    }

    private void submitComplaintSuggestions(int type) {
        Intent intent = new Intent(this, ComplaintAndSuggestionsSubmitActivity.class);
        intent.putExtra(ComplaintAndSuggestionsSubmitActivity.TYPE, type);
        startActivity(intent);
    }
}