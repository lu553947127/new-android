package com.ktw.bitbit.ui.me.question;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ktw.bitbit.R;
import com.ktw.bitbit.adapter.QuestionChildListAdapter;
import com.ktw.bitbit.bean.QuestionBean;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

/**
 * 常见问题子问题列表
 */
public class ViewQuestionChildActivity extends BaseActivity {

    public final static String PAGE_TYPE = "page_type";

    public final static int PAGE_TYPE_QUESTION = 1;
    public final static int PAGE_TYPE_SEAL = 2;


    ListView lvViewChildQuestion;
    List<QuestionBean> dataList = new ArrayList<>();
    private QuestionChildListAdapter adapter;
    private String type;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_child_question);

        initView();


    }

    private void initView() {
        initActionBar();

        lvViewChildQuestion = (ListView) findViewById(R.id.lvViewChildQuestion);
        adapter = new QuestionChildListAdapter(this, dataList);
        lvViewChildQuestion.setAdapter(adapter);
        lvViewChildQuestion.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int poistion, long l) {
                QuestionBean questionBean = dataList.get(poistion);
                if (questionBean != null) {
                    Intent intent = new Intent(ViewQuestionChildActivity.this, ViewQuestionContentActivity.class);
                    intent.putExtra(ViewQuestionActivity.KEY_TYPE, questionBean.getType());
                    intent.putExtra(ViewQuestionActivity.KEY_NAME, questionBean.getTitle());
                    intent.putExtra(ViewQuestionActivity.KEY_CONTENT, questionBean.getContent());
                    startActivity(intent);
                }
            }
        });
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
            tvTitle.setText(name);

            int pageType = intent.getIntExtra(PAGE_TYPE, PAGE_TYPE_QUESTION);
            if (pageType == PAGE_TYPE_QUESTION) {
                getQuestionList();
            } else {
                getSealQuestionList();
            }
        }
    }

    private void getQuestionList() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("type", type);
        HttpUtils.get().url(coreManager.getConfig().VIEW_QUESTION_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<QuestionBean>(QuestionBean.class) {

                    @Override
                    public void onResponse(ArrayResult<QuestionBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1 && result.getData() != null && result.getData().size() > 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dataList = result.getData();
                                    adapter.setData(dataList);
                                    adapter.notifyDataSetChanged();
                                }
                            });

                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(ViewQuestionChildActivity.this);
                    }
                });
    }

    private void getSealQuestionList() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("type", String.valueOf(7));
        HttpUtils.get().url(coreManager.getConfig().GET_QUESTION_ITEM)
                .params(params)
                .build()
                .execute(new ListCallback<QuestionBean>(QuestionBean.class) {

                    @Override
                    public void onResponse(ArrayResult<QuestionBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1 && result.getData() != null && result.getData().size() > 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dataList = result.getData();
                                    adapter.setData(dataList);
                                    adapter.notifyDataSetChanged();
                                }
                            });

                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(ViewQuestionChildActivity.this);
                    }
                });
    }
}
