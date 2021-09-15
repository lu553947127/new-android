package com.ktw.fly.ui.me.question;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ktw.fly.R;
import com.ktw.fly.adapter.QuestionListAdapter;
import com.ktw.fly.bean.QuestionBean;
import com.ktw.fly.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 常见问题列表
 */
public class ViewQuestionActivity extends BaseActivity {

    public final static String KEY_TYPE = "type";
    public final static String KEY_NAME = "name";
    public final static String KEY_CONTENT = "content";

    ListView lvViewQuestion;
    List<QuestionBean> dataList = new ArrayList<>();
    private QuestionListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_question);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.view_questions);

        initView();

    }

    private void initView() {
        lvViewQuestion = (ListView) findViewById(R.id.lvViewQuestion);
        dataList.add(new QuestionBean(R.mipmap.icon_question, "AAChat设置", "1"));
        dataList.add(new QuestionBean(R.mipmap.icon_question, "好友添加", "2"));
        dataList.add(new QuestionBean(R.mipmap.icon_question, "收发消息", "3"));
        dataList.add(new QuestionBean(R.mipmap.icon_question, "AAChat群聊", "4"));
        dataList.add(new QuestionBean(R.mipmap.icon_question, "聊趣", "5"));
        dataList.add(new QuestionBean(R.mipmap.icon_question, "AAChat支付", "6"));
        adapter = new QuestionListAdapter(this, dataList);
        lvViewQuestion.setAdapter(adapter);
        lvViewQuestion.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int poistion, long l) {
                QuestionBean questionBean = dataList.get(poistion);
                if (questionBean != null) {
                    Intent intent = new Intent(ViewQuestionActivity.this, ViewQuestionChildActivity.class);
                    intent.putExtra(KEY_TYPE, questionBean.getType());
                    intent.putExtra(KEY_NAME, questionBean.getTitle());
                    intent.putExtra(ViewQuestionChildActivity.PAGE_TYPE, ViewQuestionChildActivity.PAGE_TYPE_QUESTION);
                    startActivity(intent);
                }
            }
        });

        findViewById(R.id.tvQuestionFeedback).

                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ViewQuestionActivity.this, QuestionFeedbackActivity.class);
                        intent.putExtra("type", QuestionFeedbackActivity.TYPE_PROPOSAL);
                        startActivity(intent);
                    }
                });

    }


}
